package dev.kaiwen.courseservice.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 内部接口返回的课程精简 DTO，供 order-service 等通过 Feign 调用时使用。
 * 与 order-service 的 CourseInternalResponse 字段结构保持一致（契约隐式一致）。
 */
@Data
public class CourseInternalResponse {

    private Long id;
    private BigDecimal price;
    private Integer validDays;
    private String status;
}
