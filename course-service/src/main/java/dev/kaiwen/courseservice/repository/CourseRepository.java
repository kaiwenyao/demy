package dev.kaiwen.courseservice.repository;

import dev.kaiwen.courseservice.entity.Course;
import dev.kaiwen.courseservice.entity.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Page<Course> findByStatus(CourseStatus status, Pageable pageable);

    Page<Course> findByStatusAndCategory(CourseStatus status, String category, Pageable pageable);

    Page<Course> findByInstructorId(Long instructorId, Pageable pageable);
}
