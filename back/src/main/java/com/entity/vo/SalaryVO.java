package com.entity.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;

public class SalaryVO {
    @ExcelProperty("序号")
    private Long id;
    @ExcelProperty("员工ID")
    private Long staffId;
    @ExcelProperty("员工编号")
    private String staffSid;
    @ExcelProperty("姓名")
    private String staffName;
    @ExcelProperty("部门")
    private String deptName;
    @ExcelProperty("职位")
    private String jobName;

    // 月份字段（保持 Date 类型，与数据库一致）
    @ExcelProperty("月份")
    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    private Date month;

    // 薪资构成字段（与数据库结构完全对应）
    @ExcelProperty("基础工资")
    private BigDecimal actualBasePay;     // 基础工资

    @ExcelProperty("加班费")
    private BigDecimal overtimePay;       // 加班费

    @ExcelProperty("请假天数")
    private BigDecimal leaveDays;         // 请假天数
    @ExcelProperty("请假扣款")
    private BigDecimal leaveDeduction;    // 请假扣款
    @ExcelProperty("实发工资")
    private BigDecimal totalSalary;       // 实发工资
    @ExcelProperty("状态")
    private String status;                // 状态：待审核/已发放
    @ExcelProperty("计算时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date calcTime;                // 计算时间
    @ExcelProperty("备注")
    private String remarks;               // 备注

    // 🔥 新增：前端需要的考勤统计字段
    @ExcelProperty("迟到次数")
    private Integer lateCount;            // 迟到次数
    @ExcelProperty("早退次数")
    private Integer earlyLeaveCount;      // 早退次数
    @ExcelProperty("迟到早退次数")
    private Integer lateEarlyCount;       // 迟到早退次数
    @ExcelProperty("缺卡次数")
    private Integer missingCardCount;     // 缺卡次数
    @ExcelProperty("缺勤天数")
    private BigDecimal absenceDays;       // 缺勤天数

    @ExcelProperty("缺勤扣款")
    private BigDecimal absenceDeduction;  // 缺勤扣款
    @ExcelProperty("迟到早退扣款")
    private BigDecimal lateDeduction;     // 迟到早退扣款
    @ExcelProperty("缺卡扣款")
    private BigDecimal missingCardDeduction; // 缺卡扣款
    @ExcelProperty("其他扣款")
    private BigDecimal otherDeduction;    // 其他扣款
    @ExcelProperty("其他扣款说明")
    private String otherDeductionRemark;  // 其他扣款说明

    // ============ getters and setters ============
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }

    public String getStaffSid() { return staffSid; }
    public void setStaffSid(String staffSid) { this.staffSid = staffSid; }

    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }

    public String getJobName() { return jobName; }
    public void setJobName(String jobName) { this.jobName = jobName; }

    public Date getMonth() { return month; }
    public void setMonth(Date month) { this.month = month; }

    public BigDecimal getActualBasePay() { return actualBasePay; }
    public void setActualBasePay(BigDecimal actualBasePay) { this.actualBasePay = actualBasePay; }

    public BigDecimal getOvertimePay() { return overtimePay; }
    public void setOvertimePay(BigDecimal overtimePay) { this.overtimePay = overtimePay; }

    public BigDecimal getLeaveDays() { return leaveDays; }
    public void setLeaveDays(BigDecimal leaveDays) { this.leaveDays = leaveDays; }

    public BigDecimal getLeaveDeduction() { return leaveDeduction; }
    public void setLeaveDeduction(BigDecimal leaveDeduction) { this.leaveDeduction = leaveDeduction; }

    public BigDecimal getTotalSalary() { return totalSalary; }
    public void setTotalSalary(BigDecimal totalSalary) { this.totalSalary = totalSalary; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCalcTime() { return calcTime; }
    public void setCalcTime(Date calcTime) { this.calcTime = calcTime; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    // 🔥 新增字段的getters和setters
    public Integer getLateCount() { return lateCount; }
    public void setLateCount(Integer lateCount) { this.lateCount = lateCount; }

    public Integer getEarlyLeaveCount() { return earlyLeaveCount; }
    public void setEarlyLeaveCount(Integer earlyLeaveCount) { this.earlyLeaveCount = earlyLeaveCount; }

    public Integer getLateEarlyCount() { return lateEarlyCount; }
    public void setLateEarlyCount(Integer lateEarlyCount) { this.lateEarlyCount = lateEarlyCount; }

    public Integer getMissingCardCount() { return missingCardCount; }
    public void setMissingCardCount(Integer missingCardCount) { this.missingCardCount = missingCardCount; }

    public BigDecimal getAbsenceDays() { return absenceDays; }
    public void setAbsenceDays(BigDecimal absenceDays) { this.absenceDays = absenceDays; }

    public BigDecimal getAbsenceDeduction() { return absenceDeduction; }
    public void setAbsenceDeduction(BigDecimal absenceDeduction) { this.absenceDeduction = absenceDeduction; }

    public BigDecimal getLateDeduction() { return lateDeduction; }
    public void setLateDeduction(BigDecimal lateDeduction) { this.lateDeduction = lateDeduction; }

    public BigDecimal getMissingCardDeduction() { return missingCardDeduction; }
    public void setMissingCardDeduction(BigDecimal missingCardDeduction) { this.missingCardDeduction = missingCardDeduction; }

    public BigDecimal getOtherDeduction() { return otherDeduction; }
    public void setOtherDeduction(BigDecimal otherDeduction) { this.otherDeduction = otherDeduction; }

    public String getOtherDeductionRemark() { return otherDeductionRemark; }
    public void setOtherDeductionRemark(String otherDeductionRemark) { this.otherDeductionRemark = otherDeductionRemark; }

    // ============ 工具方法 ============
    /**
     * 获取总扣款（所有扣款类型的总和）
     */
    public BigDecimal getTotalDeductions() {
        BigDecimal total = BigDecimal.ZERO;
        if (leaveDeduction != null) total = total.add(leaveDeduction);
        if (absenceDeduction != null) total = total.add(absenceDeduction);
        if (lateDeduction != null) total = total.add(lateDeduction);
        if (missingCardDeduction != null) total = total.add(missingCardDeduction);
        if (otherDeduction != null) total = total.add(otherDeduction);
        return total;
    }

    /**
     * 格式化月份显示（用于前端展示）
     */
    public String getDisplayMonth() {
        if (month != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy年MM月");
            return sdf.format(month);
        }
        return "";
    }

    /**
     * 获取状态的中文描述
     */
    public String getStatusText() {
        if ("待审核".equals(status)) {
            return "待审核";
        } else if ("已发放".equals(status)) {
            return "已发放";
        }
        return status;
    }

    /**
     * 获取状态标签类型（用于前端显示）
     */
    public String getStatusType() {
        if ("待审核".equals(status)) {
            return "warning";
        } else if ("已发放".equals(status)) {
            return "success";
        }
        return "info";
    }

    @Override
    public String toString() {
        return "SalaryVO{" +
                "id=" + id +
                ", staffId=" + staffId +
                ", staffName='" + staffName + '\'' +
                ", deptName='" + deptName + '\'' +
                ", month=" + month +
                ", actualBasePay=" + actualBasePay +
                ", overtimePay=" + overtimePay +
                ", totalSalary=" + totalSalary +
                ", status='" + status + '\'' +
                '}';
    }
}