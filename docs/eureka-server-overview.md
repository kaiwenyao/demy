# Eureka Server 服务概述

本文档详细介绍 Spring Cloud 微服务架构中的 **Eureka Server** 服务，帮助你理解其作用、文件结构和配置信息。

---

## 一、Eureka Server 是什么？

**Eureka** 是 Netflix 开源的服务发现组件，Spring Cloud 将其集成后作为服务注册与发现的核心组件。Eureka Server 是服务发现的**服务端**，也称为**注册中心**。

### 核心作用

| 作用 | 说明 |
|------|------|
| **服务注册** | 微服务启动时，将自己注册到 Eureka Server，告知注册中心自己的地址、端口、服务名等信息 |
| **服务发现** | 其他微服务或客户端可以从 Eureka Server 查询已注册的服务列表，获取服务实例地址，实现服务间调用 |
| **心跳检测** | 定期接收各个服务的心跳，检测服务是否存活，剔除不可用的实例 |
| **负载均衡** | 配合 Ribbon 或 Spring Cloud LoadBalancer，可以从多个实例中选择一个进行调用 |

### 在微服务架构中的位置

```
                    ┌─────────────────────────┐
                    │     Eureka Server       │
                    │   (服务注册与发现中心)    │
                    │   localhost:8761        │
                    └───────────┬─────────────┘
                                │
         ┌──────────────────────┼──────────────────────┐
         │                      │                      │
         ▼                      ▼                      ▼
┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐
│   user-service  │   │  auth-service   │   │  course-service │
│   (注册 + 发现)   │   │   (注册 + 发现)   │   │   (注册 + 发现)   │
└─────────────────┘   └─────────────────┘   └─────────────────┘
```

---

## 二、文件结构

```
eureka-server/
├── pom.xml                                    # Maven 依赖配置
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── dev/kaiwen/eurekaserver/
│   │   │       └── EurekaServerApplication.java    # 启动类
│   │   └── resources/
│   │       └── application.yaml                     # 应用配置
│   └── test/
│       └── java/
│           └── dev/kaiwen/eurekaserver/
│               └── EurekaServerApplicationTests.java  # 单元测试
├── .gitignore
└── .gitattributes
```

### 各文件说明

| 文件/目录 | 说明 |
|-----------|------|
| `pom.xml` | Maven 项目配置，定义对 `spring-cloud-starter-netflix-eureka-server` 的依赖 |
| `EurekaServerApplication.java` | Spring Boot 启动类，使用 `@EnableEurekaServer` 启用 Eureka 服务端 |
| `application.yaml` | 应用配置文件，包含服务端口、Eureka 客户端配置等 |
| `EurekaServerApplicationTests.java` | 基础上下文加载测试，验证应用能正常启动 |

---

## 三、核心代码解析

### 启动类：`EurekaServerApplication.java`

```java
package dev.kaiwen.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication    // Spring Boot 应用标识
@EnableEurekaServer      // 启用 Eureka 服务端，将当前应用作为注册中心
public class EurekaServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(EurekaServerApplication.class, args);
  }
}
```

**关键注解**：

- `@SpringBootApplication`：Spring Boot 应用入口，整合了 `@Configuration`、`@EnableAutoConfiguration`、`@ComponentScan`
- `@EnableEurekaServer`：**最重要的注解**，将应用标记为 Eureka Server，自动配置注册中心所需组件

---

## 四、配置详解：`application.yaml`

```yaml
spring:
  application:
    name: eureka-server        # 应用名称，便于识别

server:
  port: 8761                   # 服务端口，Eureka 默认端口为 8761

eureka:
  instance:
    hostname: localhost        # 实例主机名，单机部署一般为 localhost
  client:
    register-with-eureka: false   # 是否向 Eureka 注册自己（服务端通常不注册自己）
    fetch-registry: false         # 是否从 Eureka 拉取注册表（服务端通常不需要）
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
      # 默认服务地址，指向自身（单节点模式）
```

### 配置项说明

| 配置项 | 值 | 说明 |
|--------|-----|------|
| `spring.application.name` | `eureka-server` | 应用名称，在监控、日志中标识该服务 |
| `server.port` | `8761` | HTTP 端口，Eureka 管理界面和 API 都通过此端口访问 |
| `eureka.instance.hostname` | `localhost` | 实例的主机名，集群模式下可改为实际主机名或域名 |
| `eureka.client.register-with-eureka` | `false` | Eureka Server 本身作为注册中心，不需要把自己注册到别的 Eureka |
| `eureka.client.fetch-registry` | `false` | Eureka Server 不需要从其他 Eureka 拉取注册表，因为自己就是中心 |
| `eureka.client.service-url.defaultZone` | `http://localhost:8761/eureka/` | 客户端连接 Eureka 的地址，单节点时指向自己 |

### 为什么 Server 要设 `register-with-eureka: false` 和 `fetch-registry: false`？

在**单节点** Eureka Server 模式下：

- 如果 `register-with-eureka: true`，Server 会尝试把自己注册到 `defaultZone` 指定的地址（即自己），没有实际意义。
- 如果 `fetch-registry: true`，Server 会从别的 Eureka 拉取注册表，但单节点时没有其他 Eureka，也没有必要。

因此，在单节点 Eureka Server 中，这两个配置通常设为 `false`。  
在 **Eureka 集群** 模式下，多个 Eureka Server 之间会互相注册、互相拉取注册表，此时应设为 `true`，并配置多个 `defaultZone` 地址。

---

## 五、Maven 依赖：`pom.xml`

```xml
<parent>
  <groupId>dev.kaiwen</groupId>
  <artifactId>demy</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <relativePath>../pom.xml</relativePath>
</parent>

<artifactId>eureka-server</artifactId>

<dependencies>
  <dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
  </dependency>
</dependencies>
```

- **父 POM**：继承自 `demy` 父项目，其中已定义 Spring Cloud 版本（如 `2025.0.1`）和 Java 版本（如 `21`）。
- **核心依赖**：`spring-cloud-starter-netflix-eureka-server` 提供 Eureka Server 所需的一切依赖。

---

## 六、如何启动与访问

### 启动服务

```bash
cd eureka-server
mvn spring-boot:run
```

或在 IDE 中直接运行 `EurekaServerApplication` 的 `main` 方法。

### 访问管理界面

启动成功后，在浏览器访问：

```
http://localhost:8761
```

可以看到 Eureka 自带的 **Dashboard**，包括：

- 当前注册的实例列表
- 系统状态
- DS Replicas（集群中的其他 Eureka 节点）

---

## 七、与其他微服务的关系

其他微服务（如 `user-service`、`auth-service` 等）需要：

1. 引入 `spring-cloud-starter-netflix-eureka-client` 依赖
2. 在配置中指定：

   ```yaml
   eureka:
     client:
       service-url:
         defaultZone: http://localhost:8761/eureka/
   ```

3. 启动后会自动向 Eureka Server 注册，并在 Eureka Dashboard 中显示

---

## 八、常见问题

### 1. 启动顺序

在微服务架构中，**Eureka Server 应该最先启动**，因为其他服务启动时需要连接注册中心完成注册。

### 2. 端口占用

确保 `8761` 端口未被占用。若被占用，可在 `application.yaml` 中修改 `server.port`。

### 3. 生产环境建议

- 使用 **Eureka 集群** 实现高可用，多节点互相注册
- 配置 **安全认证**（如 Spring Security）保护管理界面
- 根据网络环境调整 `eureka.instance.hostname` 和 `defaultZone`，使用实际域名或内网地址

---

## 九、总结

| 项目 | 内容 |
|------|------|
| **角色** | 服务注册与发现中心（服务端） |
| **端口** | 8761 |
| **核心注解** | `@EnableEurekaServer` |
| **核心依赖** | `spring-cloud-starter-netflix-eureka-server` |
| **访问地址** | http://localhost:8761 |

Eureka Server 是 Spring Cloud 微服务架构的基础组件，负责维护所有微服务的注册信息，为服务间的动态发现和调用提供支持。
