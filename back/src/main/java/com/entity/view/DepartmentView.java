package com.entity.view;

import com.entity.DepartmentEntity;
import com.baomidou.mybatisplus.annotations.TableName;
import org.apache.commons.beanutils.BeanUtils;
import java.lang.reflect.InvocationTargetException;

import java.io.Serializable;


/**
 * 部门
 * 后端返回视图实体辅助类
 * （通常后端关联的表或者自定义的字段需要返回使用）
 * @author
 * @email
 * @date 2021-05-07 10:42:31
 */
@TableName("departments")
public class DepartmentView extends DepartmentEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 部门负责人姓名
	 */
	private String managerName;

	/**
	 * 员工数量
	 */
	private Integer staffCount;

	/**
	 * 负责人工号
	 */
	private String managerSid;

	/**
	 * 负责人职位
	 */
	private String managerJobName;

	public DepartmentView(){
	}

	public DepartmentView(DepartmentEntity departmentEntity){
		try {
			BeanUtils.copyProperties(this, departmentEntity);
		} catch (IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// ==================== Getter and Setter ====================

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

	public String getManagerSid() {
		return managerSid;
	}

	public void setManagerSid(String managerSid) {
		this.managerSid = managerSid;
	}

	public String getManagerJobName() {
		return managerJobName;
	}

	public void setManagerJobName(String managerJobName) {
		this.managerJobName = managerJobName;
	}

	// toString 方法，便于调试
	@Override
	public String toString() {
		return "DepartmentView{" +
				"id=" + getId() +
				", dname='" + getDname() + '\'' +
				", managerId=" + getManagerId() +
				", managerName='" + managerName + '\'' +
				", staffCount=" + staffCount +
				", managerSid='" + managerSid + '\'' +
				", managerJobName='" + managerJobName + '\'' +
				", createTime=" + getCreateTime() +
				'}';
	}
}