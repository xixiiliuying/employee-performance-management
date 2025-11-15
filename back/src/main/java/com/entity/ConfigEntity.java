package com.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;

/**
 * 类说明 : 系统配置表
 */
@TableName("config")
public class ConfigEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键id (AUTO_INCREMENT)
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	/**
	 * 配置参数名称 - 修改为name以匹配数据库字段
	 */
	private String name;

	/**
	 * 配置参数值
	 */
	private String value;

	/**
	 * 配置说明
	 */
	private String description;

	// ==================== Getter and Setter ====================

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	// 构造函数
	public ConfigEntity() {
	}

	public ConfigEntity(Long id, String name, String value, String description) {
		this.id = id;
		this.name = name;
		this.value = value;
		this.description = description;
	}

	@Override
	public String toString() {
		return "ConfigEntity{" +
				"id=" + id +
				", name='" + name + '\'' +
				", value='" + value + '\'' +
				", description='" + description + '\'' +
				'}';
	}
}