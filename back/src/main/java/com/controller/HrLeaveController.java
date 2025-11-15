package com.controller;

import com.entity.vo.LeaveRequestVO;
import com.annotation.LoginUser;
import com.entity.UserEntity;
import com.service.LeaveRequestService;
import com.utils.PageUtils;
import com.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * HR请假审批
 */
@RestController
@RequestMapping("/hr/leave")
public class HrLeaveController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    /**
     * 待审批列表
     * 实现思路：
     * 1. 查询状态为"待审批"的请假申请
     * 2. 关联查询员工和部门信息
     * 3. 分页返回结果
     */
    @GetMapping("/pendingList")
    public R pendingList(@RequestParam Map<String, Object> params) {
        params.put("status", "待审批");
        PageUtils page = leaveRequestService.queryPage(params);
        return R.ok().put("page", page);
    }

    /**
     * 审批通过
     * 实现思路：
     * 1. 验证申请是否存在且为待审批状态
     * 2. 更新申请状态为"已批准"
     * 3. 记录审批人和审批时间
     */
    @PostMapping("/approve/{id}")
    public R approve(@PathVariable("id") Long id,
                     @LoginUser UserEntity user) {
        try {
            leaveRequestService.approveLeave(id, user.getId());
            return R.ok("审批通过");
        } catch (Exception e) {
            return R.error("审批失败: " + e.getMessage());
        }
    }

    /**
     * 审批拒绝
     * 实现思路：
     * 1. 验证申请是否存在且为待审批状态
     * 2. 更新申请状态为"已拒绝"
     * 3. 记录审批人、审批时间和拒绝原因
     */
    @PostMapping("/reject/{id}")
    public R reject(@PathVariable("id") Long id,
                    @RequestParam String reason,
                    @LoginUser UserEntity user) {
        try {
            leaveRequestService.rejectLeave(id, user.getId(), reason);
            return R.ok("已拒绝");
        } catch (Exception e) {
            return R.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 所有请假记录查询（HR）
     * 实现思路：
     * 1. 支持按部门、员工、状态、日期筛选
     * 2. 关联查询完整信息
     * 3. 分页返回结果
     */
    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = leaveRequestService.queryPage(params);
        return R.ok().put("page", page);
    }
}