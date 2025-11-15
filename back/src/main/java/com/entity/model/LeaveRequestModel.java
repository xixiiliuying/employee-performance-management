package com.entity.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

/**
 * 请假申请
 * 接收传参的实体类
 */
public class LeaveRequestModel implements Serializable {
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
     * 请假类型
     */
    @NotBlank(message = "请假类型不能为空")
    private String leaveType;

    /**
     * 请假天数
     */
    @NotNull(message = "请假天数不能为空")
    private Double days;

    /**
     * 开始日期
     */
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    /**
     * 审批状态
     */
    private String status;

    /**
     * 审批人ID
     */
    private Long hrId;

    /**
     * 审批时间
     */
    private Date approvalTime;

    /**
     * 请假事由
     */
    @NotBlank(message = "请假事由不能为空")
    private String reason;

    /**
     * 创建时间
     */
    private Date createTime;

    // ==================== 支持字符串格式的字段 ====================

    /**
     * 开始日期字符串格式
     */
    private String startDateStr;

    /**
     * 结束日期字符串格式
     */
    private String endDateStr;

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

    public LocalDate getStartDate() {
        if (startDate != null) {
            return startDate;
        } else if (startDateStr != null && !startDateStr.trim().isEmpty()) {
            try {
                return LocalDate.parse(startDateStr);
            } catch (Exception e) {
                throw new RuntimeException("开始日期格式错误，请使用 yyyy-MM-dd 格式: " + startDateStr);
            }
        }
        return null;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        if (endDate != null) {
            return endDate;
        } else if (endDateStr != null && !endDateStr.trim().isEmpty()) {
            try {
                return LocalDate.parse(endDateStr);
            } catch (Exception e) {
                throw new RuntimeException("结束日期格式错误，请使用 yyyy-MM-dd 格式: " + endDateStr);
            }
        }
        return null;
    }

    public void setEndDate(LocalDate endDate) {
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

    public String getStartDateStr() {
        return startDateStr;
    }

    public void setStartDateStr(String startDateStr) {
        this.startDateStr = startDateStr;
    }

    public String getEndDateStr() {
        return endDateStr;
    }

    public void setEndDateStr(String endDateStr) {
        this.endDateStr = endDateStr;
    }

    // ==================== 工具方法 ====================

    /**
     * 计算请假天数
     */
    public Double calculateDays() {
        LocalDate start = getStartDate();
        LocalDate end = getEndDate();

        if (start == null || end == null) {
            return 0.0;
        }

        if (start.isAfter(end)) {
            throw new RuntimeException("开始日期不能晚于结束日期");
        }

        // 计算天数（包含开始和结束日期）
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
        return (double) daysBetween;
    }

    /**
     * 验证请假日期是否有效
     */
    public void validateDates() {
        LocalDate start = getStartDate();
        LocalDate end = getEndDate();

        if (start == null) {
            throw new RuntimeException("开始日期不能为空");
        }

        if (end == null) {
            throw new RuntimeException("结束日期不能为空");
        }

        if (start.isAfter(end)) {
            throw new RuntimeException("开始日期不能晚于结束日期");
        }

        // 不能申请过去的日期
        if (start.isBefore(LocalDate.now())) {
            throw new RuntimeException("不能申请过去的日期");
        }
    }

    @Override
    public String toString() {
        return "LeaveRequestModel{" +
                "id=" + id +
                ", staffId=" + staffId +
                ", leaveType='" + leaveType + '\'' +
                ", days=" + days +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status='" + status + '\'' +
                ", hrId=" + hrId +
                ", approvalTime=" + approvalTime +
                ", reason='" + reason + '\'' +
                ", createTime=" + createTime +
                ", startDateStr='" + startDateStr + '\'' +
                ", endDateStr='" + endDateStr + '\'' +
                '}';
    }
}