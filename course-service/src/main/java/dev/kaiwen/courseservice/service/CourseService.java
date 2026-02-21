package dev.kaiwen.courseservice.service;

import dev.kaiwen.common.response.PageDto;
import dev.kaiwen.courseservice.dto.*;
import org.springframework.data.domain.Pageable;

public interface CourseService {

    PageDto<CourseResponse> findAll(String category, Pageable pageable);

    CourseResponse findById(Long id);

    CourseResponse create(CourseRequest request, Long adminId);

    CourseResponse update(Long courseId, CourseRequest request, Long adminId);

    void deactivate(Long courseId);

    SectionResponse addSection(Long courseId, SectionRequest request);
}
