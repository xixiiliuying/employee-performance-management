<template>
  <div class="notice-manage">
    <div class="page-header">
      <h1>公告管理</h1>
      <p>管理系统公告信息</p>
    </div>

    <el-card class="main-card">
      <!-- 操作工具栏 -->
      <div class="toolbar">
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">
          发布公告
        </el-button>
        <el-button type="danger" icon="el-icon-delete" :disabled="selectedRows.length === 0" @click="handleBatchDelete">
          批量删除
        </el-button>

        <div class="search-box">
          <el-input
              v-model="searchParams.title"
              placeholder="搜索公告标题"
              prefix-icon="el-icon-search"
              style="width: 300px"
              @keyup.enter.native="handleSearch"
          ></el-input>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </div>

      <!-- 公告列表 -->
      <el-table
          :data="tableData"
          v-loading="loading"
          @selection-change="handleSelectionChange"
          style="width: 100%"
          border
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>

        <el-table-column prop="title" label="公告标题" min-width="200">
          <template slot-scope="{ row }">
            <span class="notice-title" @click="handleView(row)">{{ row.title }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="content" label="公告内容" min-width="300" show-overflow-tooltip>
        </el-table-column>

        <el-table-column prop="publishDate" label="发布日期" width="120" align="center">
          <template slot-scope="{ row }">
            {{ formatDate(row.publishDate) }}
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="160" align="center">
          <template slot-scope="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column prop="publisherName" label="发布人" width="120" align="center">
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template slot-scope="{ row }">
            <el-button type="text" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="text" size="small" @click="handleView(row)">
              查看
            </el-button>
            <el-button type="text" size="small" style="color: #f56c6c" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="pagination.current"
            :page-sizes="[10, 20, 50, 100]"
            :page-size="pagination.size"
            layout="total, sizes, prev, pager, next, jumper"
            :total="pagination.total"
        ></el-pagination>
      </div>
    </el-card>

    <!-- 公告编辑/查看对话框 -->
    <el-dialog
        :title="dialogTitle"
        :visible.sync="dialogVisible"
        width="600px"
        :before-close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题"></el-input>
        </el-form-item>

        <el-form-item label="公告内容" prop="content">
          <el-input
              type="textarea"
              :rows="6"
              v-model="form.content"
              placeholder="请输入公告内容"
          ></el-input>
        </el-form-item>

        <el-form-item label="发布日期">
          <el-date-picker
              v-model="form.publishDate"
              type="date"
              placeholder="选择发布日期"
              value-format="yyyy-MM-dd"
              style="width: 100%"
          ></el-date-picker>
        </el-form-item>

        <el-form-item label="公告图片">
          <el-input v-model="form.image" placeholder="请输入图片URL（可选）"></el-input>
        </el-form-item>
      </el-form>

      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确 定</el-button>
      </span>
    </el-dialog>

    <!-- 公告查看对话框 -->
    <el-dialog
        title="公告详情"
        :visible.sync="viewDialogVisible"
        width="700px"
    >
      <div class="notice-detail">
        <h2 class="detail-title">{{ currentNotice.title }}</h2>
        <div class="detail-meta">
          <span>发布时间：{{ formatDate(currentNotice.publishDate) }}</span>
          <span>发布人：{{ currentNotice.publisherName || '系统' }}</span>
          <span>创建时间：{{ formatDateTime(currentNotice.createTime) }}</span>
        </div>
        <div v-if="currentNotice.image" class="detail-image">
          <img :src="currentNotice.image" alt="公告图片" style="max-width: 100%; max-height: 200px;">
        </div>
        <div class="detail-content">
          {{ currentNotice.content }}
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="viewDialogVisible = false">关 闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import API from '@/utils/api'

export default {
  name: 'NoticeManage',
  data() {
    return {
      currentUser: null,
      loading: false,
      submitting: false,
      tableData: [],
      selectedRows: [],

      // 搜索参数
      searchParams: {
        title: ''
      },

      // 分页参数
      pagination: {
        current: 1,
        size: 10,
        total: 0
      },

      // 对话框控制
      dialogVisible: false,
      viewDialogVisible: false,
      dialogType: 'add', // add, edit

      // 表单数据
      form: {
        id: null,
        title: '',
        content: '',
        image: '',
        publishDate: new Date().toISOString().split('T')[0] // 默认今天
      },

      // 当前查看的公告
      currentNotice: {},

      // 验证规则
      rules: {
        title: [
          { required: true, message: '请输入公告标题', trigger: 'blur' },
          { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '请输入公告内容', trigger: 'blur' },
          { min: 10, message: '公告内容至少10个字符', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    dialogTitle() {
      return this.dialogType === 'add' ? '发布公告' : '编辑公告'
    }
  },
  mounted() {
    this.loadCurrentUser()
    this.loadTableData()
  },
  methods: {
    async loadCurrentUser() {
      try {
        // 方法1: 从 sessionStorage 获取
        const userStr = sessionStorage.getItem('user')
        if (userStr) {
          this.currentUser = JSON.parse(userStr)
          return
        }

        // 方法2: 调用用户信息接口
        const response = await this.$http.get(API.url.user.session)
        if (response.data && response.data.code === 0) {
          this.currentUser = response.data.data
          // 存储到 sessionStorage
          sessionStorage.setItem('user', JSON.stringify(this.currentUser))
        }
      } catch (error) {
        console.warn('获取用户信息失败:', error)
        // 设置默认用户
        this.currentUser = { id: 1, name: '系统管理员' }
      }
    },
    // 加载公告列表
    async loadTableData() {
      this.loading = true
      try {
        const params = {
          page: this.pagination.current,
          limit: this.pagination.size,
          ...this.searchParams
        }

        // 清理空参数
        Object.keys(params).forEach(key => {
          if (params[key] === '' || params[key] === null || params[key] === undefined) {
            delete params[key]
          }
        })

        console.log('公告列表请求参数:', params)

        const response = await this.$http.get(API.url.notice.page, { params })

        console.log('公告列表响应:', response.data)

        if (response.data && response.data.code === 0) {
          const data = response.data.data

          if (data && data.page) {
            this.tableData = data.page.list || []
            this.pagination.total = data.page.totalCount || 0
            this.pagination.current = data.page.currPage || 1
            this.pagination.size = data.page.pageSize || 10
          } else {
            this.tableData = data.list || data.records || []
            this.pagination.total = data.total || this.tableData.length
          }

          console.log('处理后的表格数据:', this.tableData)
        } else {
          this.$message.error(response.data?.msg || '加载公告列表失败')
          this.tableData = []
          this.pagination.total = 0
        }
      } catch (error) {
        console.error('加载公告列表失败:', error)
        this.$message.error('加载公告列表失败: ' + (error.response?.data?.msg || error.message))
        this.tableData = []
        this.pagination.total = 0
      } finally {
        this.loading = false
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
        title: ''
      }
      this.pagination.current = 1
      this.loadTableData()
    },

    // 发布公告
    handleAdd() {
      this.dialogType = 'add'
      this.form = {
        id: null,
        title: '',
        content: '',
        image: '',
        publishDate: new Date().toISOString().split('T')[0]
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.formRef?.clearValidate()
      })
    },

    // 编辑公告
    handleEdit(row) {
      this.dialogType = 'edit'
      this.form = {
        id: row.id,
        title: row.title,
        content: row.content,
        image: row.image || '',
        publishDate: this.formatDate(row.publishDate)
      }
      this.dialogVisible = true
    },

    // 查看公告
    handleView(row) {
      this.currentNotice = { ...row }
      this.viewDialogVisible = true
    },

    // 删除公告
    handleDelete(row) {
      this.$confirm('确定要删除该公告吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          const response = await this.$http.post(API.url.notice.delete, [row.id])

          if (response.data && response.data.code === 0) {
            this.$message.success('删除成功')
            this.loadTableData()
          } else {
            this.$message.error(response.data?.msg || '删除失败')
          }
        } catch (error) {
          console.error('删除失败:', error)
          this.$message.error('删除失败: ' + (error.response?.data?.msg || error.message))
        }
      })
    },

    // 批量删除
    handleBatchDelete() {
      if (this.selectedRows.length === 0) return

      this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 条公告吗？`, '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          const ids = this.selectedRows.map(item => item.id)
          const response = await this.$http.post(API.url.notice.delete, ids)

          if (response.data && response.data.code === 0) {
            this.$message.success('删除成功')
            this.selectedRows = []
            this.loadTableData()
          } else {
            this.$message.error(response.data?.msg || '删除失败')
          }
        } catch (error) {
          console.error('批量删除失败:', error)
          this.$message.error('批量删除失败: ' + (error.response?.data?.msg || error.message))
        }
      })
    },

    // 修改提交方法
    async handleSubmit() {
      try {
        await this.$refs.formRef.validate()
        this.submitting = true

        const submitData = {
          ...this.form,
          publishDate: this.form.publishDate ? new Date(this.form.publishDate) : new Date(),
          // 🔥 使用当前用户ID
          publisherId: this.currentUser ? this.currentUser.id : 1
        }

        console.log('提交公告数据:', submitData)
        console.log('发布人ID:', this.currentUser ? this.currentUser.id : '未获取到')

        let response
        if (this.dialogType === 'add') {
          response = await this.$http.post(API.url.notice.save, submitData)
        } else {
          response = await this.$http.post(API.url.notice.update, submitData)
        }

        if (response.data && response.data.code === 0) {
          this.$message.success(this.dialogType === 'add' ? '发布成功' : '更新成功')
          this.dialogVisible = false
          this.loadTableData()
        } else {
          this.$message.error(response.data?.msg || '操作失败')
        }
      } catch (error) {
        console.error('提交失败:', error)
        if (error.name !== 'ValidationError') {
          this.$message.error('操作失败: ' + (error.response?.data?.msg || error.message))
        }
      } finally {
        this.submitting = false
      }
    },

    // 对话框关闭
    handleDialogClose() {
      this.dialogVisible = false
    },

    // 选择变化
    handleSelectionChange(selection) {
      this.selectedRows = selection
    },

    // 分页大小变化
    handleSizeChange(size) {
      this.pagination.size = size
      this.pagination.current = 1
      this.loadTableData()
    },

    // 当前页变化
    handleCurrentChange(page) {
      this.pagination.current = page
      this.loadTableData()
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
.notice-manage {
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

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-box {
  display: flex;
  gap: 10px;
}

.notice-title {
  color: #409EFF;
  cursor: pointer;
}

.notice-title:hover {
  text-decoration: underline;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.notice-detail {
  padding: 10px;
}

.detail-title {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 20px;
  text-align: center;
}

.detail-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
  color: #909399;
  font-size: 14px;
}

.detail-content {
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
}

.detail-image {
  margin-bottom: 20px;
  text-align: center;
}
</style>