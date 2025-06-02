# 旅游日记网站API接口文档

## 基础信息
- 基础URL: `http://localhost:8081`
- 所有需要认证的接口都需要在请求头中添加JWT令牌：
  ```
  Authorization: Bearer <token>
  ```

## 技术说明
该后端项目完全使用传统Java代码实现，不依赖Lombok等代码生成库，所有的getter/setter方法都是显式定义的。项目使用MySQL数据库进行数据持久化。

### 排序算法
为了提高性能并减轻数据库负担，系统使用自定义快速排序算法（QuickSort）在内存中对日记数据进行排序，而不是依赖数据库的ORDER BY操作。这种方法在数据量较大时尤其有效，可以显著提高API响应速度。

实现位置：`src/main/java/com/traveldiary/utils/QuickSortUtils.java`

支持的排序方式：
- 按浏览量排序（views）
- 按评分排序（rating）
- 按标题排序（title）
- 按标题关键词搜索并排序

时间复杂度：O(n log n)
空间复杂度：O(log n)

## 认证相关接口

### 用户注册
- **URL**: `/api/auth/signup`
- **方法**: POST
- **描述**: 用户注册
- **请求体**:
  ```json
  {
    "username": "用户名",
    "email": "邮箱",
    "password": "密码"
  }
  ```
- **响应**:
  ```json
  {
    "message": "用户注册成功!"
  }
  ```

### 用户登录
- **URL**: `/api/auth/signin`
- **方法**: POST
- **描述**: 用户登录
- **请求体**:
  ```json
  {
    "username": "用户名",
    "password": "密码"
  }
  ```
- **响应**:
  ```json
  {
    "id": 1,
    "username": "用户名",
    "email": "邮箱",
    "tokenType": "Bearer",
    "accessToken": "JWT令牌"
  }
  ```

## 日记相关接口

### 获取所有日记
- **URL**: `/api/diaries/all`
- **方法**: GET
- **描述**: 获取所有日记
- **参数**:
  - `orderType`: 排序类型，可选值: "views"(按浏览量排序), "rating"(按评分排序)
- **实现说明**: 该接口使用快速排序算法而非数据库排序，可以显著提高大数据量下的排序性能
- **响应**:
  ```json
  [
    {
      "id": 1,
      "title": "日记标题",
      "content": "日记内容",
      "location": "景点名称",
      "createdAt": "2023-01-01T12:00:00",
      "updatedAt": "2023-01-01T12:00:00",
      "views": 10,
      "averageRating": 4.5,
      "ratingCount": 2,
      "user": {
        "id": 1,
        "username": "用户名",
        "email": "邮箱"
      },
      "mediaList": [
        {
          "id": 1,
          "fileName": "image.jpg",
          "fileType": "image/jpeg",
          "fileUrl": "/api/media/files/image.jpg",
          "fileSize": 1024,
          "mediaType": "IMAGE"
        },
        {
          "id": 2,
          "fileName": "video.mp4",
          "fileType": "video/mp4",
          "fileUrl": "/api/media/files/video.mp4",
          "fileSize": 10240,
          "mediaType": "VIDEO"
        }
      ],
      "ratings": [
        {
          "id": 1,
          "score": 4.5,
          "user": {
            "id": 2,
            "username": "评分用户名"
          }
        }
      ]
    }
  ]
  ```

### 获取日记详情
- **URL**: `/api/diaries/{id}`
- **方法**: GET
- **描述**: 获取指定ID的日记详情
- **响应**: 日记对象

### 获取用户的日记
- **URL**: `/api/diaries/user/{userId}`
- **方法**: GET
- **描述**: 获取指定用户的所有日记
- **响应**: 日记对象列表

### 按标题搜索日记
- **URL**: `/api/diaries/diary/search_title/{title}`
- **方法**: GET
- **描述**: 根据标题关键词搜索并排序日记
- **路径参数**:
  - `title`: 标题关键词
- **实现说明**: 使用快速排序算法进行内存中的过滤和排序
- **响应**: 匹配的日记对象列表

### 按景点搜索日记
- **URL**: `/api/diaries/location/{location}`
- **方法**: GET
- **描述**: 根据景点名称搜索日记
- **路径参数**:
  - `location`: 景点名称关键词
- **响应**: 匹配的日记对象列表

### 创建日记
- **URL**: `/api/diaries`
- **方法**: POST
- **描述**: 创建新日记
- **权限**: 需要认证
- **请求体**:
  ```json
  {
    "title": "日记标题",
    "content": "日记内容",
    "location": "景点名称"
  }
  ```
- **响应**: 创建的日记对象

### 更新日记
- **URL**: `/api/diaries/{id}`
- **方法**: PUT
- **描述**: 更新日记
- **权限**: 需要认证，只能更新自己的日记
- **请求体**:
  ```json
  {
    "title": "更新后的标题",
    "content": "更新后的内容",
    "location": "更新后的景点名称"
  }
  ```
- **响应**: 更新后的日记对象

### 删除日记
- **URL**: `/api/diaries/{id}`
- **方法**: DELETE
- **描述**: 删除日记
- **权限**: 需要认证，只能删除自己的日记
- **响应**:
  ```json
  {
    "message": "日记删除成功!"
  }
  ```

## 媒体相关接口

### 上传媒体文件
- **URL**: `/api/media/upload/{diaryId}`
- **方法**: POST
- **描述**: 为指定日记上传媒体文件(图片/视频)，仅支持JPEG图片和MP4视频格式
- **路径参数**:
  - `diaryId`: 日记ID，必须是当前用户拥有的日记
- **请求头**:
  - `Content-Type`: `multipart/form-data`
  - `Authorization`: `Bearer <token>`（必需，用于认证）
- **请求体（表单数据）**:
  - `file`: （必需）媒体文件，文件大小限制为10MB
- **错误情况**:
  - 401: 未授权 - 用户未登录或token无效
  - 400: 请求错误 - 日记不存在、不是日记的拥有者、文件格式不支持
  - 413: 文件过大 - 超过10MB限制
- **响应**: 
  ```json
  {
    "id": 1,
    "fileName": "d8e8fca2-dc0f-4a9e-8431-6092f8571b6c_image.jpg",
    "fileType": "image/jpeg",
    "fileUrl": "/api/media/files/d8e8fca2-dc0f-4a9e-8431-6092f8571b6c_image.jpg",
    "fileSize": 1024,
    "mediaType": "IMAGE"
  }
  ```

### 获取媒体文件
- **URL**: `/api/media/files/{fileName}`
- **方法**: GET
- **描述**: 获取媒体文件，仅支持JPEG图片和MP4视频格式
- **路径参数**:
  - `fileName`: 媒体文件名，包含扩展名
- **响应头**:
  - `Content-Type`: 根据文件类型动态设置（`image/jpeg`或`video/mp4`）
  - `Content-Disposition`: `inline; filename="文件名"`
- **错误情况**:
  - 404: 文件不存在
  - 400: 不支持的文件类型
- **响应**: 媒体文件的二进制数据（图片或视频）

### 获取日记的媒体文件
- **URL**: `/api/media/diary/{diaryId}`
- **方法**: GET
- **描述**: 获取指定日记的所有媒体文件，结果按类型排序（所有图片在前，所有视频在后）
- **路径参数**:
  - `diaryId`: 日记ID
- **错误情况**:
  - 400: 日记不存在
- **响应**: 
  ```json
  [
    {
      "id": 1,
      "fileName": "d8e8fca2-dc0f-4a9e-8431-6092f8571b6c_image.jpg",
      "fileType": "image/jpeg",
      "fileUrl": "/api/media/files/d8e8fca2-dc0f-4a9e-8431-6092f8571b6c_image.jpg",
      "fileSize": 1024,
      "mediaType": "IMAGE"
    },
    {
      "id": 2,
      "fileName": "a1e0f78b-dc0f-4a9e-8431-6092f8571b6c_video.mp4",
      "fileType": "video/mp4",
      "fileUrl": "/api/media/files/a1e0f78b-dc0f-4a9e-8431-6092f8571b6c_video.mp4",
      "fileSize": 10240,
      "mediaType": "VIDEO"
    }
  ]
  ```

### 删除媒体文件
- **URL**: `/api/media/{mediaId}`
- **方法**: DELETE
- **描述**: 删除媒体文件
- **路径参数**:
  - `mediaId`: 媒体文件ID
- **请求头**:
  - `Authorization`: `Bearer <token>`（必需，用于认证）
- **权限**: 需要认证，只能删除自己的日记的媒体文件
- **错误情况**:
  - 401: 未授权 - 用户未登录或token无效
  - 400: 媒体文件不存在或不是日记的拥有者
- **响应**:
  ```json
  {
    "message": "媒体文件删除成功!"
  }
  ```

## 评分相关接口

### 为日记评分
- **URL**: `/api/ratings/{diaryId}`
- **方法**: POST
- **描述**: 为指定日记评分
- **权限**: 需要认证，不能为自己的日记评分
- **请求体**:
  ```json
  {
    "score": 4.5
  }
  ```
- **响应**: 评分对象

### 删除评分
- **URL**: `/api/ratings/{ratingId}`
- **方法**: DELETE
- **描述**: 删除评分
- **权限**: 需要认证，只能删除自己的评分
- **响应**:
  ```json
  {
    "message": "评分删除成功!"
  }
  ```

## 路径规划相关接口

### 景区之间的路径规划
- **URL**: `/api/routes/between-scenic`
- **方法**: POST
- **描述**: 规划两个景区之间的最短路径
- **请求体**:
  ```json
  {
    "startLocation": "起点景区名称",
    "endLocation": "终点景区名称"
  }
  ```
- **实现说明**: 
  - 该接口使用Dijkstra最短路径算法，基于高德地图API提供的路网数据
  - 返回路径上的经纬度点，前端直接使用这些点绘制路径
- **错误情况**:
  - 400: 无法解析起点位置 - 高德地图API无法找到对应地点
  - 400: 无法解析终点位置 - 高德地图API无法找到对应地点
  - 400: 路网构建失败 - 无法在路网中找到路径节点
  - 400: 无法找到有效路径 - 两点之间无法规划路径
- **响应**:
  ```json
  {
    "pathNames": ["道路名称1", "道路名称2", "道路名称3"],
    "distance": 5000,
    "duration": 600,
    "pathPoints": [
      {"longitude": 116.397428, "latitude": 39.90923},
      {"longitude": 116.398438, "latitude": 39.91923},
      {"longitude": 116.399448, "latitude": 39.92923},
      {"longitude": 116.400458, "latitude": 39.93923}
    ]
  }
  ```

### 多点路径规划
- **URL**: `/api/routes/within-scenic`
- **方法**: POST
- **描述**: 规划从起点出发，经过多个途径点后返回起点的路径
- **请求体**:
  ```json
  {
    "startLocation": "起点位置名称",
    "waypoints": ["途径点1", "途径点2", "途径点3"]
  }
  ```
- **实现说明**: 
  - 该接口首先使用最近邻算法（贪心策略）确定访问点的顺序
  - 然后基于高德地图API提供的实际路网数据，分段规划每两个点之间的路径
  - 每段路径使用Dijkstra最短路径算法进行规划
  - 返回完整的路径上的经纬度点，前端直接使用这些点绘制路径
  - 路径会从起点出发，经过所有指定的途径点，最后返回起点
- **错误情况**:
  - 400: 起点位置或途径点不能为空 - 请求参数不完整
  - 400: 无法解析起点位置 - 高德地图API无法找到对应地点
  - 400: 无法解析途径点位置 - 高德地图API无法找到对应地点
  - 400: 无法规划多点路径顺序 - 无法确定访问顺序
- **响应**:
  ```json
  {
    "pathNames": ["道路名称1", "道路名称2", "道路名称3", "道路名称4"],
    "distance": 5000,
    "duration": 600,
    "pathPoints": [
      {"longitude": 116.397428, "latitude": 39.90923},
      {"longitude": 116.398438, "latitude": 39.91923},
      {"longitude": 116.399448, "latitude": 39.92923},
      {"longitude": 116.400458, "latitude": 39.93923},
      {"longitude": 116.397428, "latitude": 39.90923}
    ]
  }
  ```

## 错误响应
所有错误响应的格式如下:
```json
{
  "message": "错误信息"
}
``` 
 