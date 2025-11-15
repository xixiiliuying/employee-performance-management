package com.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 岗位
 * 数据库通用操作实体类（普通增删改查）
 * @author
 * @email
 * @date 2021-05-07 10:42:31
 */
@TableName("jobs")
public class JobEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键id (AUTO_INCREMENT)
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	/**
	 * 岗位名称
	 */
	private String jname;

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

	public String getJname() {
		return jname;
	}

	public void setJname(String jname) {
		this.jname = jname;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	// 构造函数
	public JobEntity() {
	}

	public JobEntity(Long id, String jname, Date createTime) {
		this.id = id;
		this.jname = jname;
		this.createTime = createTime;
	}
}
