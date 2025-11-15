package com.dao;

import com.entity.AttendanceEntity;
import com.entity.vo.AttendanceVO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 考勤数据访问接口
 */
@Mapper
public interface AttendanceDao {

    /**
     * 根据ID查询考勤记录
     */
    AttendanceEntity selectById(Long id);

    /**
     * 根据员工和日期查询考勤记录
     */
    AttendanceEntity selectByStaffAndDate(@Param("staffId") Long staffId,
                                          @Param("date") String date);

    /**
     * 新增考勤记录
     */
    int insert(AttendanceEntity attendance);

    /**
     * 更新考勤记录
     */
    int update(AttendanceEntity attendance);

    /**
     * 删除考勤记录
     */
    int deleteById(Long id);

    /**
     * 批量插入考勤记录
     */
    int batchInsert(List<AttendanceEntity> list);

    /**
     * 查询考勤记录列表（带分页）
     */
    List<AttendanceVO> selectAttendanceList(Map<String, Object> params);

    /**
     * 查询考勤记录总数
     */
    int countAttendanceList(Map<String, Object> params);

    /**
     * 根据日期范围查询个人考勤统计
     */
    Map<String, Object> selectPersonalStatsByDateRange(@Param("staffId") Long staffId,
                                                       @Param("startDate") String startDate,
                                                       @Param("endDate") String endDate);

    /**
     * 查询个人考勤统计
     */
    Map<String, Object> selectPersonalStats(@Param("staffId") Long staffId,
                                            @Param("month") String month);

    /**
     * 查询指定部门考勤统计
     */

    Map<String, Object> selectSingleDepartmentStats(@Param("month") String month,
                                                    @Param("deptId") Long deptId);

    /**
     * 查询所有部门考勤汇总
     */

    Map<String, Object> selectAllDepartmentSummary(@Param("month") String month);

    /**
     * 查询月度考勤汇总
     */
    List<Map<String, Object>> selectMonthlySummary(@Param("month") String month);

    /**
     * 查询员工某月的考勤详情
     */
    List<AttendanceEntity> selectByStaffAndMonth(@Param("staffId") Long staffId,
                                                 @Param("month") String month);

    /**
     * 查询迟到记录
     */
    List<AttendanceVO> selectLateRecords(@Param("month") String month,
                                         @Param("deptId") Long deptId);

    /**
     * 查询早退记录
     */
    List<AttendanceVO> selectEarlyLeaveRecords(@Param("month") String month,
                                               @Param("deptId") Long deptId);

    /**
     * 查询缺勤记录
     */
    List<AttendanceVO> selectAbsentRecords(@Param("month") String month,
                                           @Param("deptId") Long deptId);

    /**
     * 更新考勤状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 批量更新考勤记录
     */
    int batchUpdate(List<AttendanceEntity> list);
}