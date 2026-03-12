package dev.kaiwen.common.dto;

import java.math.BigDecimal;
import lombok.Data;

/**
 * course-service 对内部消费者暴露的课程精简 DTO。
 */
@Data
public class CourseInternalResponse {

  private Long id;
  private BigDecimal price;
  private Integer validDays;
  private String status;
}
