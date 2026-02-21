package dev.kaiwen.enrollmentservice.repository;

import dev.kaiwen.enrollmentservice.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Page<Enrollment> findByUserIdOrderByUpdateTimeDesc(Long userId, Pageable pageable);

    List<Enrollment> findByUserIdAndStatusOrderByLatestLearnTimeDesc(Long userId, Integer status, Pageable pageable);

    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}
