# 旅游日记网站API接口文档

## 基础信息
- 基础URL: `http://localhost:8081`
- 所有需要认证的接口都需要在请求头中添加JWT令牌：
  ```
  Authorization: Bearer <token>
  ```

## 技术说明
该后端项目完全使用传统Java代码实现，不依赖Lombok等代码生成库，所有的getter/setter方法都是显式定义的。项目使用MySQL数据库进行数据持久化。

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
- **响应**:
  ```json
  [
    {
      "id": 1,
      "title": "日记标题",
      "content": "日记内容",
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

### 创建日记
- **URL**: `/api/diaries`
- **方法**: POST
- **描述**: 创建新日记
- **权限**: 需要认证
- **请求体**:
  ```json
  {
    "title": "日记标题",
    "content": "日记内容"
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
    "content": "更新后的内容"
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

## 错误响应
所有错误响应的格式如下:
```json
{
  "message": "错误信息"
}
``` 
 