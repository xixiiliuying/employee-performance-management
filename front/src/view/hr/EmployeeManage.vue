<template>
  <div class="employee-manage">
    <!-- 搜索和操作区域 -->
    <div class="search-operate-area">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
              v-model="searchParams.sname"
              placeholder="请输入员工姓名"
              clearable
              @clear="handleSearch"
              @keyup.enter.native="handleSearch"
          >
            <i slot="prefix" class="el-input__icon el-icon-search"></i>
          </el-input>
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
                :label="getDepartmentDisplayName(dept)"
                :value="dept.id"
            />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select
              v-model="searchParams.jobId"
              placeholder="选择岗位"
              clearable
              @change="handleSearch"
          >
            <el-option
                v-for="job in jobOptions"
                :key="job.id"
                :label="getJobDisplayName(job)"
                :value="job.id"
            />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select
              v-model="searchParams.role"
              placeholder="选择角色"
              clearable
              @change="handleSearch"
          >
            <el-option label="普通员工" value="staff" />
            <el-option label="HR" value="hr" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="handleSearch">
            <i class="el-icon-search"></i>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <i class="el-icon-refresh"></i>
            重置
          </el-button>
          <el-button type="success" @click="handleAdd">
            <i class="el-icon-plus"></i>
            新增员工
          </el-button>
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
          :default-sort="{ prop: 'id', order: 'descending' }"
          fit
          highlight-current-row
          :header-cell-style="{ textAlign: 'center', background: '#fafafa', fontWeight: '600' }"
          :cell-style="{ textAlign: 'center' }"
      >
        <el-table-column type="index" label="序号" width="60" />

        <el-table-column prop="sid" label="工号" min-width="100" sortable />
        <el-table-column prop="sname" label="姓名" min-width="100" />
        <el-table-column prop="gender" label="性别" min-width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.gender === '男' ? 'primary' : 'danger'">
              {{ scope.row.gender }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="role" label="角色" min-width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.role === 'hr' ? 'warning' : 'success'">
              {{ scope.row.role === 'hr' ? 'HR' : '员工' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="deptId" label="部门" min-width="120">
          <template slot-scope="scope">
            <span class="info-value">{{ getDepartmentName(scope.row.deptId) || '未分配' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="jobId" label="岗位" min-width="140">
          <template slot-scope="scope">
            <span class="info-value">{{ getJobTitle(scope.row.jobId) || '未分配' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="hireDate" label="入职日期" min-width="130" sortable>
          <template slot-scope="scope">
            {{ formatDate(scope.row.hireDate) }}
          </template>
        </el-table-column>

        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" min-width="160" sortable>
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" fixed="right" min-width="260">
          <template slot-scope="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button
                size="small"
                type="warning"
                @click="handleResetPassword(scope.row)"
            >重置密码</el-button>
            <el-button
                size="small"
                type="danger"
                @click="handleDelete(scope.row)"
            >删除</el-button>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
        :visible.sync="dialogVisible"
        :title="dialogType === 'add' ? '新增员工' : '编辑员工'"
        width="600px"
        @close="handleDialogClose"
    >
      <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="员工姓名" prop="sname">
              <el-input v-model="formData.sname" placeholder="请输入员工姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工号" prop="sid">
              <el-input
                  v-model="formData.sid"
                  placeholder="请输入工号"
                  :disabled="dialogType === 'edit'"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="formData.gender">
                <el-radio label="男">男</el-radio>
                <el-radio label="女">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色" prop="role">
              <el-radio-group v-model="formData.role">
                <el-radio label="staff">普通员工</el-radio>
                <el-radio label="hr">HR</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="部门" prop="deptId">
              <el-select
                  v-model="formData.deptId"
                  placeholder="请选择部门"
                  style="width: 100%"
              >
                <el-option
                    v-for="dept in departmentOptions"
                    :key="dept.id"
                    :label="getDepartmentDisplayName(dept)"
                    :value="dept.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位" prop="jobId">
              <el-select
                  v-model="formData.jobId"
                  placeholder="请选择岗位"
                  style="width: 100%"
              >
                <el-option
                    v-for="job in jobOptions"
                    :key="job.id"
                    :label="getJobDisplayName(job)"
                    :value="job.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="入职日期" prop="hireDate">
              <el-date-picker
                  v-model="formData.hireDate"
                  type="date"
                  placeholder="选择入职日期"
                  style="width: 100%"
                  value-format="yyyy-MM-dd"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>

        <el-form-item v-if="dialogType === 'add'" label="初始密码" prop="password">
          <el-input
              v-model="formData.password"
              type="password"
              placeholder="请输入初始密码"
              show-password
          />
        </el-form-item>
      </el-form>

      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import API from '@/utils/api'

export default {
  name: 'EmployeeManage',
  data() {
    return {
      // 响应式数据
      loading: false,
      dialogVisible: false,
      dialogType: 'add',
      submitting: false,

      // 表格数据
      tableData: [],

      // 搜索参数
      searchParams: {
        sname: '',
        deptId: null,
        jobId: null,
        role: ''
      },

      // 分页参数
      pagination: {
        current: 1,
        size: 10,
        total: 0
      },

      // 表单数据
      formData: {
        id: '',
        sid: '',
        sname: '',
        gender: '男',
        role: 'staff',
        deptId: '',
        jobId: '',
        hireDate: '',
        phone: '',
        email: '',
        password: '123456'
      },

      // 表单验证规则
      formRules: {
        sid: [
          { required: true, message: '请输入工号', trigger: 'blur' },
          { pattern: /^\d+$/, message: '工号必须为数字', trigger: 'blur' }
        ],
        sname: [
          { required: true, message: '请输入员工姓名', trigger: 'blur' }
        ],
        gender: [
          { required: true, message: '请选择性别', trigger: 'change' }
        ],
        role: [
          { required: true, message: '请选择角色', trigger: 'change' }
        ],
        deptId: [
          { required: true, message: '请选择部门', trigger: 'change' }
        ],
        jobId: [
          { required: true, message: '请选择岗位', trigger: 'change' }
        ],
        hireDate: [
          { required: true, message: '请选择入职日期', trigger: 'change' }
        ],
        phone: [
          {
            required: false,
            validator: (rule, value, callback) => {
              if (!value) return callback()
              const v = value.trim()
              if (!/^1[3-9]\d{9}$/.test(v)) {
                return callback(new Error('请输入正确的手机号码'))
              }
              callback()
            },
            trigger: ['blur', 'change']
          }
        ],
        email: [
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入初始密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ]
      },

      // 选项数据
      departmentOptions: [],
      jobOptions: []
    }
  },
  async mounted() {
    await this.loadOptions()
    this.loadTableData()
  },
  methods: {
    // 提取列表数据
    extractList(data) {
      if (!data) return []
      if (Array.isArray(data)) return data
      if (data.list && Array.isArray(data.list)) return data.list
      if (data.data && Array.isArray(data.data)) return data.data
      if (data.records && Array.isArray(data.records)) return data.records
      return []
    },

    // 获取部门显示名称
    getDepartmentDisplayName(dept) {
      return dept?.dname || dept?.Dname || dept?.name || dept?.department_name || `部门(${dept?.id})`
    },

    // 获取岗位显示名称
    getJobDisplayName(job) {
      return job?.jname || job?.Jname || job?.name || job?.position_name || `岗位(${job?.id})`
    },

    // 获取部门名称
    getDepartmentName(deptId) {
      if (!deptId || !Array.isArray(this.departmentOptions)) return ''
      const dept = this.departmentOptions.find(item => String(item.id) === String(deptId))
      return dept ? this.getDepartmentDisplayName(dept) : `部门(${deptId})`
    },

    // 获取岗位名称
    getJobTitle(jobId) {
      if (!jobId || !Array.isArray(this.jobOptions)) return ''
      const job = this.jobOptions.find(item => String(item.id) === String(jobId))
      return job ? this.getJobDisplayName(job) : `岗位(${jobId})`
    },

    // 构建请求参数
    buildRequestParams() {
      const params = {
        page: String(this.pagination.current),
        size: String(this.pagination.size)
      }

      // 处理姓名
      if (this.searchParams.sname && this.searchParams.sname.trim() !== '') {
        params.sname = this.searchParams.sname.trim()
      }

      // 处理部门ID
      if (this.searchParams.deptId !== undefined &&
          this.searchParams.deptId !== null &&
          this.searchParams.deptId !== '') {
        params.dept_id = String(this.searchParams.deptId)
      }

      // 处理岗位ID
      if (this.searchParams.jobId !== undefined &&
          this.searchParams.jobId !== null &&
          this.searchParams.jobId !== '') {
        params.job_id = String(this.searchParams.jobId)
      }

      // 处理角色
      if (this.searchParams.role && this.searchParams.role !== '') {
        params.role = this.searchParams.role
      }

      return params
    },

    // 加载表格数据
    async loadTableData() {
      this.loading = true
      try {
        const params = this.buildRequestParams()
        const response = await this.$http.get(API.url.user.page, { params })

        if (response.data && response.data.code === 0) {
          const pageData = response.data.data
          let records = this.extractList(pageData)
          this.tableData = records || []

          // 处理分页信息
          if (pageData && typeof pageData === 'object') {
            this.pagination.total = pageData.total || pageData.count || this.tableData.length
            this.pagination.current = pageData.current || pageData.page || this.pagination.current
            this.pagination.size = pageData.size || pageData.limit || this.pagination.size
          } else {
            this.pagination.total = this.tableData.length
          }
        } else {
          this.$message.error(response.data?.msg || '加载员工数据失败')
        }
      } catch (error) {
        console.error('加载员工数据失败:', error)
        this.$message.error('加载员工数据失败')
      } finally {
        this.loading = false
      }
    },

    // 加载选项数据
    async loadOptions() {
      try {
        const [deptRes, jobRes] = await Promise.all([
          this.$http.get(API.url.option.department),
          this.$http.get(API.url.option.job)
        ])

        // 处理部门数据
        if (deptRes.data && deptRes.data.code === 0) {
          this.departmentOptions = this.extractList(deptRes.data.data) || []
        } else {
          this.departmentOptions = []
          this.$message.error('加载部门数据失败')
        }

        // 处理岗位数据
        if (jobRes.data && jobRes.data.code === 0) {
          this.jobOptions = this.extractList(jobRes.data.data) || []
        } else {
          this.jobOptions = []
          this.$message.error('加载岗位数据失败')
        }
      } catch (error) {
        console.error('加载选项数据失败:', error)
        this.$message.error('加载选项数据失败')
      }
    },

    // 搜索
    handleSearch() {
      this.pagination.current = 1
      this.loadTableData()
    },

    // 重置搜索
    handleReset() {
      Object.assign(this.searchParams, {
        sname: '',
        deptId: null,
        jobId: null,
        role: ''
      })
      this.pagination.current = 1
      this.loadTableData()
    },

    // 新增员工
    handleAdd() {
      this.dialogType = 'add'
      this.dialogVisible = true
      this.resetForm()
    },

    // 编辑员工
    handleEdit(row) {
      this.dialogType = 'edit'
      this.dialogVisible = true
      Object.assign(this.formData, {
        id: row.id,
        sid: row.sid,
        sname: row.sname,
        gender: row.gender,
        role: row.role,
        deptId: row.deptId,
        jobId: row.jobId,
        hireDate: row.hireDate,
        phone: row.phone,
        email: row.email,
        password: ''
      })
    },

// 🔥 修复后的删除功能实现
    async handleDelete(row) {
      try {
        // 确认对话框
        await this.$confirm(
            `确定要删除员工 "${row.sname} (工号: ${row.sid})" 吗？此操作不可恢复！`,
            '删除确认',
            {
              confirmButtonText: '确定删除',
              cancelButtonText: '取消',
              type: 'error',
              confirmButtonClass: 'el-button--danger'
            }
        )

        console.log('开始删除员工:', {
          id: row.id,
          sid: row.sid,
          sname: row.sname
        })

        // 显示加载状态
        const loadingInstance = this.$loading({
          lock: true,
          text: '删除中...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        })

        try {
          // 🔥 修复：使用数组格式 [row.id] 而不是对象 {id: row.id}
          const deleteData = [row.id]  // 将ID包装在数组中
          console.log('删除请求数据（数组格式）:', deleteData)

          const response = await this.$http.post(API.url.user.delete, deleteData, {
            headers: {
              'Content-Type': 'application/json'
            }
          })

          console.log('删除响应:', response)

          if (response.data && response.data.code === 0) {
            this.$message.success({
              message: `员工 "${row.sname}" 删除成功`,
              duration: 2000
            })

            // 删除成功后重新加载数据
            await this.loadTableData()

            // 如果删除的是当前页的最后一条数据，且不是第一页，则跳转到前一页
            if (this.tableData.length === 0 && this.pagination.current > 1) {
              this.pagination.current -= 1
              this.loadTableData()
            }
          } else {
            const errorMsg = response.data?.msg || '删除失败，请重试'
            this.$message.error(errorMsg)
          }
        } finally {
          loadingInstance.close()
        }

      } catch (error) {
        // 用户取消删除操作
        if (error === 'cancel' || error.message === 'cancel') {
          this.$message.info('已取消删除操作')
          return
        }

        console.error('删除员工失败:', error)
        this.$message.error('删除失败，请重试')
      }
    },

    // 重置密码
    async handleResetPassword(row) {
      try {
        await this.$confirm(
            `确定要重置员工 "${row.sname}" 的密码吗？`,
            '重置密码确认',
            {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }
        )

        const payload = { username: row.sid }
        const response = await this.$http.post(API.url.user.resetPass, payload)

        if (response.data && response.data.code === 0) {
          this.$message.success('密码重置成功，初始密码为：123456')
        } else {
          this.$message.error(response.data?.msg || '密码重置失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('重置密码失败:', error)
          this.$message.error('密码重置失败')
        }
      }
    },

    // 表单提交
    async handleSubmit() {
      try {
        await this.$refs.formRef.validate()
        this.submitting = true

        // 处理手机号
        if(this.formData.phone) {
          this.formData.phone = this.formData.phone.trim().replace(/\D/g, '')
        }

        const submitData = {
          id: this.formData.id,
          sid: this.formData.sid,
          sname: this.formData.sname,
          gender: this.formData.gender,
          role: this.formData.role,
          deptId: this.formData.deptId,
          jobId: this.formData.jobId,
          hireDate: this.formData.hireDate,
          phone: this.formData.phone,
          email: this.formData.email
        }

        if (this.dialogType === 'add') {
          submitData.password = String(this.formData.password)
        }

        const response = this.dialogType === 'add'
            ? await this.$http.post(API.url.user.save, submitData)
            : await this.$http.post(API.url.user.update, submitData)

        if (response.data && response.data.code === 0) {
          this.$message.success(this.dialogType === 'add' ? '新增成功' : '编辑成功')
          this.dialogVisible = false
          this.loadTableData()
        } else {
          this.$message.error(response.data?.msg || '操作失败')
        }
      } catch (error) {
        console.error('表单提交失败:', error)
        this.$message.error('操作失败，请重试')
      } finally {
        this.submitting = false
      }
    },

    // 重置表单
    resetForm() {
      if (this.$refs.formRef) {
        this.$refs.formRef.resetFields()
      }
      Object.assign(this.formData, {
        id: '',
        sid: '',
        sname: '',
        gender: '男',
        role: 'staff',
        deptId: '',
        jobId: '',
        hireDate: '',
        phone: '',
        email: '',
        password: '123456'
      })
    },

    // 对话框关闭
    handleDialogClose() {
      this.resetForm()
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

    // 日期格式化
    formatDate(date) {
      if (!date) return '未设置'
      try {
        const dateObj = new Date(date)
        if (isNaN(dateObj.getTime())) {
          return '日期无效'
        }
        return dateObj.toLocaleDateString('zh-CN')
      } catch (error) {
        return date || '日期错误'
      }
    },

    formatDateTime(datetime) {
      if (!datetime) return '未设置'
      try {
        const dateObj = new Date(datetime)
        if (isNaN(dateObj.getTime())) {
          return '日期无效'
        }
        return dateObj.toLocaleString('zh-CN')
      } catch (error) {
        return datetime || '日期错误'
      }
    }
  }
}
</script>

<style scoped>
/* 确保操作列有足够空间 */
.table-area .el-table .el-table__body-wrapper .el-table__row td:last-child {
  padding: 8px 4px;
}

/* 按钮紧凑样式 */
.employee-manage .el-button--mini {
  padding: 5px 8px;
  margin: 0 2px;
}
.employee-manage {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 搜索与操作区域 */
.search-operate-area {
  background: #fff;
  padding: 20px 24px;
  border-radius: 12px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
}

.search-operate-area .el-row {
  align-items: center;
}

.search-operate-area .el-col {
  display: flex;
  align-items: center;
}

/* 按钮组样式 */
.search-operate-area .el-button {
  margin-left: 8px;
}

.search-operate-area .el-button:first-of-type {
  margin-left: 0;
}

.table-area {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
  padding: 10px 20px 20px;
  flex-grow: 1;
  overflow-x: auto;
}

/* 保证单元格文字居中且不拥挤 */
::v-deep(.el-table .cell) {
  text-align: center;
  white-space: nowrap;
  line-height: 22px;
}

/* 保证头部对齐 */
::v-deep .el-table th > .cell {
  text-align: center;
  font-weight: 600;
}

.info-value {
  color: #333;
}

/* 自适应布局 */
@media (max-width: 992px) {
  .table-area {
    padding: 10px;
    overflow-x: auto;
  }

  ::v-deep(.el-table) {
    min-width: 900px;
  }
}

/* 分页区域 */
.pagination-area {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
  padding: 12px 24px;
  display: flex;
  justify-content: flex-end;
}

/* 对话框样式优化 */
::v-deep(.el-dialog) {
  border-radius: 12px;
}

::v-deep(.el-dialog__header) {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 12px;
}

::v-deep(.el-dialog__body) {
  padding: 24px 30px;
  background: #fafafa;
}

::v-deep(.el-form-item) {
  margin-bottom: 16px;
}

/* 表单输入框更宽松 */
::v-deep(.el-input__inner),
::v-deep(.el-select),
::v-deep(.el-date-editor) {
  border-radius: 8px;
}

/* 对话框底部按钮 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 10px;
}

.dialog-footer .el-button {
  margin-left: 12px;
}

/* 标签样式统一 */
::v-deep(.el-tag) {
  font-size: 13px;
  border-radius: 6px;
  padding: 2px 8px;
}

/* 响应式适配 */
@media (max-width: 992px) {
  .search-operate-area .el-row {
    flex-direction: column;
    gap: 10px;
  }

  .search-operate-area .el-col {
    width: 100%;
  }

  .table-area {
    padding: 10px;
  }
}

</style>