# 知拾录 - 个人知识收藏管理系统

## 项目简介

知拾录是一个用于记录和管理好文章、网站、图片、视频等内容的个人知识管理系统。

## 技术栈

- **Java 17**
- **Spring Boot 3.2.1**
- **Spring MVC**
- **Apache Shiro + JWT** (认证鉴权)
- **Elasticsearch 8.x** (数据存储)
- **Lombok**

## 功能特性

### 1. 用户认证
- 用户注册/登录
- JWT Token认证
- 基于Shiro的权限控制

### 2. 内容管理
- 创建/编辑/删除内容
- 支持标题、类别、正文、网址、图片
- 自动记录创建者、时间、地点
- 内容只能由创建者修改

### 3. 内容查询
- 支持标题模糊查询
- 支持类别精确查询
- 支持正文全文搜索
- 支持用户名、地点查询
- 分页展示

### 4. 类别推荐
- 获取用户最常用的10个类别
- 支持自定义类别

### 5. 文件上传
- 支持多图片上传
- 图片存储在本地，ES只存路径

### 6. 操作日志
- 所有接口自动记录日志
- 记录：接口路径、时间戳、参数、用户名
- 日志存储在ES中

## API接口

### 认证接口
```
POST /api/auth/register  - 用户注册
POST /api/auth/login     - 用户登录
```

### 文章接口
```
POST   /api/article           - 创建文章
PUT    /api/article/{id}      - 更新文章
DELETE /api/article/{id}      - 删除文章
GET    /api/article/{id}      - 获取文章详情
GET    /api/article/list      - 分页查询文章
GET    /api/article/categories/top - 获取常用类别
```

### 文件接口
```
POST /api/file/upload       - 上传单个文件
POST /api/file/upload/batch - 批量上传文件
GET  /api/file/download     - 下载文件
```

## 快速开始

### 1. 环境要求
- JDK 17+
- Maven 3.6+
- Elasticsearch 8.x

### 2. 配置ES
修改 `application.yml` 中的ES配置：
```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200
    username: elastic
    password: your_password
```

### 3. 运行项目
```bash
mvn spring-boot:run
```

### 4. 访问接口
服务启动后，接口地址为：`http://localhost:8080/api`

## 目录结构

```
src/main/java/com/zhishilu/
├── ZhishiluApplication.java    # 主启动类
├── common/                      # 通用类
│   ├── Result.java             # 统一返回结果
│   └── PageResult.java         # 分页结果
├── config/                      # 配置类
│   ├── ShiroConfig.java        # Shiro配置
│   ├── WebMvcConfig.java       # MVC配置
│   └── AsyncConfig.java        # 异步配置
├── controller/                  # 控制器
│   ├── AuthController.java     # 认证接口
│   ├── ArticleController.java  # 文章接口
│   └── FileController.java     # 文件接口
├── dto/                         # 数据传输对象
├── entity/                      # 实体类
│   ├── Article.java            # 文章实体
│   ├── User.java               # 用户实体
│   └── OperationLog.java       # 操作日志实体
├── exception/                   # 异常处理
├── filter/                      # 过滤器
├── interceptor/                 # 拦截器
├── repository/                  # ES仓库
├── service/                     # 服务层
├── shiro/                       # Shiro相关
│   ├── JwtToken.java           # JWT Token
│   ├── JwtRealm.java           # JWT Realm
│   └── JwtFilter.java          # JWT过滤器
└── util/                        # 工具类
    ├── JwtUtil.java            # JWT工具
    └── UserContext.java        # 用户上下文
```
