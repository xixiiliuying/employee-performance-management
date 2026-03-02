package com.entity.vo;

import java.io.Serializable;

/**
 * 员工 - 手机端接口返回实体辅助类
 */
public class UserVO implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 员工号
	 */
	private Integer sid;

	/**
	 * 员工姓名
	 */
	private String sname;

	/**
	 * 性别
	 */
	private String gender;

	/**
	 * 角色
	 */
	private String role;

	/**
	 * 入职日期
	 */
	private String hireDate;

	/**
	 * 联系电话
	 */
	private String phone;

	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * 部门ID
	 */
	private Long deptId;

	/**
	 * 岗位ID
	 */
	private Long jobId;

	/**
	 * 部门名称
	 */
	private String deptName;

	/**
	 * 岗位名称
	 */
	private String jobName;

	// getters and setters
	public Integer getSid() { return sid; }
	public void setSid(Integer sid) { this.sid = sid; }

	public String getSname() { return sname; }
	public void setSname(String sname) { this.sname = sname; }

	public String getGender() { return gender; }
	public void setGender(String gender) { this.gender = gender; }

	public String getRole() { return role; }
	public void setRole(String role) { this.role = role; }

	public String getHireDate() { return hireDate; }
	public void setHireDate(String hireDate) { this.hireDate = hireDate; }

	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public Long getDeptId() { return deptId; }
	public void setDeptId(Long deptId) { this.deptId = deptId; }

	public Long getJobId() { return jobId; }
	public void setJobId(Long jobId) { this.jobId = jobId; }

	public String getDeptName() { return deptName; }
	public void setDeptName(String deptName) { this.deptName = deptName; }

	public String getJobName() { return jobName; }
	public void setJobName(String jobName) { this.jobName = jobName; }
}