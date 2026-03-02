<template>
  <div class="attendance-page">
    <div class="page-header">
      <h1>考勤打卡</h1>
      <p>记录您的上下班时间</p>
    </div>

    <el-row :gutter="20">
      <!-- 打卡操作区域 -->
      <el-col :span="16">
        <el-card class="clock-card" shadow="never">
          <div class="clock-container">
            <!-- 当前时间显示 -->
            <div class="current-time">
              <div class="time-display">{{ currentTime }}</div>
              <div class="date-display">{{ currentDate }}</div>
            </div>

            <!-- 打卡按钮 -->
            <div class="clock-actions">
              <el-button
                  class="clock-btn check-in-btn"
                  :type="todayAttendance.clockInTime ? 'success' : 'primary'"
                  :disabled="!canCheckIn"
                  @click="handleCheckIn"
                  size="large"
              >
                <i class="el-icon-upload2"></i>
                {{ todayAttendance.clockInTime ? '已上班打卡' : '上班打卡' }}
                <div class="clock-time" v-if="todayAttendance.clockInTime">
                  {{ todayAttendance.clockInTime }}
                </div>
              </el-button>

              <el-button
                  class="clock-btn check-out-btn"
                  :type="todayAttendance.clockOutTime ? 'success' : 'primary'"
                  :disabled="!canCheckOut"
                  @click="handleCheckOut"
                  size="large"
              >
                <i class="el-icon-download"></i>
                {{ todayAttendance.clockOutTime ? '已下班打卡' : '下班打卡' }}
                <div class="clock-time" v-if="todayAttendance.clockOutTime">
                  {{ todayAttendance.clockOutTime }}
                </div>
              </el-button>
            </div>

            <!-- 考勤状态 -->
            <div class="attendance-status">
              <el-alert
                  :title="statusTitle"
                  :type="statusType"
                  :description="statusDescription"
                  show-icon
                  :closable="false"
              >
              </el-alert>
            </div>

            <!-- 忘记打卡提示 -->
            <div class="forget-tips">
              <el-alert
                  title="忘记打卡？"
                  type="info"
                  description="如忘记打卡，请及时联系HR进行处理"
                  :closable="false"
                  show-icon
              >
              </el-alert>
            </div>
          </div>
        </el-card>

        <!-- 本月考勤统计 -->
        <el-card class="stats-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>考勤统计</span>
              <div class="month-selector">
                <el-date-picker
                    v-model="selectedMonth"
                    type="month"
                    placeholder="选择月份"
                    format="yyyy年MM月"
                    value-format="yyyy-MM"
                    @change="handleMonthChange"
                    size="small"
                    style="width: 140px; margin-right: 10px;"
                >
                </el-date-picker>
                <el-button
                    type="text"
                    @click="loadCurrentMonth"
                    size="mini"
                >
                  本月
                </el-button>
                <el-button
                    type="text"
                    @click="loadMonthlyStats"
                    size="mini"
                    :loading="statsLoading"
                >
                  查询
                </el-button>
              </div>
            </div>
          </template>

          <!-- 加载状态 -->
          <div v-if="statsLoading" class="stats-loading">
            <i class="el-icon-loading"></i> 加载中...
          </div>

          <!-- 统计内容 -->
          <div v-else class="stats-content">
            <div class="stats-grid">
              <div class="stat-item">
                <div class="stat-value">{{ monthlyStats.workDays || 0 }}</div>
                <div class="stat-label">正常出勤</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ monthlyStats.lateCount || 0 }}</div>
                <div class="stat-label">迟到次数</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ monthlyStats.earlyLeaveCount || 0 }}</div>
                <div class="stat-label">早退次数</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ monthlyStats.lateEarlyCount || 0 }}</div>
                <div class="stat-label">迟到早退</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ monthlyStats.missingCardDays || 0 }}</div>
                <div class="stat-label">缺卡天数</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ monthlyStats.absentCount || 0 }}</div>
                <div class="stat-label">缺勤天数</div>
              </div>
            </div>

            <!-- 额外统计信息 -->
            <div class="additional-stats" v-if="monthlyStats.totalDays > 0">
              <div class="stat-summary">
                <span class="summary-label">统计月份：</span>
                <span class="summary-value">{{ displayMonth }}</span>
              </div>
              <div class="stat-summary">
                <span class="summary-label">总天数：</span>
                <span class="summary-value">{{ monthlyStats.totalDays || 0 }} 天</span>
              </div>
              <div class="stat-summary">
                <span class="summary-label">出勤率：</span>
                <span class="summary-value">{{ calculateAttendanceRate() }}%</span>
              </div>
              <div class="stat-summary">
                <span class="summary-label">异常率：</span>
                <span class="summary-value">{{ calculateAbnormalRate() }}%</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 侧边信息区域 -->
      <el-col :span="8">
        <!-- 今日考勤详情 -->
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>今日考勤详情</span>
            </div>
          </template>
          <div class="detail-list">
            <div class="detail-item">
              <span class="label">上班时间：</span>
              <span class="value">{{ todayAttendance.clockInTime || '未打卡' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">下班时间：</span>
              <span class="value">{{ todayAttendance.clockOutTime || '未打卡' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">工作时长：</span>
              <span class="value">{{ workDuration }}</span>
            </div>
            <div class="detail-item">
              <span class="label">考勤状态：</span>
              <span class="value">
                <el-tag :type="getStatusTagType(todayAttendance.status)" size="small">
                  {{ getStatusText(todayAttendance.status) }}
                </el-tag>
              </span>
            </div>
            <div class="detail-item">
              <span class="label">打卡位置：</span>
              <span class="value">{{ locationInfo || '正在获取...' }}</span>
            </div>
          </div>
        </el-card>

        <!-- 考勤规则 -->
        <el-card class="rules-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>考勤规则</span>
            </div>
          </template>
          <div class="rules-list">
            <div class="rule-item">
              <i class="el-icon-time"></i>
              <span>上班时间：09:00</span>
            </div>
            <div class="rule-item">
              <i class="el-icon-time"></i>
              <span>下班时间：18:00</span>
            </div>
            <div class="rule-item">
              <i class="el-icon-warning"></i>
              <span>迟到判定：09:30后打卡</span>
            </div>
            <div class="rule-item">
              <i class="el-icon-warning"></i>
              <span>早退判定：17:30前打卡</span>
            </div>
            <div class="rule-item">
              <i class="el-icon-warning-outline"></i>
              <span>缺卡判定：只打上班或下班卡</span>
            </div>
            <div class="rule-item">
              <i class="el-icon-info"></i>
              <span>忘记打卡请联系HR</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近考勤记录 -->
    <el-card class="records-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>考勤记录</span>
          <div>
            <el-date-picker
                v-model="recordsMonth"
                type="month"
                placeholder="选择月份"
                format="yyyy年MM月"
                value-format="yyyy-MM"
                @change="handleRecordsMonthChange"
                size="small"
                style="width: 140px; margin-right: 10px;"
            >
            </el-date-picker>
            <el-button type="text" @click="loadCurrentMonthRecords">本月</el-button>
            <el-button type="text" @click="loadAttendanceRecords" :loading="loading">查询</el-button>
          </div>
        </div>
      </template>
      <el-table
          :data="attendanceRecords"
          v-loading="loading"
          style="width: 100%"
      >
        <el-table-column prop="workDate" label="日期" width="120">
          <template slot-scope="scope">
            {{ formatDate(scope.row.workDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="clockInTime" label="上班时间" width="100">
          <template slot-scope="scope">
            <span :class="{'late-time': isLate(scope.row.clockInTime), 'missing-time': !scope.row.clockInTime}">
              {{ scope.row.clockInTime || '未打卡' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="clockOutTime" label="下班时间" width="100">
          <template slot-scope="scope">
            <span :class="{'early-time': isEarly(scope.row.clockOutTime), 'missing-time': !scope.row.clockOutTime}">
              {{ scope.row.clockOutTime || '未打卡' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="workDuration" label="工作时长" width="100">
          <template slot-scope="scope">
            {{ scope.row.workDuration || '--' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusTagType(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="打卡位置">
          <template slot-scope="scope">
            {{ scope.row.location || '--' }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'AttendancePage',
  data() {
    return {
      // 当前时间
      currentTime: '',
      currentDate: '',
      // 今日考勤
      todayAttendance: {
        clockInTime: '',
        clockOutTime: '',
        status: '',
        workDate: ''
      },
      // 月度统计
      monthlyStats: {
        workDays: 0,
        lateCount: 0,
        earlyLeaveCount: 0,
        lateEarlyCount: 0,
        missingCardDays: 0, // 新增：缺卡天数
        absentCount: 0,
        totalDays: 0
      },
      // 考勤记录
      attendanceRecords: [],
      loading: false,
      statsLoading: false,
      // 位置信息
      locationInfo: '',
      // 选择的月份
      selectedMonth: '',
      recordsMonth: '',
      // 显示月份
      displayMonth: ''
    }
  },
  computed: {
    // 是否可以上班打卡
    canCheckIn() {
      if (this.todayAttendance.clockInTime) return false;
      const now = new Date();
      const currentHour = now.getHours();
      // 只允许在6:00-12:00之间打卡
      return currentHour >= 8 && currentHour < 11;
    },
    // 是否可以下班打卡
    canCheckOut() {
      if (this.todayAttendance.clockOutTime) return false;
      const now = new Date();
      const currentHour = now.getHours();
      // 只允许在16:00-24:00之间打卡
      return currentHour >= 16 || currentHour < 23;
    },
    // 工作时长
    workDuration() {
      if (!this.todayAttendance.clockInTime || !this.todayAttendance.clockOutTime) {
        return '--';
      }
      return this.calculateWorkDuration(
          this.todayAttendance.clockInTime,
          this.todayAttendance.clockOutTime
      );
    },
    // 状态标题
    statusTitle() {
      const status = this.todayAttendance.status;
      if (status === '正常') {
        return '今日考勤正常';
      } else if (status === '迟到') {
        return '今日上班迟到';
      } else if (status === '早退') {
        return '今日下班早退';
      } else if (status === '迟到早退') {
        return '今日迟到且早退';
      } else if (status === '缺卡') {
        return '今日考勤异常（缺卡）';
      } else if (status === '缺勤') {
        return '今日缺勤';
      } else if (status === '请假') {
        return '今日请假';
      } else {
        return '等待上班打卡';
      }
    },
    // 状态类型
    statusType() {
      const status = this.todayAttendance.status;
      if (status === '正常') {
        return 'success';
      } else if (status === '迟到' || status === '早退' || status === '缺卡') {
        return 'warning';
      } else if (status === '迟到早退' || status === '缺勤') {
        return 'error';
      } else if (status === '请假') {
        return 'info';
      } else {
        return 'info';
      }
    },
    // 状态描述
    statusDescription() {
      const status = this.todayAttendance.status;
      const now = new Date();
      const currentHour = now.getHours();

      if (status === '正常') {
        return `今日工作 ${this.workDuration}，辛苦了！`;
      } else if (status === '迟到') {
        return '今日上班迟到，请注意考勤纪律';
      } else if (status === '早退') {
        return '今日下班早退，请合理安排工作时间';
      } else if (status === '迟到早退') {
        return '今日迟到且早退，请注意考勤纪律';
      } else if (status === '缺卡') {
        if (this.todayAttendance.clockInTime && !this.todayAttendance.clockOutTime) {
          if (currentHour >= 16) {
            return '已上班打卡但未下班打卡，请及时补卡';
          } else {
            return '已上班打卡，请记得下班打卡';
          }
        } else if (!this.todayAttendance.clockInTime && this.todayAttendance.clockOutTime) {
          return '已下班打卡但未上班打卡，请及时补卡';
        } else {
          return '考勤记录不完整，请及时补卡';
        }
      } else if (status === '缺勤') {
        return '今日无打卡记录，如已出勤请及时补卡';
      } else if (status === '请假') {
        return '今日请假，好好休息';
      } else {
        if (currentHour >= 6 && currentHour < 12) {
          return '现在可以进行上班打卡';
        } else if (currentHour < 6) {
          return '上班打卡时间：06:00 - 12:00';
        } else {
          return '上班打卡时间已过，如需补打卡请联系HR';
        }
      }
    }
  },
  mounted() {
    this.startClock();
    this.loadTodayAttendance();
    this.initMonthSelection();
    this.loadMonthlyStats();
    this.loadAttendanceRecords();
    this.getLocation();
  },
  beforeDestroy() {
    if (this.clockTimer) {
      clearInterval(this.clockTimer);
    }
  },
  methods: {
    // 初始化月份选择
    initMonthSelection() {
      const currentMonth = new Date().toISOString().slice(0, 7);
      this.selectedMonth = currentMonth;
      this.recordsMonth = currentMonth;
      this.displayMonth = this.formatDisplayMonth(currentMonth);
    },

    // 格式化显示月份
    formatDisplayMonth(month) {
      if (!month) return '';
      const [year, monthNum] = month.split('-');
      return `${year}年${monthNum}月`;
    },

    // 月份变化处理
    handleMonthChange(month) {
      if (month) {
        this.displayMonth = this.formatDisplayMonth(month);
        this.loadMonthlyStats(month);
      }
    },

    // 记录月份变化处理
    handleRecordsMonthChange(month) {
      if (month) {
        this.loadAttendanceRecords(month);
      }
    },

    // 加载当前月数据
    loadCurrentMonth() {
      const currentMonth = new Date().toISOString().slice(0, 7);
      this.selectedMonth = currentMonth;
      this.displayMonth = this.formatDisplayMonth(currentMonth);
      this.loadMonthlyStats(currentMonth);
    },

    // 加载当前月记录
    loadCurrentMonthRecords() {
      const currentMonth = new Date().toISOString().slice(0, 7);
      this.recordsMonth = currentMonth;
      this.loadAttendanceRecords(currentMonth);
    },

    // 开始时钟
    startClock() {
      this.updateTime();
      this.clockTimer = setInterval(() => {
        this.updateTime();
      }, 1000);
    },

    // 更新时间显示
    updateTime() {
      const now = new Date();
      this.currentTime = now.toLocaleTimeString('zh-CN', {
        hour12: false,
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
      this.currentDate = now.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        weekday: 'long'
      });
    },

    // 加载今日考勤
    async loadTodayAttendance() {
      try {
        console.log('开始加载今日考勤...');
        const today = new Date().toISOString().split('T')[0];

        const { data } = await this.$http({
          url: '/attendance/myRecords',
          method: 'get',
          params: {
            date: today,
            page: 1,
            limit: 1
          }
        });

        console.log('今日考勤响应:', data);

        if (data && data.code === 0) {
          const pageData = data.page;
          const records = pageData?.list || [];

          if (records.length > 0) {
            const todayRecord = records[0];
            console.log('今日考勤记录:', todayRecord);

            this.todayAttendance = {
              clockInTime: todayRecord.checkInTime || '',
              clockOutTime: todayRecord.checkOutTime || '',
              status: todayRecord.status || '',
              workDate: todayRecord.date || today
            };
          } else {
            console.log('今日无考勤记录');
            this.todayAttendance = {
              clockInTime: '',
              clockOutTime: '',
              status: '未打卡',
              workDate: today
            };
          }
        } else {
          console.log('API返回异常:', data);
          this.initEmptyAttendance();
        }
      } catch (error) {
        console.error('加载今日考勤失败:', error);
        console.error('错误详情:', error.response?.data);
        this.$message.error('加载今日考勤失败');
        this.initEmptyAttendance();
      }
    },

    // 初始化空数据
    initEmptyAttendance() {
      const today = new Date().toISOString().split('T')[0];
      this.todayAttendance = {
        clockInTime: '',
        clockOutTime: '',
        status: '未打卡',
        workDate: today
      };
    },

    // 加载月度统计
    async loadMonthlyStats(month = null) {
      this.statsLoading = true;
      try {
        const currentMonth = month || this.selectedMonth;
        console.log('请求月度统计，月份:', currentMonth);

        const { data } = await this.$http({
          url: '/attendance/myStatistics',
          method: 'get',
          params: {
            month: currentMonth
          }
        });

        console.log('月度统计完整响应:', data);

        if (data && data.code === 0) {
          const stats = data.data || {};
          console.log('后端返回的统计字段详情:', stats);

          // 根据后端实际返回的字段名进行正确映射
          this.monthlyStats = {
            workDays: stats.normalDays || 0,
            lateCount: stats.lateDays || 0,
            earlyLeaveCount: stats.earlyDays || 0,
            lateEarlyCount: stats.lateEarlyDays || 0,
            missingCardDays: stats.missingCardDays || 0, // 新增：缺卡天数
            absentCount: stats.absentDays || 0,
            totalDays: stats.totalDays || 0
          };

          console.log('映射后的月度统计:', this.monthlyStats);
          this.$message.success(`${this.formatDisplayMonth(currentMonth)}统计加载成功`);
        } else {
          console.log('API返回code不为0:', data?.code, '消息:', data?.msg);
          this.setDefaultStats();
          this.$message.warning(`${this.formatDisplayMonth(currentMonth)}无考勤数据`);
        }
      } catch (error) {
        console.error('加载月度统计失败:', error);
        console.error('错误详情:', error.response?.data);
        this.setDefaultStats();
        this.$message.error(`加载${this.formatDisplayMonth(month)}统计失败`);
      } finally {
        this.statsLoading = false;
      }
    },

    // 计算出勤率
    calculateAttendanceRate() {
      const { workDays, totalDays } = this.monthlyStats;
      if (!totalDays || totalDays === 0) return 0;
      return ((workDays / totalDays) * 100).toFixed(1);
    },

    // 计算异常率
    calculateAbnormalRate() {
      const { workDays, totalDays } = this.monthlyStats;
      if (!totalDays || totalDays === 0) return 0;
      const abnormalDays = totalDays - workDays;
      return ((abnormalDays / totalDays) * 100).toFixed(1);
    },

    // 设置默认统计值
    setDefaultStats() {
      this.monthlyStats = {
        workDays: 0,
        lateCount: 0,
        earlyLeaveCount: 0,
        lateEarlyCount: 0,
        missingCardDays: 0,
        absentCount: 0,
        totalDays: 0
      };
    },

    // 加载考勤记录
    async loadAttendanceRecords(month = null) {
      this.loading = true;
      try {
        const params = {
          page: 1,
          limit: 10,
          sort: 'date',
          order: 'desc'
        };

        const currentMonth = month || this.recordsMonth;
        if (currentMonth) {
          params.month = currentMonth;
          console.log('加载指定月份记录:', currentMonth);
        }

        const { data } = await this.$http({
          url: '/attendance/myRecords',
          method: 'get',
          params: params
        });

        console.log('考勤记录响应:', data);

        if (data && data.code === 0) {
          const pageData = data.page;
          const records = pageData?.list || [];

          console.log('考勤记录列表:', records);

          this.attendanceRecords = records.map(record => ({
            workDate: record.date,
            clockInTime: record.checkInTime,
            clockOutTime: record.checkOutTime,
            status: record.status,
            workDuration: this.calculateWorkDuration(record.checkInTime, record.checkOutTime),
            location: record.location || '--'
          }));

          this.$message.success(`${this.formatDisplayMonth(currentMonth)}记录加载成功`);
        }
      } catch (error) {
        console.error('加载考勤记录失败:', error);
        this.$message.error('加载考勤记录失败');
      } finally {
        this.loading = false;
      }
    },

    // 上班打卡
    async handleCheckIn() {
      try {
        const { data } = await this.$http({
          url: '/attendance/checkIn',
          method: 'post'
        });

        console.log('上班打卡响应:', data);

        if (data && data.code === 0) {
          this.$message.success('上班打卡成功');
          this.loadTodayAttendance();
          this.loadMonthlyStats();
          this.loadAttendanceRecords();
        } else {
          this.$message.error(data?.msg || '打卡失败');
        }
      } catch (error) {
        console.error('上班打卡失败:', error);
        const errorMsg = error.response?.data?.msg || error.message || '打卡失败，请重试';
        this.$message.error(errorMsg);
      }
    },

    // 下班打卡
    async handleCheckOut() {
      try {
        const { data } = await this.$http({
          url: '/attendance/checkOut',
          method: 'post'
        });

        console.log('下班打卡响应:', data);

        if (data && data.code === 0) {
          this.$message.success('下班打卡成功');
          this.loadTodayAttendance();
          this.loadMonthlyStats();
          this.loadAttendanceRecords();
        } else {
          this.$message.error(data?.msg || '打卡失败');
        }
      } catch (error) {
        console.error('下班打卡失败:', error);
        const errorMsg = error.response?.data?.msg || error.message || '打卡失败，请重试';
        this.$message.error(errorMsg);
      }
    },

    // 获取位置信息
    getLocation() {
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            (position) => {
              const { latitude, longitude } = position.coords;
              this.locationInfo = `坐标: ${latitude.toFixed(6)}, ${longitude.toFixed(6)}`;
            },
            (error) => {
              console.error('获取位置失败:', error);
              this.locationInfo = '位置获取失败';
            }
        );
      } else {
        this.locationInfo = '浏览器不支持定位';
      }
    },

    // 计算工作时长
    calculateWorkDuration(startTime, endTime) {
      if (!startTime || !endTime) return '--';

      try {
        const start = new Date(`2000-01-01 ${startTime}`);
        const end = new Date(`2000-01-01 ${endTime}`);
        const diff = end - start;
        const hours = Math.floor(diff / (1000 * 60 * 60));
        const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
        return `${hours}小时${minutes}分钟`;
      } catch (error) {
        return '--';
      }
    },

    // 格式化日期
    formatDate(date) {
      if (!date) return '';
      try {
        return new Date(date).toLocaleDateString('zh-CN');
      } catch (error) {
        return date;
      }
    },

    // 判断是否迟到
    isLate(clockInTime) {
      if (!clockInTime) return false;
      try {
        const [hour, minute] = clockInTime.split(':').map(Number);
        return hour > 9 || (hour === 9 && minute > 0);
      } catch (error) {
        return false;
      }
    },

    // 判断是否早退
    isEarly(clockOutTime) {
      if (!clockOutTime) return false;
      try {
        const [hour, minute] = clockOutTime.split(':').map(Number);
        return hour < 18;
      } catch (error) {
        return false;
      }
    },

    // 获取状态标签类型
    getStatusTagType(status) {
      const statusMap = {
        'NORMAL': 'success',
        '正常': 'success',
        'LATE': 'warning',
        '迟到': 'warning',
        'EARLY_LEAVE': 'warning',
        '早退': 'warning',
        'LATE_EARLY': 'danger',
        '迟到早退': 'danger',
        'MISSING_CARD': 'warning',
        '缺卡': 'warning',
        'ABSENT': 'danger',
        '缺勤': 'danger',
        'LEAVE': 'info',
        '请假': 'info'
      };
      return statusMap[status] || 'info';
    },

    // 获取状态文本
    getStatusText(status) {
      const statusMap = {
        'NORMAL': '正常',
        '正常': '正常',
        'LATE': '迟到',
        '迟到': '迟到',
        'EARLY_LEAVE': '早退',
        '早退': '早退',
        'LATE_EARLY': '迟到早退',
        '迟到早退': '迟到早退',
        'MISSING_CARD': '缺卡',
        '缺卡': '缺卡',
        'ABSENT': '缺勤',
        '缺勤': '缺勤',
        'LEAVE': '请假',
        '请假': '请假'
      };
      return statusMap[status] || '未打卡';
    },
  }
}
</script>

<style lang="scss" scoped>
.attendance-page {
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

.clock-card {
  margin-bottom: 20px;
}

.clock-container {
  text-align: center;
  padding: 40px 20px;
}

.current-time {
  margin-bottom: 40px;
}

.time-display {
  font-size: 48px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.date-display {
  font-size: 16px;
  color: #909399;
}

.clock-actions {
  display: flex;
  gap: 40px;
  justify-content: center;
  margin-bottom: 30px;
}

.clock-btn {
  width: 180px;
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  border-radius: 12px;

  i {
    font-size: 32px;
    margin-bottom: 12px;
  }
}

.clock-time {
  margin-top: 8px;
  font-size: 14px;
  font-weight: normal;
}

.attendance-status {
  margin-bottom: 20px;
}

.forget-tips {
  margin-top: 20px;
}

.stats-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.month-selector {
  display: flex;
  align-items: center;
}

.stats-loading {
  text-align: center;
  padding: 40px;
  color: #909399;

  i {
    margin-right: 8px;
  }
}

.stats-content {
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 15px;
    margin-bottom: 20px;
  }

  .additional-stats {
    padding: 15px;
    background: #f8f9fa;
    border-radius: 8px;

    .stat-summary {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 0;

      .summary-label {
        color: #606266;
        font-size: 14px;
      }

      .summary-value {
        color: #303133;
        font-weight: 500;
      }
    }
  }
}

.stat-item {
  text-align: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .stat-value {
    font-size: 24px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .month-selector {
    width: 100%;
    justify-content: flex-end;
  }
}

.info-card, .rules-card {
  margin-bottom: 20px;
}

.detail-list {
  .detail-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .label {
      color: #606266;
    }

    .value {
      color: #303133;
      font-weight: 500;
    }
  }
}

.rules-list {
  .rule-item {
    display: flex;
    align-items: center;
    padding: 8px 0;
    color: #606266;

    i {
      margin-right: 8px;
      color: #409EFF;
    }
  }
}

.records-card {
  margin-top: 20px;
}

.late-time {
  color: #E6A23C;
  font-weight: 500;
}

.early-time {
  color: #E6A23C;
  font-weight: 500;
}

// 响应式设计
@media (max-width: 768px) {
  .clock-actions {
    flex-direction: column;
    gap: 20px;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .clock-btn {
    width: 100%;
  }
}
</style>