---
trigger: glob
glob: *.java
---

## 版本要求
- JDK 1.8
- Spring-boot 2.3.2.RELEASE
- mybatis-plus-boot-starter 3.3.0

## 代码风格
- 符合阿里巴巴Java 开发手册
- service层和Mapper层和XML层都要保留
- 使用mybatis plus LambdaQueryWrapper 编写sql语句, (除连表sql在*.xml里编写sql)
- 遵循提早返回/提前跳出的原则，可以显著提高代码的可读性、可维护性
- {A}Controller类 只能使用对应{A}Service类,不能使用其它{B}Service类
- {A}Service类 只能使用对应{A}Mapper/{A}Dao类,不能使用其它{B}Mapper/{B}Dao类
- 常量对应有枚举类，必须使用枚举类
- 固定数值或字符串定义为常量
- 查询数据库,只返回对应的字段

## 安全要求
- 所有数据库查询必须使用参数化查询，禁止字符串拼接
- 敏感信息不得硬编码或记录到日志
- 所有外部输入必须进行验证和清理

## 错误处理
- 使用具体的异常类型，避免捕获通用 Exception
- 确保资源正确关闭(使用 try-with-resources)