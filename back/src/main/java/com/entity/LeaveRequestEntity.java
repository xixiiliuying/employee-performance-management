package com.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 请假信息
 * 数据库通用操作实体类（普通增删改查）
 * @author
 * @email
 * @date 2021-05-07 10:42:31
 */
@TableName("leave_request")
public class LeaveRequestEntity implements Serializable {
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
     * 请假类型: 病假 / 事假 等
     */
    private String leaveType;

    /**
     * 请假天数
     */
    private Double days;

    /**
     * 开始日期
     */
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    /**
     * 结束日期
     */
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    /**
     * 审批状态: 待审批 / 已批准 / 已拒绝 (default '待审批')
     */
    private String status;

    /**
     * 审批人员 (FK)
     */
    private Long hrId;

    /**
     * 审批时间
     */
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date approvalTime;

    /**
     * 创建时间 (DEFAULT CURRENT_TIMESTAMP)
     */
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 请假事由
     */
    private String reason;

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

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public Double getDays() {
        return days;
    }

    public void setDays(Double days) {
        this.days = days;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getHrId() {
        return hrId;
    }

    public void setHrId(Long hrId) {
        this.hrId = hrId;
    }

    public Date getApprovalTime() {
        return approvalTime;
    }

    public void setApprovalTime(Date approvalTime) {
        this.approvalTime = approvalTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    // 构造函数
    public LeaveRequestEntity() {
    }

    public LeaveRequestEntity(Long id, Long staffId, String leaveType, Double days, Date startDate,
                              Date endDate, String status, Long hrId, Date approvalTime, Date createTime, String reason) {
        this.id = id;
        this.staffId = staffId;
        this.leaveType = leaveType;
        this.days = days;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.hrId = hrId;
        this.approvalTime = approvalTime;
        this.createTime = createTime;
        this.reason = reason;
    }
}
