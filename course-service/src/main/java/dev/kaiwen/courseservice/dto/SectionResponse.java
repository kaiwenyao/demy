package dev.kaiwen.courseservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "小节响应")
@Getter
@Setter
public class SectionResponse {

    private Long id;
    private String title;
    private Integer duration;
    private Integer sortOrder;
    private Boolean isFree;
}
