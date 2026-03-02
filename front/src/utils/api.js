// utils/api.js

class API {
    baseURL = "";
    url = {
        // ------------------------------------------------
        // 用户管理模块 (UserController)
        // ------------------------------------------------
        user: {
            login: "/users/login",                    // POST 登录
            register: "/users/register",              // POST 注册
            logout: "/users/logout",                  // GET 退出
            resetPass: "/users/resetPass",            // 密码重置
            changePassword: "/users/changePassword",
            page: "/users/page",                      // 分页列表
            list: "/users/list",                      // 全部列表
            info: "/users/info/{id}",                 // 单条信息
            session: "/users/session",                // 当前用户信息
            save: "/users/save",                      // POST 新增
            update: "/users/update",                  // 修改
            delete: "/users/delete",                  // 删除
            sendResetCode: "/users/sendResetCode",
            resetPasswordByPhone: "/users/resetPassword",
        },

        // ------------------------------------------------
        // 选项数据模块
        // ------------------------------------------------
        option: {
            department: "/department/list",    // 获取部门列表
            job: "/job/list",                  // 获取岗位列表
        },

        // ------------------------------------------------
        // 薪资管理模块 (SalaryController)
        // ------------------------------------------------
        salary: {
            mySalary: "/salary/mySalary",             // GET 我的薪资记录
            detail: "/salary/detail/{id}",            // GET 薪资详情（通过ID）
            monthlyDetail: "/salary/monthlyDetail",   // GET 我的月度薪资详情
            hrMonthlyDetail: "/salary/hrMonthlyDetail", // GET HR查看任意员工月度详情
            list: "/salary/list",                     // GET HR薪资列表
            calculate: "/salary/calculate",           // POST 计算月度薪资
            export: "/salary/export",
            exportMySalary: "/salary/exportMySalary",// GET 导出薪资Excel
            updateStatus: "/salary/updateStatus",     // POST 更新薪资状态
            statistics: "/salary/statistics",         // GET 薪资统计
            checkCalculated: "/salary/checkCalculated", // GET 检查是否已计算
            recalculate: "/salary/recalculate",       // POST 重新计算
            myStatistics: "/salary/myStatistics",     // GET 我的薪资统计（当前年）
            myStatisticsByYear: "/salary/myStatistics/{year}", // GET 我的薪资统计（指定年）
        },
        // ------------------------------------------------
        // 公告管理模块 (NoticeController)
        // ------------------------------------------------
        notice: {
            page: "/notice/page",                     // 后端列表
            list: "/notice/list",                     // 前端列表
            frontList: "/notice/frontList",           // 前端列表优化版
            lists: "/notice/lists",                   // 列表查询
            query: "/notice/query",                   // 查询
            info: "/notice/info/{id}",                // 后端详情
            detail: "/notice/detail/{id}",            // 前端详情
            save: "/notice/save",                     // 后端保存
            add: "/notice/add",                       // 前端保存
            update: "/notice/update",                 // 修改
            delete: "/notice/delete",                 // 删除
            remind: "/notice/remind/{columnName}/{type}", // 提醒接口
            statistics: "/notice/statistics",         // 统计接口
            latest: "/notice/latest",                 // 获取最新公告
        },

        // ------------------------------------------------
        // 请假管理模块 (LeaveRequestController)
        // ------------------------------------------------
        leave: {
            apply: "/leave/apply",                    // POST 提交请假申请
            myApplications: "/leave/myApplications",  // GET 我的请假记录
            cancel: "/leave/cancel/{id}",             // POST 撤销请假申请
            detail: "/leave/detail/{id}",             // GET 请假详情
        },

        // ------------------------------------------------
        // HR请假审批模块 (HrLeaveController)
        // ------------------------------------------------
        hrLeave: {
            pendingList: "/hr/leave/pendingList",     // GET 待审批列表
            approve: "/hr/leave/approve/{id}",        // POST 审批通过
            reject: "/hr/leave/reject/{id}",           // POST 审批拒绝
            list: "/hr/leave/list",                   // GET 所有请假记录
        },

        // ------------------------------------------------
        // 岗位管理模块 (JobController)
        // ------------------------------------------------
        job: {
            page: "/job/page",                        // 后端列表
            list: "/job/list",                        // 前端列表
            lists: "/job/lists",                      // 列表查询
            query: "/job/query",                      // 查询
            info: "/job/info/{id}",                   // 后端详情
            detail: "/job/detail/{id}",               // 前端详情
            save: "/job/save",                        // 后端保存
            add: "/job/add",                          // 前端保存
            update: "/job/update",                    // 修改
            delete: "/job/delete",                    // 删除
            remind: "/job/remind/{columnName}/{type}", // 提醒接口
        },

        // ------------------------------------------------
        // 部门管理模块 (DepartmentController)
        // ------------------------------------------------
        department: {
            page: "/department/page",                 // 后端列表
            list: "/department/list",                 // 前端列表
            lists: "/department/lists",               // 列表查询
            query: "/department/query",               // 查询
            info: "/department/info/{id}",            // 后端详情
            detail: "/department/detail/{id}",        // 前端详情
            save: "/department/save",                 // 后端保存
            add: "/department/add",                   // 前端保存
            update: "/department/update",             // 修改
            delete: "/department/delete",             // 删除
            remind: "/department/remind/{columnName}/{type}", // 提醒接口
        },

        // ------------------------------------------------
        // 文件管理模块 (FileController)
        // ------------------------------------------------
        file: {
            upload: "/file/upload",                   // 上传文件
            download: "/file/download",               // 下载文件
        },

        // ------------------------------------------------
        // 考勤管理模块 (AttendanceController)
        // ------------------------------------------------
        attendance: {
            checkIn: "/attendance/checkIn",           // POST 上班打卡
            checkOut: "/attendance/checkOut",         // POST 下班打卡
            manualCheck: "/attendance/manualCheck",   // POST 手动补打卡
            myRecords: "/attendance/myRecords",       // GET 我的考勤记录
            myStatistics: "/attendance/myStatistics", // GET 我的考勤统计
            export: "/attendance/export",             // POST 导出考勤数据（新增）
            list: "/attendance/list",                 // GET HR查看所有考勤记录
            departmentStats: "/attendance/departmentStats", // GET 部门考勤统计
        },

        // ------------------------------------------------
        // 配置管理模块 (ConfigController)
        // ------------------------------------------------
        config: {
            list: "/config/list",                     // GET 配置列表（分页）
            all: "/config/all",                       // GET 获取所有配置
            salary: "/config/salary",                 // GET 获取薪资相关配置
            salaryValues: "/config/salaryValues",     // GET 获取薪资配置值
            info: "/config/info/{id}",                // GET 根据ID获取配置
            infoByName: "/config/infoByName/{name}",  // GET 根据名称获取配置
            save: "/config/save",                     // POST 保存配置
            update: "/config/update",                 // POST 更新配置
            updateByName: "/config/updateByName",     // POST 根据名称更新配置值
            delete: "/config/delete",                 // POST 删除配置
            batchUpdate: "/config/batchUpdate",       // POST 批量更新配置
        },

        // ------------------------------------------------
        // 积分订单模块
        // ------------------------------------------------
        order: {
            page: "/orders/page",                     // 分页
            delete: "/orders/delete",                 // 删除
            info: "/orders/info/{id}",                // 详情
            save: "/orders/save",                     // 新增
            update: "/orders/update",                 // 修改
        }
    };

    // 可选：添加路径参数替换方法
    buildUrl(url, params = {}) {
        let builtUrl = url;
        Object.keys(params).forEach(key => {
            builtUrl = builtUrl.replace(`{${key}}`, params[key]);
        });
        return builtUrl;
    }
}

export default new API();