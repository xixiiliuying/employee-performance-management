# employee-performance-management （员工考勤薪资核算系统）
## 项目概述
员工考勤薪资核算系统是用于企业管理员工考勤及计算薪资的 Web 应用，简化考勤统计与薪资发放流程。开发了一个支持员工- HR管理员双角色的系统，员工能够记录每日打卡（标注 “正常/迟到/早退/迟到早退/缺卡/缺勤/请假”，迟到/早退按30分钟为界限判断）、提交请假申请（填写请假类型、请假天数），HR 管理员能够查看员工考勤记录（按月份统计）、设置基础工资和加班费标准（如加班费按基础工资 1.5 倍 / 小时计算），系统自动计算员工月薪资。

功能模块：用户登录、员工管理、考勤管理/打卡、请假申请/审批处理、薪资管理、部门管理、通知公告、个人中心 

技术栈：SpringBoot + Vue2 + MyBatis Plus + Element UI + MySQL

适用场景：中小企业人力资源管理和员工绩效考核

业务价值：实现员工考勤情况与薪资数字化管理

## 项目演示
- 登录/注册
<img width="2878" height="1468" alt="image" src="https://github.com/user-attachments/assets/05fd19f7-54aa-4b10-86bf-2f71ee75b00e" />
<img width="2878" height="1474" alt="image" src="https://github.com/user-attachments/assets/d72b1080-35e2-4fa1-99c9-34bbb9727449" />

- 员工端
<img width="2862" height="1468" alt="image" src="https://github.com/user-attachments/assets/cac9f113-8baf-4f24-a608-d044a753f5e0" />

- 管理员端
<img width="2858" height="1468" alt="image" src="https://github.com/user-attachments/assets/36e7a34a-4506-4baf-a8bf-a534bcaeeca8" />

【展示地址待补充】

## 📂 项目结构

```
T021/
├── back/                     # 后端 Spring Boot 项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/     # Java 源代码
│   │   │   │   ├── annotation/     # 自定义注解
│   │   │   │   ├── config/         # 配置类
│   │   │   │   ├── controller/     # 控制层
│   │   │   │   ├── entity/         # 实体类
│   │   │   │   ├── dao/            # 数据访问层
│   │   │   │   ├── service/        # 业务逻辑层
│   │   │   │   ├── converter/      # 类型转换器
│   │   │   │   ├── resolver/       # 自定义解析器
│   │   │   │   ├── interceptor/    # 拦截器
│   │   │   │   └── utils/          # 工具类
│   │   │   ├── resources/
│   │   │   │   ├── mapper/         # MyBatis XML 映射文件
│   │   │   │   ├── application.yml # 应用配置文件
│   │   │   │   └── static/         # 静态资源
│   │   └── test/              # 测试代码
│   └── pom.xml               # Maven 依赖配置
│
├── front/                     # 前端 Vue 项目
│   ├── src/
│   │   ├── components/        # Vue 组件
│   │   │   ├── common/        # 通用组件
│   │   │   └── SvgIcon/       # 图标组件
│   │   ├── view/              # 页面视图
│   │   │   ├── hr/            # HR 模块页面
│   │   │   └── staffs/        # 员工模块页面
│   │   ├── store/             # Vuex 状态管理
│   │   ├── router/            # 路由配置
│   │   ├── utils/             # 工具函数
│   │   ├── assets/            # 静态资源 (CSS, 图片)
│   │   ├── icons/             # SVG 图标
│   │   ├── App.vue            # 根组件
│   │   └── main.js            # 入口文件
│   ├── public/                # 公共目录
│   ├── package.json           # npm 依赖配置
│   ├── vue.config.js          # Vue CLI 配置
│   ├── babel.config.js        # Babel 配置
│   ├── 1-install.bat          # 安装依赖脚本 (Windows)
│   ├── 2-run.bat              # 运行开发服务器脚本 (Windows)
│   └── 3-build.bat            # 构建生产版本脚本 (Windows)
│
└── README.md                  # 项目文档
```

## 🚀 快速开始

### 前置条件
- **JDK 1.8+** (推荐 JDK 8 或 17)
- **Maven 3.6+**
- **Node.js 12+** (前端开发)
- **MySQL 5.7+** (已配置云数据库)

### 后端部署

#### 1. 进入后端项目目录
```bash
cd back
```

#### 2. 使用 Maven 构建
```bash
# 编译和打包
mvn clean package -DskipTests

# 如果只想验证 POM 配置
mvn validate
```

#### 3. 运行应用
```bash
# 方式一：使用 Spring Boot Maven 插件
mvn spring-boot:run

# 方式二：运行生成的 JAR 文件
java -jar target/T021-0.0.1-SNAPSHOT.jar
```

**后端访问地址**: `http://localhost:8081/springboot`

#### 4. 数据库配置

配置文件位置: [back/src/main/resources/application.yml](back/src/main/resources/application.yml)

### 前端部署

#### 1. 进入前端项目目录
```bash
cd front
```

#### 2. 安装依赖
```bash
npm install
# 或使用国内镜像加速
npm install --registry=https://registry.npmmirror.com
```

#### 3. 开发模式运行
```bash
npm run serve
```

**前端访问地址**: `http://localhost:8082` 

#### 4. 生产构建
```bash
npm run build
```

**快速脚本 (Windows)**:
```bash
# 进入前端目录
cd front

# 安装依赖
1-install.bat

# 开发模式运行
2-run.bat

# 生产构建
3-build.bat
```

## 📋 API 文档

后端应用启动后，所有 API 接口的基础路径为:
```
http://localhost:8081/springboot/*
```

## 🔐 权限与安全

系统使用 **Apache Shiro** 进行权限管理，支持：
- 用户认证与授权
- 角色与权限控制
- Token 令牌验证
- 拦截器防护

Token 数据存储在 `token` 表中（数据库）

## 📊 主要功能特性

### HR 模块
- ✅ 员工信息维护（添加、编辑、删除）
- ✅ 部门与职位管理
- ✅ 考勤打卡记录查询
- ✅ 请假申请与审批流程
- ✅ 薪资计算与发放
- ✅ 员工通知公告

### 数据处理
- ✅ Excel 导入导出（使用 EasyExcel）
- ✅ 图表展示与分析（ECharts）
- ✅ 富文本编辑支持
- ✅ 文件上传管理

## 🔧 开发指南

### 后端开发建议
1. 创建新功能时，遵循分层架构：`entity` → `dao` → `service` → `controller`
2. MyBatis 映射文件统一放在 `src/main/resources/mapper/` 目录
3. 使用 Shiro 进行权限控制
4. 参考现有代码规范（如 UserDao.xml）

### 前端开发建议
1. 新增页面放在 `src/view/` 目录
2. 可复用组件放在 `src/components/` 目录
3. API 调用在 `src/utils/api.js` 中定义
4. 使用 Element UI 组件保持风格一致

