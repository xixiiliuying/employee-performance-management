package com.service.impl;

import com.alibaba.excel.EasyExcel;
import com.dao.AttendanceDao;
import com.dao.UserDao;
import com.entity.AttendanceEntity;
import com.entity.UserEntity;
import com.entity.model.AttendanceModel;
import com.entity.vo.AttendanceVO;
import com.service.AttendanceService;
import com.utils.DateUtils;
import com.utils.PageUtils;
import com.utils.R;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考勤服务实现类
 */
@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceDao attendanceDao;

    @Autowired
    private UserDao userDao;

    // 考勤规则配置
    private static final LocalTime WORK_START_TIME = LocalTime.of(9, 0);
    private static final LocalTime WORK_END_TIME = LocalTime.of(18, 0);
    private static final int LATE_THRESHOLD_MINUTES = 30; // 9:30后算迟到
    private static final int EARLY_THRESHOLD_MINUTES = 30; // 18:00前30分钟内算早退

    @Override
    @Transactional
    public AttendanceVO checkIn(Long staffId) {
        LocalDate today = LocalDate.now();

        // 检查今天是否已打卡
        AttendanceEntity existing = attendanceDao.selectByStaffAndDate(staffId, today.toString());
        if (existing != null && existing.getCheckInTime() != null) {
            throw new RuntimeException("今日已打卡，请勿重复操作");
        }

        LocalTime now = LocalTime.now();

        AttendanceEntity attendance = new AttendanceEntity();
        if (existing != null) {
            attendance.setId(existing.getId());
            // 如果已有下班打卡，重新计算全天状态
            if (existing.getCheckOutTime() != null) {
                String status = calculateFullDayStatus(now, existing.getCheckOutTime().toLocalTime());
                attendance.setStatus(status);
            } else {
                // 只有上班打卡，状态为缺卡
                attendance.setStatus("缺卡");
            }
        } else {
            attendance.setStaffId(staffId);
            attendance.setDate(java.sql.Date.valueOf(today));
            // 新建记录只有上班打卡，状态为缺卡
            attendance.setStatus("缺卡");
        }

        attendance.setCheckInTime(Time.valueOf(now));
        attendance.setLateMin(calculateLateMinutes(now));
        attendance.setCreateTime(new Date());

        if (existing != null) {
            attendanceDao.update(attendance);
        } else {
            attendanceDao.insert(attendance);
        }

        return convertToVO(attendance);
    }

    @Override
    @Transactional
    public AttendanceVO checkOut(Long staffId) {
        LocalDate today = LocalDate.now();
        String todayStr = today.toString();

        // 查找今天的考勤记录
        AttendanceEntity attendance = attendanceDao.selectByStaffAndDate(staffId, todayStr);

        // 如果没有记录，创建新记录（允许直接下班打卡）
        if (attendance == null) {
            attendance = new AttendanceEntity();
            attendance.setStaffId(staffId);
            attendance.setDate(java.sql.Date.valueOf(today));
            attendance.setCreateTime(new Date());

            // 只有下班打卡，状态设为"缺卡"
            attendance.setStatus("缺卡");
            attendanceDao.insert(attendance);
        }

        // 检查是否已下班打卡
        if (attendance.getCheckOutTime() != null) {
            throw new RuntimeException("今日已签退，请勿重复操作");
        }

        LocalTime now = LocalTime.now();

        // 重新计算全天状态
        LocalTime checkInTime = attendance.getCheckInTime() != null ?
                attendance.getCheckInTime().toLocalTime() : null;
        String status = calculateFullDayStatus(checkInTime, now);

        attendance.setCheckOutTime(java.sql.Time.valueOf(now));
        attendance.setStatus(status);
        attendance.setEarlyMin(calculateEarlyMinutes(now));

        attendanceDao.update(attendance);

        return convertToVO(attendance);
    }

    @Override
    @Transactional
    public AttendanceVO manualCheck(AttendanceModel model) {
        try {
            // 1. 基础参数验证
            if (model.getStaffId() == null) {
                throw new RuntimeException("员工ID不能为空");
            }

            // 2. 处理日期和时间
            LocalDate localDate = processDate(model);
            LocalTime checkInTime = processCheckInTime(model);
            LocalTime checkOutTime = processCheckOutTime(model);

            // 验证至少有一个时间
            if (checkInTime == null && checkOutTime == null) {
                throw new RuntimeException("至少需要提供上班或下班时间");
            }

            // 3. 检查是否已存在记录
            String dateStr = localDate.toString();
            AttendanceEntity existing = attendanceDao.selectByStaffAndDate(model.getStaffId(), dateStr);

            // 4. 创建考勤记录
            AttendanceEntity attendance = new AttendanceEntity();
            attendance.setStaffId(model.getStaffId());
            attendance.setDate(java.sql.Date.valueOf(localDate));

            if (checkInTime != null) {
                attendance.setCheckInTime(Time.valueOf(checkInTime));
            }
            if (checkOutTime != null) {
                attendance.setCheckOutTime(Time.valueOf(checkOutTime));
            }

            attendance.setReason(model.getReason());
            attendance.setCreateTime(new Date());

            // 5. 计算考勤状态 - 使用修正后的方法
            String status = calculateFullDayStatus(checkInTime, checkOutTime);
            attendance.setStatus(status);
            attendance.setLateMin(calculateLateMinutes(checkInTime));
            attendance.setEarlyMin(calculateEarlyMinutes(checkOutTime));

            // 6. 保存到数据库
            if (existing != null) {
                attendance.setId(existing.getId());
                attendanceDao.update(attendance);
            } else {
                attendanceDao.insert(attendance);
            }

            // 7. 直接返回转换结果，不再重新查询
            return convertToVO(attendance);

        } catch (Exception e) {
            // 去掉重复的错误包装
            if (e.getMessage().contains("补打卡失败")) {
                throw e;
            }
            throw new RuntimeException("补打卡失败: " + e.getMessage());
        }
    }

    // ============ 私有方法 ============

    /**
     * 处理日期参数 - 修复版本
     */
    private LocalDate processDate(AttendanceModel model) {
        try {
            // 优先使用 manualDate
            if (model.getManualDate() != null && !model.getManualDate().isEmpty()) {
                return LocalDate.parse(model.getManualDate());
            }
            // 其次使用 date 字段
            else if (model.getDate() != null) {
                return model.getDate();
            }
            // 最后使用当前日期
            else {
                return LocalDate.now();
            }
        } catch (Exception e) {
            throw new RuntimeException("日期格式错误，请使用 yyyy-MM-dd 格式");
        }
    }

    /**
     * 处理上班时间
     */
    private LocalTime processCheckInTime(AttendanceModel model) {
        try {
            if (model.getManualTime() != null && !model.getManualTime().isEmpty()) {
                // 如果有 manualTime，根据 checkType 决定
                if ("check-in".equals(model.getCheckType()) || model.getCheckType() == null) {
                    return LocalTime.parse(model.getManualTime());
                }
            }
            // 返回原始的 checkInTime（可能为 null）
            return model.getCheckInTime();
        } catch (Exception e) {
            throw new RuntimeException("上班时间格式错误，请使用 HH:mm 格式: " + e.getMessage());
        }
    }

    /**
     * 处理下班时间
     */
    private LocalTime processCheckOutTime(AttendanceModel model) {
        try {
            if (model.getManualTime() != null && !model.getManualTime().isEmpty()) {
                // 如果有 manualTime，根据 checkType 决定
                if ("check-out".equals(model.getCheckType())) {
                    return LocalTime.parse(model.getManualTime());
                }
            }
            // 返回原始的 checkOutTime（可能为 null）
            return model.getCheckOutTime();
        } catch (Exception e) {
            throw new RuntimeException("下班时间格式错误，请使用 HH:mm 格式: " + e.getMessage());
        }
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        // 处理查询参数
        if (params.containsKey("month") && params.get("month") != null) {
            String month = (String) params.get("month");

            // 修复：正确计算月份的开始和结束日期
            try {
                LocalDate startDate = LocalDate.parse(month + "-01");
                LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth()); // 获取当月最后一天

                params.put("startDate", startDate.toString());
                params.put("endDate", endDate.toString());

                System.out.println("查询月份: " + month + ", 日期范围: " + startDate + " 到 " + endDate);
            } catch (Exception e) {
                System.err.println("月份参数格式错误: " + month + ", 错误: " + e.getMessage());
                // 设置默认值，避免查询失败
                params.put("startDate", month + "-01");
                params.put("endDate", month + "-28"); // 安全值
            }
        }

        List<AttendanceVO> attendanceList = attendanceDao.selectAttendanceList(params);
        int total = attendanceDao.countAttendanceList(params);

        PageUtils pageUtils = new PageUtils(attendanceList, total,
                Integer.parseInt(params.getOrDefault("page", "1").toString()),
                Integer.parseInt(params.getOrDefault("limit", "10").toString()));

        return pageUtils;
    }

    @Override
    public Map<String, Object> getPersonalStats(Long staffId, String month) {
        Map<String, Object> stats;

        // 如果传入了月份参数，确保日期范围正确
        if (month != null && !month.isEmpty()) {
            try {
                LocalDate startDate = LocalDate.parse(month + "-01");
                LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

                // 调用DAO方法时传入正确的日期范围
                stats = attendanceDao.selectPersonalStatsByDateRange(staffId, startDate.toString(), endDate.toString());
            } catch (Exception e) {
                System.err.println("月份参数错误，使用默认统计: " + e.getMessage());
                stats = attendanceDao.selectPersonalStats(staffId, month);
            }
        } else {
            stats = attendanceDao.selectPersonalStats(staffId, month);
        }

        if (stats == null) {
            stats = new HashMap<>();
        }

        // 设置默认值
        stats.putIfAbsent("normalDays", 0);
        stats.putIfAbsent("lateDays", 0);
        stats.putIfAbsent("earlyDays", 0);
        stats.putIfAbsent("absentDays", 0);
        stats.putIfAbsent("lateEarlyDays", 0);
        stats.putIfAbsent("missingCardDays", 0);
        stats.putIfAbsent("totalDays", 0);

        return stats;
    }

    @Override
    public Map<String, Object> getDepartmentStats(String month, Long deptId) {
        Map<String, Object> stats;

        if (deptId != null) {
            // 查询指定部门的统计
            stats = attendanceDao.selectSingleDepartmentStats(month, deptId);
        } else {
            // 查询所有部门的汇总统计
            stats = attendanceDao.selectAllDepartmentSummary(month);
        }

        if (stats == null) {
            stats = createEmptyStats();
        }

        // 使用 Number 类型来安全地处理 Long 和 Integer
        Number totalStaff = (Number) stats.getOrDefault("totalStaff", 0);
        Number attendedStaff = (Number) stats.getOrDefault("attendedStaff", 0);
        Number totalRecords = (Number) stats.getOrDefault("totalRecords", 0);
        Number normalRecords = (Number) stats.getOrDefault("normalRecords", 0);
        Number lateEarlyRecords = (Number) stats.getOrDefault("lateEarlyRecords", 0);

        // 计算员工出勤率
        if (totalStaff != null && totalStaff.intValue() > 0) {
            double staffAttendanceRate = attendedStaff.doubleValue() / totalStaff.doubleValue() * 100;
            stats.put("staffAttendanceRate", String.format("%.2f%%", staffAttendanceRate));
        } else {
            stats.put("staffAttendanceRate", "0.00%");
        }

        // 计算记录正常率
        if (totalRecords != null && totalRecords.intValue() > 0) {
            double recordNormalRate = normalRecords.doubleValue() / totalRecords.doubleValue() * 100;
            stats.put("recordNormalRate", String.format("%.2f%%", recordNormalRate));
        } else {
            stats.put("recordNormalRate", "0.00%");
        }

        return stats;
    }

    /**
     * 创建空的统计数据
     */
    private Map<String, Object> createEmptyStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStaff", 0L);
        stats.put("attendedStaff", 0L);
        stats.put("totalRecords", 0L);
        stats.put("normalRecords", 0L);
        stats.put("lateRecords", 0L);
        stats.put("earlyRecords", 0L);
        stats.put("absentRecords", 0L);
        stats.put("staffAttendanceRate", "0.00%");
        stats.put("recordNormalRate", "0.00%");
        stats.put("lateEarlyRecords", 0L);

        // 如果是部门查询，添加部门信息
        stats.put("deptId", 0L);
        stats.put("deptName", "无数据");
        stats.put("departmentCount", 0L);

        return stats;
    }

    @Override
    public Map<String, Object> getTodayStatus(Long staffId) {
        String today = LocalDate.now().toString();
        AttendanceEntity attendance = attendanceDao.selectByStaffAndDate(staffId, today);

        Map<String, Object> status = new HashMap<>();
        status.put("hasCheckedIn", attendance != null && attendance.getCheckInTime() != null);
        status.put("hasCheckedOut", attendance != null && attendance.getCheckOutTime() != null);

        // 修复返回的时间格式
        if (attendance != null && attendance.getCheckInTime() != null) {
            status.put("checkInTime", attendance.getCheckInTime().toLocalTime().toString());
        } else {
            status.put("checkInTime", null);
        }

        if (attendance != null && attendance.getCheckOutTime() != null) {
            status.put("checkOutTime", attendance.getCheckOutTime().toLocalTime().toString());
        } else {
            status.put("checkOutTime", null);
        }

        // 修正状态显示逻辑
        if (attendance == null) {
            status.put("status", "未打卡");
        } else if (attendance.getStatus() == null) {
            // 重新计算状态
            String calculatedStatus = calculateFullDayStatus(
                    attendance.getCheckInTime() != null ? attendance.getCheckInTime().toLocalTime() : null,
                    attendance.getCheckOutTime() != null ? attendance.getCheckOutTime().toLocalTime() : null
            );
            status.put("status", calculatedStatus);
        } else {
            status.put("status", attendance.getStatus());
        }

        return status;
    }

    @Override
    public boolean hasCheckedIn(Long staffId, String date) {
        AttendanceEntity attendance = attendanceDao.selectByStaffAndDate(staffId, date);
        return attendance != null && attendance.getCheckInTime() != null;
    }

    @Override
    public boolean hasCheckedOut(Long staffId, String date) {
        AttendanceEntity attendance = attendanceDao.selectByStaffAndDate(staffId, date);
        return attendance != null && attendance.getCheckOutTime() != null;
    }

    @Override
    public AttendanceVO getDetail(Long id) {
        AttendanceEntity attendance = attendanceDao.selectById(id);
        if (attendance == null) {
            throw new RuntimeException("考勤记录不存在");
        }
        return convertToVO(attendance);
    }

    @Override
    @Transactional
    public R updateAttendance(AttendanceModel model) {
        if (model.getId() == null) {
            return R.error("考勤记录ID不能为空");
        }

        AttendanceEntity attendance = attendanceDao.selectById(model.getId());
        if (attendance == null) {
            return R.error("考勤记录不存在");
        }

        BeanUtils.copyProperties(model, attendance);

        // 重新计算状态 - 使用修正后的方法
        if (attendance.getCheckInTime() != null || attendance.getCheckOutTime() != null) {
            String status = calculateFullDayStatus(
                    attendance.getCheckInTime() != null ? attendance.getCheckInTime().toLocalTime() : null,
                    attendance.getCheckOutTime() != null ? attendance.getCheckOutTime().toLocalTime() : null
            );
            attendance.setStatus(status);

            // 重新计算迟到早退分钟数
            attendance.setLateMin(calculateLateMinutes(
                    attendance.getCheckInTime() != null ? attendance.getCheckInTime().toLocalTime() : null
            ));
            attendance.setEarlyMin(calculateEarlyMinutes(
                    attendance.getCheckOutTime() != null ? attendance.getCheckOutTime().toLocalTime() : null
            ));
        }

        int result = attendanceDao.update(attendance);
        return result > 0 ? R.ok("更新成功") : R.error("更新失败");
    }

    @Override
    @Transactional
    public R deleteAttendance(Long id) {
        AttendanceEntity attendance = attendanceDao.selectById(id);
        if (attendance == null) {
            return R.error("考勤记录不存在");
        }

        int result = attendanceDao.deleteById(id);
        return result > 0 ? R.ok("删除成功") : R.error("删除失败");
    }

    @Override
    @Transactional
    public R batchImport(List<AttendanceModel> attendanceList) {
        if (attendanceList == null || attendanceList.isEmpty()) {
            return R.error("导入数据不能为空");
        }

        int successCount = 0;
        List<String> errorMessages = new ArrayList<>();

        for (int i = 0; i < attendanceList.size(); i++) {
            AttendanceModel model = attendanceList.get(i);
            try {
                manualCheck(model);
                successCount++;
            } catch (Exception e) {
                errorMessages.add("第" + (i + 1) + "行数据导入失败: " + e.getMessage());
            }
        }

        String message = "成功导入 " + successCount + " 条记录";
        if (!errorMessages.isEmpty()) {
            message += "，失败 " + errorMessages.size() + " 条记录";
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("errorCount", errorMessages.size());
        result.put("errorMessages", errorMessages);

        return R.ok(message).put("data", result);
    }

    @Override
    public void exportAttendance(Map<String, Object> params, HttpServletResponse response) {
        try {
            // 1. 查询考勤数据 - 使用已注入的 attendanceDao
            List<AttendanceVO> attendanceList = attendanceDao.selectAttendanceList(params);

            // 2. 设置响应头
            String fileName = "考勤记录_" + System.currentTimeMillis() + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));

            // 3. 使用 EasyExcel 导出 - 直接使用 AttendanceVO 类
            EasyExcel.write(response.getOutputStream(), AttendanceVO.class)
                    .sheet("考勤记录")
                    .doWrite(attendanceList);

        } catch (Exception e) {
            // 将检查异常转换为运行时异常
            throw new RuntimeException("导出考勤记录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 计算上班打卡状态
     */
    private String calculateCheckInStatus(LocalTime checkInTime) {
        if (checkInTime == null) return null;

        LocalTime lateThreshold = WORK_START_TIME.plusMinutes(LATE_THRESHOLD_MINUTES);
        return checkInTime.isAfter(lateThreshold) ? "迟到" : "正常";
    }

    /**
     * 计算下班打卡状态
     */
    private String calculateCheckOutStatus(LocalTime checkOutTime) {
        if (checkOutTime == null) return null;

        LocalTime earlyThreshold = WORK_END_TIME.minusMinutes(EARLY_THRESHOLD_MINUTES);
        if (checkOutTime.isBefore(WORK_END_TIME) && checkOutTime.isAfter(earlyThreshold)) {
            return "早退";
        }
        return "正常";
    }

    /**
     * 计算全天考勤状态
     */
    private String calculateFullDayStatus(LocalTime checkInTime, LocalTime checkOutTime) {
        // 情况1: 全天未打卡
        if (checkInTime == null && checkOutTime == null) {
            return "缺勤";
        }

        // 情况2: 只有上班打卡
        if (checkInTime != null && checkOutTime == null) {
            return "缺卡";
        }

        // 情况3: 只有下班打卡
        if (checkInTime == null && checkOutTime != null) {
            return "缺卡";
        }

        // 情况4: 全天打卡，计算具体状态
        String checkInStatus = calculateCheckInStatus(checkInTime);
        String checkOutStatus = calculateCheckOutStatus(checkOutTime);

        // 迟到 + 早退 = 迟到早退
        if ("迟到".equals(checkInStatus) && "早退".equals(checkOutStatus)) {
            return "迟到早退";
        }
        // 迟到
        else if ("迟到".equals(checkInStatus)) {
            return "迟到";
        }
        // 早退
        else if ("早退".equals(checkOutStatus)) {
            return "早退";
        }
        // 正常
        else {
            return "正常";
        }
    }

    /**
     * 计算迟到分钟数
     */
    private int calculateLateMinutes(LocalTime checkInTime) {
        if (checkInTime == null) return 0;

        LocalTime lateThreshold = WORK_START_TIME.plusMinutes(LATE_THRESHOLD_MINUTES);
        if (checkInTime.isAfter(lateThreshold)) {
            return (int) java.time.Duration.between(lateThreshold, checkInTime).toMinutes();
        }
        return 0;
    }

    /**
     * 计算早退分钟数
     */
    private int calculateEarlyMinutes(LocalTime checkOutTime) {
        if (checkOutTime == null) return 0;

        if (checkOutTime.isBefore(WORK_END_TIME)) {
            return (int) java.time.Duration.between(checkOutTime, WORK_END_TIME).toMinutes();
        }
        return 0;
    }

    /**
     * 转换为VO对象 - 修复版本，正确处理日期时间转换
     */
    private AttendanceVO convertToVO(AttendanceEntity entity) {
        if (entity == null) {
            return null;
        }

        AttendanceVO vo = new AttendanceVO();

        // 复制基本属性
        vo.setId(entity.getId());
        vo.setStaffId(entity.getStaffId());
        vo.setLateMin(entity.getLateMin());
        vo.setEarlyMin(entity.getEarlyMin());
        vo.setStatus(entity.getStatus());
        vo.setReason(entity.getReason());
        vo.setCreateTime(entity.getCreateTime());

        // 修复日期转换 - 关键修改
        if (entity.getDate() != null) {
            try {
                // 直接转换为 LocalDate
                if (entity.getDate() instanceof java.sql.Date) {
                    vo.setDate(((java.sql.Date) entity.getDate()).toLocalDate());
                } else {
                    // 如果是 java.util.Date，先转换为 java.sql.Date
                    java.sql.Date sqlDate = new java.sql.Date(entity.getDate().getTime());
                    vo.setDate(sqlDate.toLocalDate());
                }
            } catch (Exception e) {
                System.err.println("日期转换失败: " + e.getMessage());
                vo.setDate(null);
            }
        }

        // 修复上班时间转换
        if (entity.getCheckInTime() != null) {
            try {
                vo.setCheckInTime(entity.getCheckInTime().toLocalTime());
            } catch (Exception e) {
                System.err.println("上班时间转换失败: " + e.getMessage());
                vo.setCheckInTime(null);
            }
        }

        // 修复下班时间转换
        if (entity.getCheckOutTime() != null) {
            try {
                vo.setCheckOutTime(entity.getCheckOutTime().toLocalTime());
            } catch (Exception e) {
                System.err.println("下班时间转换失败: " + e.getMessage());
                vo.setCheckOutTime(null);
            }
        }

        // 安全地查询员工信息
        try {
            UserEntity user = userDao.selectById(entity.getStaffId());
            if (user != null && user.getSname() != null) {
                vo.setStaffName(user.getSname());
            } else {
                vo.setStaffName("员工-" + entity.getStaffId());
            }
        } catch (Exception e) {
            System.err.println("查询员工信息失败，员工ID: " + entity.getStaffId() + ", 错误: " + e.getMessage());
            vo.setStaffName("员工-" + entity.getStaffId());
        }

        return vo;
    }
}