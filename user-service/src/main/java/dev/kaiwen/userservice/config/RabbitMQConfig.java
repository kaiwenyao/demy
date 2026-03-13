package dev.kaiwen.userservice.config;

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

    // 接收 order-service 的扣款请求
    public static final String USER_EXCHANGE = "user.exchange";
    public static final String PAYMENT_REQUEST_QUEUE = "payment.request.queue";
    public static final String PAYMENT_REQUEST_ROUTING_KEY = "user.payment.request";

    // 发送扣款结果回 order-service
    public static final String PAYMENT_RESULT_EXCHANGE = "payment.result.exchange";
    public static final String PAYMENT_RESULT_ROUTING_KEY = "payment.result";

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE, true, false);
    }

    @Bean
    public Queue paymentRequestQueue() {
        return new Queue(PAYMENT_REQUEST_QUEUE, true);
    }

    @Bean
    public Binding paymentRequestBinding() {
        return BindingBuilder
                .bind(paymentRequestQueue())
                .to(userExchange())
                .with(PAYMENT_REQUEST_ROUTING_KEY);
    }

    @Bean
    public TopicExchange paymentResultExchange() {
        return new TopicExchange(PAYMENT_RESULT_EXCHANGE, true, false);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

