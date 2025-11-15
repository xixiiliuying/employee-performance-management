package com.entity.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * 部门
 * 手机端接口返回实体辅助类
 * （主要作用去除一些不必要的字段）
 * @author
 * @email
 * @date 2021-05-07 10:42:31
 */
public class DepartmentVO implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 部门ID
	 */
	private Long id;

	/**
	 * 部门名称
	 */
	private String dname;

	/**
	 * 部门负责人ID
	 */
	private Long managerId;

	/**
	 * 部门负责人姓名
	 */
	private String managerName;

	/**
	 * 员工数量
	 */
	private Integer staffCount;

	/**
	 * 创建时间
	 */
	private Date createTime;

	// getter 和 setter 方法
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDname() {
		return dname;
	}

	public void setDname(String dname) {
		this.dname = dname;
	}

	public Long getManagerId() {
		return managerId;
	}

	public void setManagerId(Long managerId) {
		this.managerId = managerId;
	}

	public String getManagerName() {
		return managerName;
	}

	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}

	public Integer getStaffCount() {
		return staffCount;
	}

	public void setStaffCount(Integer staffCount) {
		this.staffCount = staffCount;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}