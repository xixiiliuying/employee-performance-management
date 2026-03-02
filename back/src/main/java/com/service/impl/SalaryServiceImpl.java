package com.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.dao.SalaryDao;
import com.entity.SalaryEntity;
import com.entity.UserEntity;
import com.entity.vo.SalaryVO;
import com.service.ConfigService;
import com.service.SalaryService;
import com.utils.FileUtil;
import com.utils.PageUtils;
import com.utils.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service("salaryService")
public class SalaryServiceImpl implements SalaryService {

    @Autowired
    private SalaryDao salaryDao;

    @Autowired
    private ConfigService configService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        // 处理月份参数转换
        if (params.containsKey("month") && params.get("month") != null) {
            String monthStr = params.get("month").toString();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                Date month = sdf.parse(monthStr);
                params.put("month", month);
            } catch (Exception e) {
                System.out.println("月份参数格式错误: " + monthStr);
            }
        }

        List<SalaryVO> salaryList = salaryDao.selectSalaryList(params);
        int total = salaryDao.countSalaryList(params);

        int page = Integer.parseInt(params.getOrDefault("page", "1").toString());
        int limit = Integer.parseInt(params.getOrDefault("limit", "10").toString());

        return new PageUtils(salaryList, total, limit, page);
    }

    @Override
    public SalaryVO getDetail(Long id, UserEntity user) {
        SalaryVO salary = salaryDao.selectSalaryById(id);

        if (salary == null) {
            throw new RuntimeException("薪资记录不存在");
        }

        // HR可以查看所有记录，员工只能查看自己的
        if (user != null && !"hr".equals(user.getRole()) && !user.getId().equals(salary.getStaffId())) {
            throw new RuntimeException("权限不足，只能查看自己的薪资记录");
        }

        return salary;
    }

    @Override
    public int calculateMonthlySalary(String month) {
        try {
            System.out.println("开始计算 " + month + " 月份薪资...");

            // 使用存储过程计算薪资（直接传递 YYYY-MM 格式）
            salaryDao.callCalculateSalary(month);

            // 查询计算后的记录数量
            Map<String, Object> params = new HashMap<>();
            params.put("month", month);
            int count = salaryDao.countSalaryList(params);

            System.out.println("薪资计算完成，共计算 " + count + " 条记录");
            return count;

        } catch (Exception e) {
            throw new RuntimeException("薪资计算失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void exportSalary(String month, HttpServletResponse response) {
        File tempFile = null;
        try {
            // 使用专门的导出查询方法
            List<SalaryVO> salaryList = salaryDao.selectSalaryForExport(month);

            if (CollectionUtils.isEmpty(salaryList)) {
                throw new RuntimeException(month + "月份暂无薪资数据");
            }

            // 创建临时Excel文件
            String fileName = month + "月份薪资表.xlsx";
            tempFile = File.createTempFile("salary_export", ".xlsx");

            // 使用EasyExcel写入临时文件
            EasyExcel.write(tempFile, SalaryVO.class)
                    .sheet("薪资表")
                    .doWrite(salaryList);

            // 使用FileUtil将文件转换为字节数组
            byte[] fileBytes = FileUtil.FileToByte(tempFile);

            // 设置响应头
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            response.setContentLength(fileBytes.length);

            // 将字节数组写入响应输出流
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(fileBytes);
            outputStream.flush();

        } catch (IOException e) {
            throw new RuntimeException("导出Excel失败: " + e.getMessage(), e);
        } finally {
            // 删除临时文件
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    @Override
    public void exportMySalary(String month, Long staffId, HttpServletResponse response) {
        ServletOutputStream outputStream = null;
        try {
            System.out.println("🔧 开始导出工资条 - 月份: " + month + ", 员工ID: " + staffId);

            // === 查询薪资数据 ===
            Map<String, Object> params = new HashMap<>();
            params.put("staffId", staffId);

            // 使用字符串月份查询，避免日期转换问题
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            Date monthDate = sdf.parse(month);
            params.put("month", monthDate);

            List<SalaryVO> salaryList = salaryDao.selectSalaryList(params);
            System.out.println("🔍 查询结果数量: " + (salaryList != null ? salaryList.size() : 0));

            if (CollectionUtils.isEmpty(salaryList)) {
                throw new RuntimeException(month + "月份暂无薪资记录");
            }

            SalaryVO salary = salaryList.get(0);
            System.out.println("✅ 找到薪资记录: " + salary.getStaffName());

            // === 准备Excel数据 ===
            List<List<Object>> dataList = new ArrayList<>();

            // 第一行：标题行
            dataList.add(Arrays.asList(
                    "员工姓名", "部门", "岗位", "月份", "发放状态",
                    "基本工资", "加班工资", "请假天数", "请假扣款", "实发工资",
                    "计算时间", "备注"
            ));

            // 第二行：数据行
            dataList.add(Arrays.asList(
                    salary.getStaffName(),
                    salary.getDeptName(),
                    salary.getJobName(),
                    formatMonth(salary.getMonth()),
                    salary.getStatus(),
                    salary.getActualBasePay() != null ? salary.getActualBasePay().doubleValue() : 0.0,
                    salary.getOvertimePay() != null ? salary.getOvertimePay().doubleValue() : 0.0,
                    salary.getLeaveDays() != null ? salary.getLeaveDays().doubleValue() : 0.0,
                    salary.getLeaveDeduction() != null ? salary.getLeaveDeduction().doubleValue() : 0.0,
                    salary.getTotalSalary() != null ? salary.getTotalSalary().doubleValue() : 0.0,
                    salary.getCalcTime() != null ? formatDateTime(salary.getCalcTime()) : "未计算",
                    salary.getRemarks() != null ? salary.getRemarks() : "无"
            ));

            System.out.println("📊 准备写入Excel数据，行数: " + dataList.size());

            // === 设置响应头 ===
            String fileName = URLEncoder.encode(
                    salary.getStaffName() + "_" + month + "_工资条.xlsx",
                    "UTF-8"
            );

            response.reset();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

            // CORS头
            response.setHeader("Access-Control-Allow-Origin", "http://localhost:8082");
            response.setHeader("Access-Control-Allow-Credentials", "true");

            // === 直接写入响应流 ===
            outputStream = response.getOutputStream();

            // 使用EasyExcel直接写入
            EasyExcel.write(outputStream)
                    .autoCloseStream(false) // 不要自动关闭流
                    .sheet("工资条")
                    .doWrite(dataList);

            outputStream.flush();
            System.out.println("✅ Excel写入完成，数据已发送");

        } catch (Exception e) {
            System.err.println("❌ 导出工资条失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("导出工资条失败: " + e.getMessage(), e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    System.err.println("⚠️ 关闭输出流失败: " + e.getMessage());
                }
            }
        }
    }

    // 创建工资条表头（增强版）
    private List<List<String>> createSalarySlipHeader() {
        List<List<String>> head = new ArrayList<>();

        // 基本信息
        head.add(Arrays.asList("基本信息", "员工姓名"));
        head.add(Arrays.asList("基本信息", "部门"));
        head.add(Arrays.asList("基本信息", "岗位"));
        head.add(Arrays.asList("基本信息", "月份"));
        head.add(Arrays.asList("基本信息", "发放状态"));

        // 收入项
        head.add(Arrays.asList("收入明细", "基本工资"));
        head.add(Arrays.asList("收入明细", "加班工资"));
        head.add(Arrays.asList("收入明细", "应发工资"));

        // 扣款项
        head.add(Arrays.asList("扣款明细", "请假天数"));
        head.add(Arrays.asList("扣款明细", "请假扣款"));
        head.add(Arrays.asList("扣款明细", "实发工资"));

        // 其他信息
        head.add(Arrays.asList("其他信息", "计算时间"));
        head.add(Arrays.asList("其他信息", "备注"));

        return head;
    }

    // 格式化月份显示
    private String formatMonth(Object month) {
        if (month == null) return "";
        try {
            if (month instanceof Date) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月");
                return sdf.format((Date) month);
            }
            String monthStr = month.toString();
            if (monthStr.length() == 7) {
                return monthStr.replace("-", "年") + "月";
            }
            return monthStr;
        } catch (Exception e) {
            return month.toString();
        }
    }

    // 格式化日期时间
    private String formatDateTime(Date dateTime) {
        if (dateTime == null) return "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format(dateTime);
        } catch (Exception e) {
            return dateTime.toString();
        }
    }

    @Override
    public void updateSalaryStatus(Map<String, Object> params) {
        System.out.println("🔧🔧🔧 updateSalaryStatus Service方法被调用了！🔧🔧🔧");
        System.out.println("Service接收参数: " + params);

        try {
            List<Long> ids = (List<Long>) params.get("ids");
            String status = (String) params.get("status");

            System.out.println("Service解析 - IDs: " + ids);
            System.out.println("Service解析 - Status: " + status);

            if (CollectionUtils.isEmpty(ids)) {
                throw new RuntimeException("请选择要更新的薪资记录");
            }

            if (!"已发放".equals(status) && !"待审核".equals(status)) {
                throw new RuntimeException("状态参数错误");
            }

            System.out.println("准备调用DAO...");
            int updatedCount = salaryDao.batchUpdateStatus(ids, status);
            System.out.println("DAO返回更新记录数: " + updatedCount);

            if (updatedCount == 0) {
                System.out.println("⚠️ 警告: 未更新任何记录");
            } else {
                System.out.println("✅ 成功更新 " + updatedCount + " 条记录");
            }

        } catch (Exception e) {
            System.err.println("💥 Service异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("更新薪资状态失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getMySalaryStatistics(Long staffId, String year) {
        Map<String, Object> statistics = new HashMap<>();

        try {
            System.out.println("开始统计员工 " + staffId + " " + year + " 年度薪资数据");

            // 查询该员工指定年份的所有薪资记录
            Map<String, Object> params = new HashMap<>();
            params.put("staffId", staffId);
            params.put("year", year);

            List<SalaryVO> salaryList = salaryDao.selectSalaryList(params);
            System.out.println("查询到 " + salaryList.size() + " 条薪资记录");

            if (CollectionUtils.isEmpty(salaryList)) {
                statistics.put("message", year + "年度暂无薪资记录");
                statistics.put("hasData", false);
                return statistics;
            }

            statistics.put("hasData", true);
            statistics.put("year", year);
            statistics.put("totalMonths", salaryList.size());

            // 年度总收入（实发工资）
            BigDecimal yearTotalSalary = salaryList.stream()
                    .map(s -> s.getTotalSalary() != null ? s.getTotalSalary() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            statistics.put("yearTotalSalary", yearTotalSalary);

            // 月平均收入
            BigDecimal monthAvgSalary = yearTotalSalary.divide(
                    new BigDecimal(salaryList.size()), 2, RoundingMode.HALF_UP);
            statistics.put("monthAvgSalary", monthAvgSalary);

            // 最高月薪和最低月薪
            BigDecimal maxSalary = salaryList.stream()
                    .map(s -> s.getTotalSalary() != null ? s.getTotalSalary() : BigDecimal.ZERO)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            BigDecimal minSalary = salaryList.stream()
                    .map(s -> s.getTotalSalary() != null ? s.getTotalSalary() : BigDecimal.ZERO)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            statistics.put("maxSalary", maxSalary);
            statistics.put("minSalary", minSalary);

            // 加班费统计
            BigDecimal totalOvertimePay = salaryList.stream()
                    .map(s -> s.getOvertimePay() != null ? s.getOvertimePay() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal avgOvertimePay = totalOvertimePay.divide(
                    new BigDecimal(salaryList.size()), 2, RoundingMode.HALF_UP);
            statistics.put("totalOvertimePay", totalOvertimePay);
            statistics.put("avgOvertimePay", avgOvertimePay);

            // 请假统计
            BigDecimal totalLeaveDays = salaryList.stream()
                    .map(s -> s.getLeaveDays() != null ? s.getLeaveDays() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            statistics.put("totalLeaveDays", totalLeaveDays);

            // 请假扣款统计
            BigDecimal totalLeaveDeduction = salaryList.stream()
                    .map(s -> s.getLeaveDeduction() != null ? s.getLeaveDeduction() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            statistics.put("totalLeaveDeduction", totalLeaveDeduction);

            // 总扣款统计（根据公式：总扣款 = 基础工资 + 加班费 - 实发工资）
            BigDecimal totalDeductions = salaryList.stream()
                    .map(s -> {
                        BigDecimal basePay = s.getActualBasePay() != null ? s.getActualBasePay() : BigDecimal.ZERO;
                        BigDecimal overtime = s.getOvertimePay() != null ? s.getOvertimePay() : BigDecimal.ZERO;
                        BigDecimal netSalary = s.getTotalSalary() != null ? s.getTotalSalary() : BigDecimal.ZERO;
                        return basePay.add(overtime).subtract(netSalary);
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            statistics.put("totalDeductions", totalDeductions);

            // 月度数据列表（用于图表展示）
            List<Map<String, Object>> monthlyData = salaryList.stream()
                    .sorted((a, b) -> b.getMonth().compareTo(a.getMonth())) // 按月份倒序
                    .map(salary -> {
                        Map<String, Object> monthData = new HashMap<>();
                        monthData.put("month", salary.getMonth());
                        monthData.put("totalSalary", salary.getTotalSalary());
                        monthData.put("actualBasePay", salary.getActualBasePay());
                        monthData.put("overtimePay", salary.getOvertimePay());
                        monthData.put("leaveDays", salary.getLeaveDays());
                        monthData.put("leaveDeduction", salary.getLeaveDeduction());
                        monthData.put("status", salary.getStatus());

                        // 计算当月总扣款
                        BigDecimal basePay = salary.getActualBasePay() != null ? salary.getActualBasePay() : BigDecimal.ZERO;
                        BigDecimal overtime = salary.getOvertimePay() != null ? salary.getOvertimePay() : BigDecimal.ZERO;
                        BigDecimal netSalary = salary.getTotalSalary() != null ? salary.getTotalSalary() : BigDecimal.ZERO;
                        monthData.put("totalDeductions", basePay.add(overtime).subtract(netSalary));

                        return monthData;
                    })
                    .collect(Collectors.toList());
            statistics.put("monthlyData", monthlyData);

            System.out.println("员工个人统计完成: " + statistics);

        } catch (Exception e) {
            System.err.println("获取个人薪资统计失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("获取个人薪资统计失败: " + e.getMessage(), e);
        }

        return statistics;
    }

    @Override
    public Map<String, Object> getMonthlySalaryDetail(Long staffId, String month) {
        Map<String, Object> detail = new HashMap<>();

        try {
            // 查询指定月份的薪资详情
            Map<String, Object> params = new HashMap<>();
            params.put("staffId", staffId);
            params.put("month", month);

            List<SalaryVO> salaryList = salaryDao.selectSalaryList(params);

            if (salaryList.isEmpty()) {
                detail.put("message", month + "月份暂无薪资记录");
                detail.put("hasData", false);
                return detail;
            }

            SalaryVO salary = salaryList.get(0); // 每月只有一条记录
            detail.put("hasData", true);
            detail.put("month", month);
            detail.put("actualBasePay", salary.getActualBasePay());
            detail.put("overtimePay", salary.getOvertimePay());
            detail.put("leaveDays", salary.getLeaveDays());
            detail.put("leaveDeduction", salary.getLeaveDeduction());
            detail.put("totalSalary", salary.getTotalSalary());
            detail.put("status", salary.getStatus());
            detail.put("calcTime", salary.getCalcTime());
            detail.put("remarks", salary.getRemarks());

            // 计算总扣款
            BigDecimal basePay = salary.getActualBasePay() != null ? salary.getActualBasePay() : BigDecimal.ZERO;
            BigDecimal overtime = salary.getOvertimePay() != null ? salary.getOvertimePay() : BigDecimal.ZERO;
            BigDecimal netSalary = salary.getTotalSalary() != null ? salary.getTotalSalary() : BigDecimal.ZERO;
            detail.put("totalDeductions", basePay.add(overtime).subtract(netSalary));

        } catch (Exception e) {
            throw new RuntimeException("获取月度薪资详情失败: " + e.getMessage(), e);
        }

        return detail;
    }

    @Override
    public Map<String, Object> getSalaryStatistics(String month) {
        Map<String, Object> statistics = new HashMap<>();

        try {
            System.out.println("📊 开始统计 " + month + " 月份数据...");

            // 获取公司名称用于报表
            String companyName = configService.getCompanyName();
            System.out.println("公司名称: " + companyName);

            // 基本统计 - 需要正确转换月份参数
            Map<String, Object> params = new HashMap<>();

            // 将字符串月份转换为Date对象
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            Date monthDate;
            try {
                monthDate = sdf.parse(month);
            } catch (Exception e) {
                System.err.println("月份格式解析错误: " + month);
                statistics.put("message", month + "月份格式错误");
                return statistics;
            }
            params.put("month", monthDate);

            System.out.println("查询参数: " + params);

            List<SalaryVO> salaryList = salaryDao.selectSalaryList(params);
            System.out.println("查询到的薪资记录数量: " + (salaryList != null ? salaryList.size() : 0));

            if (CollectionUtils.isEmpty(salaryList)) {
                statistics.put("message", month + "月份暂无薪资数据");
                return statistics;
            }

            System.out.println("✅ 找到 " + salaryList.size() + " 条薪资记录，开始统计...");

            // 薪资总额统计
            BigDecimal totalSalary = salaryList.stream()
                    .map(SalaryVO::getTotalSalary)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal avgSalary = totalSalary.divide(
                    new BigDecimal(salaryList.size()), 2, RoundingMode.HALF_UP);

            BigDecimal maxSalary = salaryList.stream()
                    .map(SalaryVO::getTotalSalary)
                    .filter(Objects::nonNull)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            BigDecimal minSalary = salaryList.stream()
                    .map(SalaryVO::getTotalSalary)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            // 按部门统计
            Map<String, List<SalaryVO>> deptSalaryMap = salaryList.stream()
                    .collect(Collectors.groupingBy(SalaryVO::getDeptName));

            Map<String, Object> deptStatistics = new HashMap<>();
            for (Map.Entry<String, List<SalaryVO>> entry : deptSalaryMap.entrySet()) {
                String deptName = entry.getKey();
                List<SalaryVO> deptSalaries = entry.getValue();

                BigDecimal deptTotal = deptSalaries.stream()
                        .map(SalaryVO::getTotalSalary)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal deptAvg = deptTotal.divide(
                        new BigDecimal(deptSalaries.size()), 2, RoundingMode.HALF_UP);

                Map<String, Object> deptInfo = new HashMap<>();
                deptInfo.put("employeeCount", deptSalaries.size());
                deptInfo.put("totalSalary", deptTotal);
                deptInfo.put("avgSalary", deptAvg);

                deptStatistics.put(deptName, deptInfo);
            }

            // 状态统计
            long pendingCount = salaryList.stream()
                    .filter(s -> "待审核".equals(s.getStatus()))
                    .count();
            long paidCount = salaryList.stream()
                    .filter(s -> "已发放".equals(s.getStatus()))
                    .count();

            statistics.put("companyName", companyName);
            statistics.put("month", month);
            statistics.put("totalCount", salaryList.size());
            statistics.put("totalSalary", totalSalary);
            statistics.put("avgSalary", avgSalary);
            statistics.put("maxSalary", maxSalary);
            statistics.put("minSalary", minSalary);
            statistics.put("deptStatistics", deptStatistics);
            statistics.put("pendingCount", pendingCount);
            statistics.put("paidCount", paidCount);

            System.out.println("📊 统计完成: " + statistics);

        } catch (Exception e) {
            System.err.println("❌ 统计失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("获取薪资统计失败: " + e.getMessage(), e);
        }

        return statistics;
    }

    @Override
    public PageUtils getMySalary(Map<String, Object> params, Long staffId) {
        // 强制设置员工ID，确保员工只能查看自己的记录
        params.put("staffId", staffId);
        return queryPage(params);
    }

    @Override
    public boolean isMonthCalculated(String month) {
        try {
            int count = salaryDao.countByMonth(month);
            System.out.println("月份 " + month + " 的薪资记录数量: " + count);
            return count > 0;
        } catch (Exception e) {
            System.err.println("检查月份是否已计算失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void recalculateStaffSalary(Long staffId, String month) {
        try {
            // 删除该员工的旧薪资记录
            Date monthDate;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                monthDate = sdf.parse(month);
            } catch (Exception e) {
                monthDate = new Date();
            }
            salaryDao.deleteByStaffIdAndMonth(staffId, monthDate);

            // 重新调用存储过程计算
            salaryDao.callCalculateSalary(month);

            System.out.println("成功重新计算员工 " + staffId + " " + month + " 月份薪资");

        } catch (Exception e) {
            throw new RuntimeException("重新计算员工薪资失败: " + e.getMessage(), e);
        }
    }
}