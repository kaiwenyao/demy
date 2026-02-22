# Gateway Service 服务概述

本文档详细介绍 Spring Cloud 微服务架构中的 **Gateway Service**（API 网关）服务，帮助你理解其作用、文件结构和配置信息。

---

## 一、Gateway Service 是什么？

**Spring Cloud Gateway** 是基于 Spring WebFlux 的 API 网关，作为微服务架构的**统一入口**，负责请求路由、过滤、鉴权等横切关注点。

### 核心作用

| 作用 | 说明 |
|------|------|
| **统一入口** | 外部请求统一通过 Gateway（如 8080 端口）进入，客户端无需知晓各个微服务的具体地址 |
| **请求路由** | 根据路径（如 `/api/v1/users/**`）将请求转发到对应的微服务（如 user-service） |
| **JWT 鉴权** | 在网关层统一校验 Token，通过后将用户信息（userId、role）注入 Header 转发给下游 |
| **路径屏蔽** | 屏蔽 `/internal/**` 等内部接口，防止外部直接调用 |
| **负载均衡** | 配合 LoadBalancer，从多个服务实例中选取一个进行转发 |

### 在微服务架构中的位置

```
                    外部请求（浏览器、移动端、第三方）
                                    │
                                    ▼
                    ┌─────────────────────────────────┐
                    │       Gateway Service           │
                    │       (API 网关)                 │
                    │       localhost:8080            │
                    │  • 路由转发  • JWT 鉴权  • 过滤   │
                    └───────────────┬─────────────────┘
                                    │
         ┌──────────────────────────┼──────────────────────────┐
         │                          │                          │
         ▼                          ▼                          ▼
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│  auth-service   │      │  user-service   │      │  course-service │
│  /api/v1/auth   │      │  /api/v1/users  │      │  /api/v1/courses│
└─────────────────┘      └─────────────────┘      └─────────────────┘
```

---

## 二、文件结构

```
gateway-service/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── dev/kaiwen/gatewayservice/
│       │       ├── GatewayServiceApplication.java      # 启动类
│       │       ├── filter/
│       │       │   ├── BlockInternalPathFilter.java    # 屏蔽内部路径
│       │       │   └── JwtAuthFilter.java              # JWT 鉴权
│       │       ├── handler/
│       │       │   └── GlobalGatewayExceptionHandler.java  # 统一异常处理
│       │       └── util/
│       │           └── ResultResponseUtil.java         # 错误响应工具
│       └── resources/
│           └── application.yml
└── target/
```

### 各文件说明

| 文件 | 说明 |
|------|------|
| `GatewayServiceApplication.java` | Spring Boot 启动类 |
| `BlockInternalPathFilter.java` | 全局过滤器，拦截 `/internal/**` 路径，返回 403 |
| `JwtAuthFilter.java` | 全局过滤器，校验 JWT，白名单路径放行，通过后注入 X-User-Id、X-User-Role |
| `GlobalGatewayExceptionHandler.java` | 统一异常处理，将错误转换为 Result 格式 JSON |
| `ResultResponseUtil.java` | 工具类，用于写入统一格式的错误响应 |
| `application.yml` | 路由规则、Eureka、JWT 等配置 |

---

## 三、核心代码解析

### 启动类：`GatewayServiceApplication.java`

```java
@SpringBootApplication
public class GatewayServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(GatewayServiceApplication.class, args);
  }
}
```

Spring Cloud Gateway 依赖 `spring-cloud-starter-gateway-server-webflux`，会自动配置网关能力，无需额外注解。

---

### 过滤器 1：`BlockInternalPathFilter`

**作用**：屏蔽 `/internal/**` 路径，不允许外部通过网关直接访问内部接口。

```java
@Override
public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String path = exchange.getRequest().getPath().value();
    if (path.startsWith("/internal/")) {
        return ResultResponseUtil.writeError(exchange, HttpStatus.FORBIDDEN, "Forbidden");
    }
    return chain.filter(exchange);
}

@Override
public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;  // 最高优先级，最先执行
}
```

- `Ordered.HIGHEST_PRECEDENCE`：确保该过滤器最先执行，优先于鉴权等逻辑。

---

### 过滤器 2：`JwtAuthFilter`

**作用**：对需要保护的接口进行 JWT 鉴权，通过后将用户信息写入 Header 转发给下游。

**白名单**（无需 Token）：

| 路径 | 说明 |
|------|------|
| `/api/v1/auth/login` | 登录 |
| `/api/v1/auth/refresh` | 刷新 Token |
| `/api/v1/users/register` | 用户注册 |
| `/swagger-ui*` | Swagger 文档 |
| `/v3/api-docs` | OpenAPI 文档 |
| `GET /api/v1/courses/**` | 课程列表、详情（公开） |

**流程**：

1. 白名单或公开接口 → 直接放行
2. 检查 `Authorization: Bearer <token>`
3. 解析 JWT，提取 `sub`（userId）和 `role`
4. 写入 `X-User-Id`、`X-User-Role`，转发请求
5. Token 无效或缺失 → 返回 401 Unauthorized

---

### 异常处理：`GlobalGatewayExceptionHandler`

将 Gateway 层的异常统一转换为 Result 格式：

```json
{"code":403,"msg":"Forbidden","data":null}
```

便于前端统一解析。

---

### 工具类：`ResultResponseUtil`

提供 `writeError(exchange, status, msg)`，用于在过滤器中快速返回统一格式的错误响应。

---

## 四、配置详解：`application.yml`

```yaml
server:
  port: 8080                    # 网关端口，外部请求入口

spring:
  application:
    name: gateway-service
  cloud:
    gateway:
      server:
        webflux:
          routes:
            # 认证服务
            - id: auth-route
              uri: lb://auth-service
              predicates:
                - Path=/api/v1/auth/**
            # 用户服务
            - id: user-route
              uri: lb://user-service
              predicates:
                - Path=/api/v1/users/**
            # 选课服务
            - id: enrollment-route
              uri: lb://enrollment-service
              predicates:
                - Path=/api/v1/enrollments/**
            # 课程服务
            - id: course-route
              uri: lb://course-service
              predicates:
                - Path=/api/v1/courses/**
            # 订单服务
            - id: order-route
              uri: lb://order-service
              predicates:
                - Path=/api/v1/orders/**

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

jwt:
  secret: c2VjcmV0LWtleS1mb3Itand0LXNpZ25pbmctYXQtbGVhc3QtMzJieXRlcw==
  # 必须和 auth-service 完全一致
```

### 路由配置说明

| 配置项 | 说明 |
|--------|------|
| `uri: lb://auth-service` | `lb` 表示 LoadBalancer，通过 Eureka 发现 `auth-service` 并负载均衡 |
| `predicates: Path=/api/v1/auth/**` | 匹配请求路径，`**` 表示多级路径 |

### 路径转发说明

Gateway 将请求**原样转发**至下游服务，不做路径裁剪。例如请求 `/api/v1/users/123` 会原样转发到 user-service。各微服务 Controller 均使用 `/api/v1/xxx` 作为基础路径，与 Gateway 路由一致。

### JWT Secret

`jwt.secret` 必须与 **auth-service** 完全一致，否则 Gateway 无法正确解析 auth-service 签发的 Token。

---

## 五、Maven 依赖：`pom.xml`

| 依赖 | 作用 |
|------|------|
| `spring-cloud-starter-gateway-server-webflux` | Spring Cloud Gateway 核心，基于 WebFlux 响应式模型 |
| `spring-cloud-starter-netflix-eureka-client` | 注册到 Eureka，并通过服务名发现下游服务 |
| `spring-cloud-starter-loadbalancer` | 负载均衡，配合 `lb://` 从多实例中选取一个 |
| `jjwt-api`、`jjwt-impl`、`jjwt-jackson` | JWT 解析，用于校验 Token 并提取 Claims |

---

## 六、请求流转示意

以 `GET /api/v1/users/123` 为例：

```
1. 请求进入 Gateway (8080)
2. BlockInternalPathFilter  → 非 /internal/**，放行
3. JwtAuthFilter            → 校验 JWT（若需鉴权），注入 X-User-Id、X-User-Role
4. 路由匹配                 → Path=/api/v1/users/** 命中 user-route
5. LoadBalancer             → 从 Eureka 获取 user-service 实例，选一个
6. 转发请求                 → 原样转发路径到 user-service，带 X-User-Id、X-User-Role
7. 返回响应                 → 原路返回给客户端
```

---

## 七、白名单与公开接口

| 类型 | 路径示例 | 说明 |
|------|----------|------|
| 白名单 | `/api/v1/auth/login`、`/api/v1/users/register` | 明确配置，无需 Token |
| 公开接口 | `GET /api/v1/courses/**` | 课程列表、详情，GET 且路径匹配即放行 |
| 需鉴权 | 其他 `/api/v1/*` | 必须携带有效 `Authorization: Bearer <token>` |

---

## 八、下游服务如何获取用户信息

Gateway 在鉴权通过后，会将 JWT 中的信息写入请求 Header：

| Header | 含义 | 示例 |
|--------|------|------|
| `X-User-Id` | 用户 ID（JWT sub） | `123` |
| `X-User-Role` | 用户角色 | `STUDENT`、`INSTRUCTOR` 等 |

下游服务（如 user-service、enrollment-service）从 Header 中读取即可，无需再解析 JWT。

---

## 九、启动顺序与验证

### 启动顺序建议

1. **Eureka Server**（8761）
2. **Config Server**（8888，若使用）
3. **业务微服务**（auth、user、course、enrollment）
4. **Gateway**（8080）— 依赖 Eureka 发现下游服务

### 验证

```bash
# 公开接口（课程列表）
curl http://localhost:8080/api/v1/courses

# 需鉴权接口（需替换为真实 token）
curl -H "Authorization: Bearer <your-jwt-token>" http://localhost:8080/api/v1/users/me

# 内部路径应被屏蔽
curl http://localhost:8080/internal/xxx
# 预期：403 Forbidden
```

---

## 十、常见问题

### 1. 503 Service Unavailable

下游服务未启动或未注册到 Eureka，Gateway 无法发现实例。确保 auth-service、user-service 等已启动并注册成功。

### 2. 401 Unauthorized

- Token 缺失、格式错误（需 `Bearer <token>`）
- Token 过期或无效
- `jwt.secret` 与 auth-service 不一致

### 3. 路径转发错误

Gateway 将路径原样转发，下游服务 Controller 需使用 `/api/v1/xxx` 等完整路径与 Gateway 路由一致。若请求未命中预期服务，检查 `predicates` 中的 `Path` 配置。

### 4. 为什么用 WebFlux？

Spring Cloud Gateway 基于 Netty 和 Reactor，采用响应式模型，适合高并发、非阻塞 I/O。因此 Gateway 不能引入 `spring-boot-starter-web`（Servlet 模型），两者冲突。

---

## 十一、总结

| 项目 | 内容 |
|------|------|
| **角色** | API 网关，统一入口 |
| **端口** | 8080 |
| **核心能力** | 路由、JWT 鉴权、路径屏蔽、负载均衡 |
| **路由协议** | `lb://`（通过 Eureka + LoadBalancer 发现服务） |
| **关键过滤器** | BlockInternalPathFilter、JwtAuthFilter |
| **下游用户信息** | 通过 X-User-Id、X-User-Role 传递 |

Gateway Service 是微服务架构的入口，集中处理路由、鉴权、过滤等横切逻辑，让业务服务专注于业务本身。
