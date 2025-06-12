# 旅行日记系统

这是一个基于Spring Boot的旅行平台，包括旅游推荐，旅游导航与旅游日记三个模块。

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
3. 配置通义万相API密钥
   - 打开 `src/main/resources/application.properties` 文件
   - 修改以下配置：
   ```properties
   dashscope.api.key=你的通义万相API密钥
   ```
4. 使用Maven启动项目（推荐开发时使用）：
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
├── service/        # 业务逻辑层
├── utils/          # 工具类，包含排序算法
└── payload/        # 请求响应模型

src/main/resources/
├── static/         # 静态资源
│   ├── traveldiary/  # 旅行日记前端文件
│   │   ├── diary.html      # 日记详情页面
│   │   ├── diary.js        # 日记详情逻辑
│   │   ├── diarystyle.css  # 日记详情样式
│   │   ├── list.html       # 日记列表页面
│   │   ├── getlist.js      # 日记列表逻辑
│   │   ├── liststyle.css   # 日记列表样式
│   │   ├── upload.html     # 日记上传页面
│   │   ├── upload.js       # 日记上传逻辑
│   │   └── uploadstyle.css # 日记上传样式
│   ├── authen/     # 认证相关前端文件
│   ├── home/       # 首页相关前端文件
│   ├── index.html  # 主页面
│   └── homestyles.css  # 主页面样式
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
   - 日记评分系统（0.5-5.0分）
   - 通过快速排序算法对日记按热度/浏览量进行排序
   - 景点标签功能（为日记添加景点标签）
   - 按景点搜索日记
   - 根据日记内容生成图片（使用通义万相-文生图V2版API）
   - **日记内容无损压缩**（使用GZIP算法，自动压缩存储，透明解压缩）

3. 媒体管理
   - 上传图片和视频
   - 查看媒体文件
   - 删除媒体文件
   - AI生成日记插图（通过通义万相API）

4. 评分系统
   - 为日记评分（0.5-5.0分，步长0.5）
   - 查看评分统计
   - 实时更新评分显示

5. 景点推荐系统
   - 基于现有日记数据动态生成景点列表
   - 按热度推荐景点（基于相关日记的总阅读量）
   - 按评价推荐景点（基于相关日记的平均评分）
   - 综合评分推荐（热度和评价的加权平均）
   - 智能搜索功能（按景点名称、类别、关键词搜索）
   - 使用Top-K堆排序算法优化前10个结果的获取效率
   - 支持快速排序算法进行完整排序
   - 自动景点类别推断和关键词提取
   - 景点相关图片列表（自动从相关日记收集景点图片）

6. 路径规划功能
   - 景点之间的路径规划
   - 多点路径规划
   - 基于高德地图API的位置查询和路径计算
   - 使用Dijkstra算法实现最短路径规划
   - 支持复杂地点名称查询（如"北京邮电大学(海淀校区)"）

7. 数据压缩功能
   - **GZIP无损压缩算法**：自动压缩日记标题和内容
   - **智能压缩策略**：短文本不压缩，长文本自动压缩
   - **透明解压缩**：读取时自动解压缩，对用户完全透明
   - **压缩统计**：提供详细的压缩率和存储空间节省统计
   - **批量压缩管理**：支持批量压缩/解压缩所有日记
   - **单个日记管理**：支持对指定日记进行压缩操作

## 性能优化

1. 快速排序算法
   - 系统使用自定义快速排序算法对日记进行排序
   - 避免使用数据库排序，减轻数据库负担
   - 支持按浏览量、评分和标题进行排序
   - 时间复杂度为O(n log n)，空间复杂度为O(log n)

2. 景点推荐算法优化
   - **Top-K堆排序算法**: 时间复杂度O(n log k)，专门优化前K个结果的获取
   - **快速排序算法**: 时间复杂度O(n log n)，用于完整排序
   - **动态景点生成**: 基于现有日记数据实时生成景点，无需预置数据
   - **智能关联算法**: 通过名称、位置、关键词匹配日记与景点的关联关系

3. 数据压缩优化
   - **GZIP压缩算法**: 使用标准GZIP算法实现无损压缩
   - **智能压缩阈值**: 小于100字节的文本不压缩，避免负优化
   - **压缩效果检查**: 压缩后大小超过原大小90%时不压缩
   - **Base64编码**: 压缩后使用Base64编码确保数据安全存储
   - **批量处理**: 支持批量压缩操作，提高处理效率
   - **存储空间节省**: 典型文本压缩率可达30-70%

## 景点推荐系统使用示例

### 1. 获取热门景点推荐
```bash
curl -X GET "http://localhost:8081/api/attractions/recommendations/popular?limit=5"
```

### 2. 搜索特定类别的景点
```bash
curl -X GET "http://localhost:8081/api/attractions/search?category=历史文化&sortBy=views&limit=5"
```

### 3. 高级搜索（使用POST方式）
```bash
curl -X POST "http://localhost:8081/api/attractions/search" \
  -H "Content-Type: application/json" \
  -d '{
    "searchTerm": "北京",
    "sortBy": "composite",
    "limit": 5,
    "useTopK": true,
    "viewsWeight": 0.7,
    "ratingWeight": 0.3
  }'
```

### 4. 更新景点统计数据
```bash
curl -X POST "http://localhost:8081/api/attractions/update-statistics"
```

### 5. 使用通义万相API为日记生成图片
```bash
# 异步调用通义万相V2版API生成图片，返回包含图片URL的JSON响应
# 注意：图片生成过程一般需要1-3分钟，请耐心等待
curl -X GET "http://localhost:8081/api/image-generation/diary/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 6. 直接获取生成的图片
```bash
curl -X GET "http://localhost:8081/api/image-generation/diary/1/image" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -o generated_image.jpg
```

### 7. 使用通义万相API根据提示词生成图片
```bash
# 异步调用通义万相V2版API生成图片，返回包含图片URL的JSON响应
# 注意：图片生成过程一般需要1-3分钟，请耐心等待
curl -X POST "http://localhost:8081/api/image-generation" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "prompt": "风景如画的西湖，夕阳西下，金光洒满湖面"
  }'
```

### 8. 使用通义万相API根据提示词生成图片并直接获取图片
```bash
# 异步调用通义万相V2版API生成图片，返回重定向到图片URL
# 注意：图片生成过程一般需要1-3分钟，请耐心等待
curl -X POST "http://localhost:8081/api/image-generation/image" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "prompt": "风景如画的西湖，夕阳西下，金光洒满湖面"
  }' \
  -L -o generated_image.jpg  # -L参数跟随重定向，将图片保存到文件
```

## 数据压缩功能使用示例

### 1. 获取压缩统计信息
```bash
curl -X GET "http://localhost:8081/api/compression/statistics"
```

### 2. 批量压缩所有日记
```bash
curl -X POST "http://localhost:8081/api/compression/compress-all"
```

### 3. 批量解压缩所有日记
```bash
curl -X POST "http://localhost:8081/api/compression/decompress-all"
```

### 4. 检查指定日记的压缩状态
```bash
curl -X GET "http://localhost:8081/api/compression/diary/1/status"
```

### 5. 压缩指定日记
```bash
curl -X POST "http://localhost:8081/api/compression/diary/1/compress"
```

### 6. 解压缩指定日记
```bash
curl -X POST "http://localhost:8081/api/compression/diary/1/decompress"
```

### 压缩功能特点

- **自动压缩**: 创建和更新日记时自动压缩内容
- **透明解压**: 读取日记时自动解压缩，用户无感知
- **智能策略**: 短文本（<100字节）不压缩，避免负优化
- **效果检查**: 压缩效果不明显时保持原文本
- **统计监控**: 提供详细的压缩率和空间节省统计
- **批量管理**: 支持批量压缩/解压缩操作

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

## 最近更新

### 2025-06-13
- 添加附近设施查询接口，支持根据经纬度查询附近的超市、卫生间等设施
- 实现了基于距离的快速排序算法，优化了查询结果的排序效率
- 集成高德地图API，提供更全面的位置服务

### 2025-06-12
- 添加用户密码修改功能，用户可以在登录后安全地修改自己的密码
- 为按标题搜索、按内容搜索、按景点搜索三个接口添加orderType参数，支持按浏览量或评分排序
- 使用快速排序算法优化搜索结果的排序

### 2025-06-11
- 修复了前端推荐系统API调用路径
- 更新了API文档，明确了返回格式
- 修复了景点搜索接口的参数名称匹配问题
- 修复了推荐系统中图片URL显示问题（将attraction.image改为使用attraction.imageUrls[0]）
- 更正了景点评分和热度的字段名称（将popularity和rating改为totalViews和averageRating）

## 技术特点

- 基于Spring Boot框架开发
- Spring Security实现JWT认证鉴权
- Spring Data JPA实现数据访问层
- PostgreSQL作为关系型数据库
- 集成阿里云通义万相文生图V2版API，支持根据日记内容生成相关图片
- Docker容器化部署

## API接口

API接口文档详见[API_DOCUMENT.md](API_DOCUMENT.md)。

主要功能接口：
- 用户注册、登录、信息获取
- 日记创建、更新、删除、查询
- 日记类别管理
- 基于通义万相文生图V2版API的图片生成

## 第三方API集成

### 通义万相文生图API

本项目集成了阿里云通义万相文生图V2版API，使用`wanx2.1-t2i-turbo`模型。该API通过异步方式工作：

1. 首先创建图片生成任务获取任务ID
2. 然后轮询任务状态，直到任务完成或失败
3. 最终获取生成的图片URL
4. 自动从URL下载图片并保存到本地服务器

**主要特点**：
- 异步任务处理，避免HTTP请求超时
- 任务轮询机制，最多等待5分钟（60次，每次间隔5秒）
- 自动将24小时有效期的图片URL保存到本地服务器，确保永久可用
- 完整日志记录，方便调试

**配置方法**：
在`application.properties`或环境变量中设置：
```
# 通义万相API配置
dashscope.api.key=your_api_key_here
dashscope.api.url=https://dashscope.aliyuncs.com/api/v1/services/aigc/text2image/image-synthesis
```

**使用示例**：
```java
// 注入服务
@Autowired
private ImageGenerationService imageGenerationService;

// 调用生成图片
Map<String, Object> result = imageGenerationService.generateImage("一个美丽的湖边风景，有山和树");
```
