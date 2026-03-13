package dev.kaiwen.orderservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  public static final String ORDER_EXCHANGE = "order.exchange";
  public static final String ENROLLMENT_QUEUE = "enrollment.queue";
  public static final String ROUTING_KEY = "order.paid";

  // 接收 user-service 的扣款结果
  public static final String PAYMENT_RESULT_EXCHANGE = "payment.result.exchange";
  public static final String PAYMENT_RESULT_QUEUE = "payment.result.queue";
  public static final String PAYMENT_RESULT_ROUTING_KEY = "payment.result";

  public static final String USER_EXCHANGE = "user.exchange";
  public static final String PAYMENT_REQUEST_ROUTING_KEY = "user.payment.request";

  @Bean
  public TopicExchange orderExchange() {
    return new TopicExchange(ORDER_EXCHANGE, true, false);
  }

  @Bean
  public Queue enrollmentQueue() {
    return new Queue(ENROLLMENT_QUEUE, true);
  }

  @Bean
  public Binding binding(Queue enrollmentQueue, TopicExchange orderExchange) {
    return BindingBuilder
        .bind(enrollmentQueue)
        .to(orderExchange)
        .with(ROUTING_KEY);
  }

  @Bean
  public TopicExchange paymentResultExchange() {
    return new TopicExchange(PAYMENT_RESULT_EXCHANGE, true, false);
  }

  @Bean
  public Queue paymentResultQueue() {
    return new Queue(PAYMENT_RESULT_QUEUE, true);
  }

  @Bean
  public Binding paymentResultBinding() {
    return BindingBuilder
        .bind(paymentResultQueue())
        .to(paymentResultExchange())
        .with(PAYMENT_RESULT_ROUTING_KEY);
  }

  @Bean
  public MessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}