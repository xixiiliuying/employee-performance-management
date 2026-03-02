package com.entity.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.converters.localdate.LocalDateStringConverter;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

/**
 * 考勤记录视图对象
 */
public class AttendanceVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ExcelProperty("序号")
    private Long id;

    @ExcelProperty("员工ID")
    private Long staffId;

    @ExcelProperty("员工姓名")
    private String staffName;

    /** 考勤日期 LocalDate → EasyExcel 支持 */
    @ExcelProperty(value = "考勤日期", converter = LocalDateStringConverter.class)
    @DateTimeFormat("yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    /** 上班时间 LocalTime → 使用你自定义的 LocalTimeStringConverter */
    @ExcelProperty(value = "上班时间", converter = com.converter.LocalTimeStringConverter.class)
    @DateTimeFormat("HH:mm:ss")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime checkInTime;

    /** 下班时间 LocalTime → 同上 */
    @ExcelProperty(value = "下班时间", converter = com.converter.LocalTimeStringConverter.class)
    @DateTimeFormat("HH:mm:ss")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime checkOutTime;

    @ExcelProperty("迟到分钟")
    private Integer lateMin;

    @ExcelProperty("早退分钟")
    private Integer earlyMin;

    @ExcelProperty("考勤状态")
    private String status;

    @ExcelProperty("原因说明")
    private String reason;

    @ExcelProperty("部门名称")
    private String departmentName;

    @ExcelProperty("岗位名称")
    private String jobName;

    // 以下为统计字段，不导出
    private Integer absentDays;
    private Integer totalRecords;
    private Integer normalCount;
    private Integer lateCount;
    private Integer earlyCount;
    private Integer lateEarlyDays;
    private Integer missingCardDays;
    private Integer absentCount;

    /** 创建时间（后端给前端时格式化） */
    @ExcelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    // Getters & Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }

    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalTime checkInTime) { this.checkInTime = checkInTime; }

    public LocalTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalTime checkOutTime) { this.checkOutTime = checkOutTime; }

    public Integer getLateMin() { return lateMin; }
    public void setLateMin(Integer lateMin) { this.lateMin = lateMin; }

    public Integer getEarlyMin() { return earlyMin; }
    public void setEarlyMin(Integer earlyMin) { this.earlyMin = earlyMin; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getJobName() { return jobName; }
    public void setJobName(String jobName) { this.jobName = jobName; }

    public Integer getAbsentDays() { return absentDays; }
    public void setAbsentDays(Integer absentDays) { this.absentDays = absentDays; }

    public Integer getTotalRecords() { return totalRecords; }
    public void setTotalRecords(Integer totalRecords) { this.totalRecords = totalRecords; }

    public Integer getNormalCount() { return normalCount; }
    public void setNormalCount(Integer normalCount) { this.normalCount = normalCount; }

    public Integer getLateCount() { return lateCount; }
    public void setLateCount(Integer lateCount) { this.lateCount = lateCount; }

    public Integer getEarlyCount() { return earlyCount; }
    public void setEarlyCount(Integer earlyCount) { this.earlyCount = earlyCount; }

    public Integer getLateEarlyDays() { return lateEarlyDays; }
    public void setLateEarlyDays(Integer lateEarlyDays) { this.lateEarlyDays = lateEarlyDays; }

    public Integer getMissingCardDays() { return missingCardDays; }
    public void setMissingCardDays(Integer missingCardDays) { this.missingCardDays = missingCardDays; }

    public Integer getAbsentCount() { return absentCount; }
    public void setAbsentCount(Integer absentCount) { this.absentCount = absentCount; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
