package com.controller;

import com.entity.model.LeaveRequestModel;
import com.entity.vo.LeaveRequestVO;
import com.annotation.LoginUser;
import com.entity.UserEntity;
import com.service.LeaveRequestService;
import com.utils.PageUtils;
import com.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * 请假管理
 */
@RestController
@RequestMapping("/leave")
public class LeaveRequestController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    /**
     * 提交请假申请
     * 实现思路：
     * 1. 参数验证（请假类型、日期、天数等）
     * 2. 验证请假日期是否冲突
     * 3. 计算实际请假天数
     * 4. 创建请假申请记录
     * 5. 返回申请结果
     */
    @PostMapping("/apply")
    public R apply(@Valid @RequestBody LeaveRequestModel model,
                   BindingResult result,
                   @LoginUser UserEntity user) {
        // 参数校验
        if (result.hasErrors()) {
            return R.error(result.getFieldError().getDefaultMessage());
        }

        // 直接返回Service的结果
        return leaveRequestService.applyLeave(model, user);
    }
    /**
     * 我的请假记录
     * 实现思路：
     * 1. 按员工ID查询
     * 2. 支持按状态、日期筛选
     * 3. 分页返回结果
     */
//    @GetMapping("/myApplications")
//    public R myApplications(@RequestParam Map<String, Object> params,
//                            @LoginUser UserEntity user) {
//        params.put("staffId", user.getId());
//        PageUtils page = leaveRequestService.queryPage(params);
//        return R.ok().put("page", page);
//    }
    @GetMapping("/myApplications")
    public R myApplications(@RequestParam Map<String, Object> params,
                            @LoginUser UserEntity user) {
        // 移除可能存在的staffId参数，强制使用当前用户ID
        params.remove("staffId");
        params.put("staffId", user.getId());

        PageUtils page = leaveRequestService.queryPage(params);
        return R.ok().put("page", page);
    }

    /**
     * 撤销请假申请
     * 实现思路：
     * 1. 验证申请是否存在且属于当前用户
     * 2. 验证申请状态是否为"待审批"
     * 3. 更新申请状态为"已撤销"
     */
    @PostMapping("/cancel/{id}")
    public R cancel(@PathVariable("id") Long id,
                    @LoginUser UserEntity user) {
        try {
            boolean success = leaveRequestService.cancelLeave(id, user.getId());
            if (success) {
                return R.ok("撤销成功");
            } else {
                return R.error("撤销失败");
            }
        } catch (Exception e) {
            return R.error("撤销失败: " + e.getMessage());
        }
    }

    /**
     * 查看请假详情
     * 实现思路：
     * 1. 根据ID查询请假详情
     * 2. 验证查看权限
     * 3. 返回详细信息
     */
    @GetMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id,
                    @LoginUser UserEntity user) {
        try {
            LeaveRequestVO vo = leaveRequestService.getDetail(id, user.getId());
            return R.ok().put("data", vo);
        } catch (Exception e) {
            return R.error("获取详情失败: " + e.getMessage());
        }
    }
}