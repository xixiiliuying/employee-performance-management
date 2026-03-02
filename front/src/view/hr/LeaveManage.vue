<template>
  <div class="leave-approve">
    <!-- 选项卡 -->
    <div class="tab-area">
      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <el-tab-pane label="待审批" name="pending"></el-tab-pane>
        <el-tab-pane label="已审批" name="approved"></el-tab-pane>
        <el-tab-pane label="全部记录" name="all"></el-tab-pane>
      </el-tabs>
    </div>

    <!-- 搜索和操作区域 -->
    <div class="search-operate-area">
      <el-row :gutter="20">
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
              v-model="searchParams.leaveType"
              placeholder="请假类型"
              clearable
              @change="handleSearch"
          >
            <el-option label="病假" value="病假" />
            <el-option label="事假" value="事假" />
            <el-option label="年假" value="年假" />
            <el-option label="调休" value="调休" />
            <el-option label="婚假" value="婚假" />
            <el-option label="产假" value="产假" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="yyyy-MM-dd"
              style="width: 100%"
              @change="handleDateChange"
          />
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
        <el-col :span="6" style="text-align: right;">
          <el-button type="primary" @click="handleSearch">
            <i class="el-icon-search"></i>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <i class="el-icon-refresh"></i>
            重置
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 统计信息 -->
    <div class="statistics-area" v-if="activeTab === 'pending'">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <div class="stat-label">待审批总数</div>
              <div class="stat-value">{{ statistics.pendingCount || 0 }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <div class="stat-label">今日申请</div>
              <div class="stat-value">{{ statistics.todayCount || 0 }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <div class="stat-label">本周申请</div>
              <div class="stat-value">{{ statistics.weekCount || 0 }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <div class="stat-label">本月申请</div>
              <div class="stat-value">{{ statistics.monthCount || 0 }}</div>
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
          :default-sort="{ prop: 'createTime', order: 'descending' }"
      >
        <el-table-column type="index" width="50" label="序号" />
        <el-table-column prop="staffName" label="申请人" width="100" />
        <el-table-column prop="staffId" label="工号" width="100" />
        <el-table-column prop="departmentName" label="部门" width="120" />
        <el-table-column prop="jobName" label="岗位" width="120" />
        <el-table-column prop="leaveType" label="请假类型" width="100">
          <template slot-scope="scope">
            <el-tag :type="getLeaveTypeTag(scope.row.leaveType)">
              {{ scope.row.leaveType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="days" label="请假天数" width="100" align="center">
          <template slot-scope="scope">
            <el-tag type="info">{{ scope.row.days }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="请假时间" width="220">
          <template slot-scope="scope">
            <div>{{ formatDate(scope.row.startDate) }}</div>
            <div style="color: #909399; font-size: 12px;">至</div>
            <div>{{ formatDate(scope.row.endDate) }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="请假事由" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" fixed="right">
          <template slot-scope="scope">
            <el-tag :type="getStatusTag(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="160">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button size="small" type="primary" @click="handleDetail(scope.row)">
              详情
            </el-button>
            <template v-if="scope.row.status === '待审批'">
              <el-button
                  size="small"
                  type="success"
                  @click="handleApprove(scope.row)"
              >
                通过
              </el-button>
              <el-button
                  size="small"
                  type="danger"
                  @click="handleReject(scope.row)"
              >
                拒绝
              </el-button>
            </template>
            <span v-else style="color: #909399;">已处理</span>
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

    <!-- 请假详情对话框 -->
    <el-dialog
        :visible.sync="detailDialogVisible"
        title="请假申请详情"
        width="600px"
    >
      <el-descriptions :column="2" border v-if="currentLeave">
        <el-descriptions-item label="申请人">{{ currentLeave.staffName }}</el-descriptions-item>
        <el-descriptions-item label="工号">{{ currentLeave.staffId }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ currentLeave.departmentName }}</el-descriptions-item>
        <el-descriptions-item label="岗位">{{ currentLeave.jobName }}</el-descriptions-item>
        <el-descriptions-item label="请假类型">
          <el-tag :type="getLeaveTypeTag(currentLeave.leaveType)">
            {{ currentLeave.leaveType }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="请假天数">
          <el-tag type="info">{{ currentLeave.days }} 天</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始日期" :span="2">
          {{ formatDate(currentLeave.startDate) }}
        </el-descriptions-item>
        <el-descriptions-item label="结束日期" :span="2">
          {{ formatDate(currentLeave.endDate) }}
        </el-descriptions-item>
        <el-descriptions-item label="请假事由" :span="2">
          <div style="padding: 10px; background: #f5f7fa; border-radius: 4px;">
            {{ currentLeave.reason }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="申请状态">
          <el-tag :type="getStatusTag(currentLeave.status)">
            {{ currentLeave.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">
          {{ formatDateTime(currentLeave.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="currentLeave.approvalTime" label="审批时间">
          {{ formatDateTime(currentLeave.approvalTime) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="currentLeave.approverName" label="审批人">
          {{ currentLeave.approverName }}
        </el-descriptions-item>
      </el-descriptions>

      <div slot="footer" v-if="currentLeave && currentLeave.status === '待审批'">
        <el-button @click="detailDialogVisible = false">取消</el-button>
        <el-button type="success" @click="handleApprove(currentLeave)">通过</el-button>
        <el-button type="danger" @click="handleReject(currentLeave)">拒绝</el-button>
      </div>
    </el-dialog>

    <!-- 审批确认对话框 -->
    <el-dialog
        :visible.sync="approveDialogVisible"
        :title="`${approveAction === 'approve' ? '通过' : '拒绝'}请假申请`"
        width="500px"
    >
      <el-form v-if="currentLeave">
        <el-form-item label="申请人">
          <el-input :value="currentLeave.staffName" disabled />
        </el-form-item>
        <el-form-item label="请假类型">
          <el-input :value="currentLeave.leaveType" disabled />
        </el-form-item>
        <el-form-item label="请假时间">
          <el-input :value="`${formatDate(currentLeave.startDate)} 至 ${formatDate(currentLeave.endDate)}`" disabled />
        </el-form-item>
        <el-form-item label="审批意见" v-if="approveAction === 'reject'">
          <el-input
              v-model="rejectReason"
              type="textarea"
              :rows="3"
              placeholder="请输入拒绝理由（可选）"
          />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button
            type="primary"
            @click="handleApproveSubmit"
            :loading="submitting"
        >
          确认{{ approveAction === 'approve' ? '通过' : '拒绝' }}
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import API from '@/utils/api'

export default {
  name: 'LeaveApprove',
  data() {
    return {
      // 响应式数据
      loading: false,
      submitting: false,
      detailDialogVisible: false,
      approveDialogVisible: false,
      activeTab: 'pending',
      debugMode: true, // 设置为 false 可以关闭调试信息

      // 表格数据
      tableData: [],

      // 搜索参数
      searchParams: {
        staffName: '',
        leaveType: '',
        deptId: '',
        startDate: '',
        endDate: ''
      },

      // 日期范围
      dateRange: [],

      // 分页参数
      pagination: {
        current: 1,
        size: 10,
        total: 0
      },

      // 统计信息
      statistics: {
        pendingCount: 0,
        todayCount: 0,
        weekCount: 0,
        monthCount: 0
      },

      // 当前操作的请假记录
      currentLeave: null,

      // 审批动作
      approveAction: 'approve', // 'approve' or 'reject'

      // 拒绝理由
      rejectReason: '',

      // 选项数据
      departmentOptions: []
    }
  },
  mounted() {
    this.loadTableData()
    this.loadStatistics()
    this.loadDepartmentOptions()
  },
  methods: {
    // 加载表格数据
    async loadTableData() {
      this.loading = true
      try {
        const params = this.buildLeaveRequestParams()

        console.log('请假审批请求参数:', params)

        let response
        if (this.activeTab === 'pending') {
          response = await this.$http.get(API.url.hrLeave.pendingList, { params })
        } else {
          const listParams = { ...params }
          if (this.activeTab === 'approved') {
            listParams.status = '已批准'
          }
          response = await this.$http.get(API.url.hrLeave.list, { params: listParams })
        }

        console.log('请假数据完整响应:', response.data)

        if (response.data && response.data.code === 0) {
          const data = response.data

          // 处理分页数据结构
          let records = []
          let total = 0

          if (data.page && data.page.list) {
            records = data.page.list
            total = data.page.total || 0
            this.pagination.current = data.page.currPage || 1
            this.pagination.size = data.page.pageSize || 10
          } else if (data.data && data.data.page) {
            records = data.data.page.list || []
            total = data.data.page.total || 0
            this.pagination.current = data.data.page.currPage || 1
            this.pagination.size = data.data.page.pageSize || 10
          } else {
            records = data.data || data.list || []
            total = records.length
          }

          console.log('提取的记录数量:', records.length)
          console.log('第一条记录字段:', records.length > 0 ? Object.keys(records[0]) : '无数据')

          this.tableData = records
          this.pagination.total = total

          console.log('最终表格数据:', this.tableData)

        } else {
          this.$message.error(response.data?.msg || '加载请假数据失败')
        }
      } catch (error) {
        console.error('加载请假数据失败:', error)
        this.$message.error('加载请假数据失败: ' + (error.response?.data?.msg || error.message))
      } finally {
        this.loading = false
      }
    },

    // 构建请假审批请求参数
    buildLeaveRequestParams() {
      const params = {
        page: this.pagination.current,
        size: this.pagination.size
      }

      // 处理搜索参数
      if (this.searchParams.staffName) {
        params.staffName = this.searchParams.staffName
      }
      if (this.searchParams.leaveType) {
        params.leaveType = this.searchParams.leaveType
      }
      if (this.searchParams.deptId) {
        params.deptId = this.searchParams.deptId
      }
      if (this.searchParams.startDate) {
        params.startDate = this.searchParams.startDate
      }
      if (this.searchParams.endDate) {
        params.endDate = this.searchParams.endDate
      }

      // 根据选项卡设置状态
      if (this.activeTab === 'pending') {
        params.status = '待审批'
      } else if (this.activeTab === 'approved') {
        params.status = '已批准'
      }

      console.log('构建的请求参数:', params)
      return params
    },

    // 加载统计信息
    async loadStatistics() {
      try {
        const response = await this.$http.get(API.url.hrLeave.pendingList, {
          params: { page: 1, size: 1000 }
        })

        if (response.data && response.data.code === 0) {
          const data = response.data
          let pendingData = []

          if (data.page && data.page.list) {
            pendingData = data.page.list
          } else if (data.data && data.data.page) {
            pendingData = data.data.page.list || []
          } else {
            pendingData = data.data || data.list || []
          }

          this.statistics.pendingCount = pendingData.length

          // 计算时间范围内的数据
          const now = new Date()
          const today = now.toISOString().split('T')[0]
          const weekStart = new Date(now.setDate(now.getDate() - now.getDay()))
          const monthStart = new Date(now.getFullYear(), now.getMonth(), 1)

          this.statistics.todayCount = pendingData.filter(item =>
              item.createTime && item.createTime.startsWith(today)
          ).length

          this.statistics.weekCount = pendingData.filter(item =>
              item.createTime && new Date(item.createTime) >= weekStart
          ).length

          this.statistics.monthCount = pendingData.filter(item =>
              item.createTime && new Date(item.createTime) >= monthStart
          ).length

        }
      } catch (error) {
        console.error('加载统计信息失败:', error)
        this.resetStatistics()
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
        }
      } catch (error) {
        console.error('加载部门选项失败:', error)
        this.departmentOptions = []
      }
    },

    // 重置统计信息
    resetStatistics() {
      this.statistics = {
        pendingCount: 0,
        todayCount: 0,
        weekCount: 0,
        monthCount: 0
      }
    },

    // 选项卡切换
    handleTabChange() {
      this.pagination.current = 1
      this.loadTableData()
      if (this.activeTab === 'pending') {
        this.loadStatistics()
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
        staffName: '',
        leaveType: '',
        deptId: '',
        startDate: '',
        endDate: ''
      }
      this.dateRange = []
      this.pagination.current = 1
      this.loadTableData()
    },

    // 日期范围变化
    handleDateChange(value) {
      if (value && value.length === 2) {
        this.searchParams.startDate = value[0]
        this.searchParams.endDate = value[1]
      } else {
        this.searchParams.startDate = ''
        this.searchParams.endDate = ''
      }
      this.handleSearch()
    },

    // 查看详情
    handleDetail(row) {
      this.currentLeave = { ...row }
      this.detailDialogVisible = true
    },

    // 审批通过
    handleApprove(row) {
      this.currentLeave = { ...row }
      this.approveAction = 'approve'
      this.rejectReason = ''
      this.approveDialogVisible = true
    },

    // 审批拒绝
    handleReject(row) {
      this.currentLeave = { ...row }
      this.approveAction = 'reject'
      this.rejectReason = ''
      this.approveDialogVisible = true
    },

// 提交审批
    async handleApproveSubmit() {
      this.submitting = true
      try {
        // 确保 ID 是数字
        const leaveId = Number(this.currentLeave.id)

        if (isNaN(leaveId)) {
          this.$message.error('请假记录ID格式错误')
          return
        }

        let url, data

        if (this.approveAction === 'approve') {
          url = `/hr/leave/approve/${leaveId}`
          data = {}
        } else {
          url = `/hr/leave/reject/${leaveId}`
          // 🔥 关键修复：使用后端需要的参数名 'reason'
          data = {
            reason: this.rejectReason || '审批拒绝'
          }
        }

        console.log('审批请求URL:', url)
        console.log('审批请求数据:', data)

        const response = await this.$http.post(url, null, { params: data })


        if (response.data && response.data.code === 0) {
          this.$message.success(
              this.approveAction === 'approve' ? '审批通过成功' : '审批拒绝成功'
          )
          this.approveDialogVisible = false
          this.detailDialogVisible = false
          this.loadTableData()
          if (this.activeTab === 'pending') {
            this.loadStatistics()
          }
        } else {
          this.$message.error(response.data?.msg || '审批操作失败')
        }
      } catch (error) {
        console.error('审批操作失败:', error)
        this.$message.error('审批操作失败: ' + (error.response?.data?.msg || error.message))
      } finally {
        this.submitting = false
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

    // 获取请假类型标签样式
    getLeaveTypeTag(type) {
      const typeMap = {
        '病假': 'warning',
        '事假': 'danger',
        '年假': 'success',
        '调休': 'info',
        '婚假': '',
        '产假': 'primary'
      }
      return typeMap[type] || 'info'
    },

    // 获取状态标签样式
    getStatusTag(status) {
      const statusMap = {
        '待审批': 'warning',
        '已批准': 'success',
        '已拒绝': 'danger',
        '已撤销': 'info'
      }
      return statusMap[status] || 'info'
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
.leave-approve {
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

.statistics-area {
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
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
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

::v-deep .el-descriptions__label {
  width: 100px;
  text-align: right;
  font-weight: bold;
}
</style>