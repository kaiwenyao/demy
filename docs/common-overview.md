# Common 模块概述

本文档详细介绍项目中的 **common** 模块。common 不是独立运行的微服务，而是一个**共享库**，被多个业务服务依赖，用于统一 API 响应格式、分页结构和异常处理。

---

## 一、Common 是什么？

**Common** 是 demy 项目中的**公共基础模块**，封装了各微服务共用的：

- **统一响应格式**：`Result`、`PageDto`
- **业务异常类**：`BadRequestException`、`ResourceNotFoundException`、`ResourceAlreadyExistsException`
- **全局异常处理**：`GlobalExceptionHandler` 自动装配，将异常转换为 `Result` 格式返回

### 核心作用

| 作用 | 说明 |
|------|------|
| **统一 API 格式** | 所有接口返回 `{"code":200,"msg":"success","data":...}` 结构，前端解析一致 |
| **分页标准化** | `PageDto` 封装分页数据，与 Spring Data `Page` 无缝衔接 |
| **异常统一处理** | 业务异常映射为对应 HTTP 状态码和 `Result` 格式，无需在各 Controller 重复 try-catch |
| **代码复用** | 避免在 auth-service、user-service 等中重复实现相同逻辑 |

### 在项目中的位置

```
                    ┌─────────────────────────────────────────┐
                    │              Common 模块                  │
                    │  Result | PageDto | 异常类 | 全局异常处理   │
                    └────────────────────┬────────────────────┘
                                         │ 依赖
         ┌───────────────────────────────┼───────────────────────────────┐
         │                               │                               │
         ▼                               ▼                               ▼
┌─────────────────┐           ┌─────────────────┐           ┌─────────────────┐
│  auth-service   │           │  user-service   │           │  course-service │
└─────────────────┘           └─────────────────┘           └─────────────────┘
         │                               │                               │
         └───────────────────────────────┼───────────────────────────────┘
                                         │
                                ┌─────────────────┐
                                │enrollment-service│
                                └─────────────────┘
```

---

## 二、文件结构

```
common/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── dev/kaiwen/common/
│       │       ├── response/
│       │       │   ├── Result.java              # 统一响应体
│       │       │   └── PageDto.java             # 分页封装
│       │       └── exception/
│       │           ├── GlobalExceptionHandler.java   # 全局异常处理
│       │           ├── BadRequestException.java      # 400 业务异常
│       │           ├── ResourceNotFoundException.java   # 404 业务异常
│       │           └── ResourceAlreadyExistsException.java  # 409 业务异常
│       └── resources/
│           └── META-INF/
│               └── spring/
│                   └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
└── target/
```

### 各文件说明

| 文件 | 说明 |
|------|------|
| `Result.java` | 统一 API 响应体，含 `code`、`msg`、`data` |
| `PageDto.java` | 分页结果封装，含 `content`、`total`、`page`、`size`、`totalPages` 等 |
| `GlobalExceptionHandler.java` | `@RestControllerAdvice` 全局异常处理，将异常转为 `Result` |
| `BadRequestException.java` | 请求参数不合法，对应 HTTP 400 |
| `ResourceNotFoundException.java` | 资源不存在，对应 HTTP 404 |
| `ResourceAlreadyExistsException.java` | 资源已存在（如重复注册），对应 HTTP 409 |
| `AutoConfiguration.imports` | Spring Boot 3 自动装配清单，使 `GlobalExceptionHandler` 在依赖 common 的应用中生效 |

---

## 三、核心组件详解

### 1. Result — 统一响应体

```java
@Getter
public class Result<T> {

  private final int code;
  private final String msg;
  private final T data;

  // 成功
  public static <T> Result<T> success(T data) { ... }
  public static <T> Result<T> success() { ... }

  // 错误
  public static <T> Result<T> error(int code, String msg) { ... }
}
```

**返回示例**：

```json
{"code":200,"msg":"success","data":{"id":1,"name":"John"}}
{"code":400,"msg":"Username is required","data":null}
```

**在 Controller 中的用法**：

```java
return Result.success(user);           // 200
return Result.success();               // 200，无 data
return Result.error(400, "Invalid");   // 一般由异常处理器使用
```

---

### 2. PageDto — 分页封装

```java
@Getter
public class PageDto<T> {

  private final List<T> content;    // 当前页数据
  private final long total;         // 总记录数
  private final int page;           // 当前页码（从 0 开始）
  private final int size;           // 每页大小
  private final int totalPages;     // 总页数

  public static <T> PageDto<T> of(List<T> content, long total, int page, int size) { ... }
  public static <T> PageDto<T> from(Page<T> page) { ... }  // 从 Spring Data Page 转换

  public boolean hasNext() { ... }
  public boolean hasPrevious() { ... }
  public boolean isFirst() { ... }
  public boolean isLast() { ... }
  public boolean isEmpty() { ... }
}
```

**返回示例**：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "content": [...],
    "total": 100,
    "page": 0,
    "size": 10,
    "totalPages": 10
  }
}
```

**用法示例**：

```java
// 从 Spring Data Page 转换
Page<Course> page = courseRepository.findAll(pageable);
return Result.success(PageDto.from(page.map(this::toResponse)));

// 手动构建
return Result.success(PageDto.of(content, total, page, size));
```

---

### 3. 业务异常类

| 异常类 | HTTP 状态码 | 使用场景 |
|--------|-------------|----------|
| `BadRequestException` | 400 | 参数不合法、业务校验失败 |
| `ResourceNotFoundException` | 404 | 查询的资源不存在 |
| `ResourceAlreadyExistsException` | 409 | 资源已存在（如用户名、邮箱重复） |

**在 Service 中的用法**：

```java
// 资源不存在
throw new ResourceNotFoundException("User not found");
throw new ResourceNotFoundException("Course not found: " + id);

// 参数错误
throw new BadRequestException("Username is required");
throw new BadRequestException("Invalid refresh token");

// 资源已存在
throw new ResourceAlreadyExistsException("Username already exists");
throw new ResourceAlreadyExistsException("Email already registered");
```

抛出后由 `GlobalExceptionHandler` 捕获，自动返回对应 HTTP 状态码和 `Result` 格式。

---

### 4. GlobalExceptionHandler — 全局异常处理

```java
@AutoConfiguration
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNotFound(ResourceNotFoundException e) {
        return Result.error(404, e.getMessage());
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result<Void> handleConflict(ResourceAlreadyExistsException e) {
        return Result.error(409, e.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBadRequest(BadRequestException e) {
        return Result.error(400, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        // @Valid 校验失败，提取第一个字段错误信息
        String message = ...;
        return Result.error(400, message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("Unexpected error", e);
        return Result.error(500, "Internal server error");
    }
}
```

**异常映射表**：

| 异常类型 | HTTP 状态码 | 说明 |
|----------|-------------|------|
| `ResourceNotFoundException` | 404 | 资源不存在 |
| `ResourceAlreadyExistsException` | 409 | 资源已存在 |
| `BadRequestException` | 400 | 请求参数不合法 |
| `MethodArgumentNotValidException` | 400 | `@Valid` 校验失败 |
| `Exception` | 500 | 其他未处理异常 |

---

### 5. 自动装配 — AutoConfiguration.imports

```
dev.kaiwen.common.exception.GlobalExceptionHandler
```

Spring Boot 3 通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 加载 `GlobalExceptionHandler`。只要业务服务依赖了 common，启动时就会自动注册该全局异常处理器，无需额外配置。

---

## 四、Maven 依赖：`pom.xml`

```xml
<parent>
  <groupId>dev.kaiwen</groupId>
  <artifactId>demy</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</parent>

<artifactId>common</artifactId>

<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-commons</artifactId>
  </dependency>
  <dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
  </dependency>
</dependencies>
```

| 依赖 | 作用 |
|------|------|
| `spring-boot-starter-web` | 提供 `@RestControllerAdvice`、`MethodArgumentNotValidException` 等 |
| `spring-data-commons` | 提供 `Page` 接口，用于 `PageDto.from(Page)` |
| `lombok` | 简化 `@Getter` 等样板代码 |

---

## 五、业务服务如何使用 Common

### 添加依赖

在业务服务的 `pom.xml` 中：

```xml
<dependency>
  <groupId>dev.kaiwen</groupId>
  <artifactId>common</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### Controller 返回 Result

```java
@GetMapping("/{id}")
public Result<CourseResponse> getById(@PathVariable Long id) {
    return Result.success(courseService.findById(id));
}

@GetMapping
public Result<PageDto<CourseResponse>> list(Pageable pageable) {
    return Result.success(courseService.findAll(pageable));
}
```

### Service 抛出业务异常

```java
User user = userRepository.findById(id)
    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

if (userRepository.existsByUsername(username)) {
    throw new ResourceAlreadyExistsException("Username already exists");
}
```

### 与业务自定义异常处理共存

部分服务（如 auth-service）会有自己的 `GlobalExceptionHandler`，用于处理本服务特有的异常（如 401 认证失败）。common 的 `GlobalExceptionHandler` 会与业务自定义的 `@RestControllerAdvice` 共存，按异常类型分别处理。

---

## 六、常见问题

### 1. Common 需要单独启动吗？

不需要。common 是 **JAR 库**，不包含 `main` 方法，不会独立运行。它被其他服务依赖，编译打包后随业务服务一起运行。

### 2. 如何新增一种业务异常？

在 common 中新增异常类（继承 `RuntimeException`），然后在 `GlobalExceptionHandler` 中新增对应的 `@ExceptionHandler` 方法即可。

### 3. 为什么 Gateway 也有 Result 格式？

Gateway 使用 `ResultResponseUtil` 自己写入 `{"code":...,"msg":"...","data":null}`，与 common 的 `Result` 格式保持一致，但 Gateway 基于 WebFlux，不依赖 common 的 Servlet 版 `GlobalExceptionHandler`。

---

## 七、总结

| 项目 | 内容 |
|------|------|
| **类型** | 共享库（JAR），非独立服务 |
| **主要功能** | 统一响应格式、分页封装、业务异常、全局异常处理 |
| **依赖方** | auth-service、user-service、course-service、enrollment-service |
| **自动装配** | `GlobalExceptionHandler` 通过 `AutoConfiguration.imports` 自动生效 |
| **核心类** | `Result`、`PageDto`、`BadRequestException`、`ResourceNotFoundException`、`ResourceAlreadyExistsException` |

Common 模块是各业务服务的基础设施层，确保 API 响应格式统一、异常处理一致，提升开发效率和可维护性。
