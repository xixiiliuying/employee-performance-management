package com.entity.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

/**
 * 考勤记录
 * 接收传参的实体类
 */
public class AttendanceModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 员工ID
     */
    private Long staffId;

    /**
     * 考勤日期
     */
    private LocalDate date;

    /**
     * 签到时间
     */
    private LocalTime checkInTime;

    /**
     * 签退时间
     */
    private LocalTime checkOutTime;

    /**
     * 迟到分钟数
     */
    private Integer lateMin;

    /**
     * 早退分钟数
     */
    private Integer earlyMin;

    /**
     * 考勤状态（正常/迟到/早退/迟到早退/缺勤/请假）
     */
    private String status;

    /**
     * 理由
     */
    private String reason;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 打卡类型（check-in/check-out）
     */
    private String checkType;

    /**
     * 补打卡日期（用于手动补打卡）
     */
    private String manualDate;

    /**
     * 补打卡时间（用于手动补打卡）
     */
    private String manualTime;

    // 空构造函数
    public AttendanceModel() {
    }

    // 带参构造函数
    public AttendanceModel(Long staffId, LocalDate date, String checkType) {
        this.staffId = staffId;
        this.date = date;
        this.checkType = checkType;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public Integer getLateMin() {
        return lateMin;
    }

    public void setLateMin(Integer lateMin) {
        this.lateMin = lateMin;
    }

    public Integer getEarlyMin() {
        return earlyMin;
    }

    public void setEarlyMin(Integer earlyMin) {
        this.earlyMin = earlyMin;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCheckType() {
        return checkType;
    }

    public void setCheckType(String checkType) {
        this.checkType = checkType;
    }

    public String getManualDate() {
        return manualDate;
    }

    public void setManualDate(String manualDate) {
        this.manualDate = manualDate;
    }

    public String getManualTime() {
        return manualTime;
    }

    public void setManualTime(String manualTime) {
        this.manualTime = manualTime;
    }

    @Override
    public String toString() {
        return "AttendanceModel{" +
                "id=" + id +
                ", staffId=" + staffId +
                ", date=" + date +
                ", checkInTime=" + checkInTime +
                ", checkOutTime=" + checkOutTime +
                ", lateMin=" + lateMin +
                ", earlyMin=" + earlyMin +
                ", status='" + status + '\'' +
                ", reason='" + reason + '\'' +
                ", createTime=" + createTime +
                ", checkType='" + checkType + '\'' +
                ", manualDate='" + manualDate + '\'' +
                ", manualTime='" + manualTime + '\'' +
                '}';
    }
}