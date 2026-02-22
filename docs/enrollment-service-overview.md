# Enrollment Service 服务概述

本文档详细介绍 **Enrollment Service**（选课/课表服务），重点说明其作为 Spring Cloud 微服务的特点，以及 JPA 在项目中的用法。

---

## 一、服务简介

**Enrollment Service** 负责管理用户的**选课记录**（课表），包括：我的课表分页查询、最近正在学习的课程、按课程查询学习状态、删除课表中的课程等。

### 在微服务架构中的位置

```
                    客户端请求
                         │
                         ▼
              Gateway (8080) ── /api/v1/enrollments/**
                         │
                         │  lb://enrollment-service (服务发现 + 负载均衡)
                         ▼
              ┌─────────────────────────────┐
              │     Enrollment Service      │
              │  • 注册到 Eureka            │
              │  • 从 Config Server 拉配置   │
              │  • 使用 common 统一响应     │
              │  • JPA + MySQL 持久化       │
              └─────────────────────────────┘
```

---

## 二、Spring Cloud 微服务特点

### 1. 服务注册与发现（Eureka Client）

Enrollment Service 是 **Eureka 客户端**，启动时将自己注册到 Eureka Server，供 Gateway 通过 `lb://enrollment-service` 发现并转发请求。

**依赖**：

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

**配置**（通常来自 Config Server，本地 `application.yml` 仅保留基础信息）：

```yaml
spring:
  application:
    name: enrollment-service
  cloud:
    config:
      discovery:
        enabled: true
        service-id: config-server
  config:
    import: "configserver:"
```

- `spring.application.name`: 服务名，Eureka 中注册的名称
- `config.import: "configserver:"`: 从 Config Server 导入配置，数据源、端口等通常在配置中心

---

### 2. 集中配置（Config Server）

Enrollment Service 通过 **服务发现** 找到 Config Server，拉取 `enrollment-service.yml` 或 `application.yml` 等配置，实现配置与代码分离、多环境切换。

**依赖**：

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```

---

### 3. 网关路由与鉴权

Gateway 路由配置：

```yaml
- id: enrollment-route
  uri: lb://enrollment-service
  predicates:
    - Path=/api/v1/enrollments/**
  filters:
    - StripPrefix=2
```

- 外部路径：`/api/v1/enrollments` → 转发后：`/enrollments`
- `lb://`：通过 Eureka 发现 `enrollment-service`，LoadBalancer 选择实例

Gateway 的 JwtAuthFilter 会：
- 校验 JWT（除白名单外）
- 解析后将 `userId` 放入 `X-User-Id` Header 转发

Controller 从 Header 读取用户身份：

```java
@RequestHeader(value = "X-User-Id", required = false) Long userId
```

---

### 4. 共享基础能力（Common 模块）

| 能力 | 用法 |
|------|------|
| `Result` | `return Result.success(data)` 统一响应格式 |
| `PageDto` | `PageDto.of(content, total, page, size)` 分页封装 |
| `ResourceNotFoundException` | 选课记录不存在时抛出，自动返回 404 |
| `BadRequestException` | `X-User-Id` 缺失时抛出，自动返回 400 |
| `GlobalExceptionHandler` | 自动装配，将异常转为 `Result` 格式 |

---

## 三、文件结构

```
enrollment-service/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── dev/kaiwen/enrollmentservice/
│       │       ├── EnrollmentServiceApplication.java
│       │       ├── controller/
│       │       │   └── EnrollmentController.java
│       │       ├── service/
│       │       │   ├── EnrollmentService.java
│       │       │   └── impl/
│       │       │       └── EnrollmentServiceImpl.java
│       │       ├── repository/
│       │       │   └── EnrollmentRepository.java
│       │       ├── entity/
│       │       │   ├── Enrollment.java
│       │       │   └── EnrollmentStatus.java
│       │       ├── dto/
│       │       │   └── EnrollmentResponse.java
│       │       └── config/
│       │           ├── SecurityConfig.java
│       │           └── OpenApiConfig.java
│       └── resources/
│           ├── application.yml
│           └── db/
│               └── migration/
│                   ├── V1__Init_Learning_Lesson_Table.sql
│                   └── V2__Update_Enrollments_Table.sql
└── target/
```

---

## 四、JPA 详解

### 1. 依赖

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
  <groupId>com.mysql</groupId>
  <artifactId>mysql-connector-j</artifactId>
  <scope>runtime</scope>
</dependency>
<dependency>
  <groupId>org.flywaydb</groupId>
  <artifactId>flyway-mysql</artifactId>
</dependency>
```

- **spring-boot-starter-data-jpa**：JPA/Hibernate + Spring Data JPA
- **mysql-connector-j**：MySQL 驱动
- **flyway-mysql**：数据库版本管理

---

### 2. 实体类：Enrollment

```java
@Entity
@Table(name = "enrollments")
public class Enrollment {

    @Id
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EnrollmentStatus status = EnrollmentStatus.NOT_STARTED;

    @Column(name = "learned_sections", nullable = false)
    private Integer learnedSections = 0;

    @Column(name = "latest_section_id")
    private Long latestSectionId;

    @Column(name = "latest_learn_time")
    private Instant latestLearnTime;

    @Column(name = "expire_time")
    private Instant expireTime;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() { ... }

    @PreUpdate
    protected void onUpdate() { ... }
}
```

**JPA 注解说明**：

| 注解 | 作用 |
|------|------|
| `@Entity` | 标记 JPA 实体，对应数据库表 |
| `@Table(name = "enrollments")` | 指定表名 |
| `@Id` | 主键 |
| `@Column` | 列名、是否可空、长度等 |
| `@Enumerated(EnumType.STRING)` | 枚举按字符串存储（NOT_STARTED），而非数字 |
| `@PrePersist` | 插入前回调，生成 TSID、设置 createdAt/updatedAt |
| `@PreUpdate` | 更新前回调，设置 updatedAt |

**主键策略**：使用 **TSID**（Time-Sortable ID）手动生成，在 `@PrePersist` 中：

```java
if (this.id == null) {
    this.id = TSID.fast().toLong();
}
```

---

### 3. 枚举：EnrollmentStatus

```java
public enum EnrollmentStatus {
    NOT_STARTED,  // 未开始学习
    IN_PROGRESS,  // 学习中
    COMPLETED,    // 已学完
    EXPIRED       // 已过期
}
```

`@Enumerated(EnumType.STRING)` 将枚举值存为字符串，便于可读和扩展。

---

### 4. Repository：Spring Data JPA

```java
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Page<Enrollment> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);

    List<Enrollment> findByUserIdAndStatusOrderByLatestLearnTimeDesc(
        Long userId, EnrollmentStatus status, Pageable pageable);

    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}
```

**方法命名规则**（Derived Query）：

| 方法名 | 生成的查询逻辑 |
|--------|----------------|
| `findByUserIdOrderByUpdatedAtDesc` | `WHERE user_id = ? ORDER BY updated_at DESC` |
| `findByUserIdAndStatusOrderByLatestLearnTimeDesc` | `WHERE user_id = ? AND status = ? ORDER BY latest_learn_time DESC` |
| `findByUserIdAndCourseId` | `WHERE user_id = ? AND course_id = ?`，返回 `Optional` |
| `existsByUserIdAndCourseId` | `EXISTS (SELECT 1 WHERE user_id = ? AND course_id = ?)` |

**分页**：`Pageable` 由 Controller 传入（如 `PageRequest.of(page, size)`），返回 `Page<Enrollment>` 或 `List<Enrollment>`，Spring Data 自动拼接 `LIMIT ? OFFSET ?`。

---

### 4.1 Page\<T\> 与 Optional\<T\> 详解

#### Page\<T\>（如 Page\<Enrollment\>）

| 项目 | 说明 |
|------|------|
| **来源** | `org.springframework.data.domain.Page`，由 **Spring Data Commons** 提供 |
| **依赖链** | `spring-boot-starter-data-jpa` → `spring-data-jpa` → `spring-data-commons` |
| **作用** | 封装分页查询结果，包含当前页数据、总记录数、页码、每页大小等元信息 |
| **常用方法** | `getContent()` 当前页数据；`getTotalElements()` 总记录数；`getNumber()` 当前页码；`getSize()` 每页大小；`getTotalPages()` 总页数 |

当 Repository 方法返回类型为 `Page<T>` 且参数包含 `Pageable` 时，Spring Data JPA 会：

1. 执行**主查询**：`SELECT ... FROM enrollments WHERE user_id = ? ORDER BY updated_at DESC LIMIT ? OFFSET ?`
2. 执行 **COUNT 查询**：`SELECT COUNT(*) FROM enrollments WHERE user_id = ?`，用于计算总记录数和总页数

#### Optional\<T\>（如 Optional\<Enrollment\>）

| 项目 | 说明 |
|------|------|
| **来源** | `java.util.Optional`，**JDK 8+ 标准库** |
| **作用** | 表示“可能有值可能为空”的查询结果，避免返回 `null` 导致 NPE，并强制调用方显式处理“不存在”的情况 |
| **常用方法** | `orElseThrow()` 不存在时抛异常；`orElse(default)` 不存在时返回默认值；`isPresent()` 判断是否有值 |

`findByUserIdAndCourseId` 返回 `Optional<Enrollment>`，因为按 userId + courseId 查询时，记录可能不存在。使用方式：

```java
enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
    .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found for course: " + courseId));
```

---

### 4.2 分页查询实现流程

```
Controller 传入 page=0, size=10
        │
        ▼
PageRequest.of(0, 10)  →  创建 Pageable 对象
        │
        ▼
enrollmentRepository.findByUserIdOrderByUpdatedAtDesc(userId, pageable)
        │
        ├── Spring Data 解析方法名，生成 SQL：
        │   SELECT * FROM enrollments WHERE user_id = ? ORDER BY updated_at DESC LIMIT 10 OFFSET 0
        │
        ├── 执行 COUNT 查询（用于 total）：
        │   SELECT COUNT(*) FROM enrollments WHERE user_id = ?
        │
        ▼
返回 Page<Enrollment>，包含：
  - content: 当前页的 List<Enrollment>
  - totalElements: 总记录数
  - number: 0（当前页码）
  - size: 10（每页大小）
  - totalPages: 由 totalElements/size 计算
```

---

### 4.3 返回结果封装流程

Service 层将 Repository 的 `Page<Enrollment>` 转为对外暴露的 `PageDto<EnrollmentResponse>`：

```java
// 1. Repository 返回 Page<Enrollment>
var result = enrollmentRepository.findByUserIdOrderByUpdatedAtDesc(userId, pageable);

// 2. 将 Entity 转为 DTO
var content = result.getContent().stream()
        .map(EnrollmentResponse::from)
        .collect(Collectors.toList());

// 3. 封装为 PageDto（common 模块提供）
return PageDto.of(content, result.getTotalElements(), page, size);
```

**封装层次**：

| 层次 | 类型 | 来源 | 说明 |
|------|------|------|------|
| Repository 层 | `Page<Enrollment>` | Spring Data | 数据库查询结果 |
| Service 层 | `PageDto<EnrollmentResponse>` | common 模块 | 业务分页结构，Entity 已转为 DTO |
| Controller 层 | `Result<PageDto<EnrollmentResponse>>` | common 模块 | 统一 API 响应格式 |

**最终 JSON 示例**：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "content": [
      {"id": 1, "userId": 100, "courseId": 5, "status": "IN_PROGRESS", ...}
    ],
    "total": 25,
    "page": 0,
    "size": 10,
    "totalPages": 3
  }
}
```

**可选简化**：若不需要 Entity→DTO 转换，可直接使用 common 的 `PageDto.from(Page<T>)`：

```java
// 适用于 Entity 即 DTO 的场景
return PageDto.from(page.map(EnrollmentResponse::from));
```

---

### 5. 事务管理

```java
@Transactional(rollbackFor = Exception.class)
public void deleteEnrollment(Long userId, Long courseId) {
    var enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found for course: " + courseId));
    enrollmentRepository.delete(enrollment);
}
```

- `@Transactional`：在 Service 方法上声明事务
- `rollbackFor = Exception.class`：任何异常都回滚（默认仅 `RuntimeException`）
- 读操作（find、get）一般无需显式事务，Spring Data 会提供只读事务

---

### 6. Flyway 数据库迁移

表结构由 **Flyway** 管理，脚本位于 `src/main/resources/db/migration/`：

| 脚本 | 说明 |
|------|------|
| `V1__Init_Learning_Lesson_Table.sql` | 创建 `enrollments` 表 |
| `V2__Update_Enrollments_Table.sql` | 调整 status 为枚举字符串，重命名字段等 |

**命名规则**：`V{版本号}__{描述}.sql`，执行顺序按版本号升序。

**与 JPA 的关系**：

- Flyway 负责 DDL（建表、改表）
- JPA/Hibernate 不自动建表（`spring.jpa.hibernate.ddl-auto` 通常为 `validate` 或 `none`）
- 实体类字段需与 Flyway 迁移后的表结构一致

---

### 7. Entity → DTO 转换

实体 `Enrollment` 仅在 Service/Repository 层使用，对外返回 DTO `EnrollmentResponse`：

```java
public static EnrollmentResponse from(Enrollment e) {
    return EnrollmentResponse.builder()
            .id(e.getId())
            .userId(e.getUserId())
            .courseId(e.getCourseId())
            .status(e.getStatus())
            // ...
            .build();
}
```

这样可避免暴露实体内部结构，并支持与数据库字段不同的 API 设计。

---

## 五、API 接口与数据流

| 接口 | 方法 | 说明 |
|------|------|------|
| `/enrollments` | GET | 分页查询我的课表 |
| `/enrollments/now` | GET | 最近正在学习的课程 |
| `/enrollments/{courseId}` | GET | 指定课程的学习状态 |
| `/enrollments/{courseId}` | DELETE | 从课表删除课程 |

**数据流示例**（分页查询）：

```
1. 请求 GET /api/v1/enrollments?page=0&size=10
2. Gateway 鉴权，注入 X-User-Id
3. StripPrefix=2 → /enrollments
4. 路由到 enrollment-service
5. Controller 校验 X-User-Id，调用 Service
6. Service 调用 Repository.findByUserIdOrderByUpdatedAtDesc
7. JPA 执行 SQL，返回 Page<Enrollment>
8. Service 转为 PageDto<EnrollmentResponse>
9. Controller 返回 Result.success(pageDto)
```

---

## 六、Maven 依赖汇总

| 依赖 | 作用 |
|------|------|
| spring-boot-starter-data-jpa | JPA 持久化 |
| mysql-connector-j | MySQL 驱动 |
| flyway-mysql | 数据库迁移 |
| spring-cloud-starter-config | 配置中心客户端 |
| spring-cloud-starter-netflix-eureka-client | 服务注册与发现 |
| spring-boot-starter-web | Web MVC |
| spring-boot-starter-security | 安全框架（本服务中全部放行，鉴权在 Gateway） |
| common | 统一响应、异常 |
| hypersistence-tsid | TSID 主键生成 |
| springdoc-openapi-starter-webmvc-ui | Swagger/OpenAPI 文档 |

---

## 七、启动与配置说明

### 启动顺序

1. Eureka Server  
2. Config Server  
3. MySQL（确保 Flyway 可连接）  
4. Enrollment Service  

### 配置来源

- 本地 `application.yml`：服务名、Config Server 发现
- Config Server（如 `enrollment-service.yml`）：数据源、端口、Eureka 地址等

典型数据源配置示例（位于 Config Server 的配置仓库）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/demy?...
    username: xxx
    password: xxx
  jpa:
    hibernate:
      ddl-auto: validate   # 不自动建表，由 Flyway 管理
```

---

## 八、总结

| 维度 | 内容 |
|------|------|
| **Spring Cloud** | Eureka 注册、Config 拉配置、Gateway 路由与鉴权、common 统一响应 |
| **JPA** | Entity + Repository、方法派生查询、事务、Flyway 迁移 |
| **业务** | 课表/选课记录管理：分页、状态查询、删除 |
| **鉴权** | 依赖 Gateway 注入 X-User-Id，无 JWT 解析 |

Enrollment Service 是典型的 Spring Cloud + JPA 业务微服务：通过 Eureka 与 Config 融入微服务架构，通过 JPA 和 Flyway 完成数据持久化与表结构管理。
