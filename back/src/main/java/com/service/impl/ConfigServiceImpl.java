package com.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.dao.ConfigDao;
import com.entity.ConfigEntity;
import com.service.ConfigService;
import com.utils.PageUtils;
import com.utils.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("configService")
public class ConfigServiceImpl extends ServiceImpl<ConfigDao, ConfigEntity> implements ConfigService {

	@Override
	public PageUtils queryPage(Map<String, Object> params) {
		Page<ConfigEntity> page = new Query<ConfigEntity>(params).getPage();
		EntityWrapper<ConfigEntity> wrapper = new EntityWrapper<>();

		// 添加查询条件
		if (params.get("name") != null) {
			wrapper.like("name", params.get("name").toString());
		}
		if (params.get("description") != null) {
			wrapper.like("description", params.get("description").toString());
		}

		page = this.selectPage(page, wrapper);
		return new PageUtils(page);
	}

	@Override
	public BigDecimal getBaseSalary() {
		return getConfigAsBigDecimal("base_salary", new BigDecimal("8000"));
	}

	@Override
	public int getWorkDaysPerMonth() {
		return getConfigAsInt("work_days_per_month", 21);
	}

	@Override
	public BigDecimal getOvertimeRate() {
		return getConfigAsBigDecimal("overtime_rate", new BigDecimal("1.5"));
	}

	@Override
	public String getWorkStartTime() {
		return getConfigValue("work_start_time", "09:00:00");
	}

	@Override
	public String getWorkEndTime() {
		return getConfigValue("work_end_time", "18:00:00");
	}

	@Override
	public int getLateThreshold() {
		return getConfigAsInt("late_threshold", 30);
	}

	@Override
	public int getEarlyThreshold() {
		return getConfigAsInt("early_threshold", 30);
	}

	@Override
	public String getCompanyName() {
		return getConfigValue("company_name", "xx科技有限公司");
	}

	@Override
	public String getHomepage() {
		return getConfigValue("homepage", "");
	}

	@Override
	public int getSalaryCalcDay() {
		return getConfigAsInt("salary_calc_day", 1);
	}

	@Override
	public Map<String, Object> getAllSalaryConfigs() {
		Map<String, Object> configs = new HashMap<>();
		configs.put("base_salary", getBaseSalary());
		configs.put("work_days_per_month", getWorkDaysPerMonth());
		configs.put("overtime_rate", getOvertimeRate());
		configs.put("work_start_time", getWorkStartTime());
		configs.put("work_end_time", getWorkEndTime());
		configs.put("late_threshold", getLateThreshold());
		configs.put("early_threshold", getEarlyThreshold());
		configs.put("salary_calc_day", getSalaryCalcDay());
		return configs;
	}

	@Override
	public List<ConfigEntity> getSalaryConfigs() {
		try {
			return baseMapper.getSalaryConfigs();
		} catch (Exception e) {
			System.err.println("获取薪资配置失败: " + e.getMessage());
			// 备用方案：使用MyBatis Plus的查询
			List<String> salaryConfigNames = new ArrayList<>();
			salaryConfigNames.add("base_salary");
			salaryConfigNames.add("work_days_per_month");
			salaryConfigNames.add("overtime_rate");
			salaryConfigNames.add("work_start_time");
			salaryConfigNames.add("work_end_time");
			salaryConfigNames.add("late_threshold");
			salaryConfigNames.add("early_threshold");
			salaryConfigNames.add("salary_calc_day");

			return this.selectList(new EntityWrapper<ConfigEntity>().in("name", salaryConfigNames));
		}
	}

	@Override
	public boolean updateConfig(String name, String value) {
		try {
			int result = baseMapper.updateConfig(name, value);
			return result > 0;
		} catch (Exception e) {
			System.err.println("更新配置 " + name + " 失败: " + e.getMessage());
			return false;
		}
	}

	@Override
	public boolean updateConfigById(Long id, String value) {
		try {
			int result = baseMapper.updateConfigById(id, value);
			return result > 0;
		} catch (Exception e) {
			System.err.println("根据ID更新配置失败: " + e.getMessage());
			return false;
		}
	}

	@Override
	public String getConfigValue(String name) {
		return getConfigValue(name, "");
	}

	@Override
	public List<ConfigEntity> getConfigsByNames(List<String> names) {
		try {
			return baseMapper.selectConfigsByNames(names);
		} catch (Exception e) {
			System.err.println("根据名称列表获取配置失败: " + e.getMessage());
			return this.selectList(new EntityWrapper<ConfigEntity>().in("name", names));
		}
	}

	// ==================== 私有辅助方法 ====================

	private String getConfigValue(String name, String defaultValue) {
		try {
			String value = baseMapper.getConfigValue(name);
			return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
		} catch (Exception e) {
			System.err.println("获取配置 " + name + " 失败: " + e.getMessage());
			return defaultValue;
		}
	}

	private int getConfigAsInt(String name, int defaultValue) {
		try {
			String value = getConfigValue(name, String.valueOf(defaultValue));
			return Integer.parseInt(value.trim());
		} catch (Exception e) {
			System.err.println("解析配置 " + name + " 为整数失败: " + e.getMessage());
			return defaultValue;
		}
	}

	private BigDecimal getConfigAsBigDecimal(String name, BigDecimal defaultValue) {
		try {
			String value = getConfigValue(name, defaultValue.toString());
			return new BigDecimal(value.trim());
		} catch (Exception e) {
			System.err.println("解析配置 " + name + " 为小数失败: " + e.getMessage());
			return defaultValue;
		}
	}
}