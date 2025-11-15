<template>
  <div class="notice-page">
    <div class="page-header">
      <h1>公告查看</h1>
      <p>查看公司最新公告信息</p>
    </div>

    <el-card class="main-card">
      <!-- 搜索和筛选 -->
      <div class="filter-bar">
        <el-input
            v-model="searchKeyword"
            placeholder="搜索公告标题或内容"
            prefix-icon="el-icon-search"
            style="width: 300px"
            @keyup.enter.native="loadNoticeList"
        ></el-input>
        <el-button type="primary" @click="loadNoticeList">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>

        <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
            @change="handleDateChange"
        >
        </el-date-picker>
      </div>

      <!-- 公告列表 -->
      <div class="notice-list" v-loading="loading">
        <div v-if="noticeList.length === 0" class="empty-state">
          <i class="el-icon-document"></i>
          <p>{{ loading ? '加载中...' : '暂无公告' }}</p>
        </div>

        <div
            v-for="notice in noticeList"
            :key="notice.id"
            class="notice-item"
            @click="handleViewNotice(notice)"
        >
          <div class="notice-header">
            <div class="notice-title">
              <span class="title-text">{{ notice.title }}</span>
              <el-tag v-if="isNewNotice(notice.publishDate)" type="success" size="mini">新</el-tag>
            </div>
            <div class="notice-time">{{ formatDate(notice.publishDate) }}</div>
          </div>

          <!-- 图片预览 -->
          <div v-if="notice.image" class="notice-image">
            <el-image
                :src="getImageUrl(notice.image)"
                :preview-src-list="[getImageUrl(notice.image)]"
                fit="cover"
                style="width: 100px; height: 80px; border-radius: 4px;"
            ></el-image>
          </div>

          <div class="notice-content">
            {{ getContentPreview(notice.content) }}
          </div>

          <div class="notice-footer">
            <div class="notice-meta">
              <span class="publisher">
                <i class="el-icon-user"></i>
                发布者：{{ notice.publisherName || '管理员' }}
              </span>
              <span class="publish-date">
                <i class="el-icon-date"></i>
                发布时间：{{ formatTime(notice.createTime) }}
              </span>
            </div>
            <el-button type="text" size="small" @click.stop="handleViewNotice(notice)">
              查看详情
            </el-button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="pagination.total > 0">
        <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="pagination.currentPage"
            :page-sizes="[10, 20, 50]"
            :page-size="pagination.pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="pagination.total"
        ></el-pagination>
      </div>
    </el-card>

    <!-- 公告详情对话框 -->
    <el-dialog
        title="公告详情"
        :visible.sync="detailDialogVisible"
        width="800px"
        :before-close="handleDialogClose"
    >
      <div class="notice-detail" v-if="currentNotice.id">
        <h2 class="detail-title">{{ currentNotice.title }}</h2>

        <div class="detail-meta">
          <div class="meta-left">
            <span><i class="el-icon-user"></i> 发布者：{{ currentNotice.publisherName || '管理员' }}</span>
            <span><i class="el-icon-date"></i> 发布日期：{{ formatDate(currentNotice.publishDate) }}</span>
          </div>
          <div class="meta-right">
            <span><i class="el-icon-time"></i> 创建时间：{{ formatTime(currentNotice.createTime) }}</span>
          </div>
        </div>

        <!-- 公告图片 -->
        <div v-if="currentNotice.image" class="detail-image">
          <el-image
              :src="getImageUrl(currentNotice.image)"
              :preview-src-list="[getImageUrl(currentNotice.image)]"
              fit="contain"
              style="max-width: 100%; max-height: 400px;"
          ></el-image>
        </div>

        <!-- 公告内容 -->
        <div class="detail-content">
          <div v-html="formatContent(currentNotice.content)"></div>
        </div>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关 闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'NoticePage',
  data() {
    return {
      loading: false,
      searchKeyword: '',
      dateRange: [],
      noticeList: [],
      currentNotice: {},
      detailDialogVisible: false,
      pagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      }
    }
  },
  mounted() {
    this.loadNoticeList()
  },
  methods: {
    // 加载公告列表 - 修复版
    async loadNoticeList() {
      this.loading = true
      try {
        const params = {
          page: this.pagination.currentPage,
          limit: this.pagination.pageSize
        }

        // 添加搜索条件 - 使用keyword参数
        if (this.searchKeyword) {
          params.keyword = this.searchKeyword.trim()
        }

        // 添加日期范围筛选
        if (this.dateRange && this.dateRange.length === 2) {
          params.startDate = this.dateRange[0]
          params.endDate = this.dateRange[1]
        }

        console.log('🔍 请求参数:', params)
        console.log('🌐 请求URL:', '/notice/frontList')

        // 调用后端API获取公告列表
        const response = await this.$http({
          url: '/notice/frontList',
          method: 'get',
          params: params
        })

        console.log('✅ 响应数据:', response.data)

        const data = response.data

        if (data && data.code === 0) {
          // 根据后端返回的数据结构调整
          if (data.data && data.data.list) {
            // PageUtils 格式：{ list: [], total: 100, ... }
            this.noticeList = data.data.list || []
            this.pagination.total = data.data.total || 0
          } else if (data.data && data.data.records) {
            // MyBatis Plus 分页格式
            this.noticeList = data.data.records || []
            this.pagination.total = data.data.total || 0
          } else if (Array.isArray(data.data)) {
            // 直接返回数组
            this.noticeList = data.data
            this.pagination.total = data.data.length
          } else if (data.data && typeof data.data === 'object') {
            // 其他对象格式，尝试提取数据
            this.noticeList = data.data.data || data.data.items || []
            this.pagination.total = data.data.total || data.total || 0
          } else {
            // 默认处理
            this.noticeList = []
            this.pagination.total = 0
          }

          console.log('📋 处理后的公告列表:', this.noticeList)
          console.log('📊 分页信息:', this.pagination)
        } else {
          const errorMsg = data?.msg || data?.message || '获取公告列表失败'
          this.$message.error(errorMsg)
          this.noticeList = []
          this.pagination.total = 0
        }
      } catch (error) {
        console.error('❌ 获取公告列表失败:', error)
        console.error('错误详情:', error.response)

        let errorMessage = '网络错误，获取公告列表失败'
        if (error.response) {
          errorMessage = `请求失败: ${error.response.status} - ${error.response.data?.msg || error.response.statusText}`
        } else if (error.request) {
          errorMessage = '网络连接失败，请检查网络连接'
        }

        this.$message.error(errorMessage)
        this.noticeList = []
        this.pagination.total = 0
      } finally {
        this.loading = false
      }
    },

    // 查看公告详情 - 修复URL
    async handleViewNotice(notice) {
      try {
        console.log('📖 查看公告详情:', notice.id)

        // 使用正确的详情接口路径 /notice/detail/{id}
        const { data } = await this.$http({
          url: `/notice/detail/${notice.id}`,
          method: 'get'
        })

        console.log('✅ 公告详情响应:', data)

        if (data && data.code === 0) {
          this.currentNotice = data.data
        } else {
          console.warn('⚠️ 详情接口失败，使用列表数据')
          this.currentNotice = notice
        }

        this.detailDialogVisible = true
      } catch (error) {
        console.error('❌ 获取公告详情失败:', error)
        // 出错时使用列表数据
        this.currentNotice = notice
        this.detailDialogVisible = true
      }
    },

    // 处理图片URL
    getImageUrl(imagePath) {
      if (!imagePath) return ''
      // 如果已经是完整URL，直接返回
      if (imagePath.startsWith('http') || imagePath.startsWith('//')) {
        return imagePath
      }
      // 如果是相对路径，添加基础URL
      return `${this.$http.defaults.baseURL || ''}${imagePath.startsWith('/') ? '' : '/'}${imagePath}`
    },

    // 去除HTML标签
    stripHtml(html) {
      if (!html) return ''
      const div = document.createElement('div')
      div.innerHTML = html
      return div.textContent || div.innerText || ''
    },

    // 获取内容预览（去除HTML标签并截取）
    getContentPreview(content) {
      if (!content) return '暂无内容'
      const plainText = this.stripHtml(content)
      return plainText.length > 100 ? plainText.substring(0, 100) + '...' : plainText
    },

    // 格式化内容（支持HTML显示）
    formatContent(content) {
      if (!content) return '<p>暂无内容</p>'
      // 确保内容有基本的HTML结构
      if (!content.includes('<')) {
        return `<p>${content.replace(/\n/g, '</p><p>')}</p>`
      }
      return content
    },

    // 判断是否为新公告（7天内）
    isNewNotice(publishDate) {
      if (!publishDate) return false
      try {
        const publish = new Date(publishDate)
        const now = new Date()
        const diffTime = now.getTime() - publish.getTime()
        const diffDays = diffTime / (1000 * 60 * 60 * 24)
        return diffDays <= 7
      } catch (error) {
        return false
      }
    },

    // 格式化日期（年月日）
    formatDate(date) {
      if (!date) return ''
      try {
        const dateObj = new Date(date)
        if (isNaN(dateObj.getTime())) {
          return String(date)
        }
        const year = dateObj.getFullYear()
        const month = String(dateObj.getMonth() + 1).padStart(2, '0')
        const day = String(dateObj.getDate()).padStart(2, '0')
        return `${year}-${month}-${day}`
      } catch (error) {
        return String(date)
      }
    },

    // 格式化时间（年月日 时分秒）
    formatTime(time) {
      if (!time) return ''
      try {
        const date = new Date(time)
        if (isNaN(date.getTime())) {
          return String(time)
        }
        const year = date.getFullYear()
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        const hours = String(date.getHours()).padStart(2, '0')
        const minutes = String(date.getMinutes()).padStart(2, '0')
        return `${year}-${month}-${day} ${hours}:${minutes}`
      } catch (error) {
        return String(time)
      }
    },

    // 日期范围变化
    handleDateChange() {
      this.pagination.currentPage = 1
      this.loadNoticeList()
    },

    // 重置搜索
    resetSearch() {
      this.searchKeyword = ''
      this.dateRange = []
      this.pagination.currentPage = 1
      this.loadNoticeList()
    },

    // 分页大小变化
    handleSizeChange(size) {
      this.pagination.pageSize = size
      this.pagination.currentPage = 1
      this.loadNoticeList()
    },

    // 当前页变化
    handleCurrentChange(page) {
      this.pagination.currentPage = page
      this.loadNoticeList()
    },

    // 对话框关闭
    handleDialogClose() {
      this.detailDialogVisible = false
      this.currentNotice = {}
    }
  }
}
</script>

<style scoped>
.notice-page {
  padding: 20px;
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

.main-card {
  border-radius: 8px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.notice-list {
  min-height: 400px;
}

.notice-item {
  padding: 20px;
  margin-bottom: 16px;
  border: 1px solid #e6e6e6;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
  position: relative;
}

.notice-item:hover {
  border-color: #409EFF;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
  transform: translateY(-2px);
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.notice-title {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.title-text {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}

.notice-time {
  color: #909399;
  font-size: 14px;
  white-space: nowrap;
  margin-left: 16px;
}

.notice-image {
  margin-bottom: 12px;
}

.notice-content {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notice-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notice-meta {
  display: flex;
  gap: 20px;
  color: #909399;
  font-size: 12px;
}

.publisher, .publish-date {
  display: flex;
  align-items: center;
  gap: 4px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #c0c4cc;
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

/* 详情对话框样式 */
.notice-detail {
  padding: 10px;
}

.detail-title {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 24px;
  text-align: center;
  line-height: 1.4;
}

.detail-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
  color: #909399;
  font-size: 14px;
}

.meta-left, .meta-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.detail-image {
  text-align: center;
  margin-bottom: 24px;
}

.detail-content {
  line-height: 1.8;
  color: #606266;
  font-size: 15px;
}

.detail-content >>> p {
  margin-bottom: 16px;
}

.detail-content >>> strong {
  color: #303133;
}

.detail-content >>> br {
  content: "";
  display: block;
  margin-bottom: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .notice-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .notice-time {
    margin-left: 0;
  }

  .detail-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .filter-bar {
    flex-direction: column;
    align-items: flex-start;
  }

  .filter-bar .el-input,
  .filter-bar .el-date-picker {
    width: 100% !important;
  }

  .notice-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .notice-meta {
    flex-direction: column;
    gap: 8px;
  }
}
</style>