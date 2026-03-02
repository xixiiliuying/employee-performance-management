<template>
  <div class="security-settings">
    <el-card header="修改密码">
      <el-form
          :model="passwordForm"
          :rules="passwordRules"
          ref="passwordFormRef"
          label-width="120px"
          class="password-form"
      >
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input
              v-model="passwordForm.oldPassword"
              type="password"
              placeholder="请输入当前密码"
              show-password
              clearable
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="请输入新密码"
              show-password
              clearable
          />
          <div class="password-tips">
            <p>密码要求：</p>
            <ul>
              <li :class="{ 'valid': isLengthValid }">至少6位字符</li>
              <li :class="{ 'valid': hasUpperCase }">包含大写字母</li>
              <li :class="{ 'valid': hasLowerCase }">包含小写字母</li>
              <li :class="{ 'valid': hasNumber }">包含数字</li>
            </ul>
          </div>
        </el-form-item>

        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password
              clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              @click="handleChangePassword"
              :loading="loading"
              :disabled="!isFormValid"
          >
            {{ loading ? '修改中...' : '确认修改' }}
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import API from '@/utils/api'

export default {
  name: 'SecuritySettings',
  data() {
    return {
      passwordForm: {
        oldPassword: '',  // 修改：currentPassword -> oldPassword
        newPassword: '',
        confirmPassword: ''
      },
      loading: false,
      passwordRules: {
        oldPassword: [  // 修改：currentPassword -> oldPassword
          { required: true, message: '请输入当前密码', trigger: 'blur' },
          { min: 1, message: '当前密码不能为空', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
          { validator: this.validatePasswordStrength, trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          { validator: this.validateConfirmPassword, trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    // 密码强度验证
    isLengthValid() {
      return this.passwordForm.newPassword.length >= 6
    },
    hasUpperCase() {
      return /[A-Z]/.test(this.passwordForm.newPassword)
    },
    hasLowerCase() {
      return /[a-z]/.test(this.passwordForm.newPassword)
    },
    hasNumber() {
      return /[0-9]/.test(this.passwordForm.newPassword)
    },
    isPasswordStrong() {
      return this.isLengthValid && this.hasUpperCase && this.hasLowerCase && this.hasNumber
    },
    isFormValid() {
      return this.passwordForm.oldPassword &&  // 修改：currentPassword -> oldPassword
          this.passwordForm.newPassword &&
          this.passwordForm.confirmPassword &&
          this.isPasswordStrong
    }
  },
  methods: {
    // 验证密码强度
    validatePasswordStrength(rule, value, callback) {
      if (!value) {
        callback(new Error('请输入新密码'))
        return
      }

      if (value.length < 6) {
        callback(new Error('密码长度不能少于6位'))
        return
      }

      if (!/[A-Z]/.test(value)) {
        callback(new Error('密码必须包含至少一个大写字母'))
        return
      }

      if (!/[a-z]/.test(value)) {
        callback(new Error('密码必须包含至少一个小写字母'))
        return
      }

      if (!/[0-9]/.test(value)) {
        callback(new Error('密码必须包含至少一个数字'))
        return
      }

      callback()
    },

    // 验证确认密码
    validateConfirmPassword(rule, value, callback) {
      if (!value) {
        callback(new Error('请再次输入新密码'))
      } else if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    },

    async handleChangePassword() {
      try {
        // 手动触发表单验证
        const valid = await this.$refs.passwordFormRef.validate()
        if (!valid) {
          this.$message.warning('请完善表单信息')
          return
        }

        this.loading = true

        // 修改：调用正确的接口和字段名
        const { data } = await this.$http.post(API.url.user.changePassword, {
          oldPassword: this.passwordForm.oldPassword,  // 修改：currentPassword -> oldPassword
          newPassword: this.passwordForm.newPassword
        })

        if (data && data.code === 0) {
          this.$message.success('密码修改成功')
          this.resetForm()
          // 可选：密码修改成功后强制重新登录
          this.$confirm('密码修改成功，是否重新登录？', '提示', {
            confirmButtonText: '重新登录',
            cancelButtonText: '稍后',
            type: 'success'
          }).then(() => {
            this.$router.push('/login')
          }).catch(() => {})
        } else {
          this.$message.error(data.msg || '密码修改失败')
        }
      } catch (error) {
        console.error('修改密码失败:', error)
        if (error.response && error.response.status === 401) {
          this.$message.error('当前密码错误')
        } else if (error.response && error.response.status === 400) {
          this.$message.error('新密码不符合要求')
        } else {
          this.$message.error('修改密码失败，请重试')
        }
      } finally {
        this.loading = false
      }
    },

    resetForm() {
      if (this.$refs.passwordFormRef) {
        this.$refs.passwordFormRef.resetFields()
      }
    }
  }
}
</script>

<style scoped lang="scss">
.security-settings {
  max-width: 600px;
  margin: 0 auto;
}

.password-form {
  padding: 20px 0;
}

.password-tips {
  margin-top: 8px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 4px;
  font-size: 12px;

  p {
    margin: 0 0 8px 0;
    color: #606266;
    font-weight: 500;
  }

  ul {
    margin: 0;
    padding-left: 16px;
    color: #909399;

    li {
      margin-bottom: 4px;

      &.valid {
        color: #67c23a;

        &::before {
          content: "✓ ";
          color: #67c23a;
        }
      }

      &::before {
        content: "○ ";
        color: #909399;
      }
    }
  }
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-input__inner) {
  border-radius: 6px;
}

:deep(.el-button) {
  border-radius: 6px;
  min-width: 100px;
}

// 响应式设计
@media (max-width: 768px) {
  .security-settings {
    max-width: 100%;
    padding: 0 10px;
  }

  .password-form {
    :deep(.el-form-item__label) {
      width: 100px !important;
    }
  }
}
</style>