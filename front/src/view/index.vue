<template>
  <div class="main-container">
    <!-- 侧边导航栏 -->
    <nav class="sidebar" :class="{ 'collapsed': isCollapsed }">
      <div class="sidebar-header">
        <div class="logo">
          <img src="@/assets/img/logo.png" alt="系统logo">
          <span class="logo-text" v-show="!isCollapsed">员工绩效系统</span>
        </div>
        <i class="el-icon-s-fold toggle-btn" @click="toggleSidebar"></i>
      </div>

      <!-- 🔥 添加加载状态 -->
      <div v-if="loadingUserInfo" class="loading-user">
        <i class="el-icon-loading"></i>
        <span>加载中...</span>
      </div>

      <el-menu
          v-else
          :default-active="activeMenu"
          class="sidebar-menu"
          :collapse="isCollapsed"
          background-color="#001529"
          text-color="#fff"
          active-text-color="#ffd04b"
          router
      >
        <!-- 首页概览 - 根据角色显示不同的首页 -->
        <el-menu-item :index="homePath">
          <i class="el-icon-s-home"></i>
          <span slot="title">{{ homeTitle }}</span>
        </el-menu-item>

        <!-- 员工专属菜单 -->
        <template v-if="!isHr">
          <el-menu-item index="/index/attendance">
            <i class="el-icon-time"></i>
            <span slot="title">考勤打卡</span>
          </el-menu-item>

          <el-menu-item index="/index/salary">
            <i class="el-icon-money"></i>
            <span slot="title">薪资查询</span>
          </el-menu-item>

          <el-menu-item index="/index/leave">
            <i class="el-icon-document"></i>
            <span slot="title">请假申请</span>
          </el-menu-item>
        </template>

        <!-- 公告查看 - 所有用户都需要 -->
        <el-menu-item index="/index/notice">
          <i class="el-icon-chat-dot-round"></i>
          <span slot="title">公告查看</span>
        </el-menu-item>

        <!-- HR管理菜单 -->
        <template v-if="isHr">
          <el-submenu index="hr-management">
            <template slot="title">
              <i class="el-icon-s-management"></i>
              <span>HR管理</span>
            </template>
            <el-menu-item index="/index/hr/employee">
              <i class="el-icon-user"></i>
              <span slot="title">员工管理</span>
            </el-menu-item>
            <el-menu-item index="/index/hr/department">
              <i class="el-icon-office-building"></i>
              <span slot="title">部门管理</span>
            </el-menu-item>
            <el-menu-item index="/index/hr/salary-manage">
              <i class="el-icon-wallet"></i>
              <span slot="title">薪资管理</span>
            </el-menu-item>
            <el-menu-item index="/index/hr/leave-manage">
              <i class="el-icon-tickets"></i>
              <span slot="title">请假审批</span>
            </el-menu-item>
            <el-menu-item index="/index/hr/attendance-manage">
              <i class="el-icon-data-analysis"></i>
              <span slot="title">考勤管理</span>
            </el-menu-item>
            <el-menu-item index="/index/hr/notice-manage">
              <i class="el-icon-bell"></i>
              <span slot="title">公告管理</span>
            </el-menu-item>
          </el-submenu>
        </template>

        <!-- 通用菜单 -->
        <el-menu-item index="/index/center">
          <i class="el-icon-user-solid"></i>
          <span slot="title">个人中心</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer" v-show="!isCollapsed && !loadingUserInfo">
        <div class="user-info">
          <el-avatar :size="32" :src="userInfo?.avatar || require('@/assets/img/avatar.png')"></el-avatar>
          <div class="user-details">
            <div class="user-name">{{ userInfo?.sname || '用户' }}</div>
            <div class="user-role">{{ roleText }}</div>
          </div>
        </div>
        <el-button type="text" @click="logout" class="logout-btn">
          <i class="el-icon-switch-button"></i>
          退出登录
        </el-button>
      </div>
    </nav>

    <!-- 主内容区域 -->
    <div class="main-content" :class="{ 'collapsed': isCollapsed }">
      <div class="content-header">
        <div class="breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: homePath }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentPageTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-actions">
          <el-button type="text" @click="toggleTheme">
            <i class="el-icon-moon" v-if="!isDarkMode"></i>
            <i class="el-icon-sunny" v-else></i>
            {{ isDarkMode ? '浅色模式' : '深色模式' }}
          </el-button>
        </div>
      </div>

      <div class="content-body">
        <!-- 🔥 添加全局加载状态 -->
        <div v-if="loadingUserInfo" class="global-loading">
          <el-skeleton animated>
            <template slot="template">
              <el-skeleton-item variant="h3" style="width: 50%" />
              <el-skeleton-item variant="text" />
              <el-skeleton-item variant="text" />
            </template>
          </el-skeleton>
        </div>
        <router-view v-else></router-view>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Index',
  data() {
    return {
      isCollapsed: false,
      isDarkMode: false,
      userInfo: null,
      currentPageTitle: '首页',
      // 🔥 新增：加载状态
      loadingUserInfo: true,
      pageTitles: {
        '/index/home': '首页概览',
        '/index/hr-home': 'HR工作台',
        '/index/attendance': '考勤打卡',
        '/index/salary': '薪资查询',
        '/index/leave': '请假申请',
        '/index/notice': '公告查看',
        '/index/center': '个人中心',
        '/index/updatePassword': '修改密码',
        '/index/hr/employee': '员工管理',
        '/index/hr/department': '部门管理',
        '/index/hr/salary-manage': '薪资管理',
        '/index/hr/leave-manage': '请假审批',
        '/index/hr/attendance-manage': '考勤管理',
        '/index/hr/notice-manage': '公告管理'
      }
    }
  },
  computed: {
    activeMenu() {
      return this.$route.path
    },
    isHr() {
      // 🔥 修复：确保userInfo加载完成后再判断
      return this.userInfo?.role === 'hr'
    },
    roleText() {
      if (!this.userInfo) return '加载中...'
      return this.userInfo.role === 'hr' ? 'HR管理员' : '员工'
    },
    homePath() {
      // 🔥 修复：如果还在加载，默认显示员工首页
      if (!this.userInfo) return '/index/home'
      return this.isHr ? '/index/hr-home' : '/index/home'
    },
    homeTitle() {
      // 🔥 修复：如果还在加载，默认显示员工首页
      if (!this.userInfo) return '首页概览'
      return this.isHr ? 'HR工作台' : '首页概览'
    }
  },
  watch: {
    '$route.path': {
      immediate: true,
      handler(path) {
        this.currentPageTitle = this.pageTitles[path] || '页面'
      }
    },
    // 🔥 新增：监听用户信息变化，加载完成后跳转到正确的首页
    'userInfo': {
      handler(newUserInfo) {
        if (newUserInfo) {
          this.redirectToCorrectHome()
        }
      },
      immediate: true
    }
  },
  mounted() {
    this.loadUserInfo()
    const savedTheme = localStorage.getItem('theme')
    if (savedTheme === 'dark') {
      this.isDarkMode = true
    }
  },
  methods: {
    async loadUserInfo() {
      try {
        const token = this.$storage?.get('Token')
        if (!token) {
          this.$router.push({ name: 'login' })
          return
        }

        // 🔥 尝试从本地存储先获取用户信息
        const cachedUserInfo = localStorage.getItem('userInfo')
        if (cachedUserInfo) {
          this.userInfo = JSON.parse(cachedUserInfo)
        }

        const { data } = await this.$http({
          url: '/users/session',
          method: "get"
        })

        if (data && data.code === 0) {
          this.userInfo = data.data
          localStorage.setItem('userInfo', JSON.stringify(data.data))
          console.log('用户信息加载成功:', this.userInfo)
        } else {
          console.error('获取用户信息失败:', data?.msg)
          this.logout()
        }
      } catch (error) {
        console.error('加载用户信息失败:', error)
        // 🔥 如果网络错误但有缓存，使用缓存数据
        if (!this.userInfo) {
          this.logout()
        }
      } finally {
        this.loadingUserInfo = false
      }
    },

    // 🔥 新增：跳转到正确的首页
    redirectToCorrectHome() {
      if (!this.userInfo) return

      const currentPath = this.$route.path
      const isAtDefaultHome = currentPath === '/index/home' || currentPath === '/index'

      if (this.isHr) {
        // 如果是HR，并且当前在员工首页，跳转到HR首页
        if (isAtDefaultHome) {
          this.$router.replace('/index/hr-home').catch(() => {})
        }
      } else {
        // 如果是员工，并且当前在HR首页，跳转到员工首页
        if (currentPath === '/index/hr-home') {
          this.$router.replace('/index/home').catch(() => {})
        }
      }
    },

    toggleSidebar() {
      this.isCollapsed = !this.isCollapsed
    },

    toggleTheme() {
      this.isDarkMode = !this.isDarkMode
      localStorage.setItem('theme', this.isDarkMode ? 'dark' : 'light')
      this.applyTheme()
    },

    applyTheme() {
      if (this.isDarkMode) {
        document.body.classList.add('dark-theme')
      } else {
        document.body.classList.remove('dark-theme')
      }
    },

    logout() {
      this.$confirm('确定要退出登录吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$storage.remove('Token')
        this.$storage.remove('role')
        this.$storage.remove('sessionTable')
        localStorage.removeItem('userInfo')

        this.$router.push('/login').then(() => {
          window.location.reload()
        }).catch(err => {
          window.location.href = '/#/login'
        })

      }).catch(() => {
        // 用户取消退出
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.loading-user {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #fff;
}

.loading-user i {
  margin-right: 8px;
  font-size: 16px;
}

.global-loading {
  padding: 20px;
}
.main-container {
  display: flex;
  height: 100vh;
  background: #f0f2f5;
}

.sidebar {
  width: 256px;
  background: #001529;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;

  &.collapsed {
    width: 64px;
  }
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-bottom: 1px solid #f0f0f0;
  background: #002140;

  .logo {
    display: flex;
    align-items: center;

    img {
      width: 32px;
      height: 32px;
      border-radius: 6px;
    }

    .logo-text {
      margin-left: 12px;
      color: #fff;
      font-size: 18px;
      font-weight: bold;
    }
  }

  .toggle-btn {
    color: #fff;
    font-size: 18px;
    cursor: pointer;

    &:hover {
      color: #1890ff;
    }
  }
}

.sidebar-menu {
  flex: 1;
  border: none;

  &:not(.el-menu--collapse) {
    width: 256px;
  }
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid #f0f0f0;
  background: #002140;

  .user-info {
    display: flex;
    align-items: center;
    margin-bottom: 12px;

    .user-details {
      margin-left: 12px;

      .user-name {
        color: #fff;
        font-size: 14px;
        font-weight: 500;
      }

      .user-role {
        color: #8c8c8c;
        font-size: 12px;
      }
    }
  }

  .logout-btn {
    width: 100%;
    color: #fff;

    &:hover {
      color: #1890ff;
    }
  }
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-left: 0px;
  transition: margin-left 0.3s;

  &.collapsed {
    margin-left: 64px;
  }
}

.content-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);

  .breadcrumb {
    flex: 1;
  }
}

.content-body {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: #f0f2f5;
}

// 响应式设计
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    z-index: 1000;
    height: 100%;

    &:not(.collapsed) {
      transform: translateX(-100%);
    }
  }

  .main-content {
    margin-left: 0 !important;
  }
}

// 深色主题样式
.dark-theme {
  .main-container {
    background: #141414;
  }

  .content-header {
    background: #1f1f1f;
    color: #fff;
  }

  .content-body {
    background: #141414;
  }

  .el-breadcrumb__inner {
    color: #fff !important;
  }
}
</style>