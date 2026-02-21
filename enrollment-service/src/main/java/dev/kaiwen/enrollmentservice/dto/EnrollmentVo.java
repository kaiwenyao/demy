package dev.kaiwen.enrollmentservice.dto;

import dev.kaiwen.enrollmentservice.entity.Enrollment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Schema(description = "课表/选课记录响应体")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentVo {

    @Schema(description = "选课记录 ID")
    private Long id;

    @Schema(description = "用户 ID")
    private Long userId;

    @Schema(description = "课程 ID")
    private Long courseId;

    @Schema(description = "课程状态：0-未学习，1-学习中，2-已学完，3-已失效")
    private Integer status;

    @Schema(description = "每周学习频率")
    private Integer weekFreq;

    @Schema(description = "学习计划状态：0-没有计划，1-计划进行中")
    private Integer planStatus;

    @Schema(description = "已学习小节数量")
    private Integer learnedSections;

    @Schema(description = "最近一次学习的小节 ID")
    private Long latestSectionId;

    @Schema(description = "最近一次学习时间")
    private Instant latestLearnTime;

    @Schema(description = "创建时间")
    private Instant createTime;

    @Schema(description = "过期时间")
    private Instant expireTime;

    @Schema(description = "更新时间")
    private Instant updateTime;

    public static EnrollmentVo from(Enrollment e) {
        if (e == null) {
            return null;
        }
        return EnrollmentVo.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .courseId(e.getCourseId())
                .status(e.getStatus())
                .weekFreq(e.getWeekFreq())
                .planStatus(e.getPlanStatus())
                .learnedSections(e.getLearnedSections())
                .latestSectionId(e.getLatestSectionId())
                .latestLearnTime(e.getLatestLearnTime())
                .createTime(e.getCreateTime())
                .expireTime(e.getExpireTime())
                .updateTime(e.getUpdateTime())
                .build();
    }
}
