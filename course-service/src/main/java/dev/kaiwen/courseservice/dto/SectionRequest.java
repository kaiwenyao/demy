package dev.kaiwen.courseservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "小节请求")
@Getter
@Setter
public class SectionRequest {

    @NotBlank
    @Schema(description = "小节标题")
    private String title;

    @Schema(description = "时长（秒）")
    private Integer duration;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "是否免费试看")
    private Boolean isFree = false;
}
