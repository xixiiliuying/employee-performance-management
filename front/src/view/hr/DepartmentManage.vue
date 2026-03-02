<template>
  <div class="department-manage">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">部门管理</h1>
          <p class="page-desc">管理系统组织架构，设置部门负责人</p>
        </div>
        <div class="header-actions">
          <el-button type="success" @click="handleAdd" icon="el-icon-plus" size="medium">
            新增部门
          </el-button>
        </div>
      </div>
    </div>

    <!-- 搜索和操作区域 -->
    <el-card class="search-card" shadow="never">
      <div class="search-operate-area">
        <el-form :model="searchParams" :inline="true" class="search-form">
          <el-form-item label="部门名称">
            <el-input
                v-model="searchParams.dname"
                placeholder="请输入部门名称"
                clearable
                prefix-icon="el-icon-search"
                @clear="handleSearch"
                @keyup.enter.native="handleSearch"
                style="width: 240px"
            />
          </el-form-item>
          <el-form-item>
            <el-button-group>
              <el-button type="primary" @click="handleSearch" icon="el-icon-search">
                搜索
              </el-button>
              <el-button @click="handleReset" icon="el-icon-refresh">
                重置
              </el-button>
            </el-button-group>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <span class="table-title">部门列表</span>
          <div class="table-actions">
            <el-button type="text" icon="el-icon-refresh" @click="loadTableData" :loading="loading">
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-table
          :data="tableData"
          v-loading="loading"
          style="width: 100%"
          :default-sort="{ prop: 'id', order: 'descending' }"
          class="department-table"
          stripe
      >
        <el-table-column type="index" width="60" label="序号" align="center" />
        <el-table-column prop="id" label="部门ID" width="100" sortable align="center" />
        <el-table-column prop="dname" label="部门名称" min-width="180">
          <template slot-scope="scope">
            <div class="dept-name-cell">
              <i class="el-icon-office dept-icon"></i>
              <span class="dept-name">{{ scope.row.dname }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="managerName" label="部门负责人" width="150" align="center">
          <template slot-scope="scope">
            <div class="manager-cell">
              <el-avatar v-if="scope.row.managerName" size="small" :src="getAvatarUrl(scope.row.managerName)" class="manager-avatar">
                {{ scope.row.managerName.charAt(0) }}
              </el-avatar>
              <span v-if="scope.row.managerName" class="manager-name">{{ scope.row.managerName }}</span>
              <el-tag v-else type="info" size="small">未设置</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="staffCount" label="员工数量" width="120" sortable align="center">
          <template slot-scope="scope">
            <el-badge :value="scope.row.staffCount || 0" class="staff-badge" type="primary">
              <el-tag type="info" effect="light" size="medium">
                <i class="el-icon-user"></i>
                {{ scope.row.staffCount || 0 }}人
              </el-tag>
            </el-badge>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" sortable align="center">
          <template slot-scope="scope">
            <div class="create-time">
              <i class="el-icon-time time-icon"></i>
              {{ formatDateTime(scope.row.createTime) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template slot-scope="scope">
            <el-button-group class="action-buttons">
              <el-button
                  size="mini"
                  type="primary"
                  @click="handleEdit(scope.row)"
                  icon="el-icon-edit"
                  plain
              >
                编辑
              </el-button>
              <el-button
                  size="mini"
                  type="danger"
                  @click="handleDelete(scope.row)"
                  icon="el-icon-delete"
                  plain
                  :disabled="scope.row.staffCount > 0"
              >
                删除
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <template #empty>
        <div class="empty-state">
          <i class="el-icon-office empty-icon"></i>
          <p class="empty-text">暂无部门数据</p>
          <el-button type="primary" @click="handleAdd" icon="el-icon-plus" size="small">
            新增部门
          </el-button>
        </div>
      </template>

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
    </el-card>

    <!-- 新增/编辑部门对话框 -->
    <el-dialog
        :visible.sync="dialogVisible"
        :title="dialogType === 'add' ? '新增部门' : '编辑部门'"
        width="500px"
        @close="handleDialogClose"
        class="department-dialog"
        :close-on-click-modal="false"
    >
      <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="100px"
          class="department-form"
      >
        <el-form-item label="部门名称" prop="dname">
          <el-input
              v-model="formData.dname"
              placeholder="请输入部门名称"
              prefix-icon="el-icon-office"
              maxlength="50"
              show-word-limit
          />
        </el-form-item>

        <el-form-item label="部门负责人" prop="managerId">
          <el-select
              v-model="formData.managerId"
              placeholder="请选择部门负责人"
              style="width: 100%"
              clearable
              filterable
              prefix-icon="el-icon-user"
          >
            <el-option
                v-for="staff in staffOptions"
                :key="staff.id"
                :label="`${staff.sname} (${staff.sid})`"
                :value="staff.id"
            >
              <div class="staff-option">
                <span class="staff-name">{{ staff.sname }}</span>
                <span class="staff-id">({{ staff.sid }})</span>
                <span v-if="staff.jobName" class="staff-job">{{ staff.jobName }}</span>
              </div>
            </el-option>
          </el-select>
          <div class="form-tip">如不选择，部门负责人将显示为未设置</div>
        </el-form-item>
      </el-form>

      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false" size="medium">取消</el-button>
        <el-button
            type="primary"
            @click="handleSubmit"
            :loading="submitting"
            size="medium"
        >
          {{ dialogType === 'add' ? '创建部门' : '保存修改' }}
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import API from '@/utils/api'

export default {
  name: 'DepartmentManage',
  data() {
    return {
      loading: false,
      dialogVisible: false,
      dialogType: 'add',
      submitting: false,

      tableData: [],

      searchParams: {
        dname: ''
      },

      pagination: {
        current: 1,
        size: 10,
        total: 0
      },

      formData: {
        id: '',
        dname: '',
        managerId: null
      },

      staffOptions: [],

      formRules: {
        dname: [
          { required: true, message: '请输入部门名称', trigger: 'blur' },
          { min: 2, max: 50, message: '部门名称长度为2-50个字符', trigger: 'blur' }
        ]
      }
    }
  },
  mounted() {
    this.loadTableData()
    this.loadStaffOptions()
  },
  methods: {
    // 获取头像URL（模拟）
    getAvatarUrl(name) {
      return `https://api.dicebear.com/7.x/avataaars/svg?seed=${name}`
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

        Object.keys(params).forEach(k => {
          if (params[k] === '' || params[k] === null || params[k] === undefined) delete params[k]
        })

        const response = await this.$http.get(API.url.department.page, { params })

        if (!(response && response.data)) {
          throw new Error('无效响应')
        }

        if (response.data.code !== 0) {
          this.$message.error(response.data.msg || '加载部门失败')
          return
        }

        const payload = response.data.data || response.data.page || {}
        let list = []
        let total = 0

        if (Array.isArray(response.data.data)) {
          list = response.data.data
          total = list.length
        } else if (payload.list && Array.isArray(payload.list)) {
          list = payload.list
          total = payload.total || payload.totalCount || payload.count || response.data.data.total || response.data.data.total
        } else if (payload.records && Array.isArray(payload.records)) {
          list = payload.records
          total = payload.total || payload.totalCount || payload.count || 0
        } else if (payload.data && Array.isArray(payload.data)) {
          list = payload.data
          total = list.length
        }

        this.tableData = list.map(item => ({
          id: item.id,
          dname: item.dname || item.Dname || item.departmentName || item.name,
          managerId: item.managerId || item.manager_id,
          managerName: item.managerName || item.manager_name || (item.manager ? item.manager.sname : null),
          staffCount: item.staffCount != null ? item.staffCount : (item.staff_count != null ? item.staff_count : 0),
          createTime: item.createTime || item.create_time || item.createTimeMillis || item.create_time_ms || item.create
        }))

        this.pagination.total = total || this.tableData.length
      } catch (err) {
        console.error('加载部门数据失败:', err)
        this.$message.error('加载部门数据失败: ' + (err.message || '未知错误'))
      } finally {
        this.loading = false
      }
    },

    async loadStaffOptions() {
      try {
        const response = await this.$http.get(API.url.user.list)
        if (response && response.data && response.data.code === 0) {
          const raw = response.data.data || response.data.list || []
          this.staffOptions = raw.map(u => ({
            id: u.id,
            sname: u.sname || u.Sname,
            sid: u.sid || u.Sid,
            jobName: u.jobName || u.job_name
          }))
        }
      } catch (err) {
        console.error('加载员工选项失败:', err)
      }
    },

    handleSearch() {
      this.pagination.current = 1
      this.loadTableData()
    },

    handleReset() {
      this.searchParams = { dname: '' }
      this.pagination.current = 1
      this.loadTableData()
    },

    handleAdd() {
      this.dialogType = 'add'
      this.dialogVisible = true
      this.resetForm()
    },

    handleEdit(row) {
      this.dialogType = 'edit'
      this.dialogVisible = true
      this.formData = {
        id: row.id,
        dname: row.dname,
        managerId: row.managerId || null
      }
    },

    async handleDelete(row) {
      try {
        await this.$confirm(
            `确定要删除部门 "${row.dname}" 吗？此操作不可恢复！`,
            '删除确认',
            {
              confirmButtonText: '确定删除',
              cancelButtonText: '取消',
              type: 'error',
              confirmButtonClass: 'el-button--danger',
              customClass: 'delete-confirm-dialog'
            }
        )

        if (row.staffCount > 0) {
          this.$message.error('该部门下还有员工，无法删除')
          return
        }

        const ids = [row.id]

        const res = await this.$http({
          url: API.url.department.delete,
          method: 'post',
          headers: { 'Content-Type': 'application/json' },
          data: ids
        })

        if (res?.data?.code === 0) {
          this.$message.success('删除成功')
          this.loadTableData()
        } else {
          this.$message.error(res.data?.msg || '删除失败')
        }
      } catch (err) {
        if (err !== 'cancel') {
          console.error('删除失败:', err)
          this.$message.error('删除失败: ' + (err.response?.data?.message || err.message || '未知错误'))
        }
      }
    },

    async handleSubmit() {
      try {
        await this.$refs.formRef.validate()
        this.submitting = true
        const payload = { ...this.formData }
        const url = this.dialogType === 'add' ? API.url.department.save : API.url.department.update
        const res = await this.$http.post(url, payload)
        if (res && res.data && res.data.code === 0) {
          this.$message.success(this.dialogType === 'add' ? '新增成功' : '编辑成功')
          this.dialogVisible = false
          this.loadTableData()
        } else {
          this.$message.error(res.data?.msg || '操作失败')
        }
      } catch (err) {
        console.error('表单提交失败:', err)
        this.$message.error('表单提交失败')
      } finally {
        this.submitting = false
      }
    },

    resetForm() {
      if (this.$refs.formRef) this.$refs.formRef.resetFields()
      this.formData = { id: '', dname: '', managerId: null }
    },

    handleDialogClose() {
      this.resetForm()
    },

    handleSizeChange(size) {
      this.pagination.size = size
      this.pagination.current = 1
      this.loadTableData()
    },

    handleCurrentChange(current) {
      this.pagination.current = current
      this.loadTableData()
    },

    formatDateTime(ts) {
      if (!ts && ts !== 0) return ''
      try {
        const n = typeof ts === 'number' ? ts : Number(ts)
        const date = new Date(n)
        if (isNaN(date.getTime())) return ts
        const y = date.getFullYear()
        const m = String(date.getMonth() + 1).padStart(2, '0')
        const d = String(date.getDate()).padStart(2, '0')
        const hh = String(date.getHours()).padStart(2, '0')
        const mm = String(date.getMinutes()).padStart(2, '0')
        return `${y}-${m}-${d} ${hh}:${mm}`
      } catch (err) {
        return ts
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.department-manage {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

// 页面标题
.page-header {
  margin-bottom: 20px;

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;

    .title-section {
      .page-title {
        margin: 0;
        font-size: 24px;
        font-weight: 600;
        color: #303133;
        line-height: 1.2;
      }

      .page-desc {
        margin: 8px 0 0 0;
        color: #909399;
        font-size: 14px;
      }
    }
  }
}

// 搜索卡片
.search-card {
  margin-bottom: 20px;
  border-radius: 8px;
  border: 1px solid #ebeef5;

  .search-form {
    margin-bottom: 0;

    ::v-deep .el-form-item {
      margin-bottom: 0;
    }
  }
}

// 表格卡片
.table-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;

  .table-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .table-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }
}

// 表格样式
.department-table {
  ::v-deep .el-table__header {
    th {
      background-color: #f8f9fa;
      color: #606266;
      font-weight: 600;
    }
  }

  .dept-name-cell {
    display: flex;
    align-items: center;

    .dept-icon {
      color: #409eff;
      margin-right: 8px;
      font-size: 16px;
    }

    .dept-name {
      font-weight: 500;
      color: #303133;
    }
  }

  .manager-cell {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;

    .manager-avatar {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    .manager-name {
      font-weight: 500;
    }
  }

  .staff-badge {
    ::v-deep .el-badge__content {
      transform: translate(50%, -50%);
    }
  }

  .create-time {
    display: flex;
    align-items: center;
    justify-content: center;
    color: #909399;
    font-size: 13px;

    .time-icon {
      margin-right: 4px;
      color: #c0c4cc;
    }
  }

  .action-buttons {
    display: flex;
    gap: 4px;

    .el-button {
      border-radius: 4px;
    }
  }
}

// 空状态
.empty-state {
  padding: 60px 0;
  text-align: center;

  .empty-icon {
    font-size: 64px;
    color: #c0c4cc;
    margin-bottom: 16px;
  }

  .empty-text {
    color: #909399;
    margin: 0 0 20px 0;
    font-size: 14px;
  }
}

// 分页
.pagination-area {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

// 对话框样式
.department-dialog {
  .department-form {
    .form-tip {
      font-size: 12px;
      color: #909399;
      margin-top: 4px;
    }

    .staff-option {
      display: flex;
      align-items: center;
      gap: 8px;

      .staff-name {
        font-weight: 500;
      }

      .staff-id {
        color: #909399;
        font-size: 12px;
      }

      .staff-job {
        margin-left: auto;
        color: #409eff;
        font-size: 12px;
        background: #ecf5ff;
        padding: 2px 6px;
        border-radius: 3px;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .department-manage {
    padding: 16px;
  }

  .page-header .header-content {
    flex-direction: column;
    gap: 16px;

    .header-actions {
      width: 100%;

      .el-button {
        width: 100%;
      }
    }
  }

  .search-form {
    ::v-deep .el-form-item {
      width: 100%;

      .el-input {
        width: 100% !important;
      }
    }
  }

  .action-buttons {
    flex-direction: column;
    gap: 4px;

    .el-button {
      width: 100%;
    }
  }
}

// 动画效果
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter, .fade-leave-to {
  opacity: 0;
}

// 删除确认对话框样式
::v-deep .delete-confirm-dialog {
  .el-message-box__btns {
    .el-button--danger {
      background-color: #f56c6c;
      border-color: #f56c6c;

      &:hover {
        background-color: #f78989;
        border-color: #f78989;
      }
    }
  }
}
</style>