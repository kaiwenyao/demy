package dev.kaiwen.enrollmentservice.dto;

import dev.kaiwen.enrollmentservice.entity.Enrollment;
import dev.kaiwen.enrollmentservice.entity.EnrollmentStatus;
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

    @Schema(description = "学习状态：NOT_STARTED-未开始，IN_PROGRESS-学习中，COMPLETED-已学完，EXPIRED-已过期")
    private EnrollmentStatus status;

    @Schema(description = "已学习小节数量")
    private Integer learnedSections;

    @Schema(description = "最近一次学习的小节 ID")
    private Long latestSectionId;

    @Schema(description = "最近一次学习时间")
    private Instant latestLearnTime;

    @Schema(description = "过期时间，null 表示永久有效")
    private Instant expireTime;

    @Schema(description = "创建时间")
    private Instant createdAt;

    @Schema(description = "更新时间")
    private Instant updatedAt;

    public static EnrollmentVo from(Enrollment e) {
        if (e == null) {
            return null;
        }
        return EnrollmentVo.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .courseId(e.getCourseId())
                .status(e.getStatus())
                .learnedSections(e.getLearnedSections())
                .latestSectionId(e.getLatestSectionId())
                .latestLearnTime(e.getLatestLearnTime())
                .expireTime(e.getExpireTime())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
