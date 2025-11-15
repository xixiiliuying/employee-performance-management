<template>
  <div class="attendance-manage">
    <!-- 选项卡 -->
    <div class="tab-area">
      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <el-tab-pane label="考勤记录" name="records"></el-tab-pane>
        <el-tab-pane label="考勤统计" name="statistics"></el-tab-pane>
        <el-tab-pane label="手动补卡" name="manual"></el-tab-pane>
      </el-tabs>
    </div>

    <!-- 考勤记录页面 -->
    <div v-if="activeTab === 'records'">
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
            <el-input
                v-model="searchParams.staffName"
                placeholder="员工姓名"
                clearable
                @clear="handleSearch"
                @keyup.enter.native="handleSearch"
            >
              <i slot="prefix" class="el-input__icon el-icon-search"></i>
            </el-input>
          </el-col>
          <el-col :span="4">
            <el-select
                v-model="searchParams.deptId"
                placeholder="选择部门"
                clearable
                @change="handleSearch"
            >
              <el-option
                  v-for="dept in departmentOptions"
                  :key="dept.id"
                  :label="dept.dname"
                  :value="dept.id"
              />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select
                v-model="searchParams.status"
                placeholder="考勤状态"
                clearable
                @change="handleSearch"
            >
              <el-option label="正常" value="正常" />
              <el-option label="迟到" value="迟到" />
              <el-option label="早退" value="早退" />
              <el-option label="迟到早退" value="迟到早退" />
              <el-option label="上班缺卡" value="上班缺卡" />
              <el-option label="下班缺卡" value="下班缺卡" />
              <el-option label="缺勤" value="缺勤" />
              <el-option label="请假" value="请假" />
              <el-option label="外出" value="外出" />
            </el-select>
          </el-col>
          <el-col :span="8" style="text-align: right;">
            <el-button @click="handleReset">
              <i class="el-icon-refresh"></i>
              重置
            </el-button>
            <el-button type="warning" @click="handleExport">
              <i class="el-icon-download"></i>
              导出Excel
            </el-button>
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
            :default-sort="{ prop: 'date', order: 'descending' }"
        >
          <el-table-column type="index" width="50" label="序号" />
          <el-table-column prop="staffName" label="员工姓名" width="100" />
          <el-table-column prop="staffId" label="工号" width="100" />
          <el-table-column label="部门" width="120">
            <template slot-scope="scope">
              {{ scope.row.departmentName || '未分配' }}
            </template>
          </el-table-column>
          <el-table-column label="岗位" width="120">
            <template slot-scope="scope">
              {{ scope.row.jobName || '未分配' }}
            </template>
          </el-table-column>
          <el-table-column prop="date" label="日期" width="120" sortable>
            <template slot-scope="scope">
              {{ formatDate(scope.row.date) }}
            </template>
          </el-table-column>

          <!-- 上班打卡列 - 修复版 -->
          <el-table-column label="上班打卡" width="120">
            <template slot-scope="scope">
              <span v-if="scope.row.status === '请假'" class="leave-time">
                请假
              </span>
              <span v-else-if="scope.row.checkInTime"
                    :class="getTimeClass(scope.row.checkInTime, 'in', scope.row)">
                {{ scope.row.checkInTime }}
              </span>
              <span v-else class="missing-time">
                缺卡
              </span>
            </template>
          </el-table-column>

          <!-- 下班打卡列 - 修复版 -->
          <el-table-column label="下班打卡" width="120">
            <template slot-scope="scope">
              <span v-if="scope.row.status === '请假'" class="leave-time">
                请假
              </span>
              <span v-else-if="scope.row.checkOutTime"
                    :class="getTimeClass(scope.row.checkOutTime, 'out', scope.row)">
                {{ scope.row.checkOutTime }}
              </span>
              <span v-else class="missing-time">
                缺卡
              </span>
            </template>
          </el-table-column>

          <!-- 迟到时间 -->
          <el-table-column label="迟到(分)" width="100" align="center">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.lateMin > 0 && scope.row.status !== '请假'"
                      type="warning" size="small">
                {{ scope.row.lateMin }}
              </el-tag>
              <span v-else-if="scope.row.status === '请假'">-</span>
              <span v-else>-</span>
            </template>
          </el-table-column>

          <!-- 早退时间 -->
          <el-table-column label="早退(分)" width="100" align="center">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.earlyMin > 0 && scope.row.status !== '请假'"
                      type="warning" size="small">
                {{ scope.row.earlyMin }}
              </el-tag>
              <span v-else-if="scope.row.status === '请假'">-</span>
              <span v-else>-</span>
            </template>
          </el-table-column>

          <!-- 考勤状态列 - 修复版 -->
          <el-table-column label="考勤状态" width="120" fixed="right">
            <template slot-scope="scope">
              <el-tag :type="getStatusTag(scope.row.status)" size="small">
                {{ getDisplayStatus(scope.row) }}
              </el-tag>
            </template>
          </el-table-column>

          <!-- 备注列 - 修复版 -->
          <el-table-column label="备注" min-width="150" show-overflow-tooltip>
            <template slot-scope="scope">
              {{ getDisplayReason(scope.row) }}
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
    </div>

    <!-- 考勤统计页面 -->
    <div v-if="activeTab === 'statistics'">
      <!-- 统计筛选 -->
      <div class="search-operate-area">
        <el-row :gutter="20">
          <el-col :span="4">
            <el-date-picker
                v-model="statisticsParams.month"
                type="month"
                placeholder="选择月份"
                value-format="yyyy-MM"
                style="width: 100%"
                @change="handleStatisticsSearch"
                :clearable="false"
                :editable="false"
            />
          </el-col>
          <el-col :span="4">
            <el-select
                v-model="statisticsParams.deptId"
                placeholder="选择部门"
                clearable
                @change="handleStatisticsSearch"
            >
              <el-option
                  v-for="dept in departmentOptions"
                  :key="dept.id"
                  :label="dept.dname"
                  :value="dept.id"
              />
            </el-select>
          </el-col>
          <el-col :span="16" style="text-align: right;">
            <el-button type="primary" @click="handleStatisticsSearch">
              <i class="el-icon-search"></i>
              查询
            </el-button>
            <el-button @click="handleStatisticsReset">
              <i class="el-icon-refresh"></i>
              重置
            </el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 总体统计 -->
      <div class="overall-statistics">
        <el-row :gutter="20">
          <el-col :span="4">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-item">
                <div class="stat-label">总员工数</div>
                <div class="stat-value">{{ overallStats.totalStaff || 0 }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-item">
                <div class="stat-label">总考勤记录</div>
                <div class="stat-value">{{ overallStats.totalRecords || 0 }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-item">
                <div class="stat-label">正常记录</div>
                <div class="stat-value" style="color: #67c23a;">{{ overallStats.normalRecords || 0 }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-item">
                <div class="stat-label">迟到记录</div>
                <div class="stat-value" style="color: #e6a23c;">{{ overallStats.lateRecords || 0 }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-item">
                <div class="stat-label">早退记录</div>
                <div class="stat-value" style="color: #e6a23c;">{{ overallStats.earlyRecords || 0 }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-item">
                <div class="stat-label">缺勤记录</div>
                <div class="stat-value" style="color: #f56c6c;">{{ overallStats.absentRecords || 0 }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="4">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-item">
                <div class="stat-label">缺卡记录</div>
                <div class="stat-value" style="color: #f56c6c;">{{ overallStats.missingCardRecords || 0 }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-item">
                <div class="stat-label">迟到早退</div>
                <div class="stat-value" style="color: #f56c6c;">{{ overallStats.lateEarlyRecords || 0 }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-item">
                <div class="stat-label">出勤员工</div>
                <div class="stat-value">{{ overallStats.attendedStaff || 0 }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-item">
                <div class="stat-label">正常率</div>
                <div class="stat-value">{{ overallStats.normalRate || '0%' }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-item">
                <div class="stat-label">员工出勤率</div>
                <div class="stat-value">{{ overallStats.staffAttendanceRate || '0%' }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-item">
                <div class="stat-label">部门数量</div>
                <div class="stat-value">{{ overallStats.departmentCount || 0 }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 部门统计 -->
<!--      <div class="department-statistics">-->
<!--        <el-card header="部门考勤统计" style="margin-top: 20px;">-->
<!--          <div style="text-align: center; padding: 20px; color: #909399;" v-if="departmentStats.length === 0">-->
<!--            <i class="el-icon-info" style="font-size: 48px; margin-bottom: 10px;"></i>-->
<!--            <div>暂无部门统计数据</div>-->
<!--            <div style="font-size: 12px; margin-top: 5px;">当前API返回的是总体统计数据</div>-->
<!--          </div>-->
<!--          <el-table-->
<!--              :data="departmentStats"-->
<!--              border-->
<!--              style="width: 100%"-->
<!--              v-else-->
<!--          >-->
<!--            <el-table-column prop="deptName" label="部门名称" />-->
<!--            <el-table-column prop="totalStaff" label="员工数" width="100" align="center" />-->
<!--            <el-table-column prop="normalCount" label="正常" width="100" align="center">-->
<!--              <template slot-scope="scope">-->
<!--                <span style="color: #67c23a;">{{ scope.row.normalCount }}</span>-->
<!--              </template>-->
<!--            </el-table-column>-->
<!--            <el-table-column prop="lateCount" label="迟到" width="100" align="center">-->
<!--              <template slot-scope="scope">-->
<!--                <span style="color: #e6a23c;">{{ scope.row.lateCount }}</span>-->
<!--              </template>-->
<!--            </el-table-column>-->
<!--            <el-table-column prop="earlyCount" label="早退" width="100" align="center">-->
<!--              <template slot-scope="scope">-->
<!--                <span style="color: #e6a23c;">{{ scope.row.earlyCount }}</span>-->
<!--              </template>-->
<!--            </el-table-column>-->
<!--            <el-table-column prop="missingCardCount" label="缺卡" width="100" align="center">-->
<!--              <template slot-scope="scope">-->
<!--                <span style="color: #f56c6c;">{{ scope.row.missingCardCount || 0 }}</span>-->
<!--              </template>-->
<!--            </el-table-column>-->
<!--            <el-table-column prop="absentCount" label="缺勤" width="100" align="center">-->
<!--              <template slot-scope="scope">-->
<!--                <span style="color: #f56c6c;">{{ scope.row.absentCount }}</span>-->
<!--              </template>-->
<!--            </el-table-column>-->
<!--            <el-table-column prop="attendanceRate" label="出勤率" width="120" align="center">-->
<!--              <template slot-scope="scope">-->
<!--                <el-progress-->
<!--                    :percentage="scope.row.attendanceRate"-->
<!--                    :color="getProgressColor(scope.row.attendanceRate)"-->
<!--                    :show-text="false"-->
<!--                />-->
<!--                <span>{{ scope.row.attendanceRate }}%</span>-->
<!--              </template>-->
<!--            </el-table-column>-->
<!--          </el-table>-->
<!--        </el-card>-->
<!--      </div>-->
    </div>

    <!-- 手动补卡页面 -->
    <div v-if="activeTab === 'manual'">
      <el-card header="手动补卡申请">
        <el-form :model="manualForm" :rules="manualRules" ref="manualFormRef" label-width="120px">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="选择员工" prop="staffId">
                <el-select
                    v-model="manualForm.staffId"
                    placeholder="请选择员工"
                    style="width: 100%"
                    filterable
                >
                  <el-option
                      v-for="staff in staffOptions"
                      :key="staff.id"
                      :label="`${staff.sname} (${staff.sid})`"
                      :value="staff.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="补卡日期" prop="date">
                <el-date-picker
                    v-model="manualForm.date"
                    type="date"
                    placeholder="选择补卡日期"
                    style="width: 100%"
                    value-format="yyyy-MM-dd"
                    :picker-options="datePickerOptions"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="补卡类型" prop="checkType">
                <el-radio-group v-model="manualForm.checkType">
                  <el-radio label="check_in">上班打卡</el-radio>
                  <el-radio label="check_out">下班打卡</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="补卡时间" prop="checkTime">
                <el-time-picker
                    v-model="manualForm.checkTime"
                    placeholder="选择补卡时间"
                    style="width: 100%"
                    value-format="HH:mm:ss"
                />
              </el-form-item>
            </el-col>
            <el-col :span="16">
              <el-form-item label="补卡理由" prop="reason">
                <el-input
                    v-model="manualForm.reason"
                    type="textarea"
                    :rows="2"
                    placeholder="请输入补卡理由"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item>
            <el-button type="primary" @click="handleManualSubmit" :loading="submitting">
              提交补卡申请
            </el-button>
            <el-button @click="handleManualReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script>
import API from '@/utils/api'

export default {
  name: 'AttendanceManage',
  data() {
    return {
      // 响应式数据
      loading: false,
      submitting: false,
      activeTab: 'records',

      // 考勤记录数据
      tableData: [],

      // 搜索参数
      searchParams: {
        month: this.getCurrentMonth(),
        staffName: '',
        deptId: '',
        status: ''
      },

      // 分页参数
      pagination: {
        current: 1,
        size: 10,
        total: 0
      },

      // 统计参数
      statisticsParams: {
        month: this.getCurrentMonth(),
        deptId: ''
      },

      // 总体统计
      overallStats: {},

      // 部门统计
      departmentStats: [],

      // 手动补卡表单
      manualForm: {
        staffId: '',
        date: '',
        checkType: 'check_in',
        checkTime: '',
        reason: ''
      },

      // 手动补卡验证规则
      manualRules: {
        staffId: [
          { required: true, message: '请选择员工', trigger: 'change' }
        ],
        date: [
          { required: true, message: '请选择补卡日期', trigger: 'change' }
        ],
        checkType: [
          { required: true, message: '请选择补卡类型', trigger: 'change' }
        ],
        checkTime: [
          { required: true, message: '请选择补卡时间', trigger: 'change' }
        ],
        reason: [
          { required: true, message: '请输入补卡理由', trigger: 'blur' },
          { min: 5, message: '补卡理由至少5个字符', trigger: 'blur' }
        ]
      },
      // 🔥 新增：补卡记录分页参数
      manualPagination: {
        current: 1,
        size: 10,
        total: 0
      },
      // 补卡记录
      manualRecords: [],

      // 选项数据
      departmentOptions: [],
      staffOptions: [],

      // 日期选择器选项
      datePickerOptions: {
        disabledDate(time) {
          return time.getTime() > Date.now()
        }
      }
    }
  },
  async mounted() {

    await this.loadTableData()
    await this.loadStaffOptions()
    this.loadDepartmentOptions()
  },
  methods: {
    // 获取当前月份
    getCurrentMonth() {
      const now = new Date()
      return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
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

        console.log('考勤记录请求参数:', params)

        const response = await this.$http.get(API.url.attendance.list, { params })

        console.log('考勤记录完整响应:', response.data)

        if (response.data && response.data.code === 0) {
          const data = response.data

          // 处理分页数据结构
          let records = []
          if (data.page && data.page.list) {
            records = data.page.list
            this.pagination.total = data.page.total || 0
            this.pagination.current = data.page.currPage || 1
            this.pagination.size = data.page.pageSize || 10
          } else {
            records = data.data || data.list || []
            this.pagination.total = data.total || records.length
          }

          // 处理空值数据
          this.tableData = records.map(item => ({
            ...item,
            departmentName: item.departmentName || '未分配',
            jobName: item.jobName || '未分配',
            reason: item.reason || '无'
          }))

          console.log('处理后的表格数据:', this.tableData)
          console.log('分页信息:', this.pagination)

        } else {
          this.$message.error(response.data?.msg || '加载考勤数据失败')
          this.tableData = []
          this.pagination.total = 0
        }
      } catch (error) {
        console.error('加载考勤数据失败:', error)
        this.$message.error('加载考勤数据失败: ' + (error.response?.data?.msg || error.message))
        this.tableData = []
        this.pagination.total = 0
      } finally {
        this.loading = false
      }
    },

    // 加载部门选项
    async loadDepartmentOptions() {
      try {
        const response = await this.$http.get(API.url.department.list)
        if (response.data && response.data.code === 0) {
          const data = response.data.data
          if (data && data.list) {
            this.departmentOptions = data.list
          } else {
            this.departmentOptions = data || []
          }
          console.log('部门选项:', this.departmentOptions)
        }
      } catch (error) {
        console.error('加载部门选项失败:', error)
        this.departmentOptions = []
      }
    },

    // 加载员工选项 - 修复版
    async loadStaffOptions() {
      try {
        const response = await this.$http.get(API.url.user.list)
        if (response.data && response.data.code === 0) {
          const data = response.data.data

          console.log('原始员工数据:', data) // 调试用

          // 构建员工部门映射表 - 从考勤记录中提取
          const departmentMap = this.buildStaffDepartmentMap()

          console.log('部门映射表:', departmentMap) // 调试用

          // 处理员工数据
          this.staffOptions = (data || []).map(staff => {
            const staffId = staff.id
            // 从部门映射表中获取部门信息
            const departmentName = departmentMap[staffId] || '未分配部门'

            return {
              id: staffId,
              sname: staff.sname || staff.name || `员工${staffId}`,
              sid: staff.sid || staff.employeeId || staffId,
              deptName: departmentName, // 添加部门信息
              // 保留所有原始字段
              ...staff
            }
          })

          console.log('最终员工选项:', this.staffOptions)

        } else {
          console.error('员工接口返回错误:', response.data)
          this.staffOptions = []
        }
      } catch (error) {
        console.error('加载员工选项失败:', error)
        this.staffOptions = []
      }
    },

// 构建员工部门映射表
    buildStaffDepartmentMap() {
      const departmentMap = {}
      if (this.tableData && this.tableData.length > 0) {
        // 遍历考勤记录，建立员工ID到部门名称的映射
        this.tableData.forEach(record => {
          if (record.staffId && record.departmentName && record.departmentName !== '未分配') {
            // 使用最新的部门信息（后面的记录会覆盖前面的）
            departmentMap[record.staffId] = record.departmentName
          }
        })
      }

      // 调试：检查映射结果
      console.log('员工部门映射结果:', departmentMap)
      return departmentMap
    },

// 获取员工部门信息（用于模板显示）
    getStaffDepartment(staffId) {
      // 先从员工选项中查找
      const staff = this.staffOptions.find(item => item.id === staffId)
      if (staff && staff.deptName && staff.deptName !== '未分配部门') {
        return staff.deptName
      }

      // 如果员工选项中没有，从考勤记录中实时查找
      if (this.tableData && this.tableData.length > 0) {
        const record = this.tableData.find(item => item.staffId === staffId)
        if (record && record.departmentName && record.departmentName !== '未分配') {
          return record.departmentName
        }
      }

      return '未分配部门'
    },

    // 加载统计信息 - 根据实际API响应修复
    async loadStatistics() {
      this.loading = true
      try {
        // 确保 month 参数一定有值
        const params = {
          month: this.statisticsParams.month || this.getCurrentMonth()
        }

        // 如果有部门筛选，添加部门参数
        if (this.statisticsParams.deptId) {
          params.deptId = this.statisticsParams.deptId
        }

        console.log('统计请求参数:', params)

        const response = await this.$http.get(API.url.attendance.departmentStats, {
          params: params
        })

        console.log('统计完整响应:', response.data)

        if (response.data && response.data.code === 0) {
          const statsData = response.data.data
          console.log('统计响应数据:', statsData)

          // 根据实际API响应结构处理数据
          this.processStatisticsData(statsData)
        } else {
          this.$message.error(response.data?.msg || '加载统计信息失败')
          this.setDefaultStats()
        }
      } catch (error) {
        console.error('加载统计信息失败:', error)

        if (error.response) {
          console.error('错误响应状态:', error.response.status)
          console.error('错误响应数据:', error.response.data)
          this.$message.error(`加载统计信息失败: ${error.response.data.message || error.response.statusText}`)
        } else {
          this.$message.error('加载统计信息失败: ' + error.message)
        }

        this.setDefaultStats()
      } finally {
        this.loading = false
      }
    },

    // 处理统计数据 - 根据实际API响应调整
    processStatisticsData(statsData) {
      if (!statsData) {
        this.setDefaultStats()
        return
      }

      // 直接使用API返回的数据结构
      this.overallStats = {
        // 员工相关
        totalStaff: statsData.totalStaff || 0,
        attendedStaff: statsData.attendedStaff || 0,
        departmentCount: statsData.departmentCount || 0,

        // 记录相关
        totalRecords: statsData.totalRecords || 0,
        normalRecords: statsData.normalRecords || 0,
        lateRecords: statsData.lateRecords || 0,
        earlyRecords: statsData.earlyRecords || 0,
        absentRecords: statsData.absentRecords || 0,
        missingCardRecords: statsData.missingCardRecords || 0,
        lateEarlyRecords: statsData.lateEarlyRecords || 0,

        // 比率相关
        normalRate: this.formatRate(statsData.normalRate),
        staffAttendanceRate: statsData.staffAttendanceRate || '0%',
        recordNormalRate: statsData.recordNormalRate || '0%'
      }

      // 由于API返回的是总体统计，没有部门细分数据
      this.departmentStats = []

      console.log('处理后的总体统计:', this.overallStats)
    },

    // 格式化比率
    formatRate(rate) {
      if (rate === undefined || rate === null) return '0%'
      if (typeof rate === 'number') {
        return rate.toFixed(2) + '%'
      }
      if (typeof rate === 'string' && !rate.includes('%')) {
        return parseFloat(rate).toFixed(2) + '%'
      }
      return rate
    },

    // 设置默认统计值
    setDefaultStats() {
      this.overallStats = {
        totalStaff: 0,
        attendedStaff: 0,
        departmentCount: 0,
        totalRecords: 0,
        normalRecords: 0,
        lateRecords: 0,
        earlyRecords: 0,
        absentRecords: 0,
        missingCardRecords: 0,
        lateEarlyRecords: 0,
        normalRate: '0%',
        staffAttendanceRate: '0%',
        recordNormalRate: '0%'
      }
      this.departmentStats = []
    },

// 加载补卡记录 - 修复版
    async loadManualRecords() {
      this.loading = true
      try {
        const params = {
          page: this.manualPagination.current,
          size: this.manualPagination.size,
          // 可以添加筛选条件，只显示补卡记录
          reason: '补卡'
        }

        console.log('加载补卡记录参数:', params)

        const response = await this.$http.get(API.url.attendance.list, { params })

        console.log('补卡记录响应:', response.data)

        if (response.data && response.data.code === 0) {
          const data = response.data.data
          let records = []

          // 处理不同的数据结构
          if (data && data.page) {
            records = data.page.list || []
            this.manualPagination.total = data.page.total || 0
          } else if (data && data.list) {
            records = data.list
            this.manualPagination.total = data.total || records.length
          } else {
            records = data || []
            this.manualPagination.total = records.length
          }

          // 过滤补卡记录
          this.manualRecords = records
              .filter(item => item.reason && item.reason.includes('补卡'))
              .map(item => ({
                id: item.id,
                staffName: item.staffName,
                staffId: item.staffId,
                date: item.date,
                checkType: item.checkInTime ? 'check_in' : 'check_out',
                checkTime: item.checkInTime || item.checkOutTime,
                reason: item.reason,
                createTime: item.createTime,
                status: 'approved' // 手动补卡默认已通过
              }))

          console.log('处理后的补卡记录:', this.manualRecords)

        } else {
          this.$message.error(response.data?.msg || '加载补卡记录失败')
          this.manualRecords = []
        }
      } catch (error) {
        console.error('加载补卡记录失败:', error)
        this.$message.error('加载补卡记录失败: ' + (error.response?.data?.msg || error.message))
        this.manualRecords = []
      } finally {
        this.loading = false
      }
    },

    // 选项卡切换
    handleTabChange(tab) {
      if (tab.name === 'statistics') {
        this.loadStatistics()
      } else if (tab.name === 'manual') {
        this.loadManualRecords()
      }
    },

    // 搜索
    handleSearch() {
      this.pagination.current = 1
      this.loadTableData()
    },

    // 重置搜索
    handleReset() {
      this.searchParams = {
        month: this.getCurrentMonth(),
        staffName: '',
        deptId: '',
        status: ''
      }
      this.pagination.current = 1
      this.loadTableData()
    },

    // 统计搜索
    handleStatisticsSearch() {
      this.loadStatistics()
    },

    // 统计重置
    handleStatisticsReset() {
      this.statisticsParams = {
        month: this.getCurrentMonth(),
        deptId: ''
      }
      this.loadStatistics()
    },
// 提交手动补卡 - 完整修复版
    async handleManualSubmit() {
      try {
        // 验证表单
        await this.$refs.manualFormRef.validate()
        this.submitting = true

        // 转换参数格式
        const checkType = this.manualForm.checkType.replace('_', '-') // check_in -> check-in
        const manualTime = this.manualForm.checkTime.substring(0, 5)  // HH:mm:ss -> HH:mm

        const submitData = {
          staffId: this.manualForm.staffId,
          date: this.manualForm.date,
          checkType: checkType,
          manualTime: manualTime,
          reason: this.manualForm.reason
        }

        console.log('提交补卡数据:', JSON.stringify(submitData, null, 2))

        const response = await this.$http.post(API.url.attendance.manualCheck, submitData)

        if (response.data && response.data.code === 0) {
          this.$message.success('补卡成功！')

          // 重置表单
          this.handleManualReset()

          // 刷新补卡记录
          this.loadManualRecords()

          // 可选：刷新考勤记录列表
          this.loadTableData()

        } else {
          this.$message.error(response.data?.msg || '补卡失败')
        }

      } catch (error) {
        console.error('补卡失败:', error)

        // 如果是表单验证错误，不显示错误消息
        if (error && error.name === 'ValidationError') {
          return
        }

        const errorMsg = error.response?.data?.msg || error.message
        this.$message.error(`补卡失败: ${errorMsg}`)
      } finally {
        this.submitting = false
      }
    },

// 重置手动补卡表单 - 修复版
    handleManualReset() {
      if (this.$refs.manualFormRef) {
        // 先重置表单验证状态
        this.$refs.manualFormRef.clearValidate()
        // 再重置表单数据
        this.$refs.manualFormRef.resetFields()
      }
      // 确保表单数据被重置
      this.manualForm = {
        staffId: '',
        date: '',
        checkType: 'check_in',
        checkTime: '',
        reason: ''
      }
    },

    // 导出Excel
    async handleExport() {
      try {
        this.loading = true

        const params = { ...this.searchParams }

        Object.keys(params).forEach(key => {
          if (params[key] === '' || params[key] === null || params[key] === undefined) {
            delete params[key]
          }
        })

        console.log('导出参数:', params)

        const response = await this.$http.get(API.url.attendance.export, {
          params: params,
          responseType: 'blob'
        })

        if (response.data instanceof Blob) {
          const blob = new Blob([response.data], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
          })

          if (blob.size === 0) {
            this.$message.error('导出的文件为空，请检查数据')
            return
          }

          const url = window.URL.createObjectURL(blob)
          const link = document.createElement('a')
          link.href = url

          const contentDisposition = response.headers['content-disposition']
          let fileName = `考勤数据_${this.searchParams.month || this.getCurrentMonth()}.xlsx`

          if (contentDisposition) {
            const match = contentDisposition.match(/filename\*=utf-8''(.+)/)
            if (match && match[1]) fileName = decodeURIComponent(match[1])
          }

          link.setAttribute('download', fileName)
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)
          window.URL.revokeObjectURL(url)

          this.$message.success('导出成功')
        } else {
          const text = await new Response(response.data).text()
          try {
            const err = JSON.parse(text)
            this.$message.error(err.msg || '导出失败')
          } catch {
            this.$message.error('导出失败: 服务器返回了非文件数据')
          }
        }
      } catch (error) {
        console.error('导出失败:', error)
        this.$message.error('导出失败: ' + error.message)
      } finally {
        this.loading = false
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

    // 获取状态标签样式
    getStatusTag(status) {
      const statusMap = {
        '正常': 'success',
        '迟到': 'warning',
        '早退': 'warning',
        '迟到早退': 'danger',
        '上班缺卡': 'danger',
        '下班缺卡': 'danger',
        '缺勤': 'danger',
        '请假': 'info',
        '外出': 'primary',
        '加班': 'warning',
        '调休': 'info'
      }
      return statusMap[status] || 'info'
    },

    // 获取时间样式
    getTimeClass(time, type, record) {
      if (!time) return 'missing-time'

      if (record.status === '请假') {
        return 'leave-time'
      }

      if (type === 'in') {
        if (time > '09:30:00') {
          return 'late-time'
        }
        if (time < '08:00:00') {
          return 'early-time'
        }
      } else if (type === 'out') {
        if (time < '18:00:00') {
          return 'early-time'
        }
        if (time > '20:00:00') {
          return 'overtime-time'
        }
      }

      return 'normal-time'
    },

    // 获取显示状态
    getDisplayStatus(record) {
      if (record.status && record.status !== '正常') {
        return record.status
      }

      const hasCheckIn = !!record.checkInTime
      const hasCheckOut = !!record.checkOutTime
      const isLate = record.lateMin > 0
      const isEarly = record.earlyMin > 0

      if (!hasCheckIn && !hasCheckOut) {
        return '缺勤'
      } else if (!hasCheckIn) {
        return '上班缺卡'
      } else if (!hasCheckOut) {
        return '下班缺卡'
      } else if (isLate && isEarly) {
        return '迟到早退'
      } else if (isLate) {
        return '迟到'
      } else if (isEarly) {
        return '早退'
      }

      return record.status || '正常'
    },

    // 获取显示备注
    getDisplayReason(record) {
      if (record.reason) return record.reason

      const status = this.getDisplayStatus(record)
      switch (status) {
        case '上班缺卡':
          return '上班未打卡'
        case '下班缺卡':
          return '下班未打卡'
        case '缺勤':
          return '全天未打卡'
        case '迟到':
          return `迟到${record.lateMin}分钟`
        case '早退':
          return `早退${record.earlyMin}分钟`
        case '迟到早退':
          return `迟到${record.lateMin}分钟，早退${record.earlyMin}分钟`
        default:
          return '无'
      }
    },

    // 获取进度条颜色
    getProgressColor(percentage) {
      if (percentage >= 95) return '#67c23a'
      if (percentage >= 90) return '#e6a23c'
      return '#f56c6c'
    },

    // 格式化日期
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleDateString('zh-CN')
    },

    // 格式化日期时间
    formatDateTime(datetime) {
      if (!datetime) return ''
      return new Date(datetime).toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
.attendance-manage {
  padding: 20px;
  background: #fff;
  border-radius: 4px;
}

.tab-area {
  margin-bottom: 20px;
}

.search-operate-area {
  margin-bottom: 20px;
}

.overall-statistics {
  margin-bottom: 20px;
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
  padding: 10px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
}

.table-area {
  margin-bottom: 20px;
}

.pagination-area {
  display: flex;
  justify-content: flex-end;
}

::v-deep .el-table .cell {
  text-align: center;
}

::v-deep .el-table th > .cell {
  text-align: center;
}

/* 考勤时间样式 */
.normal-time {
  color: #67c23a;
}

.late-time {
  color: #e6a23c;
  font-weight: bold;
}

.early-time {
  color: #e6a23c;
  font-weight: bold;
}

.overtime-time {
  color: #f56c6c;
  font-weight: bold;
}

.missing-time {
  color: #f56c6c;
  font-weight: bold;
}

.leave-time {
  color: #909399;
  font-style: italic;
}
</style>