package com.entity.model;

import java.io.Serializable;

/**
 * 员工
 * 接收传参的实体类
 *（实际开发中配合移动端接口开发手动去掉些没用的字段， 后端一般用entity就够用了）
 * 取自ModelAndView 的model名称
 * @author
 * @email
 * @date 2021-05-07 10:42:31
 */
public class 		UserModel implements Serializable {
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
	 * 密码
	 */
	private String password;

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
	 * 设置：员工号
	 */
	public void setSid(Integer sid) {
		this.sid = sid;
	}

	/**
	 * 获取：员工号
	 */
	public Integer getSid() {
		return sid;
	}

	/**
	 * 设置：员工姓名
	 */
	public void setSname(String sname) {
		this.sname = sname;
	}

	/**
	 * 获取：员工姓名
	 */
	public String getSname() {
		return sname;
	}

	/**
	 * 设置：密码
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * 获取：密码
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * 设置：性别
	 */
	public void setGender(String gender) {
		this.gender = gender;
	}

	/**
	 * 获取：性别
	 */
	public String getGender() {
		return gender;
	}

	/**
	 * 设置：角色
	 */
	public void setRole(String role) {
		this.role = role;
	}

	/**
	 * 获取：角色
	 */
	public String getRole() {
		return role;
	}

	/**
	 * 设置：入职日期
	 */
	public void setHireDate(String hireDate) {
		this.hireDate = hireDate;
	}

	/**
	 * 获取：入职日期
	 */
	public String getHireDate() {
		return hireDate;
	}

	/**
	 * 设置：联系电话
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * 获取：联系电话
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * 设置：邮箱
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * 获取：邮箱
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * 设置：部门ID
	 */
	public void setDeptId(Long deptId) {
		this.deptId = deptId;
	}

	/**
	 * 获取：部门ID
	 */
	public Long getDeptId() {
		return deptId;
	}

	/**
	 * 设置：岗位ID
	 */
	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}

	/**
	 * 获取：岗位ID
	 */
	public Long getJobId() {
		return jobId;
	}

}