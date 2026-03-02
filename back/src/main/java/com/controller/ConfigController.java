package com.controller;

import com.entity.ConfigEntity;
import com.service.ConfigService;
import com.utils.PageUtils;
import com.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/config")
public class ConfigController {

    @Autowired
    private ConfigService configService;

    /**
     * 配置列表（分页）
     */
    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = configService.queryPage(params);
        return R.ok().put("page", page);
    }

    /**
     * 获取所有配置
     */
    @GetMapping("/all")
    public R getAll() {
        try {
            List<ConfigEntity> configList = configService.selectList(null);
            return R.ok().put("data", configList);
        } catch (Exception e) {
            return R.error("获取配置列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取薪资相关配置
     */
    @GetMapping("/salary")
    public R getSalaryConfigs() {
        try {
            List<ConfigEntity> configList = configService.getSalaryConfigs();
            return R.ok().put("data", configList);
        } catch (Exception e) {
            return R.error("获取薪资配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取薪资配置值（用于薪资计算）
     */
    @GetMapping("/salaryValues")
    public R getSalaryConfigValues() {
        try {
            Map<String, Object> configs = configService.getAllSalaryConfigs();
            return R.ok().put("data", configs);
        } catch (Exception e) {
            return R.error("获取薪资配置值失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取配置
     */
    @GetMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        ConfigEntity config = configService.selectById(id);
        return R.ok().put("data", config);
    }

    /**
     * 根据名称获取配置
     */
    @GetMapping("/infoByName/{name}")
    public R infoByName(@PathVariable("name") String name) {
        ConfigEntity config = configService.selectOne(
                new com.baomidou.mybatisplus.mapper.EntityWrapper<ConfigEntity>()
                        .eq("name", name)
        );
        return R.ok().put("data", config);
    }

    /**
     * 保存配置
     */
    @PostMapping("/save")
    public R save(@RequestBody ConfigEntity config) {
        try {
            configService.insert(config);
            return R.ok("配置保存成功");
        } catch (Exception e) {
            return R.error("配置保存失败: " + e.getMessage());
        }
    }

    /**
     * 更新配置
     */
    @PostMapping("/update")
    public R update(@RequestBody ConfigEntity config) {
        try {
            configService.updateById(config);
            return R.ok("配置更新成功");
        } catch (Exception e) {
            return R.error("配置更新失败: " + e.getMessage());
        }
    }

    /**
     * 根据名称更新配置值
     */
    @PostMapping("/updateByName")
    public R updateByName(@RequestBody ConfigEntity config) {
        try {
            boolean success = configService.updateConfig(config.getName(), config.getValue());
            if (success) {
                return R.ok("配置更新成功");
            } else {
                return R.error("配置更新失败");
            }
        } catch (Exception e) {
            return R.error("配置更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除配置
     */
    @PostMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        try {
            configService.deleteBatchIds(java.util.Arrays.asList(ids));
            return R.ok("配置删除成功");
        } catch (Exception e) {
            return R.error("配置删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新配置
     */
    @PostMapping("/batchUpdate")
    public R batchUpdate(@RequestBody List<ConfigEntity> configList) {
        try {
            for (ConfigEntity config : configList) {
                configService.updateConfig(config.getName(), config.getValue());
            }
            return R.ok("批量更新配置成功");
        } catch (Exception e) {
            return R.error("批量更新配置失败: " + e.getMessage());
        }
    }
}