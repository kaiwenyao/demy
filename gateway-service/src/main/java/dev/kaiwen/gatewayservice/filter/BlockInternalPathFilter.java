package dev.kaiwen.gatewayservice.filter;

import dev.kaiwen.gatewayservice.util.ResultResponseUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 屏蔽 /internal/** 路径，不允许外部通过 Gateway 直接调用内部接口。
 */
@Component
public class BlockInternalPathFilter implements GlobalFilter, Ordered {

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
        return Ordered.HIGHEST_PRECEDENCE; // 最高优先级，最先执行
    }
}
