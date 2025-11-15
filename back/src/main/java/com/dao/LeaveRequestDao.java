package com.dao;

import com.entity.LeaveRequestEntity;
import com.entity.vo.LeaveRequestVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 请假申请数据访问接口
 */
@Mapper
public interface LeaveRequestDao {

    /**
     * 根据ID查询请假申请
     */
    LeaveRequestEntity selectById(Long id);

    /**
     * 新增请假申请
     */
    int insert(LeaveRequestEntity leaveRequest);

    /**
     * 更新请假申请
     */
    int update(LeaveRequestEntity leaveRequest);

    /**
     * 删除请假申请
     */
    int deleteById(Long id);

    /**
     * 查询请假申请列表
     */
    List<LeaveRequestVO> selectLeaveList(Map<String, Object> params);

    /**
     * 查询请假申请总数
     */
    int countLeaveList(Map<String, Object> params);

    /**
     * 查询请假统计信息
     */
    Map<String, Object> selectLeaveStatistics(@Param("month") String month,
                                              @Param("deptId") Long deptId);

    /**
     * 根据员工ID查询请假申请
     */
    List<LeaveRequestEntity> selectByStaffId(Long staffId);

    /**
     * 查询待审批的请假申请
     */
    List<LeaveRequestEntity> selectPendingApplications();

    /**
     * 查询员工某月的请假天数
     */
    Double selectLeaveDaysByMonth(@Param("staffId") Long staffId,
                                  @Param("month") String month);

    /**
     * 批量更新请假申请状态
     */
    int batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") String status);
}