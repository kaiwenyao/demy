package dev.kaiwen.courseservice.dto;

import dev.kaiwen.courseservice.entity.CourseLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Schema(description = "创建/更新课程请求")
@Getter
@Setter
public class CourseRequest {

    @NotBlank
    @Schema(description = "课程标题")
    private String title;

    @Schema(description = "课程描述")
    private String description;

    @Schema(description = "封面图 URL")
    private String coverImage;

    @NotNull
    @DecimalMin("0.00")
    @Schema(description = "价格，0 表示免费")
    private BigDecimal price;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "难度")
    private CourseLevel level;

    @Schema(description = "有效天数，不填表示永久有效")
    private Integer validDays;
}
