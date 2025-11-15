package com.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.entity.ConfigEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 配置
 */
public interface ConfigDao extends BaseMapper<ConfigEntity> {

    /**
     * 根据配置名称获取配置值
     */
    String getConfigValue(@Param("name") String name);

    /**
     * 获取薪资相关配置
     */
    List<ConfigEntity> getSalaryConfigs();

    /**
     * 根据名称更新配置值
     */
    int updateConfig(@Param("name") String name, @Param("value") String value);

    /**
     * 根据ID更新配置值
     */
    int updateConfigById(@Param("id") Long id, @Param("value") String value);

    /**
     * 自定义查询配置列表（带分页）
     */
    List<ConfigEntity> selectConfigPage(Pagination page, @Param("ew") Wrapper<ConfigEntity> wrapper);

    /**
     * 根据名称列表批量查询配置
     */
    List<ConfigEntity> selectConfigsByNames(@Param("names") List<String> names);
}