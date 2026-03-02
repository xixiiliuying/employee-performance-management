import Vue from 'vue';
// 配置路由
import VueRouter from 'vue-router';
Vue.use(VueRouter);

// 1. 创建组件
import Index from '@/view/index';
import Login from '@/view/login';
import NotFound from '@/view/404';

import pay from '@/view/pay';
import center from '@/view/center';

// HR管理相关页面
import EmployeeManage from '@/view/hr/EmployeeManage';
import DepartmentManage from '@/view/hr/DepartmentManage';
import SalaryManage from '@/view/hr/SalaryManage';
import LeaveManage from '@/view/hr/LeaveManage';
import AttendanceManage from '@/view/hr/AttendanceManage';
import HrIndexPage from '@/view/hr/HrIndexPage';
import NoticeManage from "@/view/hr/NoticeManage.vue";

// 员工端功能页面
import AttendancePage from '@/view/staffs/AttendancePage';
import SalaryPage from '@/view/staffs/SalaryPage';
import LeavePage from '@/view/staffs/LeavePage';
import NoticePage from '@/view/staffs/NoticePage';
import IndexPage from '@/view/staffs/IndexPage';


// 2. 配置路由
const routes = [
    {
        path: '/index',
        name: '首页',
        component: Index,
        redirect: '/index/home' ,// 先默认重定向到home
        children: [
            {
                path: 'home',
                name: '首页概览',
                component: IndexPage, // 使用 IndexPage 组件
                meta: { icon: '', title: 'home', requireStaff: true }
            },
            {
                path: 'hr-home', // 新增HR首页路由
                name: 'HR工作台',
                component: HrIndexPage,
                meta: { icon: '', title: 'hrHome', requireHr: true } // 添加HR权限标识
            },
            {
                path: 'attendance', // 去掉开头的斜杠
                name: '考勤打卡',
                component: AttendancePage,
                meta: { icon: '', title: 'attendance', requireStaff: true }
            },
            {
                path: 'salary', // 去掉开头的斜杠
                name: '薪资查询',
                component: SalaryPage,
                meta: { icon: '', title: 'salary', requireStaff: true }
            },
            {
                path: 'leave', // 去掉开头的斜杠
                name: '请假申请',
                component: LeavePage,
                meta: { icon: '', title: 'leave', requireStaff: true }
            },
            {
                path: 'notice', // 去掉开头的斜杠
                name: '公告查看',
                component: NoticePage,
                meta: { icon: '', title: 'notice'}
            },
            {
                path: 'hr/employee', // 去掉开头的斜杠
                name: '员工管理',
                component: EmployeeManage,
                meta: { icon: '', title: 'employee', requireHr: true } // 添加权限标识
            },
            {
                path: 'hr/department', // 去掉开头的斜杠
                name: '部门管理',
                component: DepartmentManage,
                meta: { icon: '', title: 'department', requireHr: true } // 添加权限标识
            },
            {
                path: 'hr/salary-manage', // 修正路径，避免与员工薪资查询冲突
                name: '工资管理',
                component: SalaryManage,
                meta: { icon: '', title: 'salaryManage', requireHr: true } // 添加权限标识
            },
            {
                path: 'hr/leave-manage', // 修正路径，避免与员工请假申请冲突
                name: '请假管理',
                component: LeaveManage,
                meta: { icon: '', title: 'leaveManage', requireHr: true } // 添加权限标识
            },
            {
                path: 'hr/attendance-manage', // 修正路径，避免与员工考勤打卡冲突
                name: '员工出勤管理',
                component: AttendanceManage,
                meta: { icon: '', title: 'attendanceManage', requireHr: true } // 添加权限标识
            },
            {
                path: 'hr/notice-manage',
                name: '公告管理',
                component: NoticeManage,
                meta: { icon: '', title: 'noticeManage', requireHr: true } // 添加权限标识
            },
            {
                path: 'pay', // 去掉开头的斜杠
                name: '支付',
                component: pay,
                meta: { icon: '', title: 'pay' }
            },
            {
                path: 'center', // 去掉开头的斜杠
                name: '个人信息',
                component: center,
                meta: { icon: '', title: 'center' }
            }
        ]
    },
    {
        path: '/login',
        name: '登录',
        component: Login,
        meta: { icon: '', title: 'login' }
    },
    {
        path: '/',
        name: '首页',
        redirect: '/login'
    },
    {
        path: '*',
        component: NotFound
    }
];

// 3. 实例化 VueRouter
const router = new VueRouter({
    mode: 'hash',
    routes
});

// 修改路由守卫
router.beforeEach((to, from, next) => {
    // 获取用户信息
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    const isHr = userInfo.role === 'hr';

    // 如果是访问首页，根据角色重定向
    if (to.path === '/index' || to.path === '/index/') {
        const targetPath = isHr ? '/index/hr-home' : '/index/home';
        next(targetPath);
        return;
    }

    // 检查是否需要HR权限
    if (to.meta.requireHr && !isHr) {
        next('/index/home');
        return;
    }

    // 检查是否需要员工权限
    if (to.meta.requireStaff && isHr) {
        next('/index/hr-home');
        return;
    }

    next();
});

export default router;