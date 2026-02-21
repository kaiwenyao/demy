# Demy 接口文档

## 统一入口

**所有请求均通过网关**：http://localhost:8080

## 服务路由

| 服务 | 网关路径 | 说明 |
|------|----------|------|
| user-service | /api/v1/users/** | 用户注册 |
| auth-service | /api/v1/auth/** | 认证登录 |
| enrollment-service | /api/v1/enrollments/** | 课表/选课 |
| course-service | /api/v1/courses/** | 课程管理 |

## 鉴权说明

- **用户注册、登录、刷新 Token**：无鉴权
- **课表、课程（管理员）接口**：需 `Authorization: Bearer <accessToken>`，网关解析 JWT 后注入 X-User-Id、X-User-Role
- **课程管理接口**（创建/更新/下架/添加小节）：需 ROLE_ADMIN 角色
- **内部接口**：/internal/** 网关屏蔽，返回 403

## 统一响应格式 (Result)

成功：

```json
{
  "code": 200,
  "msg": "success",
  "data": { ... }
}
```

错误（包括 Gateway 层的 401/403）：

```json
{
  "code": 400,
  "msg": "错误描述",
  "data": null
}
```

## 验证顺序

1. 启动所有服务
2. POST 登录 → 拿到 Token
3. GET 课表 不带 Token → 预期 401（Result 格式）
4. GET 课表 带 Token → 预期 200
5. GET 内部接口（走网关）→ 预期 403（Result 格式）

---

## 1. 用户注册

### 接口概览

| 属性 | 值 |
|------|-----|
| 名称 | 用户注册 |
| 方法 | POST |
| 路径 | /api/v1/users/register |
| 鉴权 | 无 |
| Content-Type | application/json |

### 请求参数

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名 |
| email | string | 否 | 邮箱 |
| password | string | 是 | 登录密码 |

### 请求示例

```json
{
  "username": "alice",
  "email": "alice@example.com",
  "password": "password123"
}
```

### 成功响应 (HTTP 200)

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "username": "alice",
    "email": "alice@example.com",
    "role": "USER",
    "createTime": "2025-02-21T12:00:00.000Z",
    "updateTime": "2025-02-21T12:00:00.000Z"
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 用户 ID |
| username | string | 用户名 |
| email | string | 邮箱 |
| role | string | 角色，默认 USER，管理员为 ROLE_ADMIN |
| createTime | string | 创建时间 (ISO 8601) |
| updateTime | string | 更新时间 (ISO 8601) |

### 错误响应

- **400** — 用户名或密码为空
- **409** — 用户名已存在 / 邮箱已存在

---

## 2. 登录

### 接口概览

| 属性 | 值 |
|------|-----|
| 名称 | 登录 |
| 方法 | POST |
| 路径 | /api/v1/auth/login |
| 鉴权 | 无 |
| Content-Type | application/json |

### 请求参数

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| identifier | string | 是 | 邮箱或用户名（统一字段） |
| password | string | 是 | 密码 |

### 请求示例

```json
{
  "identifier": "alice@example.com",
  "password": "password123"
}
```

### 成功响应 (HTTP 200)

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer"
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| data.accessToken | string | JWT 访问令牌（15 分钟） |
| data.refreshToken | string | 刷新令牌（7 天），用于换取新 accessToken |
| data.tokenType | string | 固定 Bearer |

### 错误响应

- **400** — 参数校验失败 (identifier/password 为空)
- **401** — 用户名或密码错误

---

## 3. 刷新 Access Token

### 接口概览

| 属性 | 值 |
|------|-----|
| 名称 | 刷新 Access Token |
| 方法 | POST |
| 路径 | /api/v1/auth/refresh |
| 鉴权 | 无 |
| Content-Type | application/json |

### 请求参数

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| refreshToken | string | 是 | 登录时返回的 refreshToken |

### 请求示例

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 成功响应 (HTTP 200)

与登录响应格式相同，返回新的 accessToken 和 refreshToken。

### 错误响应

- **400** — refreshToken 无效或格式错误
- **404** — 用户不存在

---

## 4. 分页查询我的课表

### 接口概览

| 属性 | 值 |
|------|-----|
| 名称 | 分页查询我的课表 |
| 方法 | GET |
| 路径 | /api/v1/enrollments/page |
| 鉴权 | Bearer Token |

### 请求参数

| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| Authorization | header | 是 | - | Bearer &lt;accessToken&gt; |
| page | query | 否 | 0 | 页码 |
| size | query | 否 | 10 | 每页大小 |

### 成功响应 (HTTP 200)

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "userId": 1,
        "courseId": 100,
        "status": 1,
        "weekFreq": 6,
        "planStatus": 1,
        "learnedSections": 3,
        "latestSectionId": 15,
        "latestLearnTime": "2025-02-21T12:00:00Z",
        "createTime": "2025-02-20T10:00:00Z",
        "expireTime": null,
        "updateTime": "2025-02-21T12:00:00Z"
      }
    ],
    "total": 1,
    "page": 0,
    "size": 10,
    "totalPages": 1
  }
}
```

### 错误响应

- **400** — 缺少 X-User-Id 请求头
- **401** — 未携带 Token 或 Token 无效

---

## 5. 查询最近正在学习的课程

### 接口概览

| 属性 | 值 |
|------|-----|
| 名称 | 查询最近正在学习的课程 |
| 方法 | GET |
| 路径 | /api/v1/enrollments/now |
| 鉴权 | Bearer Token |

### 说明

返回 status=1（学习中）的课程列表，按最近学习时间倒序，最多 10 条。

### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| Authorization | header | 是 | Bearer &lt;accessToken&gt; |

### 成功响应 (HTTP 200)

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "courseId": 100,
      "status": 1,
      "weekFreq": 6,
      "planStatus": 1,
      "learnedSections": 3,
      "latestSectionId": 15,
      "latestLearnTime": "2025-02-21T12:00:00Z",
      "createTime": "2025-02-20T10:00:00Z",
      "expireTime": null,
      "updateTime": "2025-02-21T12:00:00Z"
    }
  ]
}
```

### 错误响应

- **400** — 缺少 X-User-Id 请求头
- **401** — 未携带 Token 或 Token 无效

---

## 6. 查询指定课程的学习状态

### 接口概览

| 属性 | 值 |
|------|-----|
| 名称 | 根据课程 ID 查询学习状态 |
| 方法 | GET |
| 路径 | /api/v1/enrollments/{courseId} |
| 鉴权 | Bearer Token |

### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| courseId | path | 课程 ID |

### 成功响应 (HTTP 200)

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "courseId": 100,
    "status": 1,
    "weekFreq": 6,
    "planStatus": 1,
    "learnedSections": 3,
    "latestSectionId": 15,
    "latestLearnTime": "2025-02-21T12:00:00Z",
    "createTime": "2025-02-20T10:00:00Z",
    "expireTime": null,
    "updateTime": "2025-02-21T12:00:00Z"
  }
}
```

**EnrollmentVo 字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| status | int | 0-未学习，1-学习中，2-已学完，3-已失效 |
| planStatus | int | 0-没有计划，1-计划进行中 |
| learnedSections | int | 已学习小节数量 |

### 错误响应

- **400** — 缺少 X-User-Id 请求头
- **401** — 未携带 Token 或 Token 无效
- **404** — 课程不在课表中

---

## 7. 删除课表中的课程

### 接口概览

| 属性 | 值 |
|------|-----|
| 名称 | 删除课表中的某课程 |
| 方法 | DELETE |
| 路径 | /api/v1/enrollments/{courseId} |
| 鉴权 | Bearer Token |

### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| courseId | path | 课程 ID |

### 成功响应 (HTTP 200)

```json
{
  "code": 200,
  "msg": "success",
  "data": null
}
```

### 错误响应

- **400** — 缺少 X-User-Id 请求头
- **401** — 未携带 Token 或 Token 无效
- **404** — 课程不在课表中

---

## 8. 分页查询课程

### 接口概览

| 属性 | 值 |
|------|-----|
| 名称 | 分页查询课程 |
| 方法 | GET |
| 路径 | /api/v1/courses |
| 鉴权 | 无 |

### 说明

仅返回 ACTIVE（上架中）的课程。`category` 为空字符串时按全量查询。

### 请求参数

| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| category | query | 否 | - | 分类筛选 |
| page | query | 否 | 0 | 页码 |
| size | query | 否 | 10 | 每页大小 |

### 成功响应 (HTTP 200)

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Spring Boot 从零到精通",
        "description": "...",
        "coverImage": "https://...",
        "price": 99.00,
        "category": "后端开发",
        "instructorId": 1,
        "level": "BEGINNER",
        "sectionCount": 5,
        "status": "ACTIVE",
        "validDays": 365,
        "createdAt": "2025-02-21T12:00:00Z",
        "sections": []
      }
    ],
    "total": 6,
    "page": 0,
    "size": 10,
    "totalPages": 1
  }
}
```

**CourseResponse 字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| level | string | BEGINNER / INTERMEDIATE / ADVANCED |
| status | string | ACTIVE / INACTIVE |
| sections | array | 列表页为空数组 `[]`，详情页含小节列表 |

---

## 9. 查询课程详情（含小节）

### 接口概览

| 属性 | 值 |
|------|-----|
| 名称 | 查询课程详情 |
| 方法 | GET |
| 路径 | /api/v1/courses/{id} |
| 鉴权 | 无 |

### 说明

仅返回 ACTIVE（上架中）的课程详情，含 sections 小节列表。

### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| id | path | 课程 ID |

### 成功响应 (HTTP 200)

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "title": "Spring Boot 从零到精通",
    "description": "...",
    "coverImage": "https://...",
    "price": 99.00,
    "category": "后端开发",
    "instructorId": 1,
    "level": "BEGINNER",
    "sectionCount": 5,
    "status": "ACTIVE",
    "validDays": 365,
    "createdAt": "2025-02-21T12:00:00Z",
    "sections": [
      {
        "id": 101,
        "title": "环境搭建与第一个 Spring Boot 项目",
        "duration": 600,
        "sortOrder": 1,
        "isFree": true
      }
    ]
  }
}
```

### 错误响应

- **404** — 课程不存在或已下架

---

## 10. 创建课程（管理员）

### 接口概览

| 属性 | 值 |
|------|-----|
| 名称 | 创建课程 |
| 方法 | POST |
| 路径 | /api/v1/courses |
| 鉴权 | Bearer Token + ROLE_ADMIN |
| Content-Type | application/json |

### 说明

仅管理员可创建课程，创建后直接上架（ACTIVE）。

### 请求体 CourseRequest

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | string | 是 | 课程标题 |
| description | string | 否 | 课程描述 |
| coverImage | string | 否 | 封面图 URL |
| price | number | 是 | 价格，0 表示免费 |
| category | string | 否 | 分类 |
| level | string | 否 | BEGINNER / INTERMEDIATE / ADVANCED |
| validDays | int | 否 | 有效天数，null 表示永久有效 |

### 请求示例

```json
{
  "title": "Java 入门教程",
  "description": "从零开始学 Java",
  "coverImage": "https://example.com/cover.jpg",
  "price": 0,
  "category": "编程",
  "level": "BEGINNER",
  "validDays": null
}
```

### 成功响应 (HTTP 201)

```json
{
  "code": 200,
  "msg": "success",
  "data": { ... CourseResponse }
}
```

### 错误响应

- **401** — 未携带 Token 或 Token 无效
- **403** — 非管理员

---

## 11. 更新课程（管理员）

### 接口概览

| 属性 | 值 |
|------|-----|
| 名称 | 更新课程 |
| 方法 | PUT |
| 路径 | /api/v1/courses/{id} |
| 鉴权 | Bearer Token + ROLE_ADMIN |
| Content-Type | application/json |

### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| id | path | 课程 ID |

### 请求体

同创建课程 CourseRequest。

### 成功响应 (HTTP 200)

```json
{
  "code": 200,
  "msg": "success",
  "data": { ... CourseResponse }
}
```

### 错误响应

- **401** — 未携带 Token 或 Token 无效
- **403** — 非管理员
- **404** — 课程不存在

---

## 12. 下架课程（管理员）

### 接口概览

| 属性 | 值 |
|------|-----|
| 名称 | 下架课程 |
| 方法 | DELETE |
| 路径 | /api/v1/courses/{id} |
| 鉴权 | Bearer Token + ROLE_ADMIN |

### 说明

将课程状态设为 INACTIVE，下架后不再在公开列表中显示。

### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| id | path | 课程 ID |

### 成功响应 (HTTP 200)

```json
{
  "code": 200,
  "msg": "success",
  "data": null
}
```

### 错误响应

- **401** — 未携带 Token 或 Token 无效
- **403** — 非管理员
- **404** — 课程不存在

---

## 13. 添加小节（管理员）

### 接口概览

| 属性 | 值 |
|------|-----|
| 名称 | 添加小节 |
| 方法 | POST |
| 路径 | /api/v1/courses/{id}/sections |
| 鉴权 | Bearer Token + ROLE_ADMIN |
| Content-Type | application/json |

### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| id | path | 课程 ID |

### 请求体 SectionRequest

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | string | 是 | 小节标题 |
| duration | int | 否 | 时长（秒） |
| sortOrder | int | 否 | 排序 |
| isFree | boolean | 否 | 是否免费试看，默认 false |

### 请求示例

```json
{
  "title": "第 1 章：环境搭建",
  "duration": 600,
  "sortOrder": 1,
  "isFree": true
}
```

### 成功响应 (HTTP 201)

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 101,
    "title": "第 1 章：环境搭建",
    "duration": 600,
    "sortOrder": 1,
    "isFree": true
  }
}
```

### 错误响应

- **401** — 未携带 Token 或 Token 无效
- **403** — 非管理员
- **404** — 课程不存在

---

## 内部接口（不对外暴露）

以下接口仅供服务间调用，通过网关访问应返回 403（Result 格式）。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /internal/users/by-email?email= | 根据邮箱查询用户凭证 |
| GET | /internal/users/by-username?username= | 根据用户名查询用户凭证 |
