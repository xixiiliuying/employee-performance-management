package com.service;

import com.entity.model.AttendanceModel;
import com.entity.vo.AttendanceVO;
import com.utils.PageUtils;
import com.utils.R;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 考勤服务接口
 */
public interface AttendanceService {

    /**
     * 上班打卡
     * @param staffId 员工ID
     * @return 打卡结果
     */
    AttendanceVO checkIn(Long staffId);

    /**
     * 下班打卡
     * @param staffId 员工ID
     * @return 签退结果
     */
    AttendanceVO checkOut(Long staffId);

    /**
     * 手动补打卡
     * @param model 考勤信息
     * @return 补打卡结果
     */
    AttendanceVO manualCheck(AttendanceModel model);

    /**
     * 分页查询考勤记录
     * @param params 查询参数
     * @return 分页结果
     */
    PageUtils queryPage(Map<String, Object> params);

    /**
     * 获取个人考勤统计
     * @param staffId 员工ID
     * @param month 月份 (格式: yyyy-MM)
     * @return 统计信息
     */
    Map<String, Object> getPersonalStats(Long staffId, String month);

    /**
     * 获取部门考勤统计
     * @param month 月份 (格式: yyyy-MM)
     * @param deptId 部门ID (可选)
     * @return 部门统计信息
     */
    Map<String, Object> getDepartmentStats(String month, Long deptId);

    /**
     * 获取今日考勤状态
     * @param staffId 员工ID
     * @return 今日考勤状态
     */
    Map<String, Object> getTodayStatus(Long staffId);

    /**
     * 检查是否已打卡
     * @param staffId 员工ID
     * @param date 日期
     * @return 是否已打卡
     */
    boolean hasCheckedIn(Long staffId, String date);

    /**
     * 检查是否已签退
     * @param staffId 员工ID
     * @param date 日期
     * @return 是否已签退
     */
    boolean hasCheckedOut(Long staffId, String date);

    /**
     * 获取考勤详情
     * @param id 考勤记录ID
     * @return 考勤详情
     */
    AttendanceVO getDetail(Long id);

    /**
     * 更新考勤记录
     * @param model 考勤信息
     * @return 更新结果
     */
    R updateAttendance(AttendanceModel model);

    /**
     * 删除考勤记录
     * @param id 考勤记录ID
     * @return 删除结果
     */
    R deleteAttendance(Long id);

    /**
     * 批量导入考勤数据
     * @param attendanceList 考勤数据列表
     * @return 导入结果
     */
    R batchImport(List<AttendanceModel> attendanceList);

    /**
     * 导出考勤数据
     * @param params 查询参数
     * @param response HTTP响应
     */
    void exportAttendance(Map<String, Object> params, HttpServletResponse response);
}