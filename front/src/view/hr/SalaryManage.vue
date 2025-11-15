<template>
  <div class="salary-manage">
    <!-- 搜索和操作区域 -->
    <div class="search-operate-area">
      <el-row :gutter="20">
        <el-col :span="4">
          <el-date-picker
              v-model="searchParams.month"
              type="month"
              placeholder="选择月份"
              value-format="yyyy-MM"
              style="width: 100%"
              @change="handleSearch"
          />
        </el-col>
        <el-col :span="4">
          <div class="dept-select-wrapper">
            <div style="display: flex; align-items: center;">
              <el-select
                  v-model="searchParams.deptId"
                  placeholder="选择部门"
                  clearable
                  @change="handleSearch"
                  style="flex: 1"
              >
                <el-option
                    v-for="dept in departmentOptions"
                    :key="dept.id"
                    :label="dept.Dname"
                    :value="dept.id"
                />
              </el-select>
              <el-button
                  type="text"
                  @click="refreshDepartmentOptions"
                  style="margin-left: 5px;"
                  title="刷新部门列表"
                  :loading="refreshingDept"
              >
                <i class="el-icon-refresh"></i>
              </el-button>
            </div>
            <div v-if="searchParams.deptId && getSelectedDeptName()"
                 class="dept-selected-hint">
              已选择: {{ getSelectedDeptName() }}
            </div>
          </div>
        </el-col>

        <el-col :span="4">
          <el-select
              v-model="searchParams.status"
              placeholder="薪资状态"
              clearable
              @change="handleSearch"
          >
            <el-option label="待审核" value="待审核" />
            <el-option label="已发放" value="已发放" />
          </el-select>
        </el-col>
        <!-- 在搜索操作区域的按钮组中添加 -->
        <el-col :span="12" style="text-align: right;">
          <el-button type="primary" @click="handleSearch">
            <i class="el-icon-search"></i>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <i class="el-icon-refresh"></i>
            重置
          </el-button>
          <!-- 🔥 新增：一键发放按钮 -->
          <el-button type="danger" @click="handleBatchPay" :loading="batchPaying" :disabled="!hasPendingSalary">
            <i class="el-icon-money"></i>
            一键发放
          </el-button>
          <el-button type="success" @click="handleCalculate" :loading="calculating">
            <i class="el-icon-cpu"></i>
            计算薪资
          </el-button>
          <el-button type="warning" @click="handleExport" :loading="exporting">
            <i class="el-icon-download"></i>
            导出Excel
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 统计信息 -->
    <div class="statistics-area">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <div class="stat-label">总薪资支出</div>
              <div class="stat-value">{{ formatCurrency(statistics.totalSalary) }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <div class="stat-label">平均薪资</div>
              <div class="stat-value">{{ formatCurrency(statistics.avgSalary) }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <div class="stat-label">待审核数量</div>
              <div class="stat-value">{{ statistics.pendingCount || 0 }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <div class="stat-label">已发放数量</div>
              <div class="stat-value">{{ statistics.paidCount || 0 }}</div>
            </div>
          </el-card>
        </el-col>
        </el-row>
    </div>

    <!-- 数据表格 -->
    <div class="table-area">
      <el-table
          :data="tableData"
          v-loading="loading"
          border
          style="width: 100%"
          :default-sort="{ prop: 'id', order: 'descending' }"
      >
        <!-- 添加序号列 -->
        <el-table-column type="index" width="60" label="序号" align="center" />

        <el-table-column prop="staffName" label="员工姓名" width="100" />
        <el-table-column prop="staffSid" label="工号" width="100">
          <template slot-scope="scope">
            {{ scope.row.staffSid || `EMP${scope.row.staffId}` }}
          </template>
        </el-table-column>
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column prop="jobName" label="岗位" width="120" />
        <el-table-column prop="month" label="月份" width="100">
          <template slot-scope="scope">
            {{ formatMonth(scope.row.month) }}
          </template>
        </el-table-column>
        <el-table-column prop="actualBasePay" label="基础工资" width="100" align="right">
          <template slot-scope="scope">
            {{ formatCurrency(scope.row.actualBasePay) }}
          </template>
        </el-table-column>
        <el-table-column prop="overtimePay" label="加班费" width="100" align="right">
          <template slot-scope="scope">
            {{ formatCurrency(scope.row.overtimePay) }}
          </template>
        </el-table-column>
        <el-table-column prop="leaveDays" label="请假天数" width="100" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.leaveDays > 0" type="warning" size="small">
              {{ scope.row.leaveDays }}
            </el-tag>
            <span v-else>{{ scope.row.leaveDays || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="leaveDeduction" label="请假扣款" width="100" align="right">
          <template slot-scope="scope">
        <span v-if="scope.row.leaveDeduction > 0" style="color: #f56c6c;">
          -{{ formatCurrency(scope.row.leaveDeduction) }}
        </span>
            <span v-else>{{ formatCurrency(scope.row.leaveDeduction) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalSalary" label="实发工资" width="120" align="right">
          <template slot-scope="scope">
            <strong style="color: #67c23a;">{{ formatCurrency(scope.row.totalSalary) }}</strong>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" fixed="right">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === '已发放' ? 'success' : 'warning'" size="small">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="calcTime" label="计算时间" width="160">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.calcTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="remarks" label="备注" width="150" show-overflow-tooltip>
          <template slot-scope="scope">
            <span v-if="scope.row.remarks" class="remarks-text" :title="scope.row.remarks">
              {{ scope.row.remarks }}
            </span>
            <span v-else class="no-remarks">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template slot="header">
            <div style="display: flex; align-items: center; justify-content: center;">
              <span>操作</span>
              <el-tooltip v-if="hasPendingSalary" content="有待发放记录，可使用一键发放功能" placement="top">
                <i class="el-icon-info" style="margin-left: 5px; color: #e6a23c;"></i>
              </el-tooltip>
            </div>
          </template>
          <template slot-scope="scope">
            <el-button size="small" type="primary" @click="handleDetail(scope.row)">
              详情
            </el-button>
            <el-button
                size="small"
                type="success"
                @click="handleUpdateStatus(scope.row)"
                :disabled="scope.row.status === '已发放'"
            >
              发放
            </el-button>
            <el-button
                size="small"
                type="warning"
                @click="handleRecalculate(scope.row)"
            >
              重算
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-area">
      <el-pagination
          :current-page="pagination.current"
          :page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </div>

    <!-- 薪资详情对话框 -->
    <el-dialog
        :visible.sync="detailDialogVisible"
        title="薪资详情"
        width="800px"
    >
      <el-descriptions :column="2" border v-if="currentSalary">
        <el-descriptions-item label="员工姓名">{{ currentSalary.staffName }}</el-descriptions-item>
        <el-descriptions-item label="工号">
          {{ currentSalary.staffSid || (currentSalary.staffId ? `EMP${String(currentSalary.staffId).padStart(4, '0')}` : '') }}
        </el-descriptions-item>
        <el-descriptions-item label="部门">{{ currentSalary.deptName }}</el-descriptions-item>
        <el-descriptions-item label="岗位">{{ currentSalary.jobName }}</el-descriptions-item>
        <el-descriptions-item label="月份">{{ currentSalary.displayMonth || formatMonth(currentSalary.month) }}</el-descriptions-item>
        <el-descriptions-item label="薪资状态">
          <el-tag :type="currentSalary.status === '已发放' ? 'success' : 'warning'">
            {{ currentSalary.status }}
          </el-tag>
        </el-descriptions-item>

        <!-- 收入部分 -->
        <el-descriptions-item label="基础工资" :span="2">
        <span style="color: #409EFF; font-weight: bold;">
          +{{ formatCurrency(currentSalary.actualBasePay) }}
        </span>
        </el-descriptions-item>

        <el-descriptions-item label="加班费" :span="2">
        <span style="color: #409EFF; font-weight: bold;">
          +{{ formatCurrency(currentSalary.overtimePay) }}
        </span>
        </el-descriptions-item>

        <!-- 扣款明细部分 -->
        <el-descriptions-item label="请假扣款" :span="2">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <span>请假 {{ currentSalary.leaveDays || 0 }} 天</span>
            <span style="color: #F56C6C; font-weight: bold;">
            -{{ formatCurrency(currentSalary.leaveDeduction || 0) }}
          </span>
          </div>
        </el-descriptions-item>

        <!-- 🔥 修复：缺勤扣款显示逻辑 -->
        <el-descriptions-item
            label="缺勤扣款"
            :span="2"
            v-if="getSafeValue(currentSalary.absenceDeduction) > 0 || getSafeValue(currentSalary.absenceDays) > 0">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <span>缺勤 {{ getSafeValue(currentSalary.absenceDays) }} 天</span>
            <span style="color: #F56C6C; font-weight: bold;">
            -{{ formatCurrency(getSafeValue(currentSalary.absenceDeduction)) }}
          </span>
          </div>
        </el-descriptions-item>

        <!-- 🔥 修复：迟到早退扣款显示逻辑 -->
        <el-descriptions-item
            label="迟到早退扣款"
            :span="2"
            v-if="getSafeValue(currentSalary.lateDeduction) > 0">
          <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>
            <span v-if="getSafeValue(currentSalary.lateCount) > 0">迟到 {{ currentSalary.lateCount }} 次 </span>
            <span v-if="getSafeValue(currentSalary.earlyLeaveCount) > 0">早退 {{ currentSalary.earlyLeaveCount }} 次</span>
            <span v-if="getSafeValue(currentSalary.lateEarlyCount) > 0">迟到早退 {{ currentSalary.lateEarlyCount }} 次</span>
            <span v-if="getSafeValue(currentSalary.lateCount) === 0 && getSafeValue(currentSalary.earlyLeaveCount) === 0 && getSafeValue(currentSalary.lateEarlyCount) === 0">
              迟到早退记录
            </span>
          </span>
            <span style="color: #F56C6C; font-weight: bold;">
            -{{ formatCurrency(getSafeValue(currentSalary.lateDeduction)) }}
          </span>
          </div>
        </el-descriptions-item>

        <!-- 🔥 修复：缺卡扣款显示逻辑 -->
        <el-descriptions-item
            label="缺卡扣款"
            :span="2"
            v-if="getSafeValue(currentSalary.missingCardDeduction) > 0">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <span>缺卡 {{ getSafeValue(currentSalary.missingCardCount) }} 次</span>
            <span style="color: #F56C6C; font-weight: bold;">
            -{{ formatCurrency(getSafeValue(currentSalary.missingCardDeduction)) }}
          </span>
          </div>
        </el-descriptions-item>

        <!-- 🔥 修复：其他扣款显示逻辑 -->
        <el-descriptions-item
            label="其他扣款"
            :span="2"
            v-if="getSafeValue(currentSalary.otherDeduction) > 0">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <span>{{ currentSalary.otherDeductionRemark || '其他扣款' }}</span>
            <span style="color: #F56C6C; font-weight: bold;">
            -{{ formatCurrency(getSafeValue(currentSalary.otherDeduction)) }}
          </span>
          </div>
        </el-descriptions-item>
        <!-- 扣款汇总 -->
        <el-descriptions-item label="总扣款" :span="2">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <span style="font-weight: bold;">扣款合计</span>
            <span style="color: #F56C6C; font-size: 16px; font-weight: bold;">
            -{{ formatCurrency(calculateTotalDeduction(currentSalary)) }}
          </span>
          </div>
        </el-descriptions-item>

        <!-- 应发工资 -->
        <el-descriptions-item label="应发工资" :span="2">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <span style="font-weight: bold;">收入合计</span>
            <span style="color: #409EFF; font-size: 16px; font-weight: bold;">
            {{ formatCurrency(calculateGrossSalary(currentSalary)) }}
          </span>
          </div>
        </el-descriptions-item>

        <!-- 实发工资 -->
        <el-descriptions-item label="实发工资" :span="2">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <span style="font-weight: bold;">最终实发</span>
            <strong style="color: #67c23a; font-size: 18px;">
              {{ formatCurrency(currentSalary.totalSalary) }}
            </strong>
          </div>
        </el-descriptions-item>

        <!-- 其他信息 -->
        <el-descriptions-item label="计算时间">
          {{ formatDateTime(currentSalary.calcTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="备注">
          {{ currentSalary.remarks || '无' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 计算薪资对话框 -->
    <el-dialog
        :visible.sync="calculateDialogVisible"
        title="计算月度薪资"
        width="400px"
    >
      <el-form>
        <el-form-item label="选择月份">
          <el-date-picker
              v-model="calculateMonth"
              type="month"
              placeholder="选择要计算的月份"
              value-format="yyyy-MM"
              style="width: 100%"
          />
        </el-form-item>
        <el-form-item>
          <el-alert
              title="薪资计算将执行以下操作："
              type="info"
              :closable="false"
              style="margin-bottom: 10px;"
          >
            <div>1. 计算基础工资、加班费、请假扣款</div>
            <div>2. 生成薪资记录</div>
            <div>3. 状态设置为"待审核"</div>
          </el-alert>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="calculateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCalculateSubmit" :loading="calculating">
          开始计算
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import API from '@/utils/api'

export default {
  name: 'SalaryManage',
  data() {
    return {
      // 响应式数据
      loading: false,
      calculating: false,
      exporting: false,
      detailDialogVisible: false,
      calculateDialogVisible: false,
      refreshingDept: false,
      batchPaying: false, // 🔥 新增：一键发放加载状态

      // 表格数据
      tableData: [],

      // 搜索参数
      searchParams: {
        month: this.getCurrentMonth(),
        deptId: '',
        status: '',
        staffSid: ''
      },

      // 分页参数
      pagination: {
        current: 1,
        size: 10,
        total: 0
      },

      // 统计信息
      statistics: {
        totalSalary: 0,
        avgSalary: 0,
        pendingCount: 0,
        paidCount: 0
      },

      // 当前查看的薪资记录
      currentSalary: null,

      // 计算薪资的月份
      calculateMonth: this.getCurrentMonth(),

      // 选项数据
      departmentOptions: []
    }
  },

  mounted() {
    this.loadTableData()
    this.loadStatistics()
    this.loadDepartmentOptions().catch(error => {
      console.error('初始化部门选项失败:', error)
      this.departmentOptions = this.getMockDepartmentOptions()
    })
  },
  computed: {
    // 🔥 新增：检查是否有待审核的薪资记录
    hasPendingSalary() {
      return this.tableData.some(item => item.status === '待审核')
    },

    // 🔥 新增：获取所有待发放的薪资ID
    pendingSalaryIds() {
      return this.tableData
          .filter(item => item.status === '待审核')
          .map(item => item.id)
    },
    hasOtherDeductions() {
      if (!this.currentSalary) return false
      return this.currentSalary.absenceDeduction !== undefined ||
          this.currentSalary.lateDeduction !== undefined ||
          this.currentSalary.missingCardDeduction !== undefined ||
          this.currentSalary.otherDeduction !== undefined
    }
  },
  methods: {
    // 在 methods 中添加安全取值方法
    getSafeValue(value, defaultValue = 0) {
      if (value === null || value === undefined || value === '') {
        return defaultValue;
      }
      // 如果是字符串数字，转换为数字
      if (typeof value === 'string' && !isNaN(value)) {
        return parseFloat(value);
      }
      // 如果是数字，直接返回
      if (typeof value === 'number') {
        return value;
      }
      // 如果是BigDecimal等对象，尝试转换
      return parseFloat(value) || defaultValue;
    },

// 修改查看详情方法，确保数据完整性
    async handleDetail(row) {
      try {
        console.log('查看详情，ID:', row.id)

        const response = await this.$http.get(`/salary/detail/${row.id}`)
        console.log('薪资详情响应:', response)

        if (response.data && response.data.code === 0) {
          const salaryData = { ...response.data.data }

          // 确保工号有值
          if (!salaryData.staffSid && salaryData.staffId) {
            salaryData.staffSid = `EMP${String(salaryData.staffId).padStart(4, '0')}`
          }

          // 🔥 确保所有字段都有默认值
          const defaultFields = {
            // 扣款金额字段
            'leaveDeduction': 0,
            'absenceDeduction': 0,
            'lateDeduction': 0,
            'missingCardDeduction': 0,
            'otherDeduction': 0,

            // 计数字段
            'leaveDays': 0,
            'absenceDays': 0,
            'lateCount': 0,
            'earlyLeaveCount': 0,
            'lateEarlyCount': 0,
            'missingCardCount': 0,

            // 其他字段
            'otherDeductionRemark': '',
            'displayMonth': this.formatMonth(salaryData.month)
          }

          Object.keys(defaultFields).forEach(field => {
            if (salaryData[field] === undefined || salaryData[field] === null) {
              salaryData[field] = defaultFields[field]
            }
          })

          this.currentSalary = salaryData
          console.log('处理后的详情数据:', this.currentSalary)
          this.detailDialogVisible = true
        } else {
          this.$message.error(response.data?.msg || '获取薪资详情失败')
        }
      } catch (error) {
        console.error('获取薪资详情失败:', error)
        this.$message.error('获取薪资详情失败: ' + (error.response?.data?.msg || error.message))
      }
    },

    // 获取当前月份
    getCurrentMonth() {
      const now = new Date()
      return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
    },
    // 🔥 修复：计算总扣款方法
    calculateTotalDeduction(salary) {
      if (!salary) return 0
      const deductions = [
        this.getSafeValue(salary.leaveDeduction),
        this.getSafeValue(salary.absenceDeduction),
        this.getSafeValue(salary.lateDeduction),
        this.getSafeValue(salary.missingCardDeduction),
        this.getSafeValue(salary.otherDeduction)
      ]
      return deductions.reduce((total, deduction) => total + deduction, 0)
    },

    // 🔥 修复：计算应发工资方法
    calculateGrossSalary(salary) {
      if (!salary) return 0
      const basePay = this.getSafeValue(salary.actualBasePay)
      const overtimePay = this.getSafeValue(salary.overtimePay)
      return basePay + overtimePay
    },


    // 🔥 修复：获取扣款明细描述
    getDeductionDescription(salary) {
      if (!salary) return '无扣款'

      const descriptions = []

      // 请假扣款
      if (this.getSafeValue(salary.leaveDeduction) > 0) {
        descriptions.push(`请假: -${this.formatCurrency(salary.leaveDeduction)}`)
      }

      // 缺勤扣款
      if (this.getSafeValue(salary.absenceDeduction) > 0) {
        descriptions.push(`缺勤: -${this.formatCurrency(salary.absenceDeduction)}`)
      }

      // 迟到早退扣款
      if (this.getSafeValue(salary.lateDeduction) > 0) {
        const lateDetails = []
        if (this.getSafeValue(salary.lateCount) > 0) lateDetails.push(`迟到${salary.lateCount}次`)
        if (this.getSafeValue(salary.earlyLeaveCount) > 0) lateDetails.push(`早退${salary.earlyLeaveCount}次`)
        if (this.getSafeValue(salary.lateEarlyCount) > 0) lateDetails.push(`迟到早退${salary.lateEarlyCount}次`)

        const detailText = lateDetails.length > 0 ? `(${lateDetails.join('、')})` : ''
        descriptions.push(`迟到早退${detailText}: -${this.formatCurrency(salary.lateDeduction)}`)
      }

      // 缺卡扣款
      if (this.getSafeValue(salary.missingCardDeduction) > 0) {
        descriptions.push(`缺卡${this.getSafeValue(salary.missingCardCount)}次: -${this.formatCurrency(salary.missingCardDeduction)}`)
      }

      // 其他扣款
      if (this.getSafeValue(salary.otherDeduction) > 0) {
        const remark = salary.otherDeductionRemark ? `(${salary.otherDeductionRemark})` : ''
        descriptions.push(`其他${remark}: -${this.formatCurrency(salary.otherDeduction)}`)
      }

      return descriptions.length > 0 ? descriptions.join('; ') : '无扣款'
    },

    // 加载表格数据
    async loadTableData() {
      this.loading = true
      try {
        const params = {
          page: this.pagination.current,
          size: this.pagination.size,
          ...this.searchParams
        }

        // 清理空参数
        Object.keys(params).forEach(key => {
          if (params[key] === '' || params[key] === null || params[key] === undefined) {
            delete params[key]
          }
        })

        console.log('请求薪资列表参数:', params)

        const response = await this.$http.get(API.url.salary.list, { params })
        console.log('薪资列表响应原始数据:', response)

        if (response.data && response.data.code === 0) {
          let pageData = response.data.page || response.data.data
          console.log('薪资列表数据:', pageData)

          if (pageData) {
            let records = pageData.records || pageData.list || []

            this.tableData = records.map(item => {
              if (!item.staffSid && item.staffId) {
                item.staffSid = `EMP${String(item.staffId).padStart(4, '0')}`
              }
              return item
            })

            this.pagination.total = pageData.total || 0
          } else {
            let records = response.data.data || response.data.list || []
            this.tableData = records.map(item => {
              if (!item.staffSid && item.staffId) {
                item.staffSid = `EMP${String(item.staffId).padStart(4, '0')}`
              }
              return item
            })
            this.pagination.total = this.tableData.length
          }

          console.log('处理后的表格数据:', this.tableData)
        } else {
          this.$message.error(response.data?.msg || '加载薪资数据失败')
        }
      } catch (error) {
        console.error('加载薪资数据失败:', error)
        this.$message.error('加载薪资数据失败: ' + (error.response?.data?.msg || error.message))
      } finally {
        this.loading = false
      }
    },
// 🔥 新增：一键发放方法
    async handleBatchPay() {
      try {
        const pendingCount = this.pendingSalaryIds.length

        if (pendingCount === 0) {
          this.$message.warning('当前没有待发放的薪资记录')
          return
        }

        await this.$confirm(
            `确定要一键发放 ${pendingCount} 条待审核薪资吗？此操作将把所有选中的薪资标记为已发放。`,
            '一键发放确认',
            {
              confirmButtonText: '确定发放',
              cancelButtonText: '取消',
              type: 'warning',
              confirmButtonClass: 'el-button--danger'
            }
        )

        this.batchPaying = true
        console.log('开始一键发放，待发放记录数量:', pendingCount)
        console.log('待发放ID列表:', this.pendingSalaryIds)

        const response = await this.$http.post('/salary/updateStatus', {
          ids: this.pendingSalaryIds,
          status: '已发放'
        })

        console.log('一键发放响应:', response)

        if (response.data && response.data.code === 0) {
          this.$message.success(`成功发放 ${pendingCount} 条薪资记录`)
          // 重新加载数据
          await this.loadTableData()
          await this.loadStatistics()
        } else {
          this.$message.error(response.data?.msg || '一键发放失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('一键发放失败:', error)
          this.$message.error('一键发放失败: ' + (error.response?.data?.msg || error.message))
        }
      } finally {
        this.batchPaying = false
      }
    },
    // 加载统计信息
    async loadStatistics() {
      try {
        const params = {
          month: this.searchParams.month,
          deptId: this.searchParams.deptId
        }

        // 清理空参数
        Object.keys(params).forEach(key => {
          if (params[key] === '' || params[key] === null || params[key] === undefined) {
            delete params[key]
          }
        })

        console.log('统计信息请求参数:', params)

        const response = await this.$http.get(API.url.salary.statistics, { params })
        console.log('统计信息响应:', response)

        if (response.data && response.data.code === 0) {
          this.statistics = response.data.data || {}
          console.log('统计信息数据:', this.statistics)
        } else {
          console.error('获取统计信息失败:', response.data?.msg)
          this.statistics = {
            totalSalary: 0,
            avgSalary: 0,
            pendingCount: 0,
            paidCount: 0
          }
        }
      } catch (error) {
        console.error('加载统计信息失败:', error)
        this.statistics = {
          totalSalary: 0,
          avgSalary: 0,
          pendingCount: 0,
          paidCount: 0
        }
      }
    },

    // 修复部门选项加载方法
    async loadDepartmentOptions() {
      try {
        console.log('开始加载部门选项...')

        const response = await this.$http.get(API.url.department.list)
        console.log('部门选项完整响应:', response)

        if (response.data && response.data.code === 0) {
          let departments = []

          if (response.data.data && Array.isArray(response.data.data)) {
            departments = response.data.data
          } else if (Array.isArray(response.data.list)) {
            departments = response.data.list
          } else if (Array.isArray(response.data.records)) {
            departments = response.data.records
          } else if (Array.isArray(response.data)) {
            departments = response.data
          }

          console.log('提取的部门数据:', departments)

          if (departments.length === 0) {
            console.warn('部门数据为空，使用模拟数据')
            this.departmentOptions = this.getMockDepartmentOptions()
          } else {
            this.departmentOptions = departments.map(dept => ({
              id: dept.id || dept.deptId,
              Dname: dept.Dname || dept.deptName || dept.name || '未知部门'
            }))
            console.log('处理后的部门选项:', this.departmentOptions)
          }

        } else {
          console.error('部门接口返回异常:', response.data)
          this.$message.warning('部门列表加载失败，使用模拟数据')
          this.departmentOptions = this.getMockDepartmentOptions()
        }
      } catch (error) {
        console.error('加载部门选项失败:', error)
        this.$message.error('部门列表加载失败: ' + (error.message || '网络错误'))
        this.departmentOptions = this.getMockDepartmentOptions()
      }
    },

    // 模拟部门数据
    getMockDepartmentOptions() {
      return [
        { id: 1, Dname: '技术部' },
        { id: 2, Dname: '人事部' },
        { id: 3, Dname: '财务部' },
        { id: 4, Dname: '市场部' },
        { id: 5, Dname: '运营部' }
      ]
    },

    // 刷新部门选项
    async refreshDepartmentOptions() {
      this.refreshingDept = true
      try {
        await this.loadDepartmentOptions()
        this.$message.success('部门列表刷新成功')
      } catch (error) {
        console.error('刷新部门列表失败:', error)
      } finally {
        this.refreshingDept = false
      }
    },

    // 获取选中的部门名称
    getSelectedDeptName() {
      if (!this.searchParams.deptId) return ''
      const dept = this.departmentOptions.find(d => d.id == this.searchParams.deptId)
      return dept ? dept.Dname : '未知部门'
    },

    // 搜索
    handleSearch() {
      console.log('搜索参数:', {
        部门ID: this.searchParams.deptId,
        部门名称: this.getSelectedDeptName(),
        月份: this.searchParams.month,
        状态: this.searchParams.status
      })
      this.pagination.current = 1
      this.loadTableData()
      this.loadStatistics()
    },

    // 重置搜索
    handleReset() {
      this.searchParams = {
        month: this.getCurrentMonth(),
        deptId: '',
        status: ''
      }
      this.pagination.current = 1
      this.loadTableData()
      this.loadStatistics()
    },

    // 更新薪资状态
    async handleUpdateStatus(row) {
      try {
        await this.$confirm(
            `确定要将 ${row.staffName} 的 ${this.formatMonth(row.month)} 薪资标记为已发放吗？`,
            '发放确认',
            {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }
        )

        console.log('开始更新薪资状态，ID:', row.id)

        const response = await this.$http.post('/salary/updateStatus', {
          ids: [row.id],
          status: '已发放'
        })

        console.log('更新响应:', response)

        if (response.data && response.data.code === 0) {
          this.$message.success('薪资发放成功')
          this.loadTableData()
          this.loadStatistics()
        } else {
          this.$message.error(response.data?.msg || '薪资发放失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('更新失败:', error)
          this.$message.error('薪资发放失败: ' + (error.response?.data?.msg || error.message))
        }
      }
    },

    // 重新计算 - 修复日期格式
    async handleRecalculate(row) {
      try {
        await this.$confirm(
            `确定要重新计算 ${row.staffName} 的 ${this.formatMonth(row.month)} 薪资吗？`,
            '重新计算确认',
            {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }
        )

        console.log('重新计算薪资，员工ID:', row.staffId, '月份:', row.month)

        // 将月份转换为完整日期格式（存储过程需要完整日期）
        const fullMonth = this.convertToFullDate(row.month)

        const response = await this.$http.post('/salary/recalculate', null, {
          params: {
            staffId: row.staffId,
            month: fullMonth // 使用完整日期格式
          }
        })

        if (response.data && response.data.code === 0) {
          this.$message.success('重新计算成功')
          this.loadTableData()
        } else {
          this.$message.error(response.data?.msg || '重新计算失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('重新计算失败:', error)
          this.$message.error('重新计算失败: ' + (error.response?.data?.msg || error.message))
        }
      }
    },

    // 计算薪资
    handleCalculate() {
      this.calculateMonth = this.searchParams.month || this.getCurrentMonth()
      this.calculateDialogVisible = true
    },

    // 提交计算 - 修复日期格式
    async handleCalculateSubmit() {
      if (!this.calculateMonth) {
        this.$message.warning('请选择要计算的月份')
        return
      }

      this.calculating = true
      try {
        // 先检查是否已计算
        const checkResponse = await this.$http.get('/salary/checkCalculated', {
          params: { month: this.calculateMonth }
        })

        if (checkResponse.data && checkResponse.data.code === 0 && checkResponse.data.calculated) {
          const confirm = await this.$confirm(
              `${this.formatMonth(this.calculateMonth)} 的薪资已经计算过，确定要重新计算吗？`,
              '重新计算确认',
              {
                confirmButtonText: '重新计算',
                cancelButtonText: '取消',
                type: 'warning'
              }
          )
          if (!confirm) return
        }

        console.log('开始计算薪资，月份:', this.calculateMonth)

        // 将月份转换为完整日期格式
        const fullMonth = this.convertToFullDate(this.calculateMonth)

        const response = await this.$http.post('/salary/calculate', null, {
          params: { month: fullMonth } // 使用完整日期格式
        })

        console.log('计算薪资响应:', response)

        if (response.data && response.data.code === 0) {
          this.$message.success(response.data.msg || '薪资计算完成')
          this.calculateDialogVisible = false
          this.loadTableData()
          this.loadStatistics()
        } else {
          this.$message.error(response.data?.msg || '薪资计算失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('薪资计算失败:', error)
          this.$message.error('薪资计算失败: ' + (error.response?.data?.msg || error.message))
        }
      } finally {
        this.calculating = false
      }
    },

    // 导出Excel - 修复导出功能
    async handleExport() {
      try {
        this.exporting = true
        const params = { ...this.searchParams }

        // 清理空参数
        Object.keys(params).forEach(key => {
          if (params[key] === '' || params[key] === null || params[key] === undefined) {
            delete params[key]
          }
        })

        console.log('导出参数:', params)

        // 🔥 修复：正确的GET请求参数传递方式
        const response = await this.$http.get('/salary/export', {
          params: params,  // 将参数放在params对象中
          responseType: 'blob'
        })

        // 创建下载链接
        const blob = new Blob([response.data], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url

        // 设置文件名
        const filename = `薪资数据_${this.searchParams.month || this.getCurrentMonth()}.xlsx`
        link.setAttribute('download', filename)

        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)

        this.$message.success('导出成功')
      } catch (error) {
        console.error('导出失败:', error)
        this.$message.error('导出失败: ' + (error.response?.data?.msg || error.message))
      } finally {
        this.exporting = false
      }
    },

    // 分页大小改变
    handleSizeChange(size) {
      this.pagination.size = size
      this.pagination.current = 1
      this.loadTableData()
    },

    // 当前页改变
    handleCurrentChange(current) {
      this.pagination.current = current
      this.loadTableData()
    },

    formatCurrency(value) {
      if (value === null || value === undefined) return '¥0.00'
      const numValue = typeof value === 'string' ? parseFloat(value) : value
      return `¥${numValue.toFixed(2)}`
    },

    formatMonth(month) {
      if (!month) return ''
      return month.replace('-', '年') + '月'
    },

    formatDateTime(datetime) {
      if (!datetime) return ''
      try {
        const date = new Date(datetime)
        return isNaN(date.getTime()) ? '' : date.toLocaleString('zh-CN')
      } catch (error) {
        return datetime
      }
    },
    // 计算加班时长
    calculateOvertimeHours(overtimePay) {
      if (!overtimePay || overtimePay === 0) return 0
      const hourlyRate = 8000 / 21 / 8
      const overtimeRate = 1.5
      const hours = overtimePay / (hourlyRate * overtimeRate)
      return hours.toFixed(1)
    },

    // 🔥 新增：将月份转换为完整日期格式（存储过程需要）
    convertToFullDate(monthStr) {
      if (!monthStr) return ''
      // 将 "2024-12" 转换为 "2024-12-01"
      return `${monthStr}-01`
    }
  }
}
</script>

<style scoped>
/* 整体容器 */
.salary-manage {
  padding: 24px;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

/* 区域间距 */
.search-operate-area,
.statistics-area,
.table-area {
  margin-bottom: 24px;
}

/* 统计卡片区 */
.stat-card {
  height: 100px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;

  background: linear-gradient(135deg, #e8f3ff 0%, #f5faff 100%);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 6px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
}

/* 搜索区域 */
.search-operate-area {
  padding: 16px;
  background: #fafbfd;
  border-radius: 8px;
  border-left: 4px solid #409eff22;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

/* 部门提示 */
.dept-select-wrapper {
  position: relative;
}

.dept-selected-hint {
  font-size: 12px;
  color: #409eff;
  margin-top: 4px;
  padding: 4px 10px;
  background: #eef7ff;
  border-radius: 5px;
  border-left: 3px solid #409eff;
}

/* 表格信息居中 */
::v-deep .el-table .cell,
::v-deep .el-table th > .cell {
  text-align: center;
}

/* 分页右对齐 */
.pagination-area {
  display: flex;
  justify-content: flex-end;
}

/* ----------- Descriptions 样式优化 ----------- */
.el-descriptions {
  margin-top: 16px;
  padding: 20px;
  background: #fafbfd;
  border-radius: 10px;
  border: 1px solid #ebeef5;
}

::v-deep .el-descriptions__label {
  width: 120px !important;
  font-weight: bold;
  color: #606266;
  padding-right: 8px;
  text-align: right;
}

::v-deep .el-descriptions-item__content {
  color: #303133;
}

/* 移除 scope 限制以增强兼容性 */
::v-deep .el-descriptions-item__label {
  font-weight: 600 !important;
}
</style>
