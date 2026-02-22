package dev.kaiwen.enrollmentservice.entity;

import io.hypersistence.tsid.TSID;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EnrollmentStatus status = EnrollmentStatus.NOT_STARTED;

    @Column(name = "learned_sections", nullable = false)
    private Integer learnedSections = 0;

    @Column(name = "latest_section_id")
    private Long latestSectionId;

    @Column(name = "latest_learn_time")
    private Instant latestLearnTime;

    @Column(name = "expire_time")
    private Instant expireTime;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = TSID.fast().toLong();
        }
    }
}
