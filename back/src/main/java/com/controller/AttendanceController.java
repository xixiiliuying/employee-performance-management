package com.controller;

import com.entity.model.AttendanceModel;
import com.entity.vo.AttendanceVO;
import com.annotation.LoginUser;
import com.entity.UserEntity;
import com.service.AttendanceService;
import com.utils.PageUtils;
import com.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 考勤管理
 */
@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    /**
     * 上班打卡
     * 实现思路：
     * 1. 获取当前员工ID和当前时间
     * 2. 检查今天是否已经打卡
     * 3. 判断是否迟到（9:30之后算迟到）
     * 4. 创建或更新考勤记录
     * 5. 返回打卡结果
     */
    @PostMapping("/checkIn")
    public R checkIn(@LoginUser UserEntity user) {
        try {
            AttendanceVO result = attendanceService.checkIn(user.getId());
            return R.ok().put("data", result);
        } catch (Exception e) {
            return R.error("打卡失败: " + e.getMessage());
        }
    }

    /**
     * 下班打卡
     * 实现思路：
     * 1. 获取当前员工ID和当前时间
     * 2. 查询今天的考勤记录
     * 3. 判断是否早退（18:00之前算早退）
     * 4. 更新签退时间和状态
     * 5. 返回签退结果
     */
    @PostMapping("/checkOut")
    public R checkOut(@LoginUser UserEntity user) {
        try {
            AttendanceVO result = attendanceService.checkOut(user.getId());
            return R.ok().put("data", result);
        } catch (Exception e) {
            return R.error("签退失败: " + e.getMessage());
        }
    }

    /**
     * 手动补打卡（HR功能）
     * 实现思路：
     * 1. 验证HR权限
     * 2. 接收补打卡参数（员工ID、日期、时间、类型）
     * 3. 验证日期是否合法
     * 4. 创建或更新考勤记录
     * 5. 计算考勤状态
     */
    @PostMapping("/manualCheck")
    public R manualCheck(@RequestBody AttendanceModel model,
                         @LoginUser UserEntity user) {
        try {
            // 这里可以添加HR权限验证
            AttendanceVO result = attendanceService.manualCheck(model);
            return R.ok().put("data", result);
        } catch (Exception e) {
            return R.error("补打卡失败: " + e.getMessage());
        }
    }

    /**
     * 我的考勤记录查询
     * 实现思路：
     * 1. 获取当前员工ID
     * 2. 按月份筛选考勤记录
     * 3. 分页查询
     * 4. 返回考勤记录列表
     */
    @GetMapping("/myRecords")
    public R myRecords(@RequestParam Map<String, Object> params,
                       @LoginUser UserEntity user) {
        params.put("staffId", user.getId());
        PageUtils page = attendanceService.queryPage(params);
        return R.ok().put("page", page);
    }

    /**
     * 考勤统计
     * 实现思路：
     * 1. 按月份统计
     * 2. 计算正常、迟到、早退、缺勤天数
     * 3. 返回统计结果
     */
    @GetMapping("/myStatistics")
    public R myStatistics(@RequestParam String month,
                          @LoginUser UserEntity user) {
        Map<String, Object> stats = attendanceService.getPersonalStats(user.getId(), month);
        return R.ok().put("data", stats);
    }

    /**
     * HR查看所有考勤记录
     * 实现思路：
     * 1. 支持按部门、员工、月份筛选
     * 2. 分页查询
     * 3. 关联查询员工和部门信息
     */
    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = attendanceService.queryPage(params);
        return R.ok().put("page", page);
    }

    /**
     * 部门考勤统计
     * 实现思路：
     * 1. 按部门和月份统计
     * 2. 计算各部门的考勤情况
     * 3. 返回部门统计报表
     */
    @GetMapping("/departmentStats")
    public R departmentStats(@RequestParam String month,
                             @RequestParam(required = false) Long deptId) {
        Map<String, Object> stats = attendanceService.getDepartmentStats(month, deptId);
        return R.ok().put("data", stats);
    }
    /**
     * 导出考勤记录（HR功能）
     * 实现思路：
     * 1. 根据查询条件筛选数据
     * 2. 生成Excel文件
     * 3. 返回文件下载链接
     */
    @GetMapping("/export")
    public void exportAttendance(@RequestParam Map<String, Object> params,
                                 HttpServletResponse response) {
        try {
            attendanceService.exportAttendance(params, response);
        } catch (Exception e) {
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }
}