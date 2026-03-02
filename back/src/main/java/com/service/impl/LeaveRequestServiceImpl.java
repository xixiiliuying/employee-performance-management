package com.service.impl;

import com.dao.LeaveRequestDao;
import com.dao.UserDao;
import com.entity.LeaveRequestEntity;
import com.entity.UserEntity;
import com.entity.model.LeaveRequestModel;
import com.entity.vo.LeaveRequestVO;
import com.service.LeaveRequestService;
import com.utils.DateUtils;
import com.utils.PageUtils;
import com.utils.R;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 请假申请服务实现类
 */
@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {

    @Autowired
    private LeaveRequestDao leaveRequestDao;

    @Autowired
    private UserDao userDao;

//    @Override
//    public PageUtils queryPage(Map<String, Object> params) {
//        // 处理查询参数
//        if (params.containsKey("month") && params.get("month") != null) {
//            String month = (String) params.get("month");
//            params.put("startDate", month + "-01");
//            params.put("endDate", month + "-31");
//        }
//
//        List<LeaveRequestVO> leaveList = leaveRequestDao.selectLeaveList(params);
//        int total = leaveRequestDao.countLeaveList(params);
//
//        PageUtils pageUtils = new PageUtils(leaveList, total,
//                Integer.parseInt(params.getOrDefault("page", "1").toString()),
//                Integer.parseInt(params.getOrDefault("limit", "10").toString()));
//
//        return pageUtils;
//    }
@Override
public PageUtils queryPage(Map<String, Object> params) {
    // 调试日志
    System.out.println("=== Service查询参数 ===");
    System.out.println("接收到的参数: " + params);

    // 处理查询参数
    if (params.containsKey("month") && params.get("month") != null) {
        String month = (String) params.get("month");
        params.put("startDate", month + "-01");
        params.put("endDate", month + "-31");
    }

    // 确保staffId参数被传递到DAO层
    List<LeaveRequestVO> leaveList = leaveRequestDao.selectLeaveList(params);
    int total = leaveRequestDao.countLeaveList(params);

    // 调试日志
    System.out.println("查询结果数量: " + leaveList.size());
    System.out.println("总记录数: " + total);

    PageUtils pageUtils = new PageUtils(leaveList, total,
            Integer.parseInt(params.getOrDefault("page", "1").toString()),
            Integer.parseInt(params.getOrDefault("limit", "10").toString()));

    return pageUtils;
}
    @Override
    @Transactional
    public R approveLeave(Long leaveId, Long approverId) {
        try {
            // 1. 验证请假申请是否存在
            LeaveRequestEntity leaveRequest = leaveRequestDao.selectById(leaveId);
            if (leaveRequest == null) {
                return R.error("请假申请不存在");
            }

            // 2. 验证状态是否为待审批
            if (!"待审批".equals(leaveRequest.getStatus())) {
                return R.error("该申请已处理，无法重复审批");
            }

            // 3. 更新申请状态
            leaveRequest.setStatus("已批准");
            leaveRequest.setHrId(approverId);
            leaveRequest.setApprovalTime(new Date());

            int result = leaveRequestDao.update(leaveRequest);
            if (result > 0) {
                return R.ok("审批通过");
            } else {
                return R.error("审批失败");
            }
        } catch (Exception e) {
            throw new RuntimeException("审批操作失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public R rejectLeave(Long leaveId, Long approverId, String reason) {
        try {
            // 1. 验证请假申请是否存在
            LeaveRequestEntity leaveRequest = leaveRequestDao.selectById(leaveId);
            if (leaveRequest == null) {
                return R.error("请假申请不存在");
            }

            // 2. 验证状态是否为待审批
            if (!"待审批".equals(leaveRequest.getStatus())) {
                return R.error("该申请已处理，无法重复操作");
            }

            // 3. 更新申请状态
            leaveRequest.setStatus("已拒绝");
            leaveRequest.setHrId(approverId);
            leaveRequest.setApprovalTime(new Date());
            leaveRequest.setReason(leaveRequest.getReason() + " [审批意见: " + reason + "]");

            int result = leaveRequestDao.update(leaveRequest);
            if (result > 0) {
                return R.ok("已拒绝该申请");
            } else {
                return R.error("操作失败");
            }
        } catch (Exception e) {
            throw new RuntimeException("拒绝操作失败: " + e.getMessage(), e);
        }
    }


    // 新增重载方法，支持 Model 参数
    @Override
    @Transactional
    public R applyLeave(LeaveRequestModel model, UserEntity user) {
        try {
            // 1. 参数验证
            if (model.getLeaveType() == null || model.getStartDate() == null || model.getEndDate() == null) {
                return R.error("请假类型、开始日期、结束日期不能为空");
            }

            // 2. 计算请假天数
            LocalDate start = model.getStartDate();
            LocalDate end = model.getEndDate();
            Double days = (double) (java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1);

            if (days <= 0) {
                return R.error("结束日期不能早于开始日期");
            }

            // 3. 检查日期冲突
            if (hasDateConflict(user.getId(), start, end)) {
                return R.error("该时间段内已有请假申请，请勿重复申请");
            }

            // 4. 创建请假申请
            LeaveRequestEntity leaveRequest = new LeaveRequestEntity();
            leaveRequest.setStaffId(user.getId());
            leaveRequest.setLeaveType(model.getLeaveType());
            leaveRequest.setStartDate(DateUtils.toDate(start));
            leaveRequest.setEndDate(DateUtils.toDate(end));
            leaveRequest.setDays(days);
            leaveRequest.setReason(model.getReason());
            leaveRequest.setStatus("待审批");
            leaveRequest.setCreateTime(new Date());

            // 5. 保存到数据库
            int result = leaveRequestDao.insert(leaveRequest);
            if (result > 0) {
                LeaveRequestVO vo = convertToVO(leaveRequest);
                return R.ok("请假申请提交成功").put("data", vo);
            } else {
                return R.error("请假申请提交失败");
            }
        } catch (Exception e) {
            throw new RuntimeException("请假申请失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查日期冲突
     */
    private boolean hasDateConflict(Long staffId, LocalDate startDate, LocalDate endDate) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("staffId", staffId);
            params.put("startDate", startDate.toString());
            params.put("endDate", endDate.toString());
            params.put("status", "待审批,已批准");

            int count = leaveRequestDao.countLeaveList(params);
            return count > 0;
        } catch (Exception e) {
            // 如果查询失败，默认没有冲突
            return false;
        }
    }

    @Override
    @Transactional
    public boolean cancelLeave(Long leaveId, Long staffId) {
        // 1. 验证请假申请是否存在且属于该员工
        LeaveRequestEntity leaveRequest = leaveRequestDao.selectById(leaveId);
        if (leaveRequest == null) {
            throw new RuntimeException("请假申请不存在");
        }

        if (!leaveRequest.getStaffId().equals(staffId)) {
            throw new RuntimeException("无权操作该申请");
        }

        // 2. 验证状态是否为待审批
        if (!"待审批".equals(leaveRequest.getStatus())) {
            throw new RuntimeException("该申请已处理，无法撤销");
        }

        // 3. 更新申请状态
        leaveRequest.setStatus("已撤销");
        int result = leaveRequestDao.update(leaveRequest);
        if (result <= 0) {
            throw new RuntimeException("撤销失败");
        }

        return true;
    }
    @Override
    public LeaveRequestVO getDetail(Long id, Long staffId) {
        LeaveRequestEntity leaveRequest = leaveRequestDao.selectById(id);
        if (leaveRequest == null) {
            throw new RuntimeException("请假申请不存在");
        }

        // 验证权限：员工只能查看自己的申请
        if (!leaveRequest.getStaffId().equals(staffId)) {
            // 这里可以添加HR权限检查逻辑
            // 暂时先允许查看，或者您可以添加HR角色检查
             UserEntity user = userDao.selectById(staffId);
             if (!"hr".equals(user.getRole())) {
                 throw new RuntimeException("无权查看该申请");
             }
        }

        return convertToVO(leaveRequest);
    }
    @Override
    public PageUtils getMyLeaveRecords(Map<String, Object> params, Long staffId) {
        params.put("staffId", staffId);
        return queryPage(params);
    }

    @Override
    public Map<String, Object> getLeaveStatistics(String month, Long deptId) {
        Map<String, Object> stats = leaveRequestDao.selectLeaveStatistics(month, deptId);
        if (stats == null) {
            stats = new HashMap<>();
        }

        // 设置默认值
        stats.putIfAbsent("totalApplications", 0);
        stats.putIfAbsent("pendingCount", 0);
        stats.putIfAbsent("approvedCount", 0);
        stats.putIfAbsent("rejectedCount", 0);
        stats.putIfAbsent("totalLeaveDays", 0);

        return stats;
    }

    /**
     * 转换为VO对象
     */
    private LeaveRequestVO convertToVO(LeaveRequestEntity leaveRequest) {
        LeaveRequestVO vo = new LeaveRequestVO();
        BeanUtils.copyProperties(leaveRequest, vo);

        // 查询员工信息
        UserEntity user = userDao.selectById(leaveRequest.getStaffId());
        if (user != null) {
            vo.setStaffName(user.getSname());
        }

        // 查询审批人信息
        if (leaveRequest.getHrId() != null) {
            UserEntity hrUser = userDao.selectById(leaveRequest.getHrId());
            if (hrUser != null) {
                vo.setApproverName(hrUser.getSname());
            }
        }

        return vo;
    }
}