package com.entity;

import java.io.Serializable;
import java.sql.Time;
import java.util.Date;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 考勤信息
 * 数据库通用操作实体类（普通增删改查）
 * @author
 * @email
 * @date 2021-05-07 10:42:31
 */
@TableName("attendance")
public class AttendanceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键id (AUTO_INCREMENT)
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 员工ID (外键, staffs.id)
     */
    private Long staffId;

    /**
     * 日期
     */
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;

    /**
     * 签到时间
     */
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "HH:mm:ss")
    @DateTimeFormat(pattern = "HH:mm:ss")
    private Time checkInTime;

    /**
     * 签退时间
     */
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "HH:mm:ss")
    @DateTimeFormat(pattern = "HH:mm:ss")
    private Time checkOutTime;

    /**
     * 迟到分钟数 (default 0)
     */
    private Integer lateMin;

    /**
     * 早退分钟数 (default 0)
     */
    private Integer earlyMin;

    /**
     * 状态: 正常 / 迟到 / 早退 / 缺勤 / 请假 (default '正常')
     */
    private String status;

    /**
     * 理由
     */
    private String reason;

    /**
     * 创建时间
     */
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    // ==================== Getter and Setter ====================

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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Time getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(Time checkInTime) {
        this.checkInTime = checkInTime;
    }

    public Time getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(Time checkOutTime) {
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

    // 构造函数
    public AttendanceEntity() {
    }

    public AttendanceEntity(Long id, Long staffId, Date date, Time checkInTime, Time checkOutTime,
                            Integer lateMin, Integer earlyMin, String status, String reason, Date createTime) {
        this.id = id;
        this.staffId = staffId;
        this.date = date;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.lateMin = lateMin;
        this.earlyMin = earlyMin;
        this.status = status;
        this.reason = reason;
        this.createTime = createTime;
    }
}
