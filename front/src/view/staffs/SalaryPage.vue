<template>
  <div class="salary-page">
    <div class="page-header">
      <h1>薪资查询</h1>
      <p>查看您的薪资明细和统计信息</p>
    </div>

    <el-row :gutter="20">
      <!-- 主要内容区域 -->
      <el-col :span="16">
        <!-- 月度薪资记录 -->
        <el-card class="salary-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>月度薪资记录</span>
              <div class="month-selector">
                <el-date-picker
                    v-model="selectedMonth"
                    type="month"
                    placeholder="选择月份"
                    format="yyyy年MM月"
                    value-format="yyyy-MM"
                    @change="handleMonthChange"
                    size="small"
                    style="width: 140px; margin-right: 10px;"
                >
                </el-date-picker>
                <el-button
                    type="text"
                    @click="loadCurrentMonth"
                    size="mini"
                >
                  本月
                </el-button>
                <el-button
                    type="primary"
                    @click="loadSalaryRecords"
                    :loading="loading"
                    size="mini"
                >
                  查询
                </el-button>
              </div>
            </div>
          </template>

          <!-- 加载状态 -->
          <div v-if="loading" class="loading-container">
            <i class="el-icon-loading"></i> 加载中...
          </div>

          <!-- 薪资记录表格 -->
          <el-table
              v-else
              :data="salaryRecords"
              style="width: 100%"
              empty-text="暂无薪资记录"
          >
            <el-table-column prop="month" label="月份" width="120">
              <template slot-scope="scope">
                {{ formatMonth(scope.row.month) }}
              </template>
            </el-table-column>
            <el-table-column prop="actualBasePay" label="基本工资" width="120">
              <template slot-scope="scope">
                {{ formatCurrency(scope.row.actualBasePay) }}
              </template>
            </el-table-column>
            <el-table-column prop="overtimePay" label="加班费" width="120">
              <template slot-scope="scope">
                {{ formatCurrency(scope.row.overtimePay) }}
              </template>
            </el-table-column>
            <el-table-column prop="leaveDeduction" label="请假扣款" width="120">
              <template slot-scope="scope">
                <span style="color: #F56C6C">
                  -{{ formatCurrency(scope.row.leaveDeduction) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="totalSalary" label="实发工资" width="130">
              <template slot-scope="scope">
                <span style="color: #67C23A; font-weight: bold">
                  {{ formatCurrency(scope.row.totalSalary) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="getStatusTagType(scope.row.status)" size="small">
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="calcTime" label="计算时间" width="150">
              <template slot-scope="scope">
                {{ formatDateTime(scope.row.calcTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template slot-scope="scope">
                <el-button
                    type="text"
                    size="mini"
                    @click="viewSalaryDetail(scope.row)"
                >
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-container" v-if="total > 0">
            <el-pagination
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :current-page="currentPage"
                :page-sizes="[10, 20, 50]"
                :page-size="pageSize"
                layout="total, sizes, prev, pager, next, jumper"
                :total="total"
            >
            </el-pagination>
          </div>
        </el-card>

        <!-- 薪资统计 -->
        <el-card class="stats-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>年度薪资统计</span>
              <div class="year-selector">
                <el-select
                    v-model="selectedYear"
                    placeholder="选择年份"
                    @change="handleYearChange"
                    size="small"
                    style="width: 120px; margin-right: 10px;"
                >
                  <el-option
                      v-for="year in yearOptions"
                      :key="year"
                      :label="year + '年'"
                      :value="year"
                  >
                  </el-option>
                </el-select>
                <el-button
                    type="text"
                    @click="loadCurrentYear"
                    size="mini"
                >
                  今年
                </el-button>
                <el-button
                    type="text"
                    @click="loadSalaryStatistics"
                    :loading="statsLoading"
                    size="mini"
                >
                  刷新
                </el-button>
              </div>
            </div>
          </template>

          <div v-if="statsLoading" class="loading-container">
            <i class="el-icon-loading"></i> 统计加载中...
          </div>

          <div v-else class="stats-content">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="stat-item">
                  <div class="stat-icon total-salary">
                    <i class="el-icon-money"></i>
                  </div>
                  <div class="stat-info">
                    <div class="stat-value">{{ formatCurrency(salaryStats.yearTotalSalary || 0) }}</div>
                    <div class="stat-label">年度总收入</div>
                  </div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-item">
                  <div class="stat-icon avg-salary">
                    <i class="el-icon-trend-chart"></i>
                  </div>
                  <div class="stat-info">
                    <div class="stat-value">{{ formatCurrency(salaryStats.monthAvgSalary || 0) }}</div>
                    <div class="stat-label">月平均收入</div>
                  </div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-item">
                  <div class="stat-icon month-count">
                    <i class="el-icon-date"></i>
                  </div>
                  <div class="stat-info">
                    <div class="stat-value">{{ salaryStats.totalMonths || 0 }}</div>
                    <div class="stat-label">已发薪月份</div>
                  </div>
                </div>
              </el-col>
            </el-row>

            <!-- 详细统计 -->
            <div class="detailed-stats" v-if="salaryStats.hasData">
              <el-row :gutter="20" class="stats-grid">
                <el-col :span="12">
                  <div class="detail-stat">
                    <span class="label">最高月薪：</span>
                    <span class="value">{{ formatCurrency(salaryStats.maxSalary) }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="detail-stat">
                    <span class="label">最低月薪：</span>
                    <span class="value">{{ formatCurrency(salaryStats.minSalary) }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="detail-stat">
                    <span class="label">总加班费：</span>
                    <span class="value" style="color: #67C23A">
                      {{ formatCurrency(salaryStats.totalOvertimePay) }}
                    </span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="detail-stat">
                    <span class="label">总扣款：</span>
                    <span class="value" style="color: #F56C6C">
                      -{{ formatCurrency(salaryStats.totalDeductions) }}
                    </span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="detail-stat">
                    <span class="label">请假天数：</span>
                    <span class="value">{{ salaryStats.totalLeaveDays || 0 }} 天</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="detail-stat">
                    <span class="label">请假扣款：</span>
                    <span class="value" style="color: #F56C6C">
                      -{{ formatCurrency(salaryStats.totalLeaveDeduction) }}
                    </span>
                  </div>
                </el-col>
              </el-row>
            </div>
            <div v-else class="no-stats-data">
              <i class="el-icon-document-remove"></i>
              <p>{{ salaryStats.message || `暂无${selectedYear}年统计数据` }}</p>
            </div>
            <div class="stats-note">
              <el-divider></el-divider>
              <p class="note-title">💡 数据说明</p>
              <div class="salary-rules">
                <p class="note-content">
                  💰 <strong>总扣款说明：</strong><br />
                  包含迟到、早退、迟到早退、缺卡、缺勤、请假等扣款项目。
                </p>

                <el-divider></el-divider>

                <p class="note-content">
                  📘 <strong>各项扣款与补贴计算规则：</strong>
                </p>
                <ul class="formula-list">
                  <li><span class="formula-name">请假扣款：</span> 请假天数 × 日薪</li>
                  <li><span class="formula-name">缺勤扣款：</span> 缺勤天数 × 日薪</li>
                  <li><span class="formula-name">缺卡扣款：</span> 缺卡天数 × 50% × 日薪</li>
                  <li><span class="formula-name">迟到早退扣款：</span> 不正常次数 × 2% × 日薪</li>
                  <li><span class="formula-name">加班费：</span> 加班时长 × 时薪 × <strong style="color:#67C23A;">1.5</strong></li>
                </ul>

                <el-divider></el-divider>

                <p class="note-content">
                  🧾 <strong>最终实发工资计算公式：</strong><br />
                  <span class="final-formula">
                    <span class="formula-name">实发工资 = </span> 基础工资 − 请假扣款 − 缺勤扣款 − 缺卡扣款 − 迟到早退扣款 + 加班费
                  </span>
                </p>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 侧边栏 -->
      <el-col :span="8">
        <!-- 月度薪资概览 -->
        <el-card class="overview-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>月度薪资概览</span>
              <div class="overview-selector">
                <el-select
                    v-model="overviewMonth"
                    placeholder="选择月份"
                    @change="handleOverviewMonthChange"
                    size="small"
                    style="width: 130px; margin-right: 10px;"
                >
                  <el-option
                      v-for="month in monthOptions"
                      :key="month.value"
                      :label="month.label"
                      :value="month.value"
                  >
                  </el-option>
                </el-select>
                <el-button
                    type="text"
                    @click="loadCurrentMonthSalary"
                    size="mini"
                >
                  本月
                </el-button>
              </div>
            </div>
          </template>

          <div v-if="currentMonthSalary" class="overview-content">
            <div class="month-title">{{ formatMonth(overviewMonth) }}薪资</div>
            <div class="salary-amount">
              <div class="amount">{{ formatCurrency(currentMonthSalary.totalSalary) }}</div>
              <div class="label">实发工资</div>
            </div>

            <div class="salary-breakdown">
              <div class="breakdown-item">
                <span class="item-label">基本工资</span>
                <span class="item-value">{{ formatCurrency(currentMonthSalary.actualBasePay) }}</span>
              </div>
              <div class="breakdown-item">
                <span class="item-label">加班费</span>
                <span class="item-value" style="color: #67C23A">
                  +{{ formatCurrency(currentMonthSalary.overtimePay) }}
                </span>
              </div>
              <div class="breakdown-item">
                <span class="item-label">请假扣款</span>
                <span class="item-value" style="color: #F56C6C">
                  -{{ formatCurrency(currentMonthSalary.leaveDeduction) }}
                </span>
              </div>
              <div class="breakdown-item total-item">
                <span class="item-label">实发工资</span>
                <span class="item-value" style="color: #67C23A; font-weight: bold">
                  {{ formatCurrency(currentMonthSalary.totalSalary) }}
                </span>
              </div>
            </div>

            <div class="salary-status">
              <el-tag :type="getStatusTagType(currentMonthSalary.status)" size="medium">
                {{ currentMonthSalary.status }}
              </el-tag>
              <span class="calc-time" v-if="currentMonthSalary.calcTime">
                计算时间：{{ formatDateTime(currentMonthSalary.calcTime) }}
              </span>
            </div>
          </div>

          <div v-else class="no-data">
            <i class="el-icon-document-remove"></i>
            <p>{{ formatMonth(overviewMonth) }}暂无薪资记录</p>
          </div>
        </el-card>

        <!-- 快速月份导航 -->
        <el-card class="quick-nav-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>快速月份导航</span>
            </div>
          </template>

          <div class="quick-nav-content">
            <el-button
                v-for="month in recentMonths"
                :key="month.value"
                type="text"
                size="mini"
                @click="quickSelectMonth(month.value)"
                :class="{ active: selectedMonth === month.value }"
            >
              {{ month.label }}
            </el-button>
          </div>
        </el-card>

        <!-- 薪资说明 -->
        <el-card class="explanation-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>薪资说明</span>
            </div>
          </template>

          <div class="explanation-content">
            <div class="explanation-item">
              <i class="el-icon-info"></i>
              <span>薪资通常在每月10日前发放</span>
            </div>
            <div class="explanation-item">
              <i class="el-icon-warning-outline"></i>
              <span>如有疑问，请联系HR部门</span>
            </div>
            <div class="explanation-item">
              <i class="el-icon-question"></i>
              <span>扣款包含请假、迟到等考勤扣款</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 薪资详情对话框 -->
    <el-dialog
        title="薪资详情"
        :visible.sync="detailDialogVisible"
        width="700px"
        :before-close="handleDetailClose"
    >
      <div v-if="selectedSalary" class="salary-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="月份">
            {{ formatMonth(selectedSalary.month) }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(selectedSalary.status)" size="small">
              {{ selectedSalary.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="员工姓名">
            {{ selectedSalary.staffName || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="部门">
            {{ selectedSalary.deptName || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="基本工资">
            {{ formatCurrency(selectedSalary.actualBasePay) }}
          </el-descriptions-item>
          <el-descriptions-item label="加班费">
            {{ formatCurrency(selectedSalary.overtimePay) }}
          </el-descriptions-item>
          <el-descriptions-item label="请假天数">
            {{ selectedSalary.leaveDays || 0 }} 天
          </el-descriptions-item>
          <el-descriptions-item label="请假扣款">
            -{{ formatCurrency(selectedSalary.leaveDeduction) }}
          </el-descriptions-item>
          <el-descriptions-item label="应发工资" :span="2">
        <span style="font-weight: bold; color: #409EFF">
          {{ formatCurrency(selectedSalary.actualBasePay + selectedSalary.overtimePay) }}
        </span>
          </el-descriptions-item>
          <el-descriptions-item label="实发工资" :span="2">
        <span style="font-weight: bold; color: #67C23A; font-size: 16px">
          {{ formatCurrency(selectedSalary.totalSalary) }}
        </span>
          </el-descriptions-item>
          <el-descriptions-item label="计算时间" :span="2">
            {{ formatDateTime(selectedSalary.calcTime) || '未计算' }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">
            {{ selectedSalary.remarks || '无' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 这里就是需要修改的footer部分 -->
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          type="primary"
          @click="downloadSalarySlip"
          v-if="selectedSalary && selectedSalary.status === '已发放'"
          :loading="downloadLoading"
        >
          <i class="el-icon-download"></i> 下载工资条
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import API from '@/utils/api.js'

export default {
  name: 'SalaryPage',
  data() {
    const currentYear = new Date().getFullYear()
    const currentMonth = new Date().toISOString().slice(0, 7)

    return {
      // 查询参数
      selectedMonth: currentMonth,
      selectedYear: currentYear.toString(),
      overviewMonth: currentMonth, // 月度概览选中的月份
      currentPage: 1,
      pageSize: 10,
      total: 0,

      // 数据
      salaryRecords: [],
      currentMonthSalary: null,
      salaryStats: {},
      selectedSalary: null,

      // 加载状态
      loading: false,
      statsLoading: false,
      downloadLoading: false, // 新增：下载加载状态


      // 对话框
      detailDialogVisible: false,

      // 最近月份和年份
      recentMonths: [],
      yearOptions: [],
      monthOptions: []
    }
  },
  computed: {
    // 计算当前月份
    currentMonth() {
      return new Date().toISOString().slice(0, 7)
    }
  },
  mounted() {
    this.initPage()
    this.generateRecentMonths()
    this.generateYearOptions()
    this.generateMonthOptions()
  },
  methods: {
    // 初始化页面
    initPage() {
      this.loadSalaryRecords()
      this.loadCurrentMonthSalary()
      this.loadSalaryStatistics()
    },

    // 生成年份选项
    generateYearOptions() {
      const currentYear = new Date().getFullYear()
      const years = []
      // 生成最近5年
      for (let i = 4; i >= 0; i--) {
        years.push(currentYear - i)
      }
      this.yearOptions = years
    },

    // 生成月份选项（最近12个月）
    generateMonthOptions() {
      const months = []
      const today = new Date()
      for (let i = 0; i < 12; i++) {
        const date = new Date(today.getFullYear(), today.getMonth() - i, 1)
        const value = date.toISOString().slice(0, 7)
        const label = `${date.getFullYear()}年${(date.getMonth() + 1).toString().padStart(2, '0')}月`
        months.push({ value, label })
      }
      this.monthOptions = months
    },

    // 生成最近月份（用于快速导航）
    generateRecentMonths() {
      const months = []
      const today = new Date()
      for (let i = 0; i < 6; i++) {
        const date = new Date(today.getFullYear(), today.getMonth() - i, 1)
        const value = date.toISOString().slice(0, 7)
        const label = `${date.getFullYear()}年${(date.getMonth() + 1).toString().padStart(2, '0')}月`
        months.push({ value, label })
      }
      this.recentMonths = months
    },

    // 快速选择月份
    quickSelectMonth(month) {
      this.selectedMonth = month
      this.currentPage = 1
      this.loadSalaryRecords()
    },

    // 年份变化处理
    handleYearChange(year) {
      if (year) {
        this.loadSalaryStatistics()
      }
    },

    // 月度概览月份变化
    handleOverviewMonthChange(month) {
      if (month) {
        this.loadCurrentMonthSalary()
      }
    },

    // 加载当前年份
    loadCurrentYear() {
      this.selectedYear = new Date().getFullYear().toString()
      this.loadSalaryStatistics()
    },

    // 加载薪资记录
    async loadSalaryRecords() {
      this.loading = true
      try {
        const params = {
          page: this.currentPage,
          limit: this.pageSize
        }

        // 如果选择了月份，添加到参数中
        if (this.selectedMonth) {
          params.month = this.selectedMonth
        }

        const { data } = await this.$http({
          url: API.url.salary.mySalary,
          method: 'get',
          params: params
        })

        if (data && data.code === 0) {
          this.salaryRecords = data.page?.list || []
          this.total = data.page?.totalCount || 0
        } else {
          this.$message.error(data?.msg || '加载薪资记录失败')
        }
      } catch (error) {
        console.error('加载薪资记录失败:', error)
        this.$message.error('加载薪资记录失败')
      } finally {
        this.loading = false
      }
    },

    // 加载月度薪资概览
    async loadCurrentMonthSalary() {
      try {
        const params = {
          page: 1,
          limit: 1,
          month: this.overviewMonth
        }

        const { data } = await this.$http({
          url: API.url.salary.mySalary,
          method: 'get',
          params: params
        })

        if (data && data.code === 0 && data.page?.list?.length > 0) {
          this.currentMonthSalary = data.page.list[0]
        } else {
          this.currentMonthSalary = null
        }
      } catch (error) {
        console.error('加载月度薪资失败:', error)
        this.currentMonthSalary = null
      }
    },

    // 加载薪资统计
    async loadSalaryStatistics() {
      this.statsLoading = true
      try {
        const { data } = await this.$http({
          url: `/salary/myStatistics/${this.selectedYear}`,
          method: 'get'
        })

        if (data && data.code === 0) {
          this.salaryStats = data.data || {}
        } else {
          this.$message.error(data?.msg || '加载统计失败')
        }
      } catch (error) {
        console.error('加载薪资统计失败:', error)
        this.$message.error('加载薪资统计失败')
      } finally {
        this.statsLoading = false
      }
    },

    // 查看薪资详情
    async viewSalaryDetail(salary) {
      try {
        const { data } = await this.$http({
          url: API.buildUrl(API.url.salary.detail, { id: salary.id }),
          method: 'get'
        })

        if (data && data.code === 0) {
          this.selectedSalary = data.data
          this.detailDialogVisible = true
        } else {
          this.$message.error(data?.msg || '获取详情失败')
        }
      } catch (error) {
        console.error('获取薪资详情失败:', error)
        this.$message.error('获取薪资详情失败')
      }
    },

// 下载工资条
    // 下载工资条
    // 下载工资条
    async downloadSalarySlip() {
      if (!this.selectedSalary) return

      this.downloadLoading = true
      try {
        this.$message.info('正在生成工资条，请稍候...')

        // 使用修复后的 $http - 注意这里不要解构，直接获取完整response
        const response = await this.$http({
          url: API.url.salary.exportMySalary,
          method: 'get',
          params: {
            month: this.selectedSalary.month
          },
          responseType: 'blob' // 重要：指定响应类型为 blob
        })

        console.log('🔍 完整响应对象:', response)
        console.log('🔍 响应数据类型:', typeof response.data)
        console.log('🔍 是否是Blob:', response.data instanceof Blob)
        console.log('🔍 Blob大小:', response.data.size)
        console.log('🔍 Blob类型:', response.data.type)

        // 现在 response.data 应该是正确的 Blob 对象
        const blob = response.data

        if (!blob || blob.size === 0) {
          this.$message.error('生成的工资条文件为空')
          return
        }

        // 创建下载链接
        const downloadUrl = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = downloadUrl

        // 设置文件名
        const fileName = `${this.selectedSalary.staffName || '员工'}_${this.selectedSalary.month}_工资条.xlsx`
        link.download = fileName
        link.style.display = 'none'

        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)

        // 释放URL对象
        window.URL.revokeObjectURL(downloadUrl)

        this.$message.success('工资条下载成功')

      } catch (error) {
        console.error('❌ 下载工资条失败:', error)
        this.$message.error('下载失败: ' + (error.message || '未知错误'))
      } finally {
        this.downloadLoading = false
      }
    },

    // 月份变化处理
    handleMonthChange(month) {
      if (month) {
        this.currentPage = 1
        this.loadSalaryRecords()
      }
    },

    // 加载当前月
    loadCurrentMonth() {
      this.selectedMonth = this.currentMonth
      this.currentPage = 1
      this.loadSalaryRecords()
    },

    // 分页大小变化
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.loadSalaryRecords()
    },

    // 当前页变化
    handleCurrentChange(page) {
      this.currentPage = page
      this.loadSalaryRecords()
    },

    // 关闭详情对话框
    handleDetailClose() {
      this.detailDialogVisible = false
      this.selectedSalary = null
    },

    // 格式化货币
    formatCurrency(amount) {
      if (amount === null || amount === undefined) return '¥0.00'
      return '¥' + parseFloat(amount).toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,')
    },

    // 格式化月份
    formatMonth(month) {
      if (!month) return ''
      if (typeof month === 'string') {
        const [year, monthNum] = month.split('-')
        return `${year}年${monthNum}月`
      }
      return month
    },

    // 格式化日期时间
    formatDateTime(dateTime) {
      if (!dateTime) return ''
      const date = new Date(dateTime)
      return date.toLocaleString('zh-CN')
    },

    // 获取状态标签类型
    getStatusTagType(status) {
      const statusMap = {
        '待审核': 'warning',
        '已发放': 'success',
        '已计算': 'info',
        '已取消': 'danger'
      }
      return statusMap[status] || 'info'
    }
  }
}
</script>

<style lang="scss" scoped>
.salary-page {
  padding: 20px;
}
.salary-rules {
  margin-top: 16px;
  padding: 16px 20px;
  background: #f9fafc;
  border-radius: 10px;
  border: 1px solid #ebeef5;
}
.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  color: #303133;
  font-size: 24px;
}

.page-header p {
  margin: 8px 0 0 0;
  color: #909399;
  font-size: 14px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}
.formula-list {
  list-style: none;
  margin: 10px 0 14px 0;
  padding: 0;
}

.formula-list li {
  margin: 6px 0;
  padding: 8px 12px;
  background: #f5f7fa; /* 统一浅灰底 */
  border-left: 4px solid #409eff; /* Element 蓝 */
  border-radius: 6px;
  font-family: 'Courier New', monospace;
  color: #303133;
  line-height: 1.6;
  transition: all 0.3s ease;
}

.formula-list li:hover {
  background: #ecf5ff; /* hover 时轻微高亮 */
}

.formula-name {
  color: #409eff;
  font-weight: 600;
}

.final-formula {
  display: block;
  margin-top: 6px;
  padding: 10px 12px;
  background: #f5f7fa;
  border-left: 4px solid #409eff;
  border-radius: 6px;
  font-family: 'Courier New', monospace;
  color: #303133;
  line-height: 1.6;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.month-selector,
.year-selector,
.overview-selector {
  display: flex;
  align-items: center;
}

.loading-container {
  text-align: center;
  padding: 40px;
  color: #909399;

  i {
    margin-right: 8px;
  }
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

/* 统计卡片样式 */
.stats-card {
  margin-top: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  height: 80px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
  color: white;

  &.total-salary {
    background: #409EFF;
  }

  &.avg-salary {
    background: #67C23A;
  }

  &.month-count {
    background: #E6A23C;
  }
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.detailed-stats {
  margin-top: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.no-stats-data {
  text-align: center;
  padding: 40px;
  color: #909399;

  i {
    font-size: 48px;
    margin-bottom: 16px;
  }

  p {
    margin: 0;
  }
}

.stats-grid {
  .detail-stat {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;

    .label {
      color: #606266;
    }

    .value {
      color: #303133;
      font-weight: 500;
    }
  }
}

/* 概览卡片样式 */
.overview-card {
  margin-bottom: 20px;
}

.overview-content {
  text-align: center;
}

.month-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 15px;
  padding: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 6px;
}

.salary-amount {
  margin-bottom: 20px;

  .amount {
    font-size: 36px;
    font-weight: bold;
    color: #67C23A;
    margin-bottom: 8px;
  }

  .label {
    color: #909399;
    font-size: 14px;
  }
}

.salary-breakdown {
  margin-bottom: 20px;

  .breakdown-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;
    border-bottom: 1px solid #f0f0f0;

    &.total-item {
      border-top: 2px solid #e0e0e0;
      margin-top: 8px;
      padding-top: 12px;
    }

    &:last-child {
      border-bottom: none;
    }

    .item-label {
      color: #606266;
    }

    .item-value {
      font-weight: 500;
    }
  }
}

.salary-status {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.calc-time {
  color: #909399;
  font-size: 12px;
}

.no-data {
  text-align: center;
  padding: 40px;
  color: #909399;

  i {
    font-size: 48px;
    margin-bottom: 16px;
  }

  p {
    margin: 0;
  }
}

/* 快速导航卡片 */
.quick-nav-card {
  margin-bottom: 20px;
}

.quick-nav-content {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;

  .el-button {
    flex: 1;
    min-width: 80px;

    &.active {
      color: #409EFF;
      background-color: #ecf5ff;
      border-color: #409EFF;
    }
  }
}

/* 说明卡片样式 */
.explanation-card {
  margin-bottom: 20px;
}

.explanation-content {
  .explanation-item {
    display: flex;
    align-items: center;
    padding: 8px 0;
    color: #606266;

    i {
      margin-right: 8px;
      color: #409EFF;
    }
  }
}

/* 薪资详情样式 */
.salary-detail {
  ::v-deep .el-descriptions__label {
    width: 100px;
    font-weight: 500;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stat-item {
    flex-direction: column;
    text-align: center;
    height: auto;

    .stat-icon {
      margin-right: 0;
      margin-bottom: 10px;
    }
  }

  .stats-grid {
    .el-col {
      margin-bottom: 10px;
    }
  }

  .quick-nav-content {
    .el-button {
      min-width: 60px;
      font-size: 12px;
    }
  }
}
.detailed-stats {
  margin-top: 20px;
}

.stats-note {
  margin-top: 20px;
  padding: 12px 16px;
  background-color: #f9fafc;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.note-title {
  font-weight: 600;
  color: #409eff;
  margin-bottom: 6px;
}

.note-content {
  color: #606266;
  line-height: 1.6;
  margin: 2px 0;
}
</style>