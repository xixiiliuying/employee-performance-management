package com.service;

import com.entity.UserEntity;
import com.entity.model.LeaveRequestModel;
import com.entity.vo.LeaveRequestVO;
import com.utils.PageUtils;
import com.utils.R;

import java.util.Map;

/**
 * 请假申请服务接口
 */
public interface LeaveRequestService {

    /**
     * 分页查询请假记录
     */
    PageUtils queryPage(Map<String, Object> params);

    /**
     * 审批通过请假申请
     */
    R approveLeave(Long leaveId, Long approverId);

    /**
     * 拒绝请假申请
     */
    R rejectLeave(Long leaveId, Long approverId, String reason);

    /**
     * 撤销请假申请
     */
    boolean cancelLeave(Long leaveId, Long staffId);

    /**
     * 获取请假详情
     */
    LeaveRequestVO getDetail(Long id, Long staffId);
    /**
     * 提交请假申请 - Model参数版本（新增）
     */
    R applyLeave(LeaveRequestModel model, UserEntity user);

    /**
     * 获取我的请假记录
     */
    PageUtils getMyLeaveRecords(Map<String, Object> params, Long staffId);

    /**
     * 统计请假数据
     */
    Map<String, Object> getLeaveStatistics(String month, Long deptId);
}