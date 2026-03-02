<template>
  <div class="hr-dashboard">
    <div class="page-header">
      <h1>HR工作台</h1>
      <p>人力资源管理概览 · 今天是 {{ currentDate }}</p>
    </div>

    <el-row :gutter="20" class="core-stats">
      <el-col :xs="12" :sm="6" :md="6" :lg="6">
        <el-card class="stat-card employee-card" shadow="hover" @click.native="$router.push('/index/hr/employee')">
          <div class="stat-content">
            <div class="stat-icon"><i class="el-icon-user-solid"></i></div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboardStats.totalEmployees || 0 }}</div>
              <div class="stat-label">总员工数</div>
              <div class="stat-trend" :class="getTrendClass(dashboardStats.employeeTrend)">
                <i :class="getTrendIcon(dashboardStats.employeeTrend)"></i>
                <span>{{ dashboardStats.employeeTrendText || '暂无数据' }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6" :md="6" :lg="6">
        <el-card class="stat-card attendance-card" shadow="hover" @click.native="$router.push('/index/hr/attendance-manage')">
          <div class="stat-content">
            <div class="stat-icon"><i class="el-icon-check"></i></div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboardStats.todayAttendance || 0 }}</div>
              <div class="stat-label">今日出勤</div>
              <div class="stat-trend">
                <span class="attendance-rate">出勤率 {{ dashboardStats.attendanceRate || '0%' }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6" :md="6" :lg="6">
        <el-card class="stat-card approval-card" shadow="hover" @click.native="$router.push('/index/hr/leave-manage')">
          <div class="stat-content">
            <div class="stat-icon"><i class="el-icon-document"></i></div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboardStats.pendingApproval || 0 }}</div>
              <div class="stat-label">待审批</div>
              <div class="stat-trend trend-warning" v-if="dashboardStats.pendingApproval > 0">
                <i class="el-icon-warning"></i>
                <span>需及时处理</span>
              </div>
              <div class="stat-trend" v-else>
                <span>暂无待审批</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6" :md="6" :lg="6">
        <el-card class="stat-card department-card" shadow="hover" @click.native="$router.push('/index/hr/department')">
          <div class="stat-content">
            <div class="stat-icon"><i class="el-icon-office-building"></i></div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboardStats.totalDepartments || 0 }}</div>
              <div class="stat-label">部门数量</div>
              <div class="stat-trend">
                <span class="dept-info">{{ dashboardStats.largestDept || '暂无数据' }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="main-content">
      <el-col :xs="24" :sm="16" :md="16" :lg="16">
        <el-row :gutter="20" class="function-modules">
          <el-col :xs="24" :sm="12" :md="12" :lg="12">
            <el-card class="module-card employee-module" shadow="hover" @click.native="$router.push('/index/hr/employee')">
              <div class="module-header">
                <div class="module-icon"><i class="el-icon-user-solid"></i></div>
                <div class="module-title">
                  <h3>员工管理</h3>
                  <p>员工信息与档案</p>
                </div>
              </div>
              <div class="module-content employee-stats">
                <div class="stat-item">
                  <div class="item-value">{{ employeeStats.monthJoin || 0 }}</div>
                  <div class="item-label">本月入职</div>
                </div>
                <div class="stat-item">
                  <div class="item-value">{{ employeeStats.monthLeave || 0 }}</div>
                  <div class="item-label">本月离职</div>
                </div>
              </div>
            </el-card>
          </el-col>

          <el-col :xs="24" :sm="12" :md="12" :lg="12">
            <el-card class="module-card attendance-module" shadow="hover" @click.native="$router.push('/index/hr/attendance-manage')">
              <div class="module-header">
                <div class="module-icon"><i class="el-icon-time"></i></div>
                <div class="module-title">
                  <h3>考勤管理</h3>
                  <p>考勤统计与异常</p>
                </div>
              </div>
              <div class="module-content attendance-details">
                <div class="detail-item">
                  <span class="label">异常考勤</span>
                  <span class="value detail-danger">{{ attendanceStats.abnormalCount || 0 }}人</span>
                </div>
                <div class="detail-item">
                  <span class="label">未打卡</span>
                  <span class="value">{{ attendanceStats.missingCount || 0 }}人</span>
                </div>
              </div>
            </el-card>
          </el-col>

          <el-col :xs="24" :sm="12" :md="12" :lg="12">
            <el-card class="module-card salary-module" shadow="hover" @click.native="$router.push('/index/hr/salary-manage')">
              <div class="module-header">
                <div class="module-icon"><i class="el-icon-money"></i></div>
                <div class="module-title">
                  <h3>薪资管理</h3>
                  <p>工资核算与发放</p>
                </div>
              </div>
              <div class="module-content salary-overview">
                <div class="salary-total" v-if="salaryStats.monthTotal > 0">
                  <span class="currency">¥</span>
                  <span class="amount">{{ formatSalary(salaryStats.monthTotal) }}</span>
                </div>
                <div class="salary-total" v-else-if="loading">
                  <span class="loading-text">加载中...</span>
                </div>
                <div class="salary-total" v-else>
                  <span class="currency">¥</span>
                  <span class="amount">0</span>
                </div>
                <div class="salary-period">本月应发总额</div>
              </div>
            </el-card>
          </el-col>

          <el-col :xs="24" :sm="12" :md="12" :lg="12">
            <el-card class="module-card leave-module" shadow="hover" @click.native="$router.push('/index/hr/leave-manage')">
              <div class="module-header">
                <div class="module-icon"><i class="el-icon-date"></i></div>
                <div class="module-title">
                  <h3>请假审批</h3>
                  <p>请假申请处理</p>
                </div>
              </div>
              <div class="module-content leave-stats">
                <div class="stat-item">
                  <div class="item-value stat-warning">{{ leaveStats.pendingCount || 0 }}</div>
                  <div class="item-label">待审批</div>
                </div>
                <div class="stat-item">
                  <div class="item-value">{{ leaveStats.monthApproved || 0 }}</div>
                  <div class="item-label">本月已批</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-card class="todo-section" shadow="never">
          <template #header>
            <div class="card-header">
              <span>待办事项</span>
              <el-tag size="small" type="danger">{{ pendingTasks.length }} 条</el-tag>
            </div>
          </template>
          <div class="todo-list">
            <div
                v-for="task in pendingTasks"
                :key="task.id"
                class="todo-item"
                @click="handleTaskClick(task)"
            >
              <div class="todo-badge" :class="task.type">
                <i :class="task.icon"></i>
              </div>
              <div class="todo-content">
                <div class="todo-title">{{ task.title }}</div>
                <div class="todo-meta">
                  <span class="todo-time">{{ task.time }}</span>
                  <el-tag v-if="task.urgent" type="danger" size="mini">紧急</el-tag>
                </div>
              </div>
            </div>
            <div v-if="pendingTasks.length === 0" class="empty-todo">
              <i class="el-icon-check" style="font-size: 48px; color: #67C23A; margin-bottom: 16px;"></i>
              <p>暂无待办事项</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="8" :md="8" :lg="8">
        <el-card class="info-card department-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>部门分布</span>
            </div>
          </template>
          <div class="department-chart">
            <div
                v-for="dept in departmentStats"
                :key="dept.name"
                class="department-item"
            >
              <div class="dept-info">
                <span class="dept-name">{{ dept.name }}</span>
                <span class="dept-count">{{ dept.count }}人</span>
              </div>
              <el-progress
                  :percentage="dept.percentage"
                  :color="dept.color"
                  :show-text="false"
                  :stroke-width="8"
              ></el-progress>
              <span class="dept-percentage">{{ dept.percentage }}%</span>
            </div>
            <div v-if="departmentStats.length === 0" class="empty-data">
              <i class="el-icon-office-building" style="font-size: 48px; color: #909399; margin-bottom: 16px;"></i>
              <p>暂无部门数据</p>
            </div>
          </div>
        </el-card>

        <el-card class="info-card activity-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>最近动态</span>
            </div>
          </template>
          <div class="activity-list">
            <div
                v-for="activity in recentActivities"
                :key="activity.id"
                class="activity-item"
            >
              <div class="activity-icon" :class="activity.type">
                <i :class="activity.icon"></i>
              </div>
              <div class="activity-content">
                <div class="activity-text">{{ activity.text }}</div>
                <div class="activity-time">{{ activity.time }}</div>
              </div>
            </div>
            <div v-if="recentActivities.length === 0" class="empty-data">
              <i class="el-icon-bell" style="font-size: 48px; color: #909399; margin-bottom: 16px;"></i>
              <p>暂无动态</p>
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
  name: 'HrIndexPage',
  data() {
    return {
      dashboardStats: {
        totalEmployees: 0,
        todayAttendance: 0,
        pendingApproval: 0,
        totalDepartments: 0,
        attendanceRate: '0',
        largestDept: '',
        employeeTrend: 0,
        employeeTrendText: ''
      },
      employeeStats: {
        monthJoin: 0,
        monthLeave: 0
      },
      attendanceStats: {
        abnormalCount: 0,
        missingCount: 0
      },
      salaryStats: {
        monthTotal: 0
      },
      leaveStats: {
        pendingCount: 0,
        monthApproved: 0
      },
      pendingTasks: [],
      departmentStats: [],
      recentActivities: [],
      currentDate: '',
      loading: false
    }
  },
  mounted() {
    this.setCurrentDate();
    this.loadDashboardData();
  },
  methods: {
    setCurrentDate() {
      const now = new Date();
      const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' };
      this.currentDate = now.toLocaleDateString('zh-CN', options);
    },

    // 加载仪表盘数据
    async loadDashboardData() {
      this.loading = true;
      try {
        await Promise.all([
          this.loadEmployeeStats(),
          this.loadAttendanceStats(),
          this.loadLeaveStats(),
          this.loadSalaryStats(),
          this.loadDepartmentStats(),
          this.loadPendingTasks(),
          this.loadRecentActivities()
        ]);
      } catch (error) {
        console.error('加载仪表盘数据失败:', error);
        this.$message.error('加载数据失败');
      } finally {
        this.loading = false;
      }
    },

    // 加载员工统计数据
    async loadEmployeeStats() {
      try {
        const response = await this.$http.get(API.url.user.list);
        console.log('员工数据响应:', response.data);

        if (response.data && response.data.code === 0) {
          let employees = [];

          // 处理不同的数据结构
          if (response.data.data && response.data.data.list) {
            employees = response.data.data.list;
          } else if (Array.isArray(response.data.data)) {
            employees = response.data.data;
          } else if (response.data.data && response.data.data.page) {
            employees = response.data.data.page.list || [];
          } else {
            employees = response.data.data || [];
          }

          this.dashboardStats.totalEmployees = employees.length;

          // 计算本月入职（根据创建时间）
          const currentMonth = new Date().getMonth() + 1;
          const currentYear = new Date().getFullYear();

          this.employeeStats.monthJoin = employees.filter(emp => {
            if (!emp.createTime) return false;
            const empDate = new Date(emp.createTime);
            return empDate.getMonth() + 1 === currentMonth && empDate.getFullYear() === currentYear;
          }).length;

          // 模拟趋势数据
          this.dashboardStats.employeeTrend = 5;
          this.dashboardStats.employeeTrendText = '较上月 +5%';
        }
      } catch (error) {
        console.error('加载员工数据失败:', error);
      }
    },

    // 加载考勤统计数据
    async loadAttendanceStats() {
      try {
        const today = new Date().toISOString().split('T')[0]; // 今天的日期 YYYY-MM-DD

        // 获取今天的考勤记录
        const response = await this.$http.get(API.url.attendance.list, {
          params: {
            date: today,
            page: 1,
            size: 1000 // 获取足够多的记录
          }
        });

        console.log('今日考勤数据响应:', response.data);

        if (response.data && response.data.code === 0) {
          let records = [];

          // 处理不同的数据结构
          if (response.data.data && response.data.data.list) {
            records = response.data.data.list;
          } else if (Array.isArray(response.data.data)) {
            records = response.data.data;
          } else if (response.data.data && response.data.data.page) {
            records = response.data.data.page.list || [];
          } else {
            records = response.data.data || [];
          }

          // 计算今日出勤人数（有打卡记录的员工）
          const attendedStaff = new Set();
          records.forEach(record => {
            if (record.staffId && (record.checkInTime || record.checkOutTime)) {
              attendedStaff.add(record.staffId);
            }
          });

          this.dashboardStats.todayAttendance = attendedStaff.size;

          // 计算异常考勤
          this.attendanceStats.abnormalCount = records.filter(record =>
              record.status && record.status !== '正常'
          ).length;

          // 计算未打卡人数
          this.attendanceStats.missingCount = records.filter(record =>
              !record.checkInTime && !record.checkOutTime
          ).length;

          // 计算出勤率
          const totalStaff = this.dashboardStats.totalEmployees;
          if (totalStaff > 0) {
            const attendanceRate = (attendedStaff.size / totalStaff * 100).toFixed(1);
            this.dashboardStats.attendanceRate = attendanceRate;
          }
        }
      } catch (error) {
        console.error('加载考勤数据失败:', error);
      }
    },

    // 加载请假统计数据 - 修复版
    async loadLeaveStats() {
      try {
        // 加载待审批请假
        const pendingResponse = await this.$http.get(API.url.hrLeave.pendingList);
        console.log('待审批请假响应:', pendingResponse.data);

        if (pendingResponse.data && pendingResponse.data.code === 0) {
          let pendingList = [];

          // 根据你的数据结构，数据在 page.list 中
          if (pendingResponse.data.page && pendingResponse.data.page.list) {
            pendingList = pendingResponse.data.page.list;
          } else if (pendingResponse.data.data && pendingResponse.data.data.page && pendingResponse.data.data.page.list) {
            pendingList = pendingResponse.data.data.page.list;
          } else if (Array.isArray(pendingResponse.data.data)) {
            pendingList = pendingResponse.data.data;
          } else if (pendingResponse.data.data && pendingResponse.data.data.list) {
            pendingList = pendingResponse.data.data.list;
          } else {
            pendingList = pendingResponse.data.data || [];
          }

          console.log('待审批列表数据:', pendingList);

          this.dashboardStats.pendingApproval = pendingList.length;
          this.leaveStats.pendingCount = pendingList.length;
        } else {
          console.log('待审批API返回错误:', pendingResponse.data?.msg);
          this.dashboardStats.pendingApproval = 0;
          this.leaveStats.pendingCount = 0;
        }

        // 加载本月已审批的请假
        const listResponse = await this.$http.get(API.url.hrLeave.list, {
          params: { page: 1, size: 1000 }
        });

        console.log('所有请假记录响应:', listResponse.data);

        if (listResponse.data && listResponse.data.code === 0) {
          let allLeaves = [];

          // 根据你的数据结构，数据在 page.list 中
          if (listResponse.data.page && listResponse.data.page.list) {
            allLeaves = listResponse.data.page.list;
          } else if (listResponse.data.data && listResponse.data.data.page && listResponse.data.data.page.list) {
            allLeaves = listResponse.data.data.page.list;
          } else if (Array.isArray(listResponse.data.data)) {
            allLeaves = listResponse.data.data;
          } else if (listResponse.data.data && listResponse.data.data.list) {
            allLeaves = listResponse.data.data.list;
          } else {
            allLeaves = listResponse.data.data || [];
          }

          console.log('所有请假记录数据:', allLeaves);

          const currentMonth = new Date().getMonth() + 1;
          const currentYear = new Date().getFullYear();

          this.leaveStats.monthApproved = allLeaves.filter(leave => {
            if (!leave.createTime) return false;
            const leaveDate = new Date(leave.createTime);
            return leaveDate.getMonth() + 1 === currentMonth &&
                leaveDate.getFullYear() === currentYear &&
                leave.status === 'approved';
          }).length;

          console.log('本月已审批请假数量:', this.leaveStats.monthApproved);
        }
      } catch (error) {
        console.error('加载请假数据失败:', error);
        this.dashboardStats.pendingApproval = 0;
        this.leaveStats.pendingCount = 0;
      }
    },

    // 加载部门统计数据
    async loadDepartmentStats() {
      try {
        const response = await this.$http.get(API.url.department.list);
        console.log('部门数据响应:', response.data);

        if (response.data && response.data.code === 0) {
          let departments = [];

          if (response.data.data && response.data.data.list) {
            departments = response.data.data.list;
          } else if (Array.isArray(response.data.data)) {
            departments = response.data.data;
          } else if (response.data.data && response.data.data.page) {
            departments = response.data.data.page.list || [];
          } else {
            departments = response.data.data || [];
          }

          this.dashboardStats.totalDepartments = departments.length;

          // 加载员工数据来计算部门人数分布
          const empResponse = await this.$http.get(API.url.user.list);
          if (empResponse.data && empResponse.data.code === 0) {
            let employees = [];

            if (empResponse.data.data && empResponse.data.data.list) {
              employees = empResponse.data.data.list;
            } else if (Array.isArray(empResponse.data.data)) {
              employees = empResponse.data.data;
            } else if (empResponse.data.data && empResponse.data.data.page) {
              employees = empResponse.data.data.page.list || [];
            } else {
              employees = empResponse.data.data || [];
            }

            this.calculateDepartmentStats(departments, employees);
          }
        }
      } catch (error) {
        console.error('加载部门数据失败:', error);
      }
    },

    // 计算部门统计
    calculateDepartmentStats(departments, employees) {
      const deptCounts = {};
      employees.forEach(emp => {
        const deptId = emp.deptId;
        if (deptId) {
          deptCounts[deptId] = (deptCounts[deptId] || 0) + 1;
        }
      });

      const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399'];
      this.departmentStats = departments.map((dept, index) => {
        const count = deptCounts[dept.id] || 0;
        const percentage = this.dashboardStats.totalEmployees > 0 ?
            Math.round((count / this.dashboardStats.totalEmployees) * 100) : 0;

        return {
          name: dept.dname || dept.name,
          count: count,
          percentage: percentage,
          color: colors[index % colors.length]
        };
      }).sort((a, b) => b.count - a.count);

      // 设置最大部门
      if (this.departmentStats.length > 0) {
        this.dashboardStats.largestDept = this.departmentStats[0].name;
      }
    },

    // 加载待办事项 - 修复版
    async loadPendingTasks() {
      try {
        // 从请假待审批列表加载
        const leaveResponse = await this.$http.get(API.url.hrLeave.pendingList);
        console.log('待办事项-待审批响应:', leaveResponse.data);

        if (leaveResponse.data && leaveResponse.data.code === 0) {
          let pendingLeaves = [];

          // 根据你的数据结构，数据在 page.list 中
          if (leaveResponse.data.page && leaveResponse.data.page.list) {
            pendingLeaves = leaveResponse.data.page.list;
          } else if (leaveResponse.data.data && leaveResponse.data.data.page && leaveResponse.data.data.page.list) {
            pendingLeaves = leaveResponse.data.data.page.list;
          } else if (Array.isArray(leaveResponse.data.data)) {
            pendingLeaves = leaveResponse.data.data;
          } else if (leaveResponse.data.data && leaveResponse.data.data.list) {
            pendingLeaves = leaveResponse.data.data.list;
          } else {
            pendingLeaves = leaveResponse.data.data || [];
          }

          console.log('待办事项-待审批数据:', pendingLeaves);

          this.pendingTasks = pendingLeaves.slice(0, 5).map((leave, index) => {
            // 确保字段名正确
            const staffName = leave.staffName || leave.name || leave.username || '员工';
            const leaveType = leave.leaveType || leave.type || '请假';
            const days = leave.days || leave.duration || 0;
            const createTime = leave.createTime || leave.applyTime || leave.createDate;

            return {
              id: `leave_${leave.id || index}`,
              type: 'leave',
              icon: 'el-icon-date',
              title: `${staffName} - ${leaveType} ${days}天`,
              time: this.formatTime(createTime),
              urgent: leave.urgent || days > 3 // 超过3天的设为紧急
            };
          });

          console.log('生成的待办事项:', this.pendingTasks);
        } else {
          console.log('待办事项API返回错误:', leaveResponse.data?.msg);
          this.pendingTasks = [];
        }
      } catch (error) {
        console.error('加载待办事项失败:', error);
        this.pendingTasks = [];
      }
    },
// 在首页的 loadSalaryStats 方法中，直接调用薪资统计API
    async loadSalaryStats() {
      console.log('开始加载薪资统计数据...');
      try {
        const currentMonth = new Date().toISOString().slice(0, 7); // YYYY-MM

        // 直接调用薪资统计API，与薪资管理页面使用相同的逻辑
        const params = {
          month: currentMonth
        };

        console.log('首页薪资统计请求参数:', params);

        const response = await this.$http.get(API.url.salary.statistics, { params });
        console.log('首页薪资统计响应:', response.data);

        if (response.data && response.data.code === 0) {
          const statsData = response.data.data || {};
          console.log('首页薪资统计数据:', statsData);

          // 使用与薪资管理页面相同的字段
          this.salaryStats.monthTotal = statsData.totalSalary || statsData.monthTotal || 0;

          console.log('首页计算出的月总工资:', this.salaryStats.monthTotal);

        } else {
          console.log('首页薪资统计API返回错误:', response.data?.msg);
          this.salaryStats.monthTotal = 0;

          // 如果统计API失败，尝试从薪资列表计算
          await this.calculateSalaryFromList();
        }

      } catch (error) {
        console.error('首页加载薪资统计数据失败:', error);
        this.salaryStats.monthTotal = 0;

        // 降级方案：从薪资列表计算
        await this.calculateSalaryFromList();
      }
    },

// 从薪资列表计算月总工资 - 与薪资管理页面保持一致
    async calculateSalaryFromList() {
      try {
        const currentMonth = new Date().toISOString().slice(0, 7);
        console.log('首页从薪资列表计算月份:', currentMonth);

        const response = await this.$http.get(API.url.salary.list, {
          params: {
            page: 1,
            size: 1000,
            month: currentMonth
          }
        });

        console.log('首页薪资列表响应:', response.data);

        if (response.data && response.data.code === 0) {
          let salaryList = [];

          // 与薪资管理页面相同的数据结构处理
          if (response.data.page && response.data.page.list) {
            salaryList = response.data.page.list;
          } else if (response.data.data && response.data.data.page && response.data.data.page.list) {
            salaryList = response.data.data.page.list;
          } else if (Array.isArray(response.data.data)) {
            salaryList = response.data.data;
          } else if (response.data.data && response.data.data.list) {
            salaryList = response.data.data.list;
          } else {
            salaryList = response.data.data || [];
          }

          console.log('首页获取到的薪资记录:', salaryList);

          // 计算月总工资 - 使用与薪资管理页面相同的逻辑
          this.salaryStats.monthTotal = salaryList.reduce((total, salary) => {
            const amount = salary.totalSalary || salary.totalAmount || salary.amount ||
                salary.netAmount || salary.grossAmount || 0;
            return total + (parseFloat(amount) || 0);
          }, 0);

          console.log('首页计算出的月总工资:', this.salaryStats.monthTotal);

        } else {
          console.log('首页薪资列表API返回错误:', response.data?.msg);
          this.salaryStats.monthTotal = 0;
        }
      } catch (error) {
        console.error('首页从列表计算薪资失败:', error);
        this.salaryStats.monthTotal = 0;
      }
    },
    // 加载最近动态
    async loadRecentActivities() {
      try {
        // 从请假列表获取动态
        const leaveResponse = await this.$http.get(API.url.hrLeave.list, {
          params: { page: 1, size: 5 }
        });

        const activities = [];

        if (leaveResponse.data && leaveResponse.data.code === 0) {
          let leaves = [];

          if (leaveResponse.data.data && leaveResponse.data.data.list) {
            leaves = leaveResponse.data.data.list;
          } else if (Array.isArray(leaveResponse.data.data)) {
            leaves = leaveResponse.data.data;
          } else if (leaveResponse.data.data && leaveResponse.data.data.page) {
            leaves = leaveResponse.data.data.page.list || [];
          } else {
            leaves = leaveResponse.data.data || [];
          }

          leaves.slice(0, 3).forEach(leave => {
            activities.push({
              id: `leave_${leave.id}`,
              type: 'leave',
              icon: 'el-icon-document',
              text: `${leave.staffName || '员工'} 提交了${leave.leaveType || '请假'}申请`,
              time: this.formatTime(leave.createTime)
            });
          });
        }

        this.recentActivities = activities.sort((a, b) =>
            new Date(b.time) - new Date(a.time)
        ).slice(0, 5);

      } catch (error) {
        console.error('加载动态数据失败:', error);
      }
    },

    // 工具方法
    formatTime(timestamp) {
      if (!timestamp) return '未知时间';
      const date = new Date(timestamp);
      const now = new Date();
      const diff = now - date;

      if (diff < 60 * 60 * 1000) {
        return `${Math.floor(diff / (60 * 1000))}分钟前`;
      } else if (diff < 24 * 60 * 60 * 1000) {
        return `${Math.floor(diff / (60 * 60 * 1000))}小时前`;
      } else {
        return date.toLocaleDateString('zh-CN');
      }
    },

    // 格式化薪资显示
    formatSalary(amount) {
      if (!amount || amount === 0) return '0';

      // 如果金额很大，显示为万单位
      if (amount >= 10000) {
        return (amount / 10000).toFixed(1) + '万';
      }

      // 否则显示原始金额，格式化千分位
      return amount.toLocaleString('zh-CN');
    },

    getTrendClass(trend) {
      return trend > 0 ? 'trend-up' : trend < 0 ? 'trend-down' : '';
    },

    getTrendIcon(trend) {
      return trend > 0 ? 'el-icon-top' : trend < 0 ? 'el-icon-bottom' : 'el-icon-minus';
    },

    handleTaskClick(task) {
      const routeMap = {
        leave: '/index/hr/leave-manage',
        salary: '/index/hr/salary-manage',
        attendance: '/index/hr/attendance-manage',
        employee: '/index/hr/employee'
      };

      if (routeMap[task.type]) {
        this.$router.push(routeMap[task.type]);
      }
    }
  }
}
</script>

<style scoped>
.loading-text {
  color: #909399;
  font-size: 16px;
}
.empty-todo,
.empty-data {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.empty-todo p,
.empty-data p {
  margin: 0;
  font-size: 14px;
}

.trend-down {
  color: #F56C6C;
}

.stat-warning {
  color: #E6A23C;
}

.detail-danger {
  color: #F56C6C;
  font-weight: bold;
}
.hr-dashboard {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 24px;
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

/* 核心数据卡片 */
.core-stats {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  height: 120px;
  margin-bottom: 16px;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1) !important;
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 20px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
  color: white;
  flex-shrink: 0;
}

.employee-card .stat-icon {
  background: linear-gradient(135deg, #409EFF, #66b1ff);
}

.attendance-card .stat-icon {
  background: linear-gradient(135deg, #67C23A, #85ce61);
}

.approval-card .stat-icon {
  background: linear-gradient(135deg, #E6A23C, #ebb563);
}

.department-card .stat-icon {
  background: linear-gradient(135deg, #F56C6C, #f78989);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 6px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}

.trend-up {
  color: #67C23A;
}

.trend-warning {
  color: #E6A23C;
}

.attendance-rate,
.dept-info {
  color: #909399;
}

/* 主要内容区域 */
.main-content {
  margin-bottom: 20px;
}

/* 功能模块 */
.function-modules {
  margin-bottom: 20px;
}

.module-card {
  border-radius: 12px;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  height: 140px;
  margin-bottom: 16px;
}

.module-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1) !important;
}

.module-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.module-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  font-size: 20px;
  color: white;
  flex-shrink: 0;
}

.employee-module .module-icon {
  background: linear-gradient(135deg, #409EFF, #66b1ff);
}

.attendance-module .module-icon {
  background: linear-gradient(135deg, #67C23A, #85ce61);
}

.salary-module .module-icon {
  background: linear-gradient(135deg, #E6A23C, #ebb563);
}

.leave-module .module-icon {
  background: linear-gradient(135deg, #F56C6C, #f78989);
}

.module-title h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
  font-weight: 600;
}

.module-title p {
  margin: 2px 0 0 0;
  font-size: 12px;
  color: #909399;
}

.module-content {
  padding: 0 8px;
}

/* 员工管理模块 */
.employee-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  text-align: center;
  flex: 1;
}

.item-value {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.item-label {
  font-size: 12px;
  color: #909399;
}

/* 考勤管理模块 */
.attendance-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.detail-item .label {
  color: #606266;
}

.detail-item .value {
  color: #303133;
  font-weight: 500;
}

/* 薪资管理模块 */
.salary-overview {
  text-align: center;
}

.salary-total {
  display: flex;
  align-items: baseline;
  justify-content: center;
  margin-bottom: 4px;
}

.currency {
  font-size: 16px;
  color: #E6A23C;
  margin-right: 2px;
}

.amount {
  font-size: 24px;
  font-weight: bold;
  color: #E6A23C;
}

.salary-period {
  font-size: 12px;
  color: #909399;
}

/* 请假管理模块 */
.leave-stats {
  display: flex;
  gap: 20px;
  justify-content: center;
}

/* 待办事项 */
.todo-section {
  border-radius: 12px;
  border: none;
  margin-bottom: 0;
}

.todo-list {
  max-height: 300px;
  overflow-y: auto;
}

.todo-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s;
}

.todo-item:last-child {
  border-bottom: none;
}

.todo-item:hover {
  background-color: #f5f7fa;
  border-radius: 6px;
  padding: 12px;
  margin: 0 -12px;
}

.todo-badge {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
  color: white;
  font-size: 16px;
}

.todo-badge.leave {
  background-color: #E6A23C;
}

.todo-badge.salary {
  background-color: #409EFF;
}

.todo-badge.attendance {
  background-color: #F56C6C;
}

.todo-content {
  flex: 1;
}

.todo-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
  line-height: 1.4;
}

.todo-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.todo-time {
  font-size: 12px;
  color: #909399;
}

/* 信息卡片 */
.info-card {
  border-radius: 12px;
  border: none;
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

/* 部门分布 */
.department-chart {
  padding: 8px 0;
}

.department-item {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  gap: 12px;
}

.department-item:last-child {
  margin-bottom: 0;
}

.dept-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex: 1;
  min-width: 120px;
}

.dept-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.dept-count {
  font-size: 12px;
  color: #909399;
}

.dept-progress {
  flex: 1;
  min-width: 80px;
}

.dept-percentage {
  font-size: 12px;
  color: #909399;
  min-width: 30px;
  text-align: right;
}

/* 最近动态 */
.activity-list {
  padding: 8px 0;
}

.activity-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
  color: white;
  font-size: 16px;
}

.activity-icon.new {
  background-color: #409EFF;
}

.activity-icon.leave {
  background-color: #E6A23C;
}

.activity-icon.salary {
  background-color: #67C23A;
}

.activity-content {
  flex: 1;
}

.activity-text {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
  line-height: 1.4;
}

.activity-time {
  font-size: 12px;
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hr-dashboard {
    padding: 16px;
  }

  .main-content .el-col {
    margin-bottom: 16px;
  }

  .stat-card,
  .module-card {
    height: auto;
    min-height: 100px;
  }

  .stat-content {
    padding: 16px;
  }
}
</style>