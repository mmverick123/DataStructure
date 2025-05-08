# 旅行日记系统

这是一个基于Spring Boot的旅行日记分享平台，允许用户创建、分享和浏览旅行日记。

## 技术栈

### 后端
- Spring Boot 3.2.3
- Spring Security
- Spring Data JPA
- MySQL 8.0
- JWT认证

### 前端
- HTML5
- CSS3
- JavaScript（原生）

## 系统要求

- JDK 17或更高版本
- Maven 3.6或更高版本
- MySQL 8.0或更高版本
- Node.js 14或更高版本（前端开发）

## 快速开始

### 1. 数据库配置

1. 安装MySQL 8.0
2. 启动MySQL服务
   - Windows: 在服务管理器中启动MySQL服务
   - Linux: `sudo service mysql start`
   - Mac: `brew services start mysql`
3. 数据库会自动创建，无需手动创建数据库
4. 如需修改数据库配置，打开 `src/main/resources/application.properties` 文件，修改以下配置：
```properties
spring.datasource.username=你的MySQL用户名
spring.datasource.password=你的MySQL密码
```

### 2. 启动项目

1. 克隆项目到本地
2. 进入项目根目录
3. 使用Maven启动项目（推荐开发时使用）：
```bash
mvn clean spring-boot:run
```

或者，如果您想打包后运行（推荐生产环境使用）：
```bash
# 1. 打包项目
mvn clean package

# 2. 运行项目
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### 3. 访问系统

1. 打开浏览器访问：http://localhost:8081
2. 或者直接打开前端文件：src/main/resources/static/traveldiary/list.html

## 项目结构

```
src/main/java/com/traveldiary/
├── config/          # 配置类
├── controller/      # 控制器
├── model/          # 数据模型
├── repository/     # 数据访问层
├── security/       # 安全相关
└── service/        # 业务逻辑层

src/main/resources/
├── static/         # 静态资源
│   └── traveldiary/  # 前端文件
└── application.properties  # 配置文件
```

## 主要功能

1. 用户管理
   - 用户注册
   - 用户登录
   - 用户信息管理

2. 日记管理
   - 创建日记
   - 编辑日记
   - 删除日记
   - 浏览日记列表
   - 查看日记详情

3. 媒体管理
   - 上传图片和视频
   - 查看媒体文件
   - 删除媒体文件

4. 评分系统
   - 为日记评分
   - 查看评分统计

## API文档

详细的API文档请参考 [API_DOCUMENT.md](API_DOCUMENT.md)

## 注意事项

1. 文件上传
   - 支持的文件类型：JPEG图片和MP4视频
   - 单个文件大小限制：10MB
   - 上传目录：项目根目录下的uploads文件夹

2. 数据库
   - 使用MySQL 8.0
   - 数据库名：traveldiary
   - 字符集：utf8mb4
   - 排序规则：utf8mb4_unicode_ci

3. 安全
   - 使用JWT进行身份认证
   - 密码加密存储
   - 文件上传类型验证

## 常见问题

1. 数据库连接失败
   - 检查MySQL服务是否启动
   - 验证数据库用户名和密码是否正确
   - 确认数据库端口是否正确（默认3306）

2. 文件上传失败
   - 检查uploads目录是否存在且有写入权限
   - 确认文件大小是否超过限制
   - 验证文件类型是否支持

## 贡献指南

1. Fork 项目
2. 创建特性分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 许可证

MIT License
