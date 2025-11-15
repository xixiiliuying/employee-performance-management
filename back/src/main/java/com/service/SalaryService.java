package com.service;

import com.entity.UserEntity;
import com.entity.vo.SalaryVO;
import com.utils.PageUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 薪资服务接口
 */
public interface SalaryService {

    PageUtils queryPage(Map<String, Object> params);

    SalaryVO getDetail(Long id, UserEntity user);

    int calculateMonthlySalary(String month);

    void exportSalary(String month, HttpServletResponse response);

    void exportMySalary(String month, Long staffId, HttpServletResponse response);

    void updateSalaryStatus(Map<String, Object> params);

    Map<String, Object> getSalaryStatistics(String month);

    Map<String, Object> getMySalaryStatistics(Long staffId, String year);

    Map<String, Object> getMonthlySalaryDetail(Long staffId, String month);

    PageUtils getMySalary(Map<String, Object> params, Long staffId);

    boolean isMonthCalculated(String month);

    void recalculateStaffSalary(Long staffId, String month);
}