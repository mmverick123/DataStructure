# 旅游分享平台 API 接口文档

本文档详细描述了旅游分享平台的API接口。目前项目使用模拟数据，未来将连接到真实后端。

## 通用说明

- 基础URL: `http://localhost:3000/api`
- 所有接口除特殊说明外均采用 JSON 格式进行数据交换
- 认证接口需要在 header 中添加 token: `Authorization: Bearer {token}`

## 认证相关接口

### 用户注册

- URL: `/auth/register`
- 方法: `POST`
- 请求体:
```json
{
  "username": "用户名",
  "password": "密码",
  "email": "邮箱"
}
```
- 响应:
```json
{
  "success": true,
  "message": "注册成功",
  "user": {
    "id": 1,
    "username": "用户名",
    "email": "邮箱"
  },
  "token": "jwt_token"
}
```

### 用户登录

- URL: `/auth/login`
- 方法: `POST`
- 请求体:
```json
{
  "username": "用户名",
  "password": "密码"
}
```
- 响应:
```json
{
  "success": true,
  "message": "登录成功",
  "user": {
    "id": 1,
    "username": "用户名",
    "email": "邮箱"
  },
  "token": "jwt_token"
}
```

## 推荐系统接口

### 获取推荐景点

- URL: `/recommend`
- 方法: `GET`
- 参数:
  - `filter`: 筛选类型，可选值: `hot`, `top-rated`, `comprehensive`
  - `limit`: 返回条数，默认为 10
- 响应:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "景点名称",
      "description": "景点描述",
      "rating": 4.8,
      "popularity": 1000,
      "category": "category",
      "image": "图片URL",
      "location": {
        "lng": 116.123,
        "lat": 39.456
      }
    }
  ]
}
```

### 搜索景点

- URL: `/attractions/search`
- 方法: `GET`
- 参数:
  - `keyword`: 搜索关键词
  - `orderBy`: 排序方式，可选值: `popularity`, `rating`, `comprehensive`
  - `categories`: 类别筛选，多个类别用逗号分隔，如 `history,nature`
  - `limit`: 返回条数，默认为 10
- 响应:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "景点名称",
      "description": "景点描述",
      "rating": 4.8,
      "popularity": 1000,
      "category": "category",
      "image": "图片URL",
      "location": {
        "lng": 116.123,
        "lat": 39.456
      }
    }
  ]
}
```

### 获取景点详情

- URL: `/attractions/:id`
- 方法: `GET`
- 响应:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "景点名称",
    "description": "景点详细描述",
    "rating": 4.8,
    "popularity": 1000,
    "category": "category",
    "images": ["图片URL1", "图片URL2"],
    "location": {
      "address": "详细地址",
      "lng": 116.123,
      "lat": 39.456
    },
    "features": ["特色1", "特色2"],
    "openingHours": "开放时间",
    "ticketInfo": "门票信息"
  }
}
```

## 旅游日记接口

### 获取日记列表

- URL: `/diaries`
- 方法: `GET`
- 参数:
  - `attractionId`: 景点ID（可选）
  - `userId`: 用户ID（可选）
  - `page`: 页码，默认为 1
  - `limit`: 每页条数，默认为 10
  - `orderBy`: 排序方式，可选值: `newest`, `hottest`, `rating`
- 响应:
```json
{
  "success": true,
  "data": {
    "total": 100,
    "page": 1,
    "limit": 10,
    "items": [
      {
        "id": 1,
        "title": "日记标题",
        "content": "日记内容",
        "createdAt": "2023-06-10T12:00:00Z",
        "views": 150,
        "averageRating": 4.5,
        "user": {
          "id": 1,
          "username": "用户名",
          "avatar": "头像URL"
        },
        "attraction": {
          "id": 1,
          "name": "景点名称"
        },
        "images": ["图片URL1", "图片URL2"]
      }
    ]
  }
}
```

### 获取日记详情

- URL: `/diaries/:id`
- 方法: `GET`
- 响应:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "日记标题",
    "content": "日记详细内容",
    "createdAt": "2023-06-10T12:00:00Z",
    "views": 150,
    "averageRating": 4.5,
    "ratings": [
      {
        "userId": 2,
        "rating": 5,
        "comment": "评论内容",
        "createdAt": "2023-06-11T12:00:00Z"
      }
    ],
    "user": {
      "id": 1,
      "username": "用户名",
      "avatar": "头像URL"
    },
    "attraction": {
      "id": 1,
      "name": "景点名称",
      "location": {
        "lng": 116.123,
        "lat": 39.456
      }
    },
    "images": ["图片URL1", "图片URL2"]
  }
}
```

### 创建日记

- URL: `/diaries`
- 方法: `POST`
- 需要认证: 是
- 请求体:
```json
{
  "title": "日记标题",
  "content": "日记内容",
  "attractionId": 1,
  "images": ["图片URL1", "图片URL2"]
}
```
- 响应:
```json
{
  "success": true,
  "message": "创建成功",
  "data": {
    "id": 1,
    "title": "日记标题",
    "content": "日记内容",
    "createdAt": "2023-06-15T12:00:00Z",
    "user": {
      "id": 1,
      "username": "用户名"
    },
    "attraction": {
      "id": 1,
      "name": "景点名称"
    },
    "images": ["图片URL1", "图片URL2"]
  }
}
```

### 评价日记

- URL: `/diaries/:id/rate`
- 方法: `POST`
- 需要认证: 是
- 请求体:
```json
{
  "rating": 5,
  "comment": "评论内容"
}
```
- 响应:
```json
{
  "success": true,
  "message": "评价成功",
  "data": {
    "diaryId": 1,
    "userId": 2,
    "rating": 5,
    "comment": "评论内容",
    "createdAt": "2023-06-15T12:00:00Z"
  }
}
```

## 用户相关接口

### 获取用户信息

- URL: `/users/:id`
- 方法: `GET`
- 响应:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "用户名",
    "avatar": "头像URL",
    "bio": "个人简介",
    "diariesCount": 10
  }
}
```

### 获取用户日记列表

- URL: `/users/:id/diaries`
- 方法: `GET`
- 参数:
  - `page`: 页码，默认为 1
  - `limit`: 每页条数，默认为 10
- 响应:
```json
{
  "success": true,
  "data": {
    "total": 10,
    "page": 1,
    "limit": 10,
    "items": [
      {
        "id": 1,
        "title": "日记标题",
        "content": "日记内容摘要",
        "createdAt": "2023-06-10T12:00:00Z",
        "views": 150,
        "averageRating": 4.5,
        "attraction": {
          "id": 1,
          "name": "景点名称"
        },
        "images": ["图片URL1"]
      }
    ]
  }
}
景点推荐相关接口

获取所有景点
- URL: /api/attractions
- 方法: GET
- 描述: 获取所有景点（基于现有日记动态生成）
- 实现说明: 系统会自动分析现有日记的location字段，动态生成景点数据并计算统计信息
- 响应:
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

根据ID获取景点
- URL: /api/attractions/{id}
- 方法: GET
- 描述: 获取指定ID的景点详情
- 响应: 景点对象

创建新景点
- URL: /api/attractions
- 方法: POST
- 描述: 手动创建新景点
- 请求体:
{
  "name": "景点名称",
  "description": "景点描述",
  "category": "景点类别",
  "location": "景点位置",
  "keywords": "关键词1,关键词2,关键词3"
}
- 响应: 创建的景点对象

更新景点信息
- URL: /api/attractions/{id}
- 方法: PUT
- 描述: 更新景点信息
- 请求体:
{
  "name": "更新后的景点名称",
  "description": "更新后的描述",
  "category": "更新后的类别",
  "location": "更新后的位置",
  "keywords": "更新后的关键词"
}
- 响应: 更新后的景点对象

删除景点
- URL: /api/attractions/{id}
- 方法: DELETE
- 描述: 删除景点
- 响应:
{
  "message": "景点删除成功!"
}

搜索景点（高级搜索）
- URL: /api/attractions/search
- 方法: POST
- 描述: 高级景点搜索，支持多种排序算法
- 请求体:
{
  "searchTerm": "搜索关键词",
  "category": "景点类别",
  "sortBy": "views|rating|composite",
  "limit": 10,
  "useTopK": true,
  "viewsWeight": 0.6,
  "ratingWeight": 0.4
}
- 参数说明:
  - searchTerm: 搜索关键词（可选）
  - category: 景点类别（可选）
  - sortBy: 排序方式
    - views: 按热度排序
    - rating: 按评价排序
    - composite: 按综合评分排序
  - limit: 返回结果数量限制（1-50，默认10）
  - useTopK: 是否使用Top-K算法（默认true）
  - viewsWeight: 热度权重（0-1，默认0.6）
  - ratingWeight: 评价权重（0-1，默认0.4）
- 算法说明:
  - Top-K算法: 时间复杂度O(n log k)，只获取前K个结果，适合用户通常只看前10个景点的场景
  - 快速排序: 时间复杂度O(n log n)，完整排序
- 响应: 排序后的景点对象列表

搜索景点（简单搜索）
- URL: /api/attractions/search
- 方法: GET
- 描述: 简单景点搜索
- 参数:
  - searchTerm: 搜索关键词（可选）
  - category: 景点类别（可选）
  - sortBy: 排序方式，默认"views"
  - limit: 返回结果数量，默认10
  - useTopK: 是否使用Top-K算法，默认true
- 响应: 排序后的景点对象列表

获取热门推荐景点
- URL: /api/attractions/recommendations/popular
- 方法: GET
- 描述: 获取按热度推荐的景点（基于相关日记的总阅读量）
- 参数:
  - limit: 返回结果数量，默认10
- 算法说明: 使用Top-K堆排序算法，时间复杂度O(n log k)
- 响应: 按热度排序的景点列表

获取高评分推荐景点
- URL: /api/attractions/recommendations/top-rated
- 方法: GET
- 描述: 获取按评价推荐的景点（基于相关日记的平均评分）
- 参数:
  - limit: 返回结果数量，默认10
- 算法说明: 使用Top-K堆排序算法，时间复杂度O(n log k)
- 响应: 按评价排序的景点列表

获取综合推荐景点
- URL: /api/attractions/recommendations/composite
- 方法: GET
- 描述: 获取按综合评分推荐的景点（热度和评价的加权平均）
- 参数:
  - limit: 返回结果数量，默认10
  - viewsWeight: 热度权重，默认0.6
  - ratingWeight: 评价权重，默认0.4
- 算法说明: 使用Top-K堆排序算法，支持自定义权重的综合评分
- 响应: 按综合评分排序的景点列表

更新所有景点统计数据
- URL: /api/attractions/update-statistics
- 方法: POST
- 描述: 手动触发更新所有景点的统计数据
- 实现说明: 重新分析所有日记，更新景点的热度、评价等统计信息
- 响应:
{
  "message": "景点统计数据更新完成"
}

更新单个景点统计数据
- URL: /api/attractions/{id}/update-statistics
- 方法: POST
- 描述: 更新指定景点的统计数据
- 响应:
{
  "message": "景点统计数据更新完成"
}
景点类别自动推断
系统会根据景点名称自动推断类别：
- 历史文化: 博物馆、故宫、寺、庙、古城、古镇、遗址
- 自然风光: 山、湖、海、河、森林、公园、峡谷
- 园林景观: 园、花园
- 教育机构: 大学、学校、学院
- 商业区域: 商场、购物、街
- 其他景点: 未匹配的其他类型

关键词自动提取
系统会从相关日记的标题和内容中自动提取关键词，用于搜索和匹配。
错误响应
所有错误响应的格式如下:
{
  "message": "错误信息"
}