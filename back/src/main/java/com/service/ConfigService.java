package com.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.service.IService;
import com.entity.ConfigEntity;
import com.utils.PageUtils;

/**
 * 系统配置
 */
public interface ConfigService extends IService<ConfigEntity> {

	PageUtils queryPage(Map<String, Object> params);

	/**
	 * 获取基础工资配置
	 */
	BigDecimal getBaseSalary();

	/**
	 * 获取月工作天数
	 */
	int getWorkDaysPerMonth();

	/**
	 * 获取加班费率
	 */
	BigDecimal getOvertimeRate();

	/**
	 * 获取上班时间
	 */
	String getWorkStartTime();

	/**
	 * 获取下班时间
	 */
	String getWorkEndTime();

	/**
	 * 获取迟到阈值（分钟）
	 */
	int getLateThreshold();

	/**
	 * 获取早退阈值（分钟）
	 */
	int getEarlyThreshold();

	/**
	 * 获取公司名称
	 */
	String getCompanyName();

	/**
	 * 获取首页图片URL
	 */
	String getHomepage();

	/**
	 * 获取薪资计算日
	 */
	int getSalaryCalcDay();

	/**
	 * 获取所有薪资相关配置
	 */
	Map<String, Object> getAllSalaryConfigs();

	/**
	 * 获取薪资相关配置列表
	 */
	List<ConfigEntity> getSalaryConfigs();

	/**
	 * 根据名称更新配置值
	 */
	boolean updateConfig(String name, String value);

	/**
	 * 根据ID更新配置值
	 */
	boolean updateConfigById(Long id, String value);

	/**
	 * 根据名称获取配置值
	 */
	String getConfigValue(String name);

	/**
	 * 根据名称列表获取配置
	 */
	List<ConfigEntity> getConfigsByNames(List<String> names);
}