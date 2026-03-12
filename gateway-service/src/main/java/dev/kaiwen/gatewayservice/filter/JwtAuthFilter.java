package dev.kaiwen.gatewayservice.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import dev.kaiwen.gatewayservice.util.ResultResponseUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String secret;

    // 不需要 Token 的路径白名单
    private static final List<String> WHITE_LIST = List.of(
        "/actuator",
        "/api/v1/auth/login",
        "/api/v1/auth/refresh",
        "/api/v1/users/register",
        "/swagger-ui",
        "/v3/api-docs"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();

        // 白名单路径或公开接口（GET 课程列表/详情）直接放行
        if (isWhitelisted(path, method)) {
            return chain.filter(exchange);
        }

        // 取 Authorization Header
        String authHeader = exchange.getRequest()
            .getHeaders()
            .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResultResponseUtil.writeError(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = parseToken(token);

            // 把 userId 和 role 写入 Header，转发给下游服务
            ServerWebExchange mutatedExchange = exchange.mutate()
                .request(r -> r
                    .header("X-User-Id", claims.getSubject())
                    .header("X-User-Role", claims.get("role", String.class))
                )
                .build();

            return chain.filter(mutatedExchange);

        } catch (Exception e) {
            return ResultResponseUtil.writeError(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
    }

    private Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private boolean isWhitelisted(String path, String method) {
        if (WHITE_LIST.stream().anyMatch(path::startsWith)) {
            return true;
        }
        // GET 课程列表和课程详情为公开接口，无需鉴权
        if ("GET".equalsIgnoreCase(method) && path.startsWith("/api/v1/courses")) {
            return true;
        }
        return false;
    }

    @Override
    public int getOrder() {
        // BlockInternalPathFilter 是 HIGHEST_PRECEDENCE
        // 这里设为 -1，确保在屏蔽内部路径之后、其他 Filter 之前执行
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
