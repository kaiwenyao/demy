package dev.kaiwen.orderservice.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 课程信息 DTO，用于 Feign 调用 course-service，仅包含订单业务所需字段。
 */
@Data
public class CourseResponse {

    private Long id;
    private BigDecimal price;
    private Integer validDays;
    private String status;
}
