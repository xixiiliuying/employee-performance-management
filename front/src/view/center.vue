<template>
  <div class="center-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>个人中心</h1>
      <p>管理您的个人信息和账户设置</p>
    </div>

    <div class="center-content">
      <!-- 左侧个人信息卡片 -->
      <div class="left-sidebar">
        <el-card class="user-card">
          <div class="user-avatar-section">
            <el-avatar
                :size="80"
                :src="userInfo.avatar || defaultAvatar"
                @click="showAvatarDialog = true"
                class="user-avatar"
            >
              {{ userInfo.name ? userInfo.name.charAt(0) : 'U' }}
            </el-avatar>
            <div class="user-basic-info">
              <h3>{{ userInfo.sname || '未知用户' }}</h3>
              <p class="user-department">{{ getDepartmentName(userInfo.deptId) || '未分配部门' }}</p>
              <p class="user-job">{{ getJobTitle(userInfo.jobId) || '未分配岗位' }}</p>
            </div>
          </div>

          <el-divider></el-divider>

          <div class="user-stats">
            <div class="stat-item">
              <span class="stat-label">工号</span>
              <span class="stat-value">{{ userInfo.sid || 'N/A' }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">入职日期</span>
              <span class="stat-value">{{ formatDate(userInfo.hireDate) || 'N/A' }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">手机号码</span>
              <span class="stat-value">{{ userInfo.phone || '未设置' }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">邮箱</span>
              <span class="stat-value">{{ userInfo.email || '未设置' }}</span>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 右侧内容区域 -->
      <div class="right-content">
        <!-- 选项卡 - 只保留基本信息和安全设置 -->
        <el-tabs v-model="activeTab" class="center-tabs">
          <!-- 基本信息标签页 -->
          <el-tab-pane label="基本信息" name="basic">
            <BasicInfo
                :user-info="userInfo"
                :department-list="departmentList"
                :job-list="jobList"
                @edit-profile="editProfile"
            />
          </el-tab-pane>

          <!-- 安全设置标签页 -->
          <el-tab-pane label="安全设置" name="security">
            <SecuritySettings />
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 头像上传对话框 -->
    <el-dialog
        title="更换头像"
        :visible.sync="showAvatarDialog"
        width="400px"
        @close="showAvatarDialog = false"
    >
      <AvatarUpload
          :current-avatar="userInfo.avatar"
          @upload-success="handleAvatarSuccess"
      />
    </el-dialog>

    <!-- 编辑资料对话框 -->
    <el-dialog
        title="编辑资料"
        :visible.sync="showEditDialog"
        width="600px"
        @close="showEditDialog = false"
    >
      <ProfileEditForm
          :user-info="userInfo"
          :department-list="departmentList"
          :job-list="jobList"
          @update-success="handleProfileUpdate"
          @cancel="showEditDialog = false"
      />
    </el-dialog>
  </div>
</template>

<script>
import API from '@/utils/api'
import BasicInfo from './staffs/component/BasicInfo.vue'
import SecuritySettings from './staffs/component/SecuritySettings.vue'
import AvatarUpload from './staffs/component/AvatarUpload.vue'
import ProfileEditForm from './staffs/component/ProfileEditForm.vue'

export default {
  name: 'Center',
  components: {
    BasicInfo,
    SecuritySettings,
    AvatarUpload,
    ProfileEditForm
  },
  data() {
    return {
      userInfo: {},
      departmentList: [],
      jobList: [],
      activeTab: 'basic',
      showAvatarDialog: false,
      showEditDialog: false,
      defaultAvatar: '/default-avatar.png'
    }
  },
  methods: {
    // 获取用户信息
    async fetchUserInfo() {
      try {
        const { data } = await this.$http.get(API.url.user.session)
        if (data && data.code === 0) {
          this.userInfo = data.data
        } else {
          this.$message.error(data.msg || '获取用户信息失败')
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
        this.$message.error('获取用户信息失败')
      }
    },

    // 处理头像上传成功
    handleAvatarSuccess(avatarUrl) {
      this.userInfo.avatar = avatarUrl
      this.showAvatarDialog = false
      this.$message.success('头像更新成功')
    },

    // 处理资料更新成功
    handleProfileUpdate() {
      this.showEditDialog = false
      this.fetchUserInfo()
      this.$message.success('资料更新成功')
    },

    // 编辑资料
    editProfile() {
      this.showEditDialog = true
    },

    // 修改密码
    changePassword() {
      this.activeTab = 'security'
    },

    // 通知设置
    notificationSettings() {
      this.$message.info('通知设置功能开发中')
    },

    // 格式化日期
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleDateString('zh-CN')
    },

    // 通用提取 list
    extractList(data) {
      if (!data) return []
      if (Array.isArray(data)) return data
      if (data.list && Array.isArray(data.list)) return data.list
      return []
    },

    // 加载部门
    async loadDepartmentList() {
      try {
        const { data: res } = await this.$http.get(API.url.option.department)
        if (res.code === 0) {
          this.departmentList = this.extractList(res.data)
        }
      } catch (err) {
        console.error('加载部门失败', err)
      }
    },

    // 加载岗位
    async loadJobList() {
      try {
        const { data: res } = await this.$http.get(API.url.option.job)
        if (res.code === 0) {
          this.jobList = this.extractList(res.data)
        }
      } catch (err) {
        console.error('加载岗位失败', err)
      }
    },

    // 获取部门名称
    getDepartmentName(deptId) {
      if (!deptId || !Array.isArray(this.departmentList)) return ''
      const dept = this.departmentList.find(item => String(item.id) === String(deptId))
      return dept ? dept.dname || dept.Dname : `部门(${deptId})`
    },

    // 获取岗位名称
    getJobTitle(jobId) {
      if (!jobId || !Array.isArray(this.jobList)) return ''
      const job = this.jobList.find(item => String(item.id) === String(jobId))
      return job ? job.jname : `岗位(${jobId})`
    }
  },
  mounted() {
    this.fetchUserInfo()
    this.loadDepartmentList()
    this.loadJobList()
  }
}
</script>

<style scoped>
.center-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
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
  margin: 5px 0 0 0;
  color: #909399;
  font-size: 14px;
}

.center-content {
  display: flex;
  gap: 20px;
}

.left-sidebar {
  width: 300px;
  flex-shrink: 0;
}

.right-content {
  flex: 1;
  min-width: 0;
}

.user-card {
  margin-bottom: 20px;
}

.user-avatar-section {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-avatar {
  cursor: pointer;
  border: 3px solid #e6f7ff;
  transition: all 0.3s;
}

.user-avatar:hover {
  border-color: #1890ff;
  transform: scale(1.05);
}

.user-basic-info h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #303133;
}

.user-department {
  margin: 0 0 5px 0;
  color: #606266;
  font-size: 14px;
}

.user-job {
  margin: 0 0 10px 0;
  color: #909399;
  font-size: 13px;
}

.user-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.stat-value {
  color: #303133;
  font-weight: 500;
}

.quick-actions-card {
  margin-bottom: 20px;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.action-btn {
  width: 100%;
  justify-content: flex-start;
}

.center-tabs {
  background: white;
  border-radius: 8px;
  padding: 20px;
}

::v-deep .el-tabs__header {
  margin-bottom: 20px;
}

::v-deep .el-tabs__nav-wrap::after {
  height: 1px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .center-content {
    flex-direction: column;
  }

  .left-sidebar {
    width: 100%;
  }

  .user-avatar-section {
    flex-direction: column;
    text-align: center;
  }
}
</style>