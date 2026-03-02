<template>
  <div class="basic-info">
    <el-card>
      <template slot="header">
        <div class="card-header">
          <span>基本信息</span>
          <el-button type="primary" size="small" @click="$emit('edit-profile')">
            编辑信息
          </el-button>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="姓名">
          <span class="info-value">{{ userInfo.sname || 'N/A' }}</span>
        </el-descriptions-item>

        <el-descriptions-item label="工号">
          <span class="info-value">{{ userInfo.sid || 'N/A' }}</span>
        </el-descriptions-item>

        <el-descriptions-item label="部门">
          <span class="info-value">{{ getDepartmentName(userInfo.deptId) || '未分配' }}</span>
        </el-descriptions-item>

        <el-descriptions-item label="岗位">
          <span class="info-value">{{ getJobTitle(userInfo.jobId) || '未分配' }}</span>
        </el-descriptions-item>

        <el-descriptions-item label="手机号码">
          <span class="info-value">{{ userInfo.phone || '未设置' }}</span>
        </el-descriptions-item>

        <el-descriptions-item label="邮箱">
          <span class="info-value">{{ userInfo.email || '未设置' }}</span>
        </el-descriptions-item>

        <el-descriptions-item label="入职日期">
          <span class="info-value">{{ formatDate(userInfo.hireDate) || 'N/A' }}</span>
        </el-descriptions-item>

        <el-descriptions-item label="员工状态">
          <el-tag type="success">在职</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script>
import API from '@/utils/api'

export default {
  name: 'BasicInfo',
  props: {
    userInfo: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      departmentList: [],
      jobList: [],
      loading: false
    }
  },
  methods: {
    // 获取部门名称
    getDepartmentName(deptId) {
      if (!deptId || !Array.isArray(this.departmentList)) return ''
      const dept = this.departmentList.find(item => String(item.id) === String(deptId))
      return dept ? dept.dname || dept.Dname : `部门(${deptId})`
    },

    // 获取岗位名称
    getJobTitle(jobId) {
      if (!jobId || !Array.isArray(this.jobList)) return '加载中...'
      const job = this.jobList.find(item => String(item.id) === String(jobId))
      return job ? job.jname : `岗位(${jobId})`
    },

    // 格式化日期
    formatDate(date) {
      if (!date) return ''
      if (typeof date === 'number') return new Date(date).toLocaleDateString()
      if (typeof date === 'string') return date.split(' ')[0]
      return date
    },

    // ✅ 统一处理分页数据结构
    extractList(data) {
      if (!data) return []
      if (Array.isArray(data)) return data
      if (data.list && Array.isArray(data.list)) return data.list
      return []
    },

    // 加载部门列表
    async loadDepartmentList() {
      try {
        const { data: response } = await this.$http.get(API.url.option.department)
        if (response.code === 0) {
          this.departmentList = this.extractList(response.data)
        } else {
          this.departmentList = []
        }
      } catch (error) {
        console.error('加载部门列表异常:', error)
        this.departmentList = []
      }
    },

    // 加载岗位列表
    async loadJobList() {
      try {
        const { data: response } = await this.$http.get(API.url.option.job)
        if (response.code === 0) {
          this.jobList = this.extractList(response.data)
        } else {
          this.jobList = []
        }
      } catch (error) {
        console.error('加载岗位列表异常:', error)
        this.jobList = []
      }
    },

    // 初始化数据
    async initData() {
      this.loading = true
      await Promise.all([
        this.loadDepartmentList(),
        this.loadJobList()
      ])
      this.loading = false
    }
  },

  mounted() {
    this.initData()
  },

  watch: {
    userInfo: {
      handler(newVal) {
        console.log('=== 用户信息更新 ===')
        console.log('完整用户信息:', newVal)
        console.log('部门ID:', newVal.deptId)
        console.log('岗位ID:', newVal.jobId)
      },
      deep: true,
      immediate: true
    }
  }
}
</script>

<style scoped>
.basic-info {
  margin: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.info-value {
  color: #333;
}
</style>
