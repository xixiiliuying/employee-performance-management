<template>
  <div class="staff-dashboard">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>员工工作台</h1>
      <p>欢迎回来，{{ userInfo.sname || '同事' }}！今天是 {{ currentDate }}</p>
    </div>

    <!-- 主要功能模块 -->
    <el-row :gutter="20" class="main-modules">
      <!-- 考勤模块 -->
      <el-col :span="8">
        <el-card class="module-card attendance-module" shadow="hover" @click.native="$router.push('/index/attendance')">
          <div class="module-header">
            <div class="module-icon">
              <i class="el-icon-time"></i>
            </div>
            <div class="module-title">
              <h3>考勤打卡</h3>
              <p>今日考勤状态</p>
            </div>
          </div>
          <div class="module-content">
            <div class="attendance-status">
              <el-tag :type="todayStatus.type" size="medium">{{ todayStatus.text }}</el-tag>
            </div>
            <div class="attendance-times">
              <div class="time-item">
                <span class="time-label">上班</span>
                <span class="time-value">{{ todayAttendance.clockInTime || '--:--' }}</span>
              </div>
              <div class="time-item">
                <span class="time-label">下班</span>
                <span class="time-value">{{ todayAttendance.clockOutTime || '--:--' }}</span>
              </div>
            </div>
          </div>
          <div class="module-footer">
            <span class="action-text">点击查看考勤详情</span>
            <i class="el-icon-arrow-right"></i>
          </div>
        </el-card>
      </el-col>

      <!-- 请假模块 -->
      <el-col :span="8">
        <el-card class="module-card leave-module" shadow="hover" @click.native="$router.push('/index/leave')">
          <div class="module-header">
            <div class="module-icon">
              <i class="el-icon-date"></i>
            </div>
            <div class="module-title">
              <h3>请假管理</h3>
              <p>假期申请与审批</p>
            </div>
          </div>
          <div class="module-content">
            <div class="leave-stats">
              <div class="stat-item">
                <div class="stat-value">{{ leaveStats.pendingCount || 0 }}</div>
                <div class="stat-label">待审批</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ leaveStats.totalCount || 0 }}</div>
                <div class="stat-label">总申请</div>
              </div>
            </div>
            <div class="leave-types">
              <el-tag size="small" type="success">年假</el-tag>
              <el-tag size="small" type="warning">事假</el-tag>
              <el-tag size="small" type="danger">病假</el-tag>
              <el-tag size="small" type="info">其他</el-tag>
            </div>
          </div>
          <div class="module-footer">
            <span class="action-text">点击申请请假</span>
            <i class="el-icon-arrow-right"></i>
          </div>
        </el-card>
      </el-col>

      <!-- 薪资模块 -->
      <el-col :span="8">
        <el-card class="module-card salary-module" shadow="hover" @click.native="$router.push('/index/salary')">
          <div class="module-header">
            <div class="module-icon">
              <i class="el-icon-money"></i>
            </div>
            <div class="module-title">
              <h3>薪资查询</h3>
              <p>工资明细与统计</p>
            </div>
          </div>
          <div class="module-content">
            <div class="salary-preview">
              <div class="salary-amount" v-if="latestSalary && (latestSalary.totalAmount || latestSalary.amount)">
                <span class="currency">¥</span>
                <span class="amount">{{ formatSalary(latestSalary.totalAmount || latestSalary.amount) }}</span>
              </div>
              <div class="salary-amount" v-else>
                <span class="currency">¥</span>
                <span class="amount">0</span>
              </div>
              <div class="salary-period" v-if="latestSalary && (latestSalary.salaryMonth || latestSalary.month)">
                最近发放：{{ formatSalaryPeriod(latestSalary.salaryMonth || latestSalary.month) }}
              </div>
              <div class="salary-period" v-else>
                暂无薪资记录
              </div>
            </div>
            <div class="salary-detail" v-if="latestSalary">
              <div class="detail-item">
                <span>基本工资</span>
                <span>¥{{ formatSalary(latestSalary.baseSalary) }}</span>
              </div>
              <div class="detail-item">
                <span>绩效奖金</span>
                <span>¥{{ formatSalary(latestSalary.performanceBonus || latestSalary.bonus) }}</span>
              </div>
            </div>
          </div>
          <div class="module-footer">
            <span class="action-text">点击查看薪资详情</span>
            <i class="el-icon-arrow-right"></i>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 底部信息区域 -->
    <el-row :gutter="20" class="bottom-section">
      <!-- 最近公告 -->
      <el-col :span="12">
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>最新公告</span>
              <el-button type="text" @click="$router.push('/index/notice')">查看全部</el-button>
            </div>
          </template>
          <div class="notice-list">
            <div
                v-for="notice in recentNotices"
                :key="notice.id"
                class="notice-item"
                @click="viewNoticeDetail(notice)"
            >
              <div class="notice-badge">
                <i class="el-icon-chat-dot-round"></i>
              </div>
              <div class="notice-content">
                <div class="notice-title">{{ notice.title }}</div>
                <div class="notice-meta">
                  <span class="notice-time">{{ formatDate(notice.createTime) }}</span>
                  <el-tag v-if="isNewNotice(notice.createTime)" type="danger" size="mini">新</el-tag>
                </div>
              </div>
            </div>
          </div>
          <div v-if="recentNotices.length === 0" class="empty-state">
            <i class="el-icon-bell"></i>
            <p>暂无新公告</p>
          </div>
        </el-card>
      </el-col>

      <!-- 个人信息 -->
      <el-col :span="12">
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
              <el-button type="text" @click="$router.push('/index/center')">编辑信息</el-button>
            </div>
          </template>
          <div class="user-profile">
            <div class="avatar-section">
              <el-avatar :size="60" :src="userInfo.avatar" icon="el-icon-user-solid"></el-avatar>
              <div class="user-basic">
                <div class="user-name">{{ userInfo.sname || '未设置' }}</div>
                <div class="user-position">{{ getJobTitle(userInfo.jobId) || '员工' }}</div>
              </div>
            </div>
            <div class="user-details">
              <div class="detail-row">
                <i class="el-icon-office-building"></i>
                <span>部门：{{ getDepartmentName(userInfo.deptId) || '未设置' }}</span>
              </div>
              <div class="detail-row">
                <i class="el-icon-mobile-phone"></i>
                <span>电话：{{ userInfo.phone || '未设置' }}</span>
              </div>
              <div class="detail-row">
                <i class="el-icon-date"></i>
                <span>入职：{{ formatDate(userInfo.hireDate) || '未设置' }}</span>
              </div>
              <div class="detail-row">
                <i class="el-icon-user"></i>
                <span>工号：{{ userInfo.sid || '未设置' }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import API from '@/utils/api'

export default {
  name: 'StaffIndexPage',
  data() {
    return {
      loading: {
        attendance: false,
        leave: false,
        salary: false,
        notices: false
      },
      hasError: {
        attendance: false,
        leave: false,
        salary: false,
        notices: false
      },
      // 统计数据
      attendanceStats: {
        workDays: 0,
        lateCount: 0,
        earlyLeaveCount: 0
      },
      leaveStats: {
        pendingCount: 0,
        totalCount: 0
      },
      // 今日考勤
      todayAttendance: {
        clockInTime: '',
        clockOutTime: '',
        status: ''
      },
      todayStatus: {
        type: 'warning',
        text: '未打卡'
      },
      // 最新公告
      recentNotices: [],
      // 用户信息
      userInfo: {},
      // 部门列表
      departmentList: [],
      // 岗位列表
      jobList: [],
      // 最新薪资
      latestSalary: null,
      currentDate: ''
    }
  },
  mounted() {
    this.setCurrentDate();
    this.loadDepartmentList();
    this.loadJobList();
    this.loadUserInfo();
  },
  methods: {
    // 设置当前日期
    setCurrentDate() {
      const now = new Date();
      const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' };
      this.currentDate = now.toLocaleDateString('zh-CN', options);
    },

    // 加载部门列表
    async loadDepartmentList() {
      try {
        const { data } = await this.$http.get(API.url.option.department);
        if (data && data.code === 0) {
          this.departmentList = this.extractList(data.data);
        } else {
          this.departmentList = [];
        }
      } catch (error) {
        console.error('加载部门列表失败:', error);
        this.departmentList = [];
      }
    },

    // 加载岗位列表
    async loadJobList() {
      try {
        const { data } = await this.$http.get(API.url.option.job);
        if (data && data.code === 0) {
          this.jobList = this.extractList(data.data);
        } else {
          this.jobList = [];
        }
      } catch (error) {
        console.error('加载岗位列表失败:', error);
        this.jobList = [];
      }
    },

    // 通用提取 list
    extractList(data) {
      if (!data) return [];
      if (Array.isArray(data)) return data;
      if (data.list && Array.isArray(data.list)) return data.list;
      if (data.data && Array.isArray(data.data)) return data.data;
      if (data.records && Array.isArray(data.records)) return data.records;
      return [];
    },

    // 获取部门名称
    getDepartmentName(deptId) {
      if (!deptId || !Array.isArray(this.departmentList)) return '';
      const dept = this.departmentList.find(item => String(item.id) === String(deptId));
      return dept ? dept.dname || dept.Dname || dept.name : '';
    },

    // 获取岗位名称
    getJobTitle(jobId) {
      if (!jobId || !Array.isArray(this.jobList)) return '';
      const job = this.jobList.find(item => String(item.id) === String(jobId));
      return job ? job.jname || job.name : '';
    },

    // 加载用户信息
    async loadUserInfo() {
      try {
        const { data } = await this.$http.get(API.url.user.session);

        if (data && data.code === 0) {
          this.userInfo = data.data;
          // 用户信息加载完成后加载其他数据
          this.loadDashboardData();
        } else {
          console.error('获取用户信息失败:', data);
          this.$message.error('获取用户信息失败');
          // 即使获取用户信息失败，也尝试加载其他数据
          this.loadDashboardData();
        }
      } catch (error) {
        console.error('获取用户信息失败:', error);
        this.$message.error('获取用户信息失败');
        // 即使获取用户信息失败，也尝试加载其他数据
        this.loadDashboardData();
      }
    },

    // 加载仪表盘数据
    async loadDashboardData() {
      console.log('开始加载仪表盘数据');
      try {
        await Promise.all([
          this.loadTodayAttendance(),
          this.loadAttendanceStatistics(),
          this.loadLeaveStatistics(),
          this.loadLatestSalary(),
          this.loadRecentNotices()
        ]);
        console.log('仪表盘数据加载完成');
      } catch (error) {
        console.error('加载仪表盘数据失败:', error);
      }
    },

    async loadTodayAttendance() {
      if (this.loading.attendance) return;

      this.loading.attendance = true;
      this.hasError.attendance = false;

      try {
        const today = new Date().toISOString().split('T')[0];

        console.log('开始调用考勤记录接口，日期:', today);

        // 使用正确的参数格式
        const { data } = await this.$http.get(API.url.attendance.myRecords, {
          params: {
            date: today,
            page: 1,
            limit: 31
          }
        });

        console.log('考勤记录接口完整响应:', data);

        if (data && data.code === 0) {
          // 关键修复：正确提取数据列表
          let records = [];

          // 处理不同的数据结构
          if (data.data && Array.isArray(data.data)) {
            records = data.data;
          } else if (data.page && data.page.list && Array.isArray(data.page.list)) {
            records = data.page.list; // 考勤页面使用的结构
          } else if (data.list && Array.isArray(data.list)) {
            records = data.list;
          }

          console.log('提取的考勤记录数据:', records);
          console.log('今日日期:', today);

          // 查找今日考勤记录
          const todayRecord = records.find(record => {
            // 关键修复：正确提取日期字段
            let recordDate = '';

            if (record.date) {
              recordDate = record.date.split(' ')[0]; // 考勤页面使用的字段
            } else if (record.workDate) {
              recordDate = record.workDate.split(' ')[0];
            } else if (record.checkInTime) {
              recordDate = record.checkInTime.split(' ')[0];
            }

            console.log('记录日期:', recordDate, '完整记录:', record);
            return recordDate === today;
          });

          if (todayRecord) {
            console.log('找到今日考勤记录:', todayRecord);

            // 关键修复：正确映射字段名
            this.todayAttendance = {
              clockInTime: todayRecord.checkInTime ? this.formatTime(todayRecord.checkInTime) : '',
              clockOutTime: todayRecord.checkOutTime ? this.formatTime(todayRecord.checkOutTime) : '',
              status: todayRecord.status || 'NOT_CLOCKED'
            };

            console.log('映射后的今日考勤:', this.todayAttendance);
            this.updateAttendanceStatus();
          } else {
            console.log('未找到今日考勤记录');
            this.setDefaultAttendance();
          }
        } else {
          console.error('获取考勤记录API返回错误:', data);
          this.setDefaultAttendance();
        }
      } catch (error) {
        console.error('获取今日考勤失败:', error);
        console.error('错误详情:', error.response?.data || error.message);
        this.hasError.attendance = true;
        this.setDefaultAttendance();
      } finally {
        this.loading.attendance = false;
      }
    },

    // 从记录中提取日期
    extractDateFromRecord(record) {
      if (record.workDate) return record.workDate.split(' ')[0];
      if (record.checkInTime) return record.checkInTime.split(' ')[0];
      if (record.createTime) return record.createTime.split(' ')[0];
      if (record.date) return record.date.split(' ')[0];
      return '';
    },

    // 更新考勤状态
    updateAttendanceStatus() {
      const { clockInTime, clockOutTime, status } = this.todayAttendance;

      if (clockInTime && clockOutTime) {
        this.todayStatus = { type: 'success', text: '已完成打卡' };
      } else if (clockInTime) {
        this.todayStatus = { type: 'warning', text: '已上班打卡' };
      } else if (status === 'LATE') {
        this.todayStatus = { type: 'danger', text: '迟到' };
      } else if (status === 'ABSENT') {
        this.todayStatus = { type: 'danger', text: '缺勤' };
      } else {
        this.todayStatus = { type: 'warning', text: '未打卡' };
      }
    },

    // 设置默认考勤状态
    setDefaultAttendance() {
      this.todayAttendance = {
        clockInTime: '',
        clockOutTime: '',
        status: 'NOT_CLOCKED'
      };
      this.todayStatus = { type: 'warning', text: '未打卡' };
    },

    async loadAttendanceStatistics() {
      try {
        const today = new Date();
        const year = today.getFullYear();
        const month = today.getMonth() + 1;

        console.log('开始调用考勤统计接口，参数:', { year, month });

        // 使用正确的参数格式
        const { data } = await this.$http.get(API.url.attendance.myStatistics, {
          params: {
            year: year,
            month: month
            // 或者根据后端实际需要的参数名调整
          }
        });

        console.log('考勤统计接口响应:', data);

        if (data && data.code === 0) {
          this.attendanceStats = data.data || {
            workDays: 0,
            lateCount: 0,
            earlyLeaveCount: 0
          };
          console.log('考勤统计数据:', this.attendanceStats);
        } else {
          console.error('考勤统计API返回错误:', data);
          this.attendanceStats = {
            workDays: 0,
            lateCount: 0,
            earlyLeaveCount: 0
          };
        }
      } catch (error) {
        console.error('获取考勤统计失败:', error);
        console.error('错误详情:', error.response?.data || error.message);
        this.attendanceStats = {
          workDays: 0,
          lateCount: 0,
          earlyLeaveCount: 0
        };
      }
    },

    async loadLeaveStatistics() {
      this.loading.leave = true;
      this.hasError.leave = false;

      try {
        console.log('开始调用请假申请接口');

        // 先尝试不带参数调用
        const { data } = await this.$http.get(API.url.leave.myApplications, {
          params: {
            page: 1,
            limit: 100
          }
        });

        console.log('请假申请接口完整响应:', data);

        if (data && data.code === 0) {
          // 关键修复：正确提取请假数据
          let applications = [];

          if (data.data && Array.isArray(data.data)) {
            applications = data.data;
          } else if (data.list && Array.isArray(data.list)) {
            applications = data.list;
          } else if (data.page && data.page.list && Array.isArray(data.page.list)) {
            applications = data.page.list;
          }

          console.log('提取的请假申请数据:', applications);

          // 计算统计信息
          const pendingCount = applications.filter(app => {
            const status = app.status || '';
            console.log('请假申请状态:', app.id, status);
            return status === 'PENDING' || status === '0' || status === '待审批' || status === 'pending' || status === 'PENDING_APPROVAL';
          }).length;

          this.leaveStats = {
            pendingCount,
            totalCount: applications.length
          };
          console.log('请假统计结果:', this.leaveStats);
        } else {
          console.error('获取请假记录API返回错误:', data);
          this.setDefaultLeaveStats();
        }
      } catch (error) {
        console.error('获取请假统计失败:', error);
        console.error('错误详情:', error.response?.data || error.message);
        this.hasError.leave = true;
        this.setDefaultLeaveStats();
      } finally {
        this.loading.leave = false;
      }
    },

    // 设置默认请假统计
    setDefaultLeaveStats() {
      this.leaveStats = {
        pendingCount: 0,
        totalCount: 0
      };
    },

    async loadLatestSalary() {
      if (this.loading.salary) return;

      this.loading.salary = true;
      this.hasError.salary = false;

      try {
        console.log('开始调用薪资接口');

        // 先尝试不带参数调用
        const { data } = await this.$http.get(API.url.salary.mySalary, {
          params: {
            page: 1,
            limit: 1
          }
        });

        console.log('薪资接口完整响应:', data);

        if (data && data.code === 0) {
          // 关键修复：正确提取薪资数据
          let salaryList = [];

          if (data.data && Array.isArray(data.data)) {
            salaryList = data.data;
          } else if (data.list && Array.isArray(data.list)) {
            salaryList = data.list;
          } else if (data.page && data.page.list && Array.isArray(data.page.list)) {
            salaryList = data.page.list;
          }

          console.log('提取的薪资记录数据:', salaryList);

          if (salaryList && salaryList.length > 0) {
            // 按月份排序，获取最新的薪资记录
            const sortedSalaries = salaryList.sort((a, b) => {
              const monthA = a.salaryMonth || a.month || '0';
              const monthB = b.salaryMonth || b.month || '0';
              return monthB.localeCompare(monthA);
            });

            this.latestSalary = sortedSalaries[0];
            console.log('最新薪资记录:', this.latestSalary);
          } else {
            console.log('薪资记录为空');
            this.latestSalary = null;
          }
        } else {
          console.error('获取薪资记录API返回错误:', data);
          this.latestSalary = null;
        }
      } catch (error) {
        console.error('获取薪资信息失败:', error);
        console.error('错误详情:', error.response?.data || error.message);
        this.hasError.salary = true;
        this.latestSalary = null;
      } finally {
        this.loading.salary = false;
      }
    },
    // 加载最新公告
    async loadRecentNotices() {
      if (this.loading.notices) return;

      this.loading.notices = true;
      this.hasError.notices = false;

      try {
        const { data } = await this.$http.get(API.url.notice.latest, {
          params: { count: 5 }
        });

        if (data && data.code === 0) {
          this.recentNotices = this.extractList(data.data) || [];
          console.log('最新公告:', this.recentNotices);
        } else {
          this.recentNotices = [];
        }
      } catch (error) {
        console.error('获取最新公告失败:', error);
        this.hasError.notices = true;
        this.recentNotices = [];
      } finally {
        this.loading.notices = false;
      }
    },

    // 查看公告详情
    viewNoticeDetail(notice) {
      this.$router.push({
        path: '/index/notice/detail',
        query: { id: notice.id }
      });
    },

    // 格式化时间
    formatTime(timeString) {
      if (!timeString) return '';
      try {
        // 处理各种时间格式
        if (typeof timeString === 'string') {
          if (timeString.includes('T')) {
            // ISO 格式: 2025-01-13T09:00:00
            const date = new Date(timeString);
            return date.toLocaleTimeString('zh-CN', {
              hour: '2-digit',
              minute: '2-digit',
              hour12: false
            });
          } else if (timeString.includes(':')) {
            // 时间字符串: 09:00:00 或 09:00
            const timeParts = timeString.split(':');
            return `${timeParts[0]}:${timeParts[1]}`;
          }
        }
        return timeString;
      } catch (error) {
        console.error('时间格式化错误:', error, timeString);
        return timeString;
      }
    },

    // 格式化薪资显示
    formatSalary(amount) {
      if (!amount) return '0';
      return Number(amount).toLocaleString('zh-CN');
    },

    // 格式化薪资期间
    formatSalaryPeriod(period) {
      if (!period) return '';
      // 处理格式: 202501 -> 2025年01月
      if (period.length === 6) {
        return `${period.substring(0, 4)}年${period.substring(4, 6)}月`;
      }
      return period;
    },

    // 格式化日期
    formatDate(date) {
      if (!date) return '';
      try {
        const dateObj = new Date(date);
        return dateObj.toLocaleDateString('zh-CN');
      } catch (error) {
        return date;
      }
    },

    // 判断是否为新公告（7天内）
    isNewNotice(publishDate) {
      if (!publishDate) return false;
      try {
        const publish = new Date(publishDate);
        const now = new Date();
        const diffTime = now.getTime() - publish.getTime();
        const diffDays = diffTime / (1000 * 60 * 60 * 24);
        return diffDays <= 7;
      } catch (error) {
        return false;
      }
    }
  }
}
</script>

<style scoped>
/* 样式保持不变 */
.staff-dashboard {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  margin: 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.page-header p {
  margin: 8px 0 0 0;
  color: #909399;
  font-size: 14px;
}

/* 主要模块样式 */
.main-modules {
  margin-bottom: 30px;
}

.module-card {
  border-radius: 12px;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  height: 220px;
  display: flex;
  flex-direction: column;
}

.module-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1) !important;
}

.module-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.module-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
  color: white;
}

.attendance-module .module-icon {
  background: linear-gradient(135deg, #409EFF, #66b1ff);
}

.leave-module .module-icon {
  background: linear-gradient(135deg, #67C23A, #85ce61);
}

.salary-module .module-icon {
  background: linear-gradient(135deg, #E6A23C, #ebb563);
}

.module-title h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
  font-weight: 600;
}

.module-title p {
  margin: 4px 0 0 0;
  font-size: 12px;
  color: #909399;
}

.module-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

/* 考勤模块内容 */
.attendance-status {
  margin-bottom: 15px;
}

.attendance-times {
  display: flex;
  gap: 30px;
}

.time-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.time-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.time-value {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

/* 请假模块内容 */
.leave-stats {
  display: flex;
  gap: 30px;
  margin-bottom: 15px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.leave-types {
  display: flex;
  gap: 8px;
  justify-content: center;
}

/* 薪资模块内容 */
.salary-preview {
  text-align: center;
  margin-bottom: 15px;
}

.salary-amount {
  display: flex;
  align-items: baseline;
  justify-content: center;
  margin-bottom: 8px;
}

.currency {
  font-size: 18px;
  color: #E6A23C;
  margin-right: 4px;
}

.amount {
  font-size: 28px;
  font-weight: bold;
  color: #E6A23C;
}

.salary-period {
  font-size: 12px;
  color: #909399;
}

.salary-detail {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #606266;
}

/* 模块底部 */
.module-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
  margin-top: auto;
}

.action-text {
  font-size: 12px;
  color: #909399;
}

.module-footer .el-icon-arrow-right {
  color: #c0c4cc;
  transition: color 0.3s;
}

.module-card:hover .el-icon-arrow-right {
  color: #409EFF;
}

/* 底部信息区域 */
.bottom-section {
  margin-bottom: 20px;
}

.info-card {
  border-radius: 12px;
  border: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

/* 公告列表 */
.notice-list {
  padding: 10px 0;
}

.notice-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-item:hover {
  background-color: #f5f7fa;
  border-radius: 6px;
  padding: 12px;
  margin: 0 -12px;
}

.notice-badge {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background-color: #ecf5ff;
  color: #409EFF;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
}

.notice-content {
  flex: 1;
}

.notice-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
  line-height: 1.4;
}

.notice-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.notice-time {
  font-size: 12px;
  color: #909399;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
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

/* 个人信息 */
.user-profile {
  padding: 10px 0;
}

.avatar-section {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.user-basic {
  margin-left: 15px;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.user-position {
  font-size: 14px;
  color: #909399;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-row {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #606266;
}

.detail-row i {
  margin-right: 10px;
  color: #909399;
  width: 16px;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .main-modules .el-col {
    margin-bottom: 20px;
  }

  .main-modules .el-col:last-child {
    margin-bottom: 0;
  }
}

@media (max-width: 768px) {
  .staff-dashboard {
    padding: 15px;
  }

  .main-modules .el-col,
  .bottom-section .el-col {
    margin-bottom: 20px;
  }

  .attendance-times,
  .leave-stats {
    gap: 15px;
  }
}
</style>