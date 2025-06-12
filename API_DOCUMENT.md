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

### 修改密码
- **URL**: `/api/user/change-password`
- **方法**: POST
- **描述**: 修改当前登录用户的密码
- **权限**: 需要认证
- **请求头**:
  - `Authorization`: `Bearer <token>`（必需，用于认证）
- **请求体**:
  ```json
  {
    "currentPassword": "当前密码",
    "newPassword": "新密码",
    "confirmPassword": "确认新密码"
  }
  ```
- **错误情况**:
  - 400: 当前密码不正确
  - 400: 新密码和确认密码不一致
  - 401: 未授权 - 用户未登录或token无效
- **响应**:
  ```json
  {
    "message": "密码修改成功!"
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
- **查询参数**:
  - `orderType`: 排序类型，可选值: "views"(按浏览量排序), "rating"(按评分排序)
- **实现说明**: 使用自定义搜索算法和归并排序进行内存中的过滤和排序
- **响应**: 匹配的日记对象列表

### 按内容搜索日记
- **URL**: `/api/diaries/search/content/{keyword}`
- **方法**: GET
- **描述**: 根据内容关键词进行全文检索
- **路径参数**:
  - `keyword`: 内容关键词
- **查询参数**:
  - `orderType`: 排序类型，可选值: "views"(按浏览量排序), "rating"(按评分排序)
- **实现说明**: 使用自定义搜索算法和归并排序，按创建时间降序排序结果
- **响应**: 匹配的日记对象列表

### 高级搜索日记
- **URL**: `/api/diaries/search/advanced`
- **方法**: GET
- **描述**: 同时搜索日记标题和内容
- **查询参数**:
  - `keyword`: 搜索关键词
  - `orderType`: 排序类型，可选值: "views"(按浏览量排序), "rating"(按评分排序)
- **实现说明**: 
  - 同时搜索标题和内容，合并结果并去重
  - 标题匹配的结果优先级高于内容匹配
  - 使用自定义的归并排序算法
  - 默认按创建时间降序排序
- **响应**: 匹配的日记对象列表

### 按景点搜索日记
- **URL**: `/api/diaries/location/{location}`
- **方法**: GET
- **描述**: 根据景点名称搜索日记
- **路径参数**:
  - `location`: 景点名称关键词
- **查询参数**:
  - `orderType`: 排序类型，可选值: "views"(按浏览量排序), "rating"(按评分排序)
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

## 景点推荐相关接口

### 获取所有景点
- **URL**: `/api/attractions`
- **方法**: GET
- **描述**: 获取所有景点（基于现有日记动态生成）
- **实现说明**: 系统会自动分析现有日记的location字段，动态生成景点数据并计算统计信息
- **响应**:
  ```json
  [
    {
      "id": 1,
      "name": "故宫博物院",
      "description": "基于旅游日记生成的景点",
      "category": "历史文化",
      "location": "故宫博物院",
      "keywords": "故宫博物院,历史,文化,古建筑",
      "totalViews": 15000,
      "averageRating": 4.8,
      "diaryCount": 120,
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00",
      "imageUrls": [
        "/api/media/files/d8e8fca2-dc0f-4a9e-8431-6092f8571b6c_image1.jpg",
        "/api/media/files/a1e0f78b-dc0f-4a9e-8431-6092f8571b6c_image2.jpg",
        "/api/media/files/c4f2e6d5-dc0f-4a9e-8431-6092f8571b6c_image3.jpg"
      ]
    }
  ]
  ```

- **字段说明**:
  - `totalViews`: 相关日记的总阅读量，用于热度排序
  - `averageRating`: 相关日记的平均评分，用于评价排序 
  - `imageUrls`: 景点相关图片URL列表，前端展示时通常使用第一张图片

### 根据ID获取景点
- **URL**: `/api/attractions/{id}`
- **方法**: GET
- **描述**: 获取指定ID的景点详情
- **响应**: 景点对象

### 创建新景点
- **URL**: `/api/attractions`
- **方法**: POST
- **描述**: 手动创建新景点
- **请求体**:
  ```json
  {
    "name": "景点名称",
    "description": "景点描述",
    "category": "景点类别",
    "location": "景点位置",
    "keywords": "关键词1,关键词2,关键词3"
  }
  ```
- **响应**: 创建的景点对象

### 更新景点信息
- **URL**: `/api/attractions/{id}`
- **方法**: PUT
- **描述**: 更新景点信息
- **请求体**:
  ```json
  {
    "name": "更新后的景点名称",
    "description": "更新后的描述",
    "category": "更新后的类别",
    "location": "更新后的位置",
    "keywords": "更新后的关键词"
  }
  ```- **响应**: 更新后的景点对象

### 删除景点
- **URL**: `/api/attractions/{id}`
- **方法**: DELETE
- **描述**: 删除景点
- **响应**:
  ```json
  {
    "message": "景点删除成功!"
  }
  ```

### 搜索景点（高级搜索）
- **URL**: `/api/attractions/search`
- **方法**: POST
- **描述**: 高级景点搜索，支持多种排序算法
- **请求体**:
  ```json
  {
    "searchTerm": "搜索关键词",
    "category": "景点类别",
    "sortBy": "views|rating|composite",
    "limit": 10,
    "useTopK": true,
    "viewsWeight": 0.6,
    "ratingWeight": 0.4
  }
  ```
- **参数说明**:
  - `searchTerm`: 搜索关键词（可选）
  - `category`: 景点类别（可选）
  - `sortBy`: 排序方式
    - `views`: 按热度排序
    - `rating`: 按评价排序
    - `composite`: 按综合评分排序
  - `limit`: 返回结果数量限制（1-50，默认10）
  - `useTopK`: 是否使用Top-K算法（默认true）
  - `viewsWeight`: 热度权重（0-1，默认0.6）
  - `ratingWeight`: 评价权重（0-1，默认0.4）
- **算法说明**:
  - **Top-K算法**: 时间复杂度O(n log k)，只获取前K个结果，适合用户通常只看前10个景点的场景
  - **快速排序**: 时间复杂度O(n log n)，完整排序
- **响应**: 排序后的景点对象列表

### 搜索景点（简单搜索）
- **URL**: `/api/attractions/search`
- **方法**: GET
- **描述**: 简单景点搜索
- **参数**:
  - `searchTerm`: 搜索关键词（可选）
  - `category`: 景点类别（可选）
  - `sortBy`: 排序方式，默认"views"
  - `limit`: 返回结果数量，默认10
  - `useTopK`: 是否使用Top-K算法，默认true
- **响应**: 景点对象列表数组（直接返回数组，不包含在data字段中）
- **前端用法示例**:
  ```javascript
  // 前端代码使用示例
  const API_BASE_URL = 'http://localhost:8081/api';
  const keyword = "北京大学";
  const sortBy = "views"; // 可选值: views, rating, composite
  const category = "education"; // 可选值: history, nature, garden, education, commercial
  
  const url = `${API_BASE_URL}/attractions/search?searchTerm=${encodeURIComponent(keyword)}&sortBy=${sortBy}&category=${category}&limit=10`;
  
  fetch(url)
    .then(response => response.json())
    .then(data => {
      if (data && data.length > 0) {
        // 使用第一个结果
        const attraction = data[0];
        // 在地图上标记位置
        markLocation([attraction.location.lng, attraction.location.lat], attraction.name);
      }
    });
  ```

### 获取热门推荐景点
- **URL**: `/api/attractions/recommendations/popular`
- **方法**: GET
- **描述**: 获取按热度推荐的景点（基于相关日记的总阅读量）
- **参数**:
  - `limit`: 返回结果数量，默认10
- **算法说明**: 使用Top-K堆排序算法，时间复杂度O(n log k)
- **响应**: 景点对象列表数组（直接返回数组，不包含在data字段中）
- **前端用法示例**:
  ```javascript
  // 前端代码使用示例
  const API_BASE_URL = 'http://localhost:8081/api';
  const url = `${API_BASE_URL}/attractions/recommendations/popular?limit=12`;
  
  fetch(url)
    .then(response => response.json())
    .then(data => {
      if (data) {
        // 直接使用返回的数组渲染
        renderAttractions(data, recommendContainer);
      }
    });
  ```### 获取高评分推荐景点
- **URL**: `/api/attractions/recommendations/top-rated`
- **方法**: GET
- **描述**: 获取按评价推荐的景点（基于相关日记的平均评分）
- **参数**:
  - `limit`: 返回结果数量，默认10
- **算法说明**: 使用Top-K堆排序算法，时间复杂度O(n log k)
- **响应**: 景点对象列表数组（直接返回数组，不包含在data字段中）
- **前端用法示例**:
  ```javascript
  // 前端代码使用示例
  const API_BASE_URL = 'http://localhost:8081/api';
  const url = `${API_BASE_URL}/attractions/recommendations/top-rated?limit=12`;
  
  fetch(url)
    .then(response => response.json())
    .then(data => {
      if (data) {
        // 直接使用返回的数组渲染
        renderAttractions(data, recommendContainer);
      }
    });
  ```

### 获取综合推荐景点
- **URL**: `/api/attractions/recommendations/composite`
- **方法**: GET
- **描述**: 获取按综合评分推荐的景点（热度和评价的加权平均）
- **参数**:
  - `limit`: 返回结果数量，默认10
  - `viewsWeight`: 热度权重，默认0.6
  - `ratingWeight`: 评价权重，默认0.4
- **算法说明**: 使用Top-K堆排序算法，支持自定义权重的综合评分
- **响应**: 景点对象列表数组（直接返回数组，不包含在data字段中）
- **前端用法示例**:
  ```javascript
  // 前端代码使用示例
  const API_BASE_URL = 'http://localhost:8081/api';
  const url = `${API_BASE_URL}/attractions/recommendations/composite?limit=12`;
  
  fetch(url)
    .then(response => response.json())
    .then(data => {
      if (data) {
        // 直接使用返回的数组渲染
        renderAttractions(data, recommendContainer);
      }
    });
  ```

### 更新所有景点统计数据
- **URL**: `/api/attractions/update-statistics`
- **方法**: POST
- **描述**: 手动触发更新所有景点的统计数据
- **实现说明**: 重新分析所有日记，更新景点的热度、评价等统计信息
- **响应**:
  ```json
  {
    "message": "景点统计数据更新完成"
  }
  ```

### 更新单个景点统计数据
- **URL**: `/api/attractions/{id}/update-statistics`
- **方法**: POST
- **描述**: 更新指定景点的统计数据
- **响应**:
  ```json
  {
    "message": "景点统计数据更新完成"
  }
  ```

## 景点推荐系统说明

### 数据来源
景点推荐系统完全基于现有的旅游日记数据：
- **景点生成**: 自动分析日记的location字段，动态生成景点列表
- **热度计算**: 相关日记的总阅读量
- **评价计算**: 相关日记的平均评分
- **关联判断**: 通过景点名称、位置、关键词与日记内容匹配

### 排序算法
系统实现了两种高效的排序算法：

#### Top-K 堆排序算法
- **时间复杂度**: O(n log k)
- **空间复杂度**: O(k)
- **适用场景**: 只需要前K个结果，不需要完整排序
- **实现方式**: 使用最小堆维护前K个最大值
- **优势**: 对于用户通常只看前10个景点的场景，性能优于完整排序

#### 快速排序算法
- **时间复杂度**: 平均O(n log n)，最坏O(n²)
- **适用场景**: 需要完整排序的情况
- **实现方式**: 递归分治，支持按热度和评价排序

### 景点类别自动推断
系统会根据景点名称自动推断类别：
- **历史文化**: 博物馆、故宫、寺、庙、古城、古镇、遗址
- **自然风光**: 山、湖、海、河、森林、公园、峡谷
- **园林景观**: 园、花园
- **教育机构**: 大学、学校、学院
- **商业区域**: 商场、购物、街
- **其他景点**: 未匹配的其他类型

### 关键词自动提取
系统会从相关日记的标题和内容中自动提取关键词，用于搜索和匹配。

## 设施查询接口

### 查询附近设施
- **URL**: `/api/facilities/nearby/search`
- **方法**: POST
- **描述**: 根据中心点经纬度，查询附近的设施（超市、卫生间等）并按距离排序
- **请求体**:
  ```json
  {
    "longitude": 116.397428,
    "latitude": 39.90923,
    "facilityType": "超市|便利店|卫生间|洗手间|厕所",
    "limit": 20,
    "radius": 2000
  }
  ```
- **参数说明**:
  - `longitude`: 经度（必需）
  - `latitude`: 纬度（必需）
  - `facilityType`: 设施类型关键词，多个类型用|分隔（可选，默认为"超市|便利店|卫生间|洗手间|厕所"）
  - `limit`: 返回结果数量限制（可选，默认20，最大50）
  - `radius`: 搜索半径，单位米（可选，默认2000米，最大5000米）
- **响应**:
  ```json
  {
    "centerLongitude": 116.397428,
    "centerLatitude": 39.90923,
    "facilityType": "超市|便利店|卫生间|洗手间|厕所",
    "totalCount": 10,
    "radius": 2000,
    "facilities": [
      {
        "id": "B000A7BD6C",
        "name": "全家便利店(北京三里屯店)",
        "type": "购物服务;便利店;便利店",
        "address": "北京市朝阳区三里屯路19号1层",
        "longitude": 116.394821,
        "latitude": 39.910434,
        "distance": 420.5,
        "category": "购物服务",
        "telephone": "010-64165380"
      },
      // 更多设施...
    ]
  }
  ```
- **实现说明**: 
  - 接口使用高德地图API获取附近设施数据
  - 使用快速排序算法按距离对结果进行排序
  - 时间复杂度：O(n log n)
  - 空间复杂度：O(n)

## 图像生成相关接口

### 基于日记内容生成图片

根据提供的日记ID和提示词，调用通义万相文生图API生成图片。

- **URL:** `/api/image/generate`
- **方法:** `POST`
- **请求体:**
  ```json
  {
    "diaryId": 1,
    "prompt": "一个美丽的湖边风景，有山和树"
  }
  ```
  - `diaryId`: 日记ID（必填）
  - `prompt`: 图片生成提示词（必填）

- **成功响应:**
  ```json
  {
    "success": true,
    "imageUrl": "https://dashscope-result-wlcb.oss-cn-wulanchabu.aliyuncs.com/example.png",
    "originalPrompt": "一个美丽的湖边风景，有山和树",
    "actualPrompt": "一个宁静美丽的湖边风景，湖水清澈反射着蓝天，周围环绕着青翠的山脉和茂密的树木。光线柔和，场景宁静祥和。"
  }
  ```
  - `imageUrl`: 生成的图片URL地址，有效期24小时
  - `originalPrompt`: 用户原始提示词
  - `actualPrompt`: 系统优化后的提示词

- **错误响应:**
  ```json
  {
    "success": false,
    "error": "错误信息"
  }
  ```

### 技术说明

本API使用通义万相文生图V2版API（`wanx2.1-t2i-turbo`模型）以异步方式生成图片。

- **API流程**:
  1. 接收用户请求的提示词
  2. 调用通义万相API创建异步任务
  3. 轮询任务状态（最多60次，间隔5秒）
  4. 任务完成后返回图片URL
  5. 前端将图片URL发送到后端
  6. 后端从URL下载图片并保存到本地服务器
  7. 返回本地图片的访问路径

- **图片处理说明**:
  - 通义万相API返回的图片URL有效期为24小时
  - 系统会自动下载该URL的图片并保存到本地服务器
  - 用户最终看到的是保存在本地服务器的图片，不受24小时有效期限制

- **注意事项**:
  - 图片生成通常需要1-3分钟
  - 若提示"异步任务超时"，请稍后重试

## 数据压缩管理接口

### 获取压缩统计信息
- **URL**: `/api/compression/statistics`
- **方法**: GET
- **描述**: 获取系统中所有日记的压缩统计信息
- **响应**:
  ```json
  {
    "totalCount": 100,
    "compressedCount": 75,
    "partiallyCompressedCount": 10,
    "uncompressedCount": 15,
    "compressionPercentage": 75.0,
    "originalSize": 1048576,
    "currentSize": 524288,
    "compressionRatio": 50.0,
    "spaceSaved": 524288
  }
  ```
- **字段说明**:
  - `totalCount`: 日记总数
  - `compressedCount`: 完全压缩的日记数量
  - `partiallyCompressedCount`: 部分压缩的日记数量
  - `uncompressedCount`: 未压缩的日记数量
  - `compressionPercentage`: 压缩百分比
  - `originalSize`: 原始总大小（字节）
  - `currentSize`: 当前总大小（字节）
  - `compressionRatio`: 压缩率（百分比）
  - `spaceSaved`: 节省的存储空间（字节）

### 批量压缩所有日记
- **URL**: `/api/compression/compress-all`
- **方法**: POST
- **描述**: 批量压缩系统中所有未压缩的日记
- **响应**:
  ```json
  {
    "success": true,
    "message": "批量压缩完成",
    "totalCount": 100,
    "compressedCount": 25,
    "alreadyCompressedCount": 75,
    "originalSize": 262144,
    "compressedSize": 131072,
    "compressionRatio": "50.00%",
    "spaceSaved": 131072
  }
  ```

### 批量解压缩所有日记
- **URL**: `/api/compression/decompress-all`
- **方法**: POST
- **描述**: 批量解压缩系统中所有已压缩的日记
- **响应**:
  ```json
  {
    "success": true,
    "message": "批量解压缩完成",
    "totalCount": 100,
    "decompressedCount": 75,
    "notCompressedCount": 25
  }
  ```

### 检查日记压缩状态
- **URL**: `/api/compression/diary/{id}/status`
- **方法**: GET
- **描述**: 检查指定日记的压缩状态和详细信息
- **路径参数**:
  - `id`: 日记ID
- **响应**:
  ```json
  {
    "success": true,
    "diaryId": 1,
    "titleCompressed": true,
    "contentCompressed": true,
    "fullyCompressed": true,
    "originalTitleSize": 50,
    "originalContentSize": 1000,
    "currentTitleSize": 35,
    "currentContentSize": 600,
    "totalOriginalSize": 1050,
    "totalCurrentSize": 635,
    "titleCompressionRatio": 30.0,
    "contentCompressionRatio": 40.0,
    "overallCompressionRatio": 39.52,
    "spaceSaved": 415
  }
  ```

### 压缩指定日记
- **URL**: `/api/compression/diary/{id}/compress`
- **方法**: POST
- **描述**: 强制压缩指定的日记（即使已经压缩过）
- **路径参数**:
  - `id`: 日记ID
- **响应**:
  ```json
  {
    "success": true,
    "message": "日记压缩成功",
    "diaryId": 1,
    "originalSize": 1050,
    "compressedSize": 635,
    "compressionRatio": 39.52,
    "spaceSaved": 415
  }
  ```

### 解压缩指定日记
- **URL**: `/api/compression/diary/{id}/decompress`
- **方法**: POST
- **描述**: 解压缩指定的日记
- **路径参数**:
  - `id`: 日记ID
- **响应**:
  ```json
  {
    "success": true,
    "message": "日记解压缩成功",
    "diaryId": 1,
    "titleWasCompressed": true,
    "contentWasCompressed": true,
    "compressedSize": 635,
    "decompressedSize": 1050,
    "sizeIncrease": 415
  }
  ```

## 压缩功能技术说明

### GZIP压缩算法
系统使用标准的GZIP压缩算法对日记的标题和内容进行无损压缩：

- **压缩流程**:
  1. 将文本转换为UTF-8字节数组
  2. 使用GZIPOutputStream进行压缩
  3. 将压缩后的字节数组进行Base64编码
  4. 添加"GZIP:"前缀标识

- **解压缩流程**:
  1. 检查文本是否包含"GZIP:"前缀
  2. 移除前缀并进行Base64解码
  3. 使用GZIPInputStream进行解压缩
  4. 将字节数组转换回UTF-8字符串

### 智能压缩策略
- **长度阈值**: 小于100字节的文本不进行压缩
- **效果检查**: 压缩后大小超过原大小90%时保持原文本
- **自动处理**: 创建和更新日记时自动压缩，读取时自动解压缩
- **透明操作**: 用户无需关心压缩状态，系统自动处理

### 性能特点
- **压缩率**: 典型中文文本压缩率可达30-70%
- **处理速度**: 单个日记压缩/解压缩通常在毫秒级完成
- **内存占用**: 压缩过程内存占用较小，适合批量处理
- **错误处理**: 压缩/解压缩失败时自动回退到原文本

## 错误响应
所有错误响应的格式如下:
```json
{
  "message": "错误信息"
}
```