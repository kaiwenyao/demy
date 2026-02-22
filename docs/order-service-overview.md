# Order Service 服务概述

本文档详细介绍 **Order Service**（订单服务），重点说明其作为 Spring Cloud 微服务的特点，以及 **RabbitMQ** 在订单支付与选课联动中的作用。

---

## 一、服务简介

**Order Service** 负责管理用户的**课程订单**，包括：创建订单、支付订单（余额扣款）、查询我的订单列表。支付成功后，通过 **RabbitMQ** 异步通知 enrollment-service 创建选课记录，实现订单与选课的松耦合。

### 在微服务架构中的位置

```
                    客户端请求
                         │
                         ▼
              Gateway (8080) ── /api/v1/orders/**
                         │
                         │  lb://order-service
                         ▼
              ┌─────────────────────────────┐
              │       Order Service          │
              │  • 创建订单 (PENDING)        │
              │  • 支付订单 (余额扣款)        │
              │  • 支付成功后发送 MQ 消息     │
              └──────────────┬──────────────┘
                             │
                             │  RabbitMQ (order.exchange)
                             │  routing key: order.paid
                             ▼
              ┌─────────────────────────────┐
              │   enrollment.queue          │
              └──────────────┬──────────────┘
                             │
                             │  @RabbitListener
                             ▼
              ┌─────────────────────────────┐
              │    Enrollment Service       │
              │  • 消费消息                 │
              │  • 创建 enrollment 记录     │
              └─────────────────────────────┘
```

---

## 二、RabbitMQ 详解

### 1. 为什么使用 RabbitMQ？

订单支付成功后，需要通知 enrollment-service 创建选课记录。有两种常见实现方式：

| 方式 | 优点 | 缺点 |
|------|------|------|
| **同步 Feign 调用** | 实现简单 | 强耦合；enrollment 异常会导致支付接口失败；响应变慢 |
| **RabbitMQ 异步** | 解耦；支付接口快速返回；enrollment 故障可重试；削峰填谷 | 需要额外中间件 |

本系统选择 **RabbitMQ**，实现订单服务与选课服务的**异步解耦**：支付成功后只负责发消息，选课创建由 enrollment-service 异步消费，互不影响。

---

### 2. RabbitMQ 核心概念

```
┌─────────────────────────────────────────────────────────────────┐
│                      RabbitMQ 消息流转                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   Producer           Exchange            Queue         Consumer  │
│  (order-service)    (order.exchange)   (enrollment.queue)        │
│       │                    │                  │         (enrollment-service)
│       │  publish           │   routing        │                  │
│       │  message ─────────►│   key match ────►│  deliver ───────►│
│       │                    │  (order.paid)    │                  │
│       │                    │                  │                  │
└─────────────────────────────────────────────────────────────────┘
```

| 概念 | 说明 | 本项目中 |
|------|------|----------|
| **Producer（生产者）** | 发送消息的应用 | order-service |
| **Exchange（交换机）** | 接收消息并按规则路由到队列 | `order.exchange`（Topic 类型） |
| **Queue（队列）** | 存储消息，等待消费者拉取 | `enrollment.queue` |
| **Binding（绑定）** | 交换机和队列的关联规则 | 通过 routing key `order.paid` 绑定 |
| **Consumer（消费者）** | 从队列接收并处理消息 | enrollment-service 的 OrderPaidConsumer |

---

### 3. Topic Exchange 与 Routing Key

本系统使用 **Topic Exchange**（主题交换机），支持基于 **routing key** 的模式匹配：

```java
// RabbitMQConfig.java (order-service)
public static final String ORDER_EXCHANGE = "order.exchange";
public static final String ENROLLMENT_QUEUE = "enrollment.queue";
public static final String ROUTING_KEY = "order.paid";

@Bean
public TopicExchange orderExchange() {
    return new TopicExchange(ORDER_EXCHANGE, true, false);  // durable, autoDelete
}

@Bean
public Binding binding(Queue enrollmentQueue, TopicExchange orderExchange) {
    return BindingBuilder
        .bind(enrollmentQueue)
        .to(orderExchange)
        .with(ROUTING_KEY);   // 精确匹配 order.paid
}
```

- **order.exchange**：Topic 类型，`durable=true`（重启不丢失），`autoDelete=false`
- **enrollment.queue**：持久化队列，`durable=true`
- **Binding**：routing key 为 `order.paid` 的消息会路由到 `enrollment.queue`

发送消息时指定 exchange 和 routing key：

```java
rabbitTemplate.convertAndSend(
    RabbitMQConfig.ORDER_EXCHANGE,   // order.exchange
    RabbitMQConfig.ROUTING_KEY,      // order.paid
    message                          // OrderPaidMessage 对象
);
```

---

### 4. 消息格式：OrderPaidMessage

消息体放在 **common 模块**，order-service 和 enrollment-service 共用，保证序列化/反序列化一致：

```java
// common/src/main/java/dev/kaiwen/common/message/OrderPaidMessage.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaidMessage {

    private Long orderId;
    private Long userId;
    private Long courseId;
    /** 课程有效天数，null 表示永久 */
    private Integer validDays;
}
```

使用 **Jackson2JsonMessageConverter** 将对象序列化为 JSON 发送，消费端自动反序列化为 `OrderPaidMessage`：

```java
@Bean
public MessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
}
```

---

### 5. 生产者：order-service 发送消息

支付成功后，在 `OrderService.payOrder()` 中发送 MQ 消息：

```java
// 7. 查询 validDays 发送 MQ
Result<CourseResponse> courseResult = courseServiceClient
        .getCourseById(order.getCourseId());
Integer validDays = courseResult != null && courseResult.getData() != null
        ? courseResult.getData().getValidDays()
        : null;

rabbitTemplate.convertAndSend(
        RabbitMQConfig.ORDER_EXCHANGE,
        RabbitMQConfig.ROUTING_KEY,
        new OrderPaidMessage(
                order.getId(),
                order.getUserId(),
                order.getCourseId(),
                validDays
        )
);
log.info("Order {} paid successfully", orderId);
```

**发送时机**：订单状态更新为 PAID、写入数据库**之后**，再发送 MQ。这样即使 MQ 短暂不可用，订单状态也已正确持久化，可后续补偿。

---

### 6. 消费者：enrollment-service 接收消息

enrollment-service 通过 `@RabbitListener` 监听队列：

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderPaidConsumer {

    private final EnrollmentService enrollmentService;

    @RabbitListener(queues = "enrollment.queue")
    public void handleOrderPaid(OrderPaidMessage message) {
        log.info("Received order paid message: {}", message);
        try {
            enrollmentService.createFromOrder(message);
        } catch (Exception e) {
            log.error("Failed to process order paid message: {}", message, e);
            throw e; // 抛出异常会触发重试
        }
    }
}
```

- `queues = "enrollment.queue"`：监听该队列
- 收到消息后调用 `createFromOrder(message)` 创建选课记录
- **抛出异常**：会触发 RabbitMQ 的**消息重试**（默认配置下），避免偶发异常导致消息丢失

---

### 7. 完整消息流转

```
用户调用 POST /api/v1/orders/{orderId}/pay
        │
        ▼
OrderService.payOrder()
        │
        ├── 1. 校验订单（归属、状态、超时）
        ├── 2. 扣减余额（付费课程）
        ├── 3. 订单状态 → PAID，保存
        ├── 4. 查询课程 validDays
        │
        ▼
rabbitTemplate.convertAndSend("order.exchange", "order.paid", OrderPaidMessage)
        │
        ▼
RabbitMQ: order.exchange 根据 routing key "order.paid" 路由
        │
        ▼
enrollment.queue 接收消息
        │
        ▼
OrderPaidConsumer.handleOrderPaid(OrderPaidMessage)
        │
        ▼
EnrollmentService.createFromOrder(message)
        │
        ├── 幂等校验：已存在则跳过
        ├── 创建 Enrollment 记录
        └── 设置 expireTime（若 validDays 非空）
```

---

### 8. 队列与 Exchange 的声明时机

| 服务 | 声明内容 | 说明 |
|------|----------|------|
| **order-service** | Exchange、Queue、Binding | 作为生产者，需要确保 exchange 和 queue 存在才能发送 |
| **enrollment-service** | Queue、MessageConverter | 作为消费者，只需声明自己监听的队列；queue 可被 order-service 先创建 |

两个服务都声明 `enrollment.queue` 是**幂等**的：RabbitMQ 会根据参数判断，同名且参数一致则复用，不会重复创建。

---

### 9. RabbitMQ 配置（application.yml）

order-service 和 enrollment-service 均需配置 RabbitMQ 连接（通常来自 Config Server）：

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: admin
    password: admin123
```

**启动顺序建议**：先启动 RabbitMQ，再启动 order-service（创建 exchange/queue），最后启动 enrollment-service（开始消费）。

---

## 三、Spring Cloud 微服务特点

### 1. 服务注册与发现（Eureka）

order-service 注册到 Eureka，Gateway 通过 `lb://order-service` 路由。

### 2. Feign 调用

| 客户端 | 调用服务 | 用途 |
|--------|----------|------|
| CourseServiceClient | course-service | 查询课程价格、validDays |
| UserServiceClient | user-service | 扣减用户余额 |

### 3. 网关路由

```yaml
- id: order-route
  uri: lb://order-service
  predicates:
    - Path=/api/v1/orders/**
  filters:
    - StripPrefix=2
```

---

## 四、文件结构

```
order-service/
├── pom.xml
├── src/main/
│   ├── java/dev/kaiwen/orderservice/
│   │   ├── OrderServiceApplication.java
│   │   ├── client/
│   │   │   ├── CourseServiceClient.java
│   │   │   └── UserServiceClient.java
│   │   ├── config/
│   │   │   ├── RabbitMQConfig.java      # RabbitMQ 配置（核心）
│   │   │   ├── SecurityConfig.java
│   │   │   └── OpenApiConfig.java
│   │   ├── controller/
│   │   │   └── OrderController.java
│   │   ├── dto/
│   │   │   ├── CreateOrderRequest.java
│   │   │   ├── OrderResponse.java
│   │   │   └── CourseResponse.java
│   │   ├── entity/
│   │   │   ├── Order.java
│   │   │   └── OrderStatus.java
│   │   ├── repository/
│   │   │   └── OrderRepository.java
│   │   └── service/
│   │       ├── OrderService.java
│   │       └── impl/
│   │           └── OrderServiceImpl.java
│   └── resources/
│       ├── application.yml
│       └── db/migration/
│           ├── V1__Init_Orders_Table.sql
│           └── V2__Add_Pay_Expire_Time.sql
```

---

## 五、业务流程

### 创建订单 → 支付 → 选课

| 步骤 | 接口 | 说明 |
|------|------|------|
| 1 | POST /api/v1/orders | 创建订单，状态 PENDING，返回 orderId、payExpireTime |
| 2 | POST /api/v1/orders/{orderId}/pay | 支付：扣余额 → PAID → 发送 RabbitMQ 消息 |
| 3 | （异步） | enrollment-service 消费消息，创建 enrollment 记录 |

### API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/orders` | POST | 创建订单 |
| `/orders/{orderId}/pay` | POST | 支付订单（余额扣款 + 发 MQ） |
| `/orders` | GET | 查询我的订单列表 |

---

## 六、Maven 依赖

| 依赖 | 作用 |
|------|------|
| spring-boot-starter-amqp | RabbitMQ 支持 |
| spring-cloud-starter-openfeign | 调用 course-service、user-service |
| spring-cloud-starter-netflix-eureka-client | 服务注册 |
| spring-boot-starter-data-jpa | 订单持久化 |
| common | Result、OrderPaidMessage、异常 |

---

## 七、总结

| 维度 | 内容 |
|------|------|
| **RabbitMQ** | Topic Exchange + Queue + Binding；order-service 生产，enrollment-service 消费；Jackson2Json 序列化 |
| **消息** | OrderPaidMessage（orderId, userId, courseId, validDays） |
| **流程** | 创建订单 → 支付（扣款）→ 发 MQ → 异步创建选课 |
| **解耦** | 订单与选课通过 MQ 异步联动，支付接口不依赖 enrollment-service 可用性 |

Order Service 是典型的 **生产者** 角色：完成订单业务后，通过 RabbitMQ 将「支付成功」事件广播出去，由 enrollment-service 异步处理选课逻辑，实现服务间松耦合。
