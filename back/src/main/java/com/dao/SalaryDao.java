package com.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.entity.SalaryEntity;
import com.entity.vo.SalaryVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 薪资DAO
 */
public interface SalaryDao extends BaseMapper<SalaryEntity> {

    List<SalaryVO> selectSalaryList(Map<String, Object> params);

    int countSalaryList(Map<String, Object> params);

    SalaryVO selectSalaryById(@Param("id") Long id);

    int deleteByStaffIdAndMonth(@Param("staffId") Long staffId, @Param("month") Date month);
    @Transactional
    int batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") String status);

    void callCalculateSalary(@Param("month") String month);

    int countByMonth(@Param("month") String month);

    // 专门用于导出查询

    List<SalaryVO> selectSalaryForExport(@Param("month") String month);
    // 在 SalaryDao 接口中添加以下方法：

    /**
     * 根据员工ID和年份查询薪资记录（用于个人统计）
     */
    List<SalaryVO> selectSalaryByStaffAndYear(@Param("staffId") Long staffId,
                                              @Param("year") String year);

    /**
     * 查询员工月度薪资详情
     */
    SalaryVO selectMonthlySalaryDetail(@Param("staffId") Long staffId,
                                       @Param("month") String month);

}