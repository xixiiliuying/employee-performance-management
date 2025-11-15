<template>
  <div class="profile-edit-form">
    <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        label-position="right"
    >
      <!-- 基本信息 -->
      <div class="form-section">
        <h3 class="section-title">基本信息</h3>
        <div class="form-row">
          <el-form-item label="姓名" prop="sname">
            <el-input
                v-model="formData.sname"
                placeholder="请输入姓名"
                maxlength="10"
                show-word-limit
            />
          </el-form-item>

          <el-form-item label="工号" prop="sid">
            <el-input
                v-model="formData.sid"
                placeholder="请输入工号"
                disabled
            />
            <div class="field-tip">工号不可修改</div>
          </el-form-item>
        </div>

        <div class="form-row">
          <el-form-item label="部门" prop="deptId">
            <el-select
                v-model="formData.deptId"
                placeholder="请选择部门"
                style="width: 100%"
                clearable
                filterable
            >
              <el-option
                  v-for="dept in departmentList"
                  :key="dept.id"
                  :label="dept.dname"
                  :value="dept.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="岗位" prop="jobId">
            <el-select
                v-model="formData.jobId"
                placeholder="请选择岗位"
                style="width: 100%"
                clearable
                filterable
            >
              <el-option
                  v-for="job in jobList"
                  :key="job.id"
                  :label="job.jname"
                  :value="job.id"
              />
            </el-select>
          </el-form-item>
        </div>
      </div>

      <!-- 联系信息 -->
      <div class="form-section">
        <h3 class="section-title">联系信息</h3>
        <div class="form-row">
          <el-form-item label="手机号码" prop="phone">
            <el-input
                v-model="formData.phone"
                placeholder="请输入手机号码"
                maxlength="11"
            />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input
                v-model="formData.email"
                placeholder="请输入邮箱地址"
                type="email"
            />
          </el-form-item>
        </div>
      </div>

      <!-- 入职信息 -->
      <div class="form-section">
        <h3 class="section-title">入职信息</h3>
        <div class="form-row">
          <el-form-item label="入职日期">
            <el-date-picker
                v-model="formData.hireDate"
                type="date"
                placeholder="选择入职日期"
                style="width: 100%"
                format="yyyy-MM-dd"
                value-format="yyyy-MM-dd"
                disabled
            />
            <div class="field-tip">入职日期不可修改</div>
          </el-form-item>

          <el-form-item label="员工状态">
            <el-tag :type="formData.status === 'active' ? 'success' : 'danger'">
              {{ formData.status === 'active' ? '在职' : '离职' }}
            </el-tag>
            <div class="field-tip">状态由系统管理</div>
          </el-form-item>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <el-button @click="cancelEdit" :disabled="loading">
          取消
        </el-button>
        <el-button @click="resetForm" :disabled="loading">
          重置
        </el-button>
        <el-button
            type="primary"
            :loading="loading"
            @click="submitForm"
        >
          {{ loading ? '保存中...' : '保存更改' }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script>
import API from '@/utils/api'

export default {
  name: 'ProfileEditForm',
  props: {
    userInfo: {
      type: Object,
      default: () => ({})
    },
    departmentList: {
      type: Array,
      default: () => []
    },
    jobList: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      loading: false,
      formData: {
        sname: '',
        sid: '',
        deptId: null,
        jobId: null,
        phone: '',
        email: '',
        hireDate: '',
        status: 'active'
      },
      formRules: {
        sname: [
          { required: true, message: '请输入姓名', trigger: 'blur' },
          { min: 2, max: 10, message: '姓名长度在 2 到 10 个字符', trigger: 'blur' }
        ],
        sid: [
          { required: true, message: '请输入工号', trigger: 'blur' }
        ],
        deptId: [
          { required: true, message: '请选择部门', trigger: 'change' }
        ],
        jobId: [
          { required: true, message: '请选择岗位', trigger: 'change' }
        ],
        phone: [
          { required: true, message: '请输入手机号码', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ],
        hireDate: [
          { required: true, message: '请选择入职日期', trigger: 'change' }
        ]
      }
    }
  },
  watch: {
    userInfo: {
      handler(newVal) {
        this.initFormData()
      },
      deep: true,
      immediate: true
    }
  },
  methods: {
    initFormData() {
      this.formData = {
        sname: this.userInfo.sname || '',
        sid: this.userInfo.sid || '',
        deptId: this.userInfo.deptId || null,
        jobId: this.userInfo.jobId || null,
        phone: this.userInfo.phone || '',
        email: this.userInfo.email || '',
        hireDate: this.userInfo.hireDate || '',
        status: this.userInfo.status || 'active'
      }
    },

    submitForm() {
      this.$refs.formRef.validate(async (valid) => {
        if (!valid) return

        this.loading = true

        try {
          // 使用封装的 $http 请求
          const { data: result } = await this.$http.post(API.url.user.update, {
            id: this.userInfo.id,
            sname: this.formData.sname,
            deptId: this.formData.deptId,
            jobId: this.formData.jobId,
            phone: this.formData.phone,
            email: this.formData.email
          })

          if (result.code === 0) {
            this.$message.success('资料更新成功')
            this.$emit('update-success')
          } else {
            this.$message.error(result.msg || '更新失败')
          }
        } catch (error) {
          console.error('更新用户信息失败:', error)
          this.$message.error('更新失败，请重试')
        } finally {
          this.loading = false
        }
      })
    },

    cancelEdit() {
      this.$confirm('确定取消编辑吗？所有未保存的更改将会丢失。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.initFormData()
        this.$emit('cancel')
      }).catch(() => {
        // 用户点击取消，不做任何操作
      })
    },

    resetForm() {
      if (this.$refs.formRef) {
        this.$refs.formRef.resetFields()
        this.initFormData()
      }
    },

    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN')
    }
  }
}
</script>

<style scoped lang="scss">
.profile-edit-form {
  padding: 0 20px;

  .form-section {
    margin-bottom: 24px;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 8px;

    .section-title {
      margin: 0 0 16px 0;
      font-size: 16px;
      color: #303133;
      font-weight: 600;
      padding-bottom: 8px;
      border-bottom: 1px solid #e0e0e0;
    }
  }

  .form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;

    &:not(:last-child) {
      margin-bottom: 16px;
    }
  }

  .field-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }

  .form-actions {
    display: flex;
    justify-content: center;
    gap: 12px;
    margin-top: 32px;
    padding-top: 20px;
    border-top: 1px solid #e0e0e0;

    .el-button {
      min-width: 100px;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .profile-edit-form {
    padding: 0 10px;

    .form-row {
      grid-template-columns: 1fr;
      gap: 0;
    }

    .form-actions {
      flex-direction: column;

      .el-button {
        width: 100%;
      }
    }
  }
}

// 表单样式优化
:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

:deep(.el-input__inner) {
  border-radius: 6px;
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-input.is-disabled .el-input__inner) {
  background-color: #f5f7fa;
  border-color: #e4e7ed;
  color: #c0c4cc;
}
</style>