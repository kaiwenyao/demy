package dev.kaiwen.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "创建订单请求")
public class CreateOrderRequest {

    @NotNull(message = "courseId is required")
    @Schema(description = "课程 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long courseId;
}
