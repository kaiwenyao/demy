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
  public MessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}
