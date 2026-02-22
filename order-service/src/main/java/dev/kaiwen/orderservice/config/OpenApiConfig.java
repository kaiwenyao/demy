package dev.kaiwen.orderservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .description("在线教育平台 - 订单服务")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Kaiwen")
                                .email("your@email.com")));
    }
}
