<template>
  <div class="login-page">
    <div class="shell">
      <!-- 注册容器 -->
      <div class="container a-container" :class="{ 'is-txl': isLogin }">
        <form class="form" @submit.prevent="register">
          <h2 class="form_title title">注册</h2>
          <div class="form_icons">
            <i class="icon icon-user"></i>
          </div>
          <span class="form_span">请输入注册信息</span>

          <input type="text" class="form_input" placeholder="员工工号" v-model="registerForm.sid" required>
          <input type="text" class="form_input" placeholder="员工姓名" v-model="registerForm.sname" required>
          <input type="password" class="form_input" placeholder="设置密码" v-model="registerForm.password" required>

          <select class="form_input" v-model="registerForm.gender" style="appearance: none;" required>
            <option value="">请选择性别</option>
            <option value="男">男</option>
            <option value="女">女</option>
          </select>

          <!-- 🔥 简化部门选择框，移除加载状态 -->
          <select class="form_input" v-model="registerForm.deptId" style="appearance: none;" required>
            <option value="">请选择部门</option>
            <option v-for="item in departmentOptions" :key="item.id" :value="item.id">
              {{ item.dname || item.Dname }}
            </option>
          </select>

          <!-- 🔥 简化岗位选择框，移除加载状态 -->
          <select class="form_input" v-model="registerForm.jobId" style="appearance: none;" required>
            <option value="">请选择岗位</option>
            <option v-for="item in jobOptions" :key="item.id" :value="item.id">
              {{ item.jname || item.Jname }}
            </option>
          </select>

          <input type="text" class="form_input" placeholder="联系电话" v-model="registerForm.phone">
          <input type="text" class="form_input" placeholder="邮箱" v-model="registerForm.email">

          <button type="submit" class="form_button button submit">注册账号</button>
        </form>
      </div>

      <!-- 登录容器 -->
      <div class="container b-container" :class="{ 'is-txl': isLogin, 'is-z': isLogin }">
        <form class="form" @submit.prevent="login">
          <h2 class="form_title title">登录</h2>
          <div class="form_icons">
            <i class="icon icon-lock"></i>
          </div>
          <span class="form_span">员工绩效考核管理系统</span>
          <input type="text" class="form_input" placeholder="员工工号" v-model="loginForm.username" required>
          <input type="password" class="form_input" placeholder="登录密码" v-model="loginForm.password" required>

          <div class="login-tips">
            <p>系统自动识别您的身份权限</p>
            <p>请输入您的工号登录</p>
          </div>

          <a class="form_link" @click="forgotPassword">忘记密码？</a>
          <button type="submit" class="form_button button submit">立即登录</button>
        </form>
      </div>

      <!-- 切换容器 -->
      <div class="switch" :class="{ 'is-txr': isLogin }">
        <div class="switch_circle" :class="{ 'is-txr': isLogin }"></div>
        <div class="switch_circle switch_circle-t" :class="{ 'is-txr': isLogin }"></div>

        <div class="switch_container" :class="{ 'is-hidden': isLogin }">
          <h2 class="switch_title title">欢迎回来！</h2>
          <p class="switch_description description">已有员工账号？立即登录系统</p>
          <button class="switch_button button switch-btn" @click="toggleForm">账号登录</button>
        </div>

        <div class="switch_container" :class="{ 'is-hidden': !isLogin }">
          <h2 class="switch_title title">新员工？</h2>
          <p class="switch_description description">注册账号加入我们的团队</p>
          <button class="switch_button button switch-btn" @click="toggleForm">注册账号</button>
        </div>
      </div>
    </div>
    <!-- 忘记密码弹窗 -->
    <el-dialog
        title="重置密码"
        :visible.sync="resetDialogVisible"
        width="400px"
        destroy-on-close
    >
      <el-form :model="resetForm" label-width="90px">

        <el-form-item label="手机号">
          <el-input v-model="resetForm.phone" placeholder="请输入注册时的手机号"></el-input>
        </el-form-item>

        <el-form-item label="验证码">
          <el-row :gutter="10">
            <el-col :span="14">
              <el-input v-model="resetForm.code" placeholder="请输入短信验证码"></el-input>
            </el-col>
            <el-col :span="10">
              <el-button
                  :disabled="codeSending"
                  @click="sendSmsCode"
                  type="primary"
                  style="width: 100%;"
              >
                {{ codeSending ? countdown + ' 秒' : '获取验证码' }}
              </el-button>
            </el-col>
          </el-row>
        </el-form-item>

        <el-form-item label="新密码">
          <el-input v-model="resetForm.newPassword" show-password placeholder="请输入新密码"></el-input>
        </el-form-item>

      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="resetDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmResetPassword">确认修改</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'NewLogin',
  data() {
    return {
      /* 忘记密码弹窗 */
      resetDialogVisible: false,
      resetForm: {
        phone: "",
        code: "",
        newPassword: ""
      },
      codeSending: false,
      countdown: 60,
      isLogin: true,
      loginLoading: false,
      registerLoading: false,
      departmentOptions: this.getDefaultDepartments(),
      jobOptions: this.getDefaultJobs(),
      loginForm: {
        username: '',
        password: '',
      },
      registerForm: {
        sid: "",
        sname: "",
        password: "",
        gender: "",
        phone: "",
        email: "",
        deptId: "",
        jobId: "",
        role: "staff"
      }
    }
  },
  mounted() {
    const savedUsername = this.$storage ? this.$storage.get("lastUsername") : null;
    if (savedUsername) {
      this.loginForm.username = savedUsername;
    }

    // 🔥 注释掉接口调用，直接使用默认数据
    // this.loadDepartmentOptions();
    // this.loadJobOptions();
  },
  methods: {
    // 切换登录/注册表单
    toggleForm() {
      this.isLogin = !this.isLogin;
    },

    // 🔥 移除复杂的加载方法，直接使用默认数据
    // 默认部门数据
    getDefaultDepartments() {
      return [
        { id: 1, dname: '技术部' },
        { id: 2, dname: '人力资源部' },
        { id: 3, dname: '财务部' },
        { id: 4, dname: '市场部' },
        { id: 5, dname: '产品部' },
        { id: 6, dname: '设计部' }
      ];
    },

    // 默认岗位数据
    getDefaultJobs() {
      return [
        { id: 1, jname: '前端开发工程师' },
        { id: 2, jname: '后端开发工程师' },
        { id: 3, jname: 'HR专员' },
        { id: 4, jname: '招聘经理' },
        { id: 5, jname: '财务会计' },
        { id: 6, jname: '市场专员' },
        { id: 7, jname: '产品经理' },
        { id: 8, jname: 'UI设计师' }
      ];
    },

    async login() {
      // 员工号格式验证
      if (!this.loginForm.username) {
        this.$message.error("请输入员工工号");
        return;
      }

      const sidReg = /^\d{4,10}$/;
      if (!sidReg.test(this.loginForm.username)) {
        this.$message.error("员工号必须为4-10位数字");
        return;
      }

      if (!this.loginForm.password) {
        this.$message.error("请输入密码");
        return;
      }

      this.loginLoading = true;

      try {
        const formData = new URLSearchParams();
        formData.append('username', this.loginForm.username);
        formData.append('password', this.loginForm.password);

        const { data } = await this.$http({
          url: '/users/login',
          method: "post",
          data: formData,
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          }
        });

        if (data && data.code === 0) {
          this.$message.success("登录成功");

          if (this.$storage) {
            this.$storage.set("Token", data.token);
            this.$storage.set("sessionTable", "users");
            this.$storage.set("adminName", this.loginForm.username);
            this.$storage.set("lastUsername", this.loginForm.username);

            if (data.role) {
              this.$storage.set("role", data.role);
            }

            if (data.userId) {
              this.$storage.set("userId", data.userId);
            }

            console.log('登录成功，用户角色:', data.role);
          }

          setTimeout(() => {
            this.$router.replace({ path: "/index/" });
          }, 500);
        } else {
          this.$message.error(data.msg || "登录失败");
        }
      } catch (error) {
        console.error("登录请求失败:", error);
        this.$message.error("登录失败");
      } finally {
        this.loginLoading = false;
      }
    },

    // 注册方法
    register() {
      // 表单验证
      if(!this.registerForm.sid){
        this.$message.error("员工号不能为空");
        return;
      }
      if(!this.registerForm.sname){
        this.$message.error("员工姓名不能为空");
        return;
      }
      if(!this.registerForm.password){
        this.$message.error("密码不能为空");
        return;
      }
      if(!this.registerForm.gender){
        this.$message.error("请选择性别");
        return;
      }
      if(!this.registerForm.deptId){
        this.$message.error("请选择部门");
        return;
      }
      if(!this.registerForm.jobId){
        this.$message.error("请选择岗位");
        return;
      }
      if(this.registerForm.phone && (!this.$validate.isMobile(this.registerForm.phone))){
        this.$message.error("联系电话应输入手机格式");
        return;
      }
      if(this.registerForm.email && (!this.$validate.isEmail(this.registerForm.email))){
        this.$message.error("邮箱应输入邮件格式");
        return;
      }

      const loading = this.$loading({
        lock: true,
        text: '注册中...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      });

      this.$http({
        url: '/users/register',
        method: "post",
        data: this.registerForm
      }).then(({ data }) => {
        loading.close();
        if (data && data.code === 0) {
          this.$message({
            message: "注册成功",
            type: "success",
            duration: 1500,
            onClose: () => {
              this.isLogin = true;
              this.loginForm.username = this.registerForm.sid;
              this.resetRegisterForm();
            }
          });
        } else {
          this.$message.error(data.msg);
        }
      }).catch(error => {
        loading.close();
        console.error("注册失败:", error);
        this.$message.error("注册失败，请稍后重试");
      });
    },

    // 重置注册表单
    resetRegisterForm() {
      this.registerForm = {
        sid: "",
        sname: "",
        password: "",
        gender: "",
        phone: "",
        email: "",
        deptId: "",
        jobId: "",
        role: "staff"
      };
    },
    forgotPassword() {
      this.resetDialogVisible = true;
    },

    /* 发送验证码 */
    async sendSmsCode() {
      if (!this.resetForm.phone) {
        return this.$message.error("请输入手机号");
      }

      if (!this.$validate.isMobile(this.resetForm.phone)) {
        return this.$message.error("手机号格式不正确");
      }

      this.codeSending = true;

      try {
        const { data } = await this.$http({
          url: "/users/sendResetCode",
          method: "post",
          data: { phone: this.resetForm.phone }
        });

        if (data.code === 0) {
          this.$message.success("验证码发送成功");

          /* 倒计时 */
          this.countdown = 60;
          const timer = setInterval(() => {
            this.countdown--;
            if (this.countdown <= 0) {
              clearInterval(timer);
              this.codeSending = false;
            }
          }, 1000);

        } else {
          this.$message.error(data.msg || "验证码发送失败");
          this.codeSending = false;
        }
      } catch (e) {
        this.$message.error("发送失败");
        this.codeSending = false;
      }
    },

    /* 确认重置密码 */
    async confirmResetPassword() {
      if (!this.resetForm.phone) return this.$message.error("手机号不能为空");
      if (!this.resetForm.code) return this.$message.error("请输入验证码");
      if (!this.resetForm.newPassword) return this.$message.error("请输入新密码");

      try {
        const { data } = await this.$http({
          url: "/users/resetPassword",
          method: "post",
          data: this.resetForm
        });

        if (data.code === 0) {
          this.$message.success("密码已重置，请重新登录");
          this.resetDialogVisible = false;
        } else {
          this.$message.error(data.msg);
        }
      } catch (e) {
        this.$message.error("重置失败，请稍后再试");
      }
    }
  }
}
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  user-select: none;
}

.login-page {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 19px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  color: #a0a5a8;
  font-family: 'Arial', sans-serif;
}

.shell {
  position: relative;
  width: 1000px;
  min-width: 1000px;
  min-height: 600px;
  height: 600px;
  padding: 25px;
  background-color: #ecf0f3;
  box-shadow: 10px 10px 10px #d1d9e6, -10px -10px 10px #f9f9f9;
  border-radius: 12px;
  overflow: hidden;
}

/* 设置响应式 */
@media (max-width: 1200px) {
  .shell {
    transform: scale(0.7);
  }
}

@media (max-width: 1000px) {
  .shell {
    transform: scale(0.6);
  }
}

@media (max-width: 800px) {
  .shell {
    transform: scale(0.5);
  }
}

@media (max-width: 600px) {
  .shell {
    transform: scale(0.4);
  }
}

.container {
  display: flex;
  justify-content: center;
  align-items: center;
  position: absolute;
  top: 0;
  width: 600px;
  height: 100%;
  padding: 25px;
  background-color: #ecf0f3;
  transition: 1.25s;
}

.form {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  width: 100%;
  height: 100%;
  overflow-y: auto;
  padding: 20px 0;
}

.form_icons {
  margin: 10px 0;
}

.icon {
  font-size: 30px;
  color: #4B70E2;
  margin: 0 10px;
}

.form_input {
  width: 350px;
  height: 40px;
  margin: 8px 0;
  padding-left: 25px;
  font-size: 16px;
  letter-spacing: 0.15px;
  border: none;
  outline: none;
  background-color: #ecf0f3;
  transition: 0.25s ease;
  border-radius: 8px;
  box-shadow: inset 2px 2px 4px #d1d9e6, inset -2px -2px 4px #f9f9f9;
  font-family: inherit;
}

.form_input:focus {
  box-shadow: inset 4px 4px 4px #d1d9e6, inset -4px -4px 4px #f9f9f9;
}

.form_span {
  margin-top: 10px;
  margin-bottom: 15px;
  color: #666;
  font-size: 16px;
  text-align: center;
}

.form_link {
  color: #4B70E2;
  font-size: 14px;
  margin-top: 15px;
  border-bottom: 1px solid #a0a5a8;
  line-height: 2;
  cursor: pointer;
  transition: 0.3s;
}

.form_link:hover {
  color: #3651b3;
}

.title {
  font-size: 40px;
  font-weight: 700;
  line-height: 2;
  color: #181818;
  letter-spacing: 2px;
  text-align: center;
}

.description {
  font-size: 20px;
  letter-spacing: 0.25px;
  text-align: center;
  line-height: 1.6;
  color: #666;
  margin: 10px 0;
}

.button {
  width: 180px;
  height: 45px;
  border-radius: 25px;
  margin-top: 20px;
  font-weight: 700;
  font-size: 19px;
  letter-spacing: 1.15px;
  background-color: #4B70E2;
  color: #f9f9f9;
  box-shadow: 8px 8px 16px #d1d9e6, -8px -8px 16px #f9f9f9;
  border: none;
  outline: none;
  cursor: pointer;
  transition: 0.3s;
}

.button:hover {
  background-color: #3651b3;
  transform: translateY(-2px);
}

.a-container {
  z-index: 100;
  left: calc(100% - 600px);
}

.b-container {
  left: calc(100% - 600px);
  z-index: 0;
}

.switch {
  display: flex;
  justify-content: center;
  align-items: center;
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  width: 400px;
  padding: 50px;
  z-index: 200;
  transition: 1.25s;
  background-color: #ecf0f3;
  overflow: hidden;
  box-shadow: 4px 4px 10px #d1d9e6, -4px -4px 10px #d1d9e6;
}

.switch_circle {
  position: absolute;
  width: 500px;
  height: 500px;
  border-radius: 50%;
  background-color: #ecf0f3;
  box-shadow: inset 8px 8px 12px #b8bec7, inset -8px -8px 12px #fff;
  bottom: -60%;
  left: -60%;
  transition: 1.25s;
}

.switch_circle-t {
  top: -30%;
  left: 60%;
  width: 300px;
  height: 300px;
}

.switch_container {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  position: absolute;
  width: 400px;
  padding: 50px 55px;
  transition: 1.25s;
}

.switch_button {
  cursor: pointer;
}

.switch_button:hover,
.submit:hover {
  box-shadow: 6px 6px 10px #d1d9e6, -6px -6px 10px #f9f9f9;
  transform: scale(0.985);
  transition: 0.25s;
}

.switch_button:active,
.switch_button:focus {
  box-shadow: 2px 2px 6px #d1d9e6, -2px -2px 6px #f9f9f9;
  transform: scale(0.97);
  transition: 0.25s;
}

.is-txr {
  left: calc(100% - 400px);
  transition: 1.25s;
  transform-origin: left;
}

.is-txl {
  left: 0;
  transition: 1.25s;
  transform-origin: right;
}

.is-z {
  z-index: 200;
  transition: 1.25s;
}

.is-hidden {
  visibility: hidden;
  opacity: 0;
  position: absolute;
  transition: 1.25s;
}

.is-gx {
  animation: is-gx 1.25s;
}

@keyframes is-gx {
  0%, 10%, 100% {
    width: 400px;
  }
  30%, 50% {
    width: 500px;
  }
}

/* 角色选择样式 */
.role-section {
  margin: 15px 0;
  text-align: center;
}

.role-label {
  display: block;
  margin-bottom: 10px;
  color: #666;
  font-size: 14px;
}

.role-options {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.role-option {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.role-radio {
  margin-right: 5px;
}

.role-text {
  color: #666;
  font-size: 14px;
}

.role-radio:checked + .role-text {
  color: #4B70E2;
  font-weight: bold;
}

/* 滚动条样式 */
.form::-webkit-scrollbar {
  width: 6px;
}

.form::-webkit-scrollbar-track {
  background: #ecf0f3;
}

.form::-webkit-scrollbar-thumb {
  background: #d1d9e6;
  border-radius: 3px;
}

.form::-webkit-scrollbar-thumb:hover {
  background: #b8bec7;
}
</style>