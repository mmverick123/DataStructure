# 旅游日记分享平台

这是一个基于Spring Boot和前端网页技术开发的旅游日记分享平台，允许用户注册、登录、发布和查看旅游日记。

## 项目特点

- 用户认证和授权（基于JWT令牌）
- 旅游日记创建、编辑、查看和删除
- 支持媒体文件上传（图片/视频）
- 日记评分系统
- 响应式UI设计

## 技术栈

### 后端
- Java / Spring Boot
- Spring Security
- Spring Data JPA
- MySQL数据库
- JWT认证

### 前端
- HTML5 / CSS3 / JavaScript
- 响应式设计

## 快速开始

### 环境要求
- JDK 11+
- Maven 3.6+
- MySQL 8.0+

### 启动步骤

1. 克隆仓库
   ```
   git clone https://github.com/你的用户名/travel-diary-demo.git
   cd travel-diary-demo
   ```

2. 配置数据库
   在`src/main/resources/application.properties`中配置你的数据库连接信息

3. 构建项目
   ```
   mvn clean install
   ```

4. 运行项目
   ```
   mvn spring-boot:run
   ```

5. 访问应用
   打开浏览器，访问`http://localhost:8081`

## API文档

详细的API文档可以在[API_DOCUMENT.md](API_DOCUMENT.md)中找到。

## 许可证

本项目采用MIT许可证。详情请参阅[LICENSE](LICENSE)文件。
