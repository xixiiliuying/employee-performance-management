<template>
  <div class="leave-apply-container">
    <div class="page-header">
      <h2 class="page-title">请假申请</h2>
      <p class="page-desc">提交您的请假申请，等待HR审批</p>
    </div>

    <div class="content-card">
      <!-- 申请表单 -->
      <el-form
          ref="leaveForm"
          :model="leaveForm"
          :rules="leaveRules"
          label-width="120px"
          class="leave-form"
      >
        <el-form-item label="请假类型" prop="leaveType">
          <el-select
              v-model="leaveForm.leaveType"
              placeholder="请选择请假类型"
              style="width: 100%"
          >
            <el-option
                v-for="type in leaveTypes"
                :key="type.value"
                :label="type.label"
                :value="type.value"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
              v-model="leaveForm.startDate"
              type="date"
              placeholder="选择开始日期"
              style="width: 100%"
              :picker-options="startDatePickerOptions"
              value-format="yyyy-MM-dd"
              @change="handleStartDateChange"
          ></el-date-picker>
          <div class="form-tip">只能选择今天及以后的日期</div>
        </el-form-item>

        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
              v-model="leaveForm.endDate"
              type="date"
              placeholder="选择结束日期"
              style="width: 100%"
              :picker-options="endDatePickerOptions"
              value-format="yyyy-MM-dd"
          ></el-date-picker>
          <div class="form-tip">不能早于开始日期</div>
        </el-form-item>

        <el-form-item label="请假天数" prop="days">
          <el-input
              v-model="leaveForm.days"
              placeholder="系统自动计算"
              readonly
          >
            <template slot="append">天</template>
          </el-input>
        </el-form-item>

        <el-form-item label="请假事由" prop="reason">
          <el-input
              v-model="leaveForm.reason"
              type="textarea"
              :rows="4"
              placeholder="请详细说明请假原因"
              maxlength="500"
              show-word-limit
          ></el-input>
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              :loading="submitting"
              @click="submitLeave"
          >
            提交申请
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 我的申请记录 -->
    <div class="content-card">
      <div class="section-header">
        <h3 class="section-title">我的申请记录</h3>
        <el-button
            type="text"
            icon="el-icon-refresh"
            @click="loadMyApplications"
        >
          刷新
        </el-button>
      </div>

      <el-table
          v-loading="loading"
          :data="leaveApplications"
          style="width: 100%"
          empty-text="暂无请假记录"
      >
        <el-table-column
            prop="leaveType"
            label="请假类型"
            width="120"
        >
          <template slot-scope="scope">
            <el-tag :type="getLeaveTypeTag(scope.row.leaveType)">
              {{ getLeaveTypeText(scope.row.leaveType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
            prop="startDate"
            label="开始日期"
            width="140"
        >
          <template slot-scope="scope">
            {{ formatDate(scope.row.startDate) }}
          </template>
        </el-table-column>

        <el-table-column
            prop="endDate"
            label="结束日期"
            width="140"
        >
          <template slot-scope="scope">
            {{ formatDate(scope.row.endDate) }}
          </template>
        </el-table-column>

        <el-table-column
            prop="days"
            label="天数"
            width="80"
            align="center"
        >
          <template slot-scope="scope">
            {{ scope.row.days }}
          </template>
        </el-table-column>

        <el-table-column
            prop="status"
            label="状态"
            width="100"
            align="center"
        >
          <template slot-scope="scope">
            <el-tag :type="getStatusTag(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
            prop="reason"
            label="事由"
            min-width="200"
            show-overflow-tooltip
        ></el-table-column>

        <el-table-column
            prop="createTime"
            label="申请时间"
            width="160"
        >
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column
            label="操作"
            width="120"
            align="center"
        >
          <template slot-scope="scope">
            <el-button
                v-if="scope.row.status === '待审批'"
                type="text"
                size="small"
                @click="cancelApplication(scope.row.id)"
            >
              撤销
            </el-button>
            <el-button
                type="text"
                size="small"
                @click="viewDetail(scope.row.id)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
            v-if="total > 0"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="queryParams.page"
            :page-sizes="[10, 20, 30, 50]"
            :page-size="queryParams.size"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
        ></el-pagination>
      </div>
    </div>

    <!-- 请假详情对话框 -->
    <el-dialog
        title="请假详情"
        :visible.sync="detailDialogVisible"
        width="600px"
    >
      <leave-detail
          v-if="detailDialogVisible"
          :leave-id="currentLeaveId"
          @close="detailDialogVisible = false"
      ></leave-detail>
    </el-dialog>
  </div>
</template>

<script>
import LeaveDetail from './component/LeaveDetail.vue'

export default {
  name: 'LeaveApply',
  components: {
    LeaveDetail
  },
  data() {
    // 结束日期验证规则
    const validateEndDate = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请选择结束日期'))
      } else if (this.leaveForm.startDate && new Date(value) < new Date(this.leaveForm.startDate)) {
        callback(new Error('结束日期不能早于开始日期'))
      } else {
        callback()
      }
    }

    return {
      // 请假表单数据 - 对应数据库字段
      leaveForm: {
        leaveType: '',
        startDate: '',
        endDate: '',
        days: 0,
        reason: ''
      },
      // 表单验证规则
      leaveRules: {
        leaveType: [
          { required: true, message: '请选择请假类型', trigger: 'change' }
        ],
        startDate: [
          { required: true, message: '请选择开始日期', trigger: 'change' }
        ],
        endDate: [
          { required: true, validator: validateEndDate, trigger: 'change' }
        ],
        reason: [
          { required: true, message: '请输入请假事由', trigger: 'blur' },
          { min: 5, max: 500, message: '长度在 5 到 500 个字符', trigger: 'blur' }
        ]
      },
      // 请假类型选项 - 对应数据库中的请假类型
      leaveTypes: [
        { value: '病假', label: '病假' },
        { value: '事假', label: '事假' },
        { value: '年假', label: '年假' },
        { value: '婚假', label: '婚假' },
        { value: '产假', label: '产假' },
        { value: '陪产假', label: '陪产假' },
        { value: '其他', label: '其他' }
      ],
      // 日期选择器选项
      startDatePickerOptions: {
        disabledDate: this.disabledStartDate
      },
      endDatePickerOptions: {
        disabledDate: this.disabledEndDate
      },
      // 提交状态
      submitting: false,
      // 申请记录数据
      leaveApplications: [],
      loading: false,
      // 查询参数
      queryParams: {
        page: 1,
        size: 10
      },
      total: 0,
      // 详情对话框
      detailDialogVisible: false,
      currentLeaveId: null
    }
  },
  computed: {
    // 计算开始日期的最小可选日期（今天）
    minStartDate() {
      const today = new Date()
      today.setHours(0, 0, 0, 0)
      return today
    }
  },
  watch: {
    // 监听开始日期和结束日期变化，自动计算请假天数
    'leaveForm.startDate': function(newVal) {
      this.calculateDays()
      // 开始日期变化时，更新结束日期的禁用规则
      this.updateEndDatePickerOptions()
    },
    'leaveForm.endDate': function(newVal) {
      this.calculateDays()
    }
  },
  mounted() {
    this.loadMyApplications()
    // 初始化日期选择器选项
    this.updateEndDatePickerOptions()
  },
  methods: {
    // 计算请假天数 - 对应数据库days字段的计算逻辑
    calculateDays() {
      if (this.leaveForm.startDate && this.leaveForm.endDate) {
        const start = new Date(this.leaveForm.startDate)
        const end = new Date(this.leaveForm.endDate)

        if (end >= start) {
          // 计算相差的天数 + 1（包含开始和结束日期）
          const diffTime = Math.abs(end - start)
          const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1
          this.leaveForm.days = diffDays
        } else {
          this.leaveForm.days = 0
        }
      } else {
        this.leaveForm.days = 0
      }
    },

    // 禁用开始日期 - 只能选择今天及以后的日期
    disabledStartDate(time) {
      // 获取今天的日期（去掉时间部分）
      const today = new Date()
      today.setHours(0, 0, 0, 0)

      // 禁用今天之前的日期（完全禁用，不能点击）
      return time.getTime() < today.getTime()
    },

    // 禁用结束日期 - 只能选择开始日期之后的日期
    disabledEndDate(time) {
      // 获取今天的日期（去掉时间部分）
      const today = new Date()
      today.setHours(0, 0, 0, 0)

      if (!this.leaveForm.startDate) {
        // 如果没有选择开始日期，只能选择今天及以后的日期
        return time.getTime() < today.getTime()
      }

      // 获取开始日期（去掉时间部分）
      const startDate = new Date(this.leaveForm.startDate)
      startDate.setHours(0, 0, 0, 0)

      // 禁用开始日期之前的日期（完全禁用，不能点击）
      return time.getTime() < startDate.getTime()
    },

    // 更新结束日期选择器选项
    updateEndDatePickerOptions() {
      this.endDatePickerOptions = {
        disabledDate: this.disabledEndDate
      }
    },

    // 开始日期变化处理
    handleStartDateChange() {
      // 如果开始日期变化且结束日期早于新的开始日期，清空结束日期
      if (this.leaveForm.endDate && new Date(this.leaveForm.endDate) < new Date(this.leaveForm.startDate)) {
        this.leaveForm.endDate = ''
        this.leaveForm.days = 0
      }
      // 强制更新结束日期的禁用状态
      this.$nextTick(() => {
        this.updateEndDatePickerOptions()
      })
    },

    // 提交请假申请
    async submitLeave() {
      try {
        // 表单验证
        const valid = await this.$refs.leaveForm.validate()
        if (!valid) return

        // 额外验证：确保开始日期不早于今天
        const today = new Date()
        today.setHours(0, 0, 0, 0)

        const startDate = new Date(this.leaveForm.startDate)
        if (startDate < today) {
          this.$message.error('请假开始日期不能早于今天')
          return
        }

        // 额外验证：确保开始日期不晚于结束日期
        if (startDate > new Date(this.leaveForm.endDate)) {
          this.$message.error('开始日期不能晚于结束日期')
          return
        }

        this.submitting = true

        // 构建请求数据 - 对应数据库字段
        const requestData = {
          leaveType: this.leaveForm.leaveType,
          startDate: this.leaveForm.startDate,
          endDate: this.leaveForm.endDate,
          days: this.leaveForm.days,
          reason: this.leaveForm.reason
        }

        // 调用API
        const { data } = await this.$http({
          url: this.$api.url.leave.apply,
          method: 'post',
          data: requestData
        })

        if (data && data.code === 0) {
          this.$message.success('请假申请提交成功')
          this.resetForm()
          this.loadMyApplications() // 刷新申请记录
        } else {
          this.$message.error(data.msg || '提交失败，请重试')
        }
      } catch (error) {
        console.error('提交请假申请失败:', error)
        this.$message.error('提交失败，请检查网络连接')
      } finally {
        this.submitting = false
      }
    },

    // 重置表单
    resetForm() {
      this.$refs.leaveForm.resetFields()
      this.leaveForm.days = 0
      // 重置后更新结束日期选择器
      this.$nextTick(() => {
        this.updateEndDatePickerOptions()
      })
    },

    // 加载我的申请记录
    async loadMyApplications() {
      try {
        this.loading = true

        const { data } = await this.$http({
          url: this.$api.url.leave.myApplications,
          method: 'get',
          params: this.queryParams
        })

        console.log('请假记录API响应:', data)

        if (data && data.code === 0) {
          // 适配不同的后端返回数据结构
          if (data.page) {
            // 情况1: 返回 page 对象，包含 list 和 totalCount
            this.leaveApplications = data.page.list || []
            this.total = data.page.totalCount || 0
          } else if (data.data && data.data.records) {
            // 情况2: 返回 data 对象，包含 records 和 total
            this.leaveApplications = data.data.records || []
            this.total = data.data.total || 0
          } else if (data.data && Array.isArray(data.data)) {
            // 情况3: 直接返回数组
            this.leaveApplications = data.data
            this.total = data.data.length
          } else if (Array.isArray(data.list)) {
            // 情况4: 直接返回 list 数组
            this.leaveApplications = data.list
            this.total = data.total || data.list.length
          } else {
            // 情况5: 其他未知结构，使用空数组
            console.warn('未知的数据结构:', data)
            this.leaveApplications = []
            this.total = 0
          }
        } else {
          this.$message.error(data?.msg || '加载申请记录失败')
          this.leaveApplications = []
          this.total = 0
        }
      } catch (error) {
        console.error('加载请假申请记录失败:', error)
        this.$message.error('加载申请记录失败')
        this.leaveApplications = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },

    // 撤销申请
    async cancelApplication(id) {
      try {
        await this.$confirm('确定要撤销这条请假申请吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        const { data } = await this.$http({
          url: this.$api.buildUrl(this.$api.url.leave.cancel, { id }),
          method: 'post'
        })

        if (data && data.code === 0) {
          this.$message.success('撤销申请成功')
          this.loadMyApplications() // 刷新列表
        } else {
          this.$message.error(data.msg || '撤销失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('撤销申请失败:', error)
          this.$message.error('撤销失败')
        }
      }
    },

    // 查看详情
    viewDetail(id) {
      this.currentLeaveId = id
      this.detailDialogVisible = true
    },

    // 分页大小改变
    handleSizeChange(size) {
      this.queryParams.size = size
      this.queryParams.page = 1
      this.loadMyApplications()
    },

    // 当前页改变
    handleCurrentChange(page) {
      this.queryParams.page = page
      this.loadMyApplications()
    },

    // 获取请假类型标签样式
    getLeaveTypeTag(type) {
      const tagMap = {
        '病假': 'danger',
        '年假': 'success',
        '事假': 'warning',
        '婚假': 'primary',
        '产假': 'info',
        '陪产假': 'info',
        '其他': ''
      }
      return tagMap[type] || ''
    },

    // 获取请假类型文本
    getLeaveTypeText(type) {
      return type // 直接返回数据库中的值
    },

    // 获取状态标签样式 - 对应数据库status枚举
    getStatusTag(status) {
      const tagMap = {
        '待审批': 'warning',
        '已批准': 'success',
        '已拒绝': 'danger'
      }
      return tagMap[status] || ''
    },

    // 获取状态文本
    getStatusText(status) {
      return status // 直接返回数据库中的值
    },

    // 格式化日期 - 使用原生JavaScript
    formatDate(date) {
      if (!date) return ''
      try {
        const dateObj = new Date(date)
        if (isNaN(dateObj.getTime())) return ''

        const year = dateObj.getFullYear()
        const month = String(dateObj.getMonth() + 1).padStart(2, '0')
        const day = String(dateObj.getDate()).padStart(2, '0')

        return `${year}-${month}-${day}`
      } catch (error) {
        console.error('日期格式化错误:', error)
        return ''
      }
    },

    // 格式化日期时间 - 使用原生JavaScript
    formatDateTime(dateTime) {
      if (!dateTime) return ''
      try {
        const dateObj = new Date(dateTime)
        if (isNaN(dateObj.getTime())) return ''

        const year = dateObj.getFullYear()
        const month = String(dateObj.getMonth() + 1).padStart(2, '0')
        const day = String(dateObj.getDate()).padStart(2, '0')
        const hours = String(dateObj.getHours()).padStart(2, '0')
        const minutes = String(dateObj.getMinutes()).padStart(2, '0')

        return `${year}-${month}-${day} ${hours}:${minutes}`
      } catch (error) {
        console.error('日期时间格式化错误:', error)
        return ''
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.leave-apply-container {
  padding: 0;
}

.page-header {
  margin-bottom: 24px;

  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 8px 0;
  }

  .page-desc {
    font-size: 14px;
    color: #606266;
    margin: 0;
  }
}

.content-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.leave-form {
  max-width: 600px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  .section-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin: 0;
  }
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

// 表单提示样式
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}

// 响应式设计
@media (max-width: 768px) {
  .content-card {
    padding: 16px;
    margin-bottom: 16px;
  }

  .leave-form {
    ::v-deep .el-form-item__label {
      width: 100px !important;
    }
  }
}
</style>