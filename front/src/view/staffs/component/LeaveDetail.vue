<template>
  <div class="leave-detail">
    <el-descriptions v-if="leaveDetail" :column="2" border>
      <el-descriptions-item label="请假类型">
        <el-tag :type="getLeaveTypeTag(leaveDetail.leaveType)">
          {{ leaveDetail.leaveType }}
        </el-tag>
      </el-descriptions-item>

      <el-descriptions-item label="申请状态">
        <el-tag :type="getStatusTag(leaveDetail.status)">
          {{ leaveDetail.status }}
        </el-tag>
      </el-descriptions-item>

      <el-descriptions-item label="开始日期">
        {{ formatDate(leaveDetail.startDate) }}
      </el-descriptions-item>

      <el-descriptions-item label="结束日期">
        {{ formatDate(leaveDetail.endDate) }}
      </el-descriptions-item>

      <el-descriptions-item label="请假天数" :span="2">
        {{ leaveDetail.days }} 天
      </el-descriptions-item>

      <el-descriptions-item label="请假事由" :span="2">
        {{ leaveDetail.reason }}
      </el-descriptions-item>

      <el-descriptions-item label="申请时间" :span="2">
        {{ formatDateTime(leaveDetail.createTime) }}
      </el-descriptions-item>

      <el-descriptions-item v-if="leaveDetail.approvalTime" label="审批时间" :span="1">
        {{ formatDateTime(leaveDetail.approvalTime) }}
      </el-descriptions-item>

      <el-descriptions-item v-if="leaveDetail.hrName" label="审批人" :span="1">
        {{ leaveDetail.hrName }}
      </el-descriptions-item>
    </el-descriptions>

    <div v-else-if="loading" class="loading-container">
      <el-skeleton :rows="6" animated />
    </div>

    <div v-else class="error-container">
      <i class="el-icon-warning"></i>
      <p>加载详情失败</p>
    </div>

    <div class="dialog-footer" style="text-align: right; margin-top: 20px;">
      <el-button @click="$emit('close')">关闭</el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LeaveDetail',
  props: {
    leaveId: {
      type: [String, Number],
      required: true
    }
  },
  data() {
    return {
      leaveDetail: null,
      loading: false
    }
  },
  watch: {
    leaveId: {
      immediate: true,
      handler(newVal) {
        console.log('🔍 LeaveDetail - leaveId 变化:', newVal)
        if (newVal) {
          this.loadLeaveDetail()
        } else {
          this.leaveDetail = null
        }
      }
    }
  },
  methods: {
    async loadLeaveDetail() {
      try {
        console.log('🚀 开始加载请假详情，ID:', this.leaveId)
        this.loading = true
        this.leaveDetail = null

        const apiUrl = this.$api.buildUrl(this.$api.url.leave.detail, { id: this.leaveId })
        console.log('📡 API URL:', apiUrl)

        const response = await this.$http({
          url: apiUrl,
          method: 'get'
        })

        console.log('✅ API 响应:', response)

        const { data } = response

        if (data && data.code === 0) {
          this.leaveDetail = data.data
          console.log('📋 请假详情数据:', this.leaveDetail)
        } else {
          console.error('❌ API 返回错误:', data)
          this.$message.error(data?.msg || '加载详情失败')
        }
      } catch (error) {
        console.error('❌ 加载请假详情失败:', error)
        if (error.response) {
          console.error('📊 响应状态:', error.response.status)
          console.error('📋 响应数据:', error.response.data)
        }
        this.$message.error('加载详情失败，请检查网络连接')
      } finally {
        this.loading = false
      }
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

    // 获取状态标签样式
    getStatusTag(status) {
      const tagMap = {
        '待审批': 'warning',
        '已批准': 'success',
        '已拒绝': 'danger'
      }
      return tagMap[status] || ''
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
.leave-detail {
  .loading-container {
    padding: 20px;
    text-align: center;
  }

  .error-container {
    padding: 40px;
    text-align: center;
    color: #909399;

    i {
      font-size: 48px;
      margin-bottom: 16px;
    }

    p {
      margin: 0;
    }
  }
}
</style>