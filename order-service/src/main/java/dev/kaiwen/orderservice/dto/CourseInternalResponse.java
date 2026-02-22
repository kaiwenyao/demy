package dev.kaiwen.orderservice.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Feign 调用 course-service 内部接口返回的 DTO。
 * 与 course-service 的 CourseInternalResponse 字段结构保持一致（契约隐式一致）。
 */
@Data
public class CourseInternalResponse {

    private Long id;
    private BigDecimal price;
    private Integer validDays;
    private String status;
}
