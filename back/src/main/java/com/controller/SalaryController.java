package com.controller;

import com.annotation.LoginUser;
import com.entity.UserEntity;
import com.entity.vo.SalaryVO;
import com.service.SalaryService;
import com.utils.PageUtils;
import com.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 薪资核算
 */
@RestController
@RequestMapping("/salary")
public class SalaryController {

    @Autowired
    private SalaryService salaryService;

    /**
     * 我的薪资记录
     */
    @GetMapping("/mySalary")
    public R mySalary(@RequestParam Map<String, Object> params,
                      @LoginUser UserEntity user) {
        try {
            PageUtils page = salaryService.getMySalary(params, user.getId());
            return R.ok().put("page", page);
        } catch (Exception e) {
            return R.error("获取薪资记录失败: " + e.getMessage());
        }
    }

    /**
     * 薪资详情（通过ID）
     */
    @GetMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id, @LoginUser UserEntity user) {
        try {
            SalaryVO salary = salaryService.getDetail(id, user);
            return R.ok().put("data", salary);
        } catch (Exception e) {
            return R.error("获取详情失败: " + e.getMessage());
        }
    }

    /**
     * 月度薪资详情（通过员工ID和月份）
     */
    @GetMapping("/monthlyDetail")
    public R monthlyDetail(@RequestParam String month,
                           @LoginUser UserEntity user) {
        try {
            // 员工只能查看自己的月度详情
            Map<String, Object> detail = salaryService.getMonthlySalaryDetail(user.getId(), month);
            return R.ok().put("data", detail);
        } catch (Exception e) {
            return R.error("获取月度详情失败: " + e.getMessage());
        }
    }

    /**
     * HR查看任意员工月度详情
     */
    @GetMapping("/hrMonthlyDetail")
    public R hrMonthlyDetail(@RequestParam Long staffId,
                             @RequestParam String month,
                             @LoginUser UserEntity user) {
        try {
            // 权限检查
            if (!"hr".equals(user.getRole())) {
                return R.error("权限不足，仅HR可查看其他员工薪资详情");
            }
            Map<String, Object> detail = salaryService.getMonthlySalaryDetail(staffId, month);
            return R.ok().put("data", detail);
        } catch (Exception e) {
            return R.error("获取月度详情失败: " + e.getMessage());
        }
    }

    /**
     * HR薪资列表查询
     */
    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> params,
                  @LoginUser UserEntity user) {
        try {
            // 权限检查
            if (!"hr".equals(user.getRole())) {
                return R.error("权限不足，仅HR可查看所有薪资记录");
            }
            PageUtils page = salaryService.queryPage(params);
            return R.ok().put("page", page);
        } catch (Exception e) {
            return R.error("获取列表失败: " + e.getMessage());
        }
    }

    /**
     * 计算月度薪资
     */
    @PostMapping("/calculate")
    public R calculateSalary(@RequestParam String month,
                             @LoginUser UserEntity user) {
        try {
            // 权限检查
            if (!"hr".equals(user.getRole())) {
                return R.error("权限不足，仅HR可执行薪资计算");
            }
            int count = salaryService.calculateMonthlySalary(month);
            return R.ok("薪资计算完成，共计算 " + count + " 条记录");
        } catch (Exception e) {
            return R.error("薪资计算失败: " + e.getMessage());
        }
    }

    /**
     * 导出薪资Excel
     */
    @GetMapping("/export")
    public void exportSalary(@RequestParam String month,
                             HttpServletResponse response,
                             @LoginUser UserEntity user) {
        try {
            // 权限检查
            if (!"hr".equals(user.getRole())) {
                throw new RuntimeException("权限不足，仅HR可导出薪资");
            }
            salaryService.exportSalary(month, response);
        } catch (Exception e) {
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    /**
     * 员工导出个人工资条
     */
    @GetMapping("/exportMySalary")
    public void exportMySalary(@RequestParam String month,
                               HttpServletResponse response,
                               @LoginUser UserEntity user) {
        try {
            response.setHeader("Access-Control-Allow-Origin", "http://localhost:8082");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            salaryService.exportMySalary(month, user.getId(), response);

        } catch (Exception e) {
            throw new RuntimeException("导出工资条失败: " + e.getMessage());
        }
    }

    /**
     * 更新薪资状态（发放薪资）
     */
    @PostMapping("/updateStatus")
    public R updateStatus(@RequestBody Map<String, Object> params,
                          @LoginUser UserEntity user) {
        System.out.println("updateStatus方法被调用了！");
        System.out.println("当前用户: " + user.getSname() + ", 角色: " + user.getRole());
        System.out.println("接收到的参数: " + params);

        try {
            // 权限检查
            if (!"hr".equals(user.getRole())) {
                System.out.println(" 权限检查失败: 用户角色不是HR");
                return R.error("权限不足，仅HR可更新薪资状态");
            }
            System.out.println(" 权限检查通过");

            // 打印参数详情
            Object idsObj = params.get("ids");
            Object statusObj = params.get("status");
            System.out.println("ids参数: " + idsObj + " (类型: " + (idsObj != null ? idsObj.getClass().getSimpleName() : "null") + ")");
            System.out.println("status参数: " + statusObj + " (类型: " + (statusObj != null ? statusObj.getClass().getSimpleName() : "null") + ")");

            salaryService.updateSalaryStatus(params);
            System.out.println(" Service调用完成");
            return R.ok("状态更新成功");

        } catch (Exception e) {
            System.err.println(" Controller异常: " + e.getMessage());
            e.printStackTrace();
            return R.error("状态更新失败: " + e.getMessage());
        }
    }

    /**
     * 我的薪资统计（员工个人）- 当前年份
     */
    @GetMapping("/myStatistics")
    public R myStatistics(@LoginUser UserEntity user) {
        try {
            // 获取当前年份
            String currentYear = new SimpleDateFormat("yyyy").format(new Date());
            Map<String, Object> stats = salaryService.getMySalaryStatistics(user.getId(), currentYear);
            return R.ok().put("data", stats);
        } catch (Exception e) {
            return R.error("获取个人统计失败: " + e.getMessage());
        }
    }

    /**
     * 我的薪资统计（员工个人）- 指定年份
     */
    @GetMapping("/myStatistics/{year}")
    public R myStatisticsByYear(@PathVariable String year,
                                @LoginUser UserEntity user) {
        try {
            Map<String, Object> stats = salaryService.getMySalaryStatistics(user.getId(), year);
            return R.ok().put("data", stats);
        } catch (Exception e) {
            return R.error("获取个人统计失败: " + e.getMessage());
        }
    }

    /**
     * 薪资统计（HR）
     */
    @GetMapping("/statistics")
    public R statistics(@RequestParam String month,
                        @LoginUser UserEntity user) {
        try {
            // 权限检查
            if (!"hr".equals(user.getRole())) {
                return R.error("权限不足，仅HR可查看统计");
            }
            return R.ok().put("data", salaryService.getSalaryStatistics(month));
        } catch (Exception e) {
            return R.error("获取统计失败: " + e.getMessage());
        }
    }

    /**
     * 检查是否已计算某月薪资
     */
    @GetMapping("/checkCalculated")
    public R checkCalculated(@RequestParam String month) {
        try {
            boolean calculated = salaryService.isMonthCalculated(month);
            return R.ok().put("calculated", calculated);
        } catch (Exception e) {
            return R.error("检查失败: " + e.getMessage());
        }
    }

    /**
     * 重新计算单个员工薪资
     */
    @PostMapping("/recalculate")
    public R recalculate(@RequestParam Long staffId,
                         @RequestParam String month,
                         @LoginUser UserEntity user) {
        try {
            // 权限检查
            if (!"hr".equals(user.getRole())) {
                return R.error("权限不足，仅HR可重新计算薪资");
            }
            salaryService.recalculateStaffSalary(staffId, month);
            return R.ok("重新计算成功");
        } catch (Exception e) {
            return R.error("重新计算失败: " + e.getMessage());
        }
    }
}