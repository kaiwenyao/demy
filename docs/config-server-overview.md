# Config Server 服务概述

本文档详细介绍 Spring Cloud 微服务架构中的 **Config Server**（配置中心）服务，帮助你理解其作用、文件结构和配置信息。

---

## 一、Config Server 是什么？

**Spring Cloud Config** 提供分布式系统中的**外部化配置**支持。Config Server 是配置中心的**服务端**，负责从 Git、本地文件等存储中读取配置，并通过 HTTP 接口提供给各个微服务。

### 核心作用

| 作用 | 说明 |
|------|------|
| **集中管理配置** | 将各微服务的配置统一存放在 Git 仓库或文件系统中，避免散落在各个服务里 |
| **环境隔离** | 支持 `dev`、`test`、`prod` 等多环境配置，同一份代码可切换不同配置 |
| **配置刷新** | 配合 `@RefreshScope`，可在不重启服务的情况下更新部分配置 |
| **版本追溯** | 使用 Git 时，配置变更可版本控制、回滚 |

### 在微服务架构中的位置

```
┌─────────────────────────────────────────────────────────────────┐
│                    Git 仓库 (demy-config-repo)                    │
│         auth-service.yml, user-service.yml, application.yml      │
└───────────────────────────────┬─────────────────────────────────┘
                                │ 读取
                                ▼
                    ┌─────────────────────────┐
                    │     Config Server       │
                    │   (配置中心)             │
                    │   localhost:8888        │
                    │   (注册到 Eureka)        │
                    └───────────┬─────────────┘
                                │ 提供配置
         ┌──────────────────────┼──────────────────────┐
         │                      │                      │
         ▼                      ▼                      ▼
┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐
│   user-service  │   │  auth-service   │   │  course-service │
│   (通过发现拉取)  │   │   (通过发现拉取)   │   │   (通过发现拉取)   │
└─────────────────┘   └─────────────────┘   └─────────────────┘
```

---

## 二、文件结构

```
config-server/
├── pom.xml                                    # Maven 依赖配置
├── src/
│   └── main/
│       ├── java/
│       │   └── dev/kaiwen/configserver/
│       │       └── ConfigServerApplication.java   # 启动类
│       └── resources/
│           └── application.yml                    # 应用配置
└── target/                                       # 编译输出（可忽略）
```

### 各文件说明

| 文件/目录 | 说明 |
|-----------|------|
| `pom.xml` | Maven 项目配置，依赖 `spring-cloud-config-server` 和 `eureka-client` |
| `ConfigServerApplication.java` | Spring Boot 启动类，使用 `@EnableConfigServer` 启用配置中心 |
| `application.yml` | Config Server 自身的配置，包括端口、Git 仓库地址、Eureka 连接等 |

---

## 三、核心代码解析

### 启动类：`ConfigServerApplication.java`

```java
package dev.kaiwen.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication     // Spring Boot 应用标识
@EnableConfigServer       // 启用配置中心服务端
public class ConfigServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(ConfigServerApplication.class, args);
  }
}
```

**关键注解**：

- `@SpringBootApplication`：Spring Boot 应用入口
- `@EnableConfigServer`：将当前应用标记为 Config Server，自动提供配置读取和 HTTP 接口

---

## 四、配置详解：`application.yml`

```yaml
server:
  port: 8888                    # Config Server 端口，Spring Cloud 默认端口

spring:
  application:
    name: config-server         # 应用名称
  cloud:
    config:
      server:
        git:
          uri: file:///Users/kaiwenyao/Documents/demy-config-repo
          # Git 仓库地址，配置文件的存储位置

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
      # 注册到 Eureka，供其他服务通过服务发现找到 Config Server
```

### 配置项说明

| 配置项 | 值 | 说明 |
|--------|-----|------|
| `server.port` | `8888` | Config Server 的 HTTP 端口，Spring Cloud Config 惯例使用 8888 |
| `spring.application.name` | `config-server` | 应用名称，在 Eureka 中注册的服务名 |
| `spring.cloud.config.server.git.uri` | `file:///Users/kaiwenyao/...` | 配置存储的 Git 仓库地址，`file://` 表示本地路径 |
| `eureka.client.service-url.defaultZone` | `http://localhost:8761/eureka/` | Eureka 地址，Config Server 会注册自己，供客户端发现 |

### Git 仓库地址说明

- `file:///Users/kaiwenyao/Documents/demy-config-repo`：本地 Git 仓库路径
- 也可改为远程仓库，例如：`uri: https://github.com/xxx/demy-config-repo.git`
- 仓库内通常存放各服务的配置文件，如 `auth-service.yml`、`user-service.yml`、`application.yml` 等

### 为什么 Config Server 要注册到 Eureka？

本项目中，其他微服务通过 **服务发现** 找到 Config Server，而不是写死 IP 和端口：

```yaml
# 客户端（如 auth-service）的配置
spring:
  cloud:
    config:
      discovery:
        enabled: true           # 启用通过 Eureka 发现 Config Server
        service-id: config-server
  config:
    import: "configserver:"     # 从 Config Server 导入配置
```

这样即使 Config Server 的地址或端口变化，客户端也能自动发现，无需修改配置。

---

## 五、Maven 依赖：`pom.xml`

```xml
<parent>
  <groupId>dev.kaiwen</groupId>
  <artifactId>demy</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</parent>

<artifactId>config-server</artifactId>

<dependencies>
  <dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
  </dependency>
</dependencies>
```

| 依赖 | 作用 |
|------|------|
| `spring-cloud-config-server` | Config Server 核心能力，从 Git/文件系统读取配置并提供 HTTP 接口 |
| `spring-cloud-starter-netflix-eureka-client` | 注册到 Eureka，让其他服务通过服务名 `config-server` 发现配置中心 |

---

## 六、Config Server 提供的 HTTP 接口

Config Server 提供 REST API，可按以下格式访问配置：

| 格式 | 示例 | 说明 |
|------|------|------|
| `/{application}/{profile}` | `/auth-service/default` | 获取 auth-service 的 default 环境配置 |
| `/{application}/{profile}/{label}` | `/auth-service/dev/main` | 获取 auth-service 的 dev 环境、main 分支配置 |
| `/{application}-{profile}.yml` | `/auth-service-dev.yml` | 直接获取 YAML 格式配置 |
| `/{label}/{application}-{profile}.yml` | `/main/auth-service-dev.yml` | 指定分支的 YAML 配置 |

**示例**（Config Server 运行在 8888 端口）：

```
http://localhost:8888/auth-service/default
http://localhost:8888/user-service/dev
```

---

## 七、配置文件命名规则（Git 仓库内）

Config Server 从 Git 中根据以下规则查找配置：

| 文件名 | 作用 |
|--------|------|
| `{application}.yml` 或 `{application}.properties` | 对应服务名为 `application` 的配置 |
| `{application}-{profile}.yml` | 对应服务 + 环境的配置，如 `auth-service-dev.yml` |
| `application.yml` | 所有服务共享的默认配置 |

其中 `{application}` 通常对应 `spring.application.name`，`{profile}` 对应 `spring.profiles.active`（如 `dev`、`prod`）。

---

## 八、与其他微服务的关系

### 客户端如何连接 Config Server

本项目采用 **通过 Eureka 发现 Config Server** 的方式：

```yaml
# 以 auth-service 为例
spring:
  application:
    name: auth-service
  cloud:
    config:
      discovery:
        enabled: true
        service-id: config-server    # 通过 Eureka 查找名为 config-server 的服务
  config:
    import: "configserver:"         # 启动时从 Config Server 拉取配置
```

### 启动顺序建议

1. **Eureka Server**（8761）— 最先启动  
2. **Config Server**（8888）— 注册到 Eureka  
3. **其他业务微服务** — 通过 Eureka 发现 Config Server，拉取配置后启动  

---

## 九、如何启动与验证

### 启动前准备

确保本地存在配置仓库：

```
/Users/kaiwenyao/Documents/demy-config-repo
```

若不存在，需先创建并放入至少一个配置文件（如 `application.yml`）。

### 启动服务

```bash
cd config-server
mvn spring-boot:run
```

### 验证

访问：

```
http://localhost:8888/application/default
```

若返回 JSON 配置内容，说明 Config Server 运行正常。

---

## 十、常见问题

### 1. Git 仓库路径不存在

若 `demy-config-repo` 不在本地，需先创建：

```bash
mkdir -p /Users/kaiwenyao/Documents/demy-config-repo
cd /Users/kaiwenyao/Documents/demy-config-repo
git init
# 添加 application.yml 等配置文件
```

### 2. 使用远程 Git 仓库

将 `application.yml` 中的 `uri` 改为远程地址：

```yaml
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/your-org/demy-config-repo.git
          # 可选：username、password 或 access-token
```

### 3. 多环境配置

在 Git 仓库中放置 `{application}-{profile}.yml`，例如：

- `auth-service-dev.yml`
- `auth-service-prod.yml`

客户端通过 `spring.profiles.active=dev` 指定环境。

---

## 十一、总结

| 项目 | 内容 |
|------|------|
| **角色** | 集中式配置中心（服务端） |
| **端口** | 8888 |
| **核心注解** | `@EnableConfigServer` |
| **配置存储** | Git 仓库（本地或远程） |
| **服务发现** | 注册到 Eureka，供其他服务通过 `config-server` 发现 |
| **核心依赖** | `spring-cloud-config-server`、`eureka-client` |

Config Server 将分散在各微服务中的配置集中管理，支持多环境和版本控制，是 Spring Cloud 微服务架构中常用的基础设施组件。
