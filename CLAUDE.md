# 知拾录项目分析

## 项目概述
知拾录是一个个人知识收藏管理系统，用于记录和管理文章、网站、图片、视频等内容。

## 技术架构

### 后端技术栈
- **Java 17** + **Spring Boot 2.7.18**
- **Apache Shiro 2.0.4** + **JWT** - 认证鉴权
- **Elasticsearch 7.17.29** - 数据存储与全文搜索
- **Redis** - 缓存与Token存储
- **Log4j2** - 日志框架
- **Hutool 5.8.24** - Java工具库
- **ip2region 2.7.0** - IP地理位置定位

### 前端技术栈
- **Vue 3.5.27** + **TypeScript 5.9.3**
- **Vite 7.3.1** - 构建工具
- **Pinia 3.0.4** - 状态管理
- **Vue Router 5.0.1** - 路由管理
- **Tailwind CSS 4.1.18** - CSS框架
- **WangEditor 5.1.23** - 富文本编辑器
- **Axios 1.13.4** - HTTP客户端
- **canvas-nest.js** - 背景动画效果

## 核心功能模块

### 1. 用户认证系统
- 用户注册/登录功能
- JWT Token认证机制
- 基于Apache Shiro的权限控制
- Redis存储Token

### 2. 内容管理
- 文章CRUD操作（创建/编辑/删除/查询）
- 支持标题、类别、正文、网址、图片
- 草稿保存功能
- 内容权限控制（仅创建者可修改）
- 自动记录创建者、时间、地点信息

### 3. 搜索功能
- 标题模糊查询
- 类别精确查询
- 正文全文搜索（基于Elasticsearch）
- 用户名、地点查询
- 分页展示
- 自动补全建议（标题/内容/类别/用户名/地点）

### 4. 文件管理
- 单文件上传
- 批量文件上传
- 图片本地存储（ES仅存储路径）
- 文件下载功能

### 5. 统计分析
- 类别使用统计
- 用户常用类别推荐（Top 10）

### 6. 操作日志
- 全接口自动日志记录
- 记录接口路径、时间戳、参数、用户名
- IP地理位置记录
- 日志存储在Elasticsearch

### 7. 评论系统
- 文章评论功能

## 项目结构

### 后端结构
```
src/main/java/com/zhishilu/
├── ZhishiluApplication.java    # 主启动类
├── common/                      # 通用类
│   ├── Result.java             # 统一返回结果
│   └── PageResult.java         # 分页结果
├── config/                      # 配置类
│   ├── ShiroConfig.java        # Shiro配置
│   ├── WebMvcConfig.java       # MVC配置
│   ├── AsyncConfig.java        # 异步配置
│   ├── AdminConfig.java        # 管理员配置
│   └── ElasticsearchIndexInitializer.java  # ES索引初始化
├── controller/                  # 控制器层
│   ├── AuthController.java     # 认证接口
│   ├── ArticleController.java  # 文章接口
│   ├── FileController.java     # 文件接口
│   ├── UserController.java     # 用户接口
│   ├── CommentController.java  # 评论接口
│   └── CategoryController.java # 类别接口
├── service/                     # 业务逻辑层
│   ├── ArticleService.java
│   ├── UserService.java
│   ├── FileService.java
│   ├── CommentService.java
│   ├── CategoryStatsService.java
│   ├── OperationLogService.java
│   ├── IpLocationService.java
│   └── RedisTokenService.java
├── repository/                  # ES数据访问层
│   ├── ArticleRepository.java
│   ├── UserRepository.java
│   ├── CommentRepository.java
│   ├── CategoryStatsRepository.java
│   └── OperationLogRepository.java
├── entity/                      # 实体类
│   ├── Article.java
│   ├── User.java
│   ├── Comment.java
│   ├── CategoryStats.java
│   ├── OperationLog.java
│   └── *Suggestion.java        # 自动补全建议实体
├── dto/                         # 数据传输对象
│   ├── ArticleDTO.java
│   └── UserDTO.java
├── req/                         # 请求对象
│   ├── LoginReq.java
│   ├── RegisterReq.java
│   ├── ArticleCreateReq.java
│   ├── ArticleUpdateReq.java
│   ├── ArticleQueryReq.java
│   ├── DraftSaveReq.java
│   └── CommentCreateReq.java
├── resp/                        # 响应对象
│   ├── LoginResp.java
│   ├── ArticleResp.java
│   ├── UserResp.java
│   ├── CommentResp.java
│   ├── DraftResp.java
│   ├── FileUploadResp.java
│   ├── CategoryStatResp.java
│   ├── SearchSuggestionResp.java
│   └── HotKeywordResp.java
├── shiro/                       # Shiro认证相关
│   ├── JwtToken.java           # JWT Token
│   ├── JwtRealm.java           # JWT Realm
│   └── JwtFilter.java          # JWT过滤器
├── filter/                      # 过滤器
│   └── AccessLogFilter.java    # 访问日志过滤器
├── interceptor/                 # 拦截器
│   └── OperationLogInterceptor.java  # 操作日志拦截器
├── exception/                   # 异常处理
│   ├── BusinessException.java
│   └── GlobalExceptionHandler.java
└── util/                        # 工具类
    ├── JwtUtil.java            # JWT工具
    ├── UserContext.java        # 用户上下文
    ├── IpUtil.java             # IP工具
    ├── AdminUtil.java          # 管理员工具
    └── EsCompletionSuggestUtil.java  # ES自动补全工具
```

### 前端结构
```
frontend/src/
├── App.vue                      # 根组件
├── main.ts                      # 入口文件
├── views/                       # 页面视图
│   ├── HomeView.vue            # 首页
│   ├── LoginView.vue           # 登录页
│   ├── RegisterView.vue        # 注册页
│   ├── ArticleCreateView.vue   # 文章创建页
│   ├── ArticleEditView.vue     # 文章编辑页
│   ├── ArticleDetailView.vue   # 文章详情页
│   ├── DraftEditView.vue       # 草稿编辑页
│   └── ProfileView.vue         # 个人中心
├── components/                  # 组件
│   ├── ArticleCard.vue         # 文章卡片
│   ├── RichEditor.vue          # 富文本编辑器
│   ├── AdvancedModal.vue       # 高级弹窗
│   ├── ConfirmationModal.vue   # 确认弹窗
│   ├── ShareModal.vue          # 分享弹窗
│   └── profile/                # 个人中心组件
│       ├── ArticleDetail.vue
│       ├── ArticleEdit.vue
│       └── DraftEdit.vue
├── stores/                      # Pinia状态管理
│   └── counter.ts
└── utils/                       # 工具类
    └── request.ts              # Axios请求封装
```

## API接口

### 认证接口
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录

### 文章接口
- `POST /api/article` - 创建文章
- `PUT /api/article/{id}` - 更新文章
- `DELETE /api/article/{id}` - 删除文章
- `GET /api/article/{id}` - 获取文章详情
- `GET /api/article/list` - 分页查询文章
- `GET /api/article/categories/top` - 获取常用类别

### 文件接口
- `POST /api/file/upload` - 上传单个文件
- `POST /api/file/upload/batch` - 批量上传文件
- `GET /api/file/download` - 下载文件

### 用户接口
- 用户相关操作

### 评论接口
- 评论相关操作

### 类别接口
- 类别统计相关操作

## 代码规范（AGENTS.md）

### 代码风格
- 符合阿里巴巴Java开发手册
- 遵循提早返回/提前跳出原则，提高代码可读性和可维护性
- Controller类只能使用对应的Service类，不能跨Service调用
- 常量必须使用枚举类，不得硬编码

### 安全要求
- 所有数据库查询必须使用参数化查询，禁止字符串拼接
- 敏感信息不得硬编码或记录到日志
- 所有外部输入必须进行验证和清理

### 错误处理
- 使用具体的异常类型，避免捕获通用Exception
- 确保资源正确关闭（使用try-with-resources）

## 环境要求

### 后端
- JDK 17+
- Maven 3.6+
- Elasticsearch 7.17.x
- Redis

### 前端
- Node.js ^20.19.0 || >=22.12.0
- npm/yarn/pnpm

## 快速启动

### 后端启动
1. 配置 `application.yml` 中的ES和Redis连接信息
2. 运行 `mvn spring-boot:run`
3. 服务地址：`http://localhost:8080/api`

### 前端启动
1. 进入frontend目录
2. 安装依赖：`npm install`
3. 启动开发服务器：`npm run dev`

## 项目特点

1. **前后端分离架构** - 清晰的职责划分
2. **Elasticsearch全文搜索** - 强大的搜索能力和自动补全
3. **JWT + Shiro认证** - 安全的认证授权机制
4. **完善的日志系统** - 访问日志和操作日志双重记录
5. **IP地理位置** - 自动记录用户地理位置
6. **现代化前端** - Vue 3 + TypeScript + Tailwind CSS
7. **富文本编辑** - WangEditor支持丰富的内容编辑
8. **代码规范** - 遵循阿里巴巴开发手册

## 当前开发状态
- 分支：dev_1.0_20260131
- 最近更新：登录/注册动画、图片切换优化、canvas-nest背景效果
