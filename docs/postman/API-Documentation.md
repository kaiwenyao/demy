# Demy 接口文档

## 服务路由说明

| 服务 | 网关路径 | 直连端口 | 说明 |
|------|----------|----------|------|
| user-service | /users/** | 8081 | 用户注册 |
| auth-service | /api/v1/auth/** | - | 认证登录 |
| enrollment-service | 未配置 | 8082 | 课表/选课 |

网关默认端口：8080

---

## 1. 用户注册

### 接口概览

| 属性 | 值 |
|------|-----|
| 名称 | 用户注册 |
| 方法 | POST |
| 路径 | /users/register |
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
| role | string | 角色，默认 USER |
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

- **400** — refreshToken 为空
- **401** — refreshToken 无效或已过期

---

## 4. 分页查询我的课表

### 接口概览

| 属性 | 值 |
|------|-----|
| 名称 | 分页查询我的课表 |
| 方法 | GET |
| 路径 | /enrollment/page |
| 鉴权 | X-User-Id 请求头 |
| 服务 | enrollment-service (直连 8082) |

### 请求参数

| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| X-User-Id | header | 是 | - | 当前用户 ID |
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

---

## 5. 查询最近正在学习的课程

### 接口概览

| 属性 | 值 |
|------|-----|
| 名称 | 查询最近正在学习的课程 |
| 方法 | GET |
| 路径 | /enrollment/now |
| 鉴权 | X-User-Id 请求头 |
| 服务 | enrollment-service (直连 8082) |

### 说明

返回 status=1（学习中）的课程列表，按最近学习时间倒序，最多 10 条。

### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| X-User-Id | header | 是 | 当前用户 ID |

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

---

## 6. 查询指定课程的学习状态

### 接口概览

| 属性 | 值 |
|------|-----|
| 名称 | 根据 ID 查询指定课程的学习状态 |
| 方法 | GET |
| 路径 | /enrollment/{courseId} |
| 鉴权 | X-User-Id 请求头 |
| 服务 | enrollment-service (直连 8082) |

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
- **404** — 课程不在课表中

---

## 7. 删除课表中的课程

### 接口概览

| 属性 | 值 |
|------|-----|
| 名称 | 删除课表中的某课程 |
| 方法 | DELETE |
| 路径 | /enrollment/{courseId} |
| 鉴权 | X-User-Id 请求头 |
| 服务 | enrollment-service (直连 8082) |

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
- **404** — 课程不在课表中

---

## 内部接口（不对外暴露）

以下接口仅供服务间调用，Gateway 应屏蔽 /internal/**。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /internal/users/by-email?email= | 根据邮箱查询用户凭证 |
| GET | /internal/users/by-username?username= | 根据用户名查询用户凭证 |
