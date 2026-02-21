package dev.kaiwen.gatewayservice.util;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 统一 Result 格式错误响应工具。
 */
public final class ResultResponseUtil {

    private ResultResponseUtil() {
    }

    /**
     * 以 Result 格式写入错误响应并完成。
     */
    public static Mono<Void> writeError(ServerWebExchange exchange, HttpStatus status, String msg) {
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        exchange.getResponse().setStatusCode(status);

        String body = """
            {"code":%d,"msg":"%s","data":null}
            """.formatted(status.value(), escapeJson(msg));

        DataBuffer buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
