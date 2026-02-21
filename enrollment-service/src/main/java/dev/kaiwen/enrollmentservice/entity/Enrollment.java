package dev.kaiwen.enrollmentservice.entity;

import io.hypersistence.tsid.TSID;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * 选课/学习记录实体，对应 enrollments 表
 */
@Getter
@Setter
@Entity
@Table(name = "enrollments")
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {

    @Id
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    /**
     * 课程状态：0-未学习，1-学习中，2-已学完，3-已失效
     */
    @Column(name = "status", nullable = false, columnDefinition = "TINYINT")
    private Integer status = 0;

    @Column(name = "week_freq", columnDefinition = "TINYINT")
    private Integer weekFreq;

    /**
     * 学习计划状态：0-没有计划，1-计划进行中
     */
    @Column(name = "plan_status", nullable = false, columnDefinition = "TINYINT")
    private Integer planStatus = 0;

    @Column(name = "learned_sections", nullable = false)
    private Integer learnedSections = 0;

    @Column(name = "latest_section_id")
    private Long latestSectionId;

    @Column(name = "latest_learn_time")
    private Instant latestLearnTime;

    @Column(name = "create_time", nullable = false)
    private Instant createTime;

    @Column(name = "expire_time")
    private Instant expireTime;

    @Column(name = "update_time", nullable = false)
    private Instant updateTime;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = TSID.fast().toLong();
        }
        Instant now = Instant.now();
        if (createTime == null) {
            createTime = now;
        }
        if (updateTime == null) {
            updateTime = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = Instant.now();
    }
}
