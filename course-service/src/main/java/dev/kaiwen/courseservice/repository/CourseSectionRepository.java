package dev.kaiwen.courseservice.repository;

import dev.kaiwen.courseservice.entity.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseSectionRepository extends JpaRepository<CourseSection, Long> {

    List<CourseSection> findByCourseIdOrderBySortOrderAsc(Long courseId);

    int countByCourseId(Long courseId);
}
