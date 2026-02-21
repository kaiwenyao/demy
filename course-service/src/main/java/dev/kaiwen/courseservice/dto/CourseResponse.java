package dev.kaiwen.courseservice.dto;

import dev.kaiwen.courseservice.entity.CourseLevel;
import dev.kaiwen.courseservice.entity.CourseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "课程响应")
@Getter
@Setter
public class CourseResponse {

    private Long id;
    private String title;
    private String description;
    private String coverImage;
    private BigDecimal price;
    private String category;
    private Long instructorId;
    private CourseLevel level;
    private Integer sectionCount;
    private CourseStatus status;
    private Integer validDays;
    private LocalDateTime createdAt;
    @Schema(description = "小节列表，仅详情页填充")
    private List<SectionResponse> sections;
}
