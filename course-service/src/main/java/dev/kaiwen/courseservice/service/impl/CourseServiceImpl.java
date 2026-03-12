package dev.kaiwen.courseservice.service.impl;

import dev.kaiwen.common.exception.ResourceNotFoundException;
import dev.kaiwen.common.response.PageDto;
import dev.kaiwen.courseservice.dto.*;
import dev.kaiwen.courseservice.entity.Course;
import dev.kaiwen.courseservice.entity.CourseSection;
import dev.kaiwen.courseservice.entity.CourseStatus;
import dev.kaiwen.courseservice.repository.CourseRepository;
import dev.kaiwen.courseservice.repository.CourseSectionRepository;
import dev.kaiwen.courseservice.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseSectionRepository sectionRepository;

    @Cacheable(value = "courses", key = "(#category ?: 'all') + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Override
    public PageDto<CourseResponse> findAll(String category, Pageable pageable) {
        Page<Course> page = (category != null && !category.isBlank())
                ? courseRepository.findByStatusAndCategory(CourseStatus.ACTIVE, category, pageable)
                : courseRepository.findByStatus(CourseStatus.ACTIVE, pageable);
        return PageDto.from(page.map(c -> toResponse(c, false)));
    }

    @Override
    public CourseResponse findById(Long id) {
        Course course = findCourseById(id);
        if (course.getStatus() != CourseStatus.ACTIVE) {
            throw new ResourceNotFoundException("Course not found: " + id);
        }
        return toResponse(course, true);
    }

    @Override
    public CourseInternalResponse findInternalById(Long id) {
        Course course = findCourseById(id);
        CourseInternalResponse r = new CourseInternalResponse();
        r.setId(course.getId());
        r.setPrice(course.getPrice());
        r.setValidDays(course.getValidDays());
        r.setStatus(course.getStatus() != null ? course.getStatus().name() : null);
        return r;
    }

    @Override
    public CourseResponse create(CourseRequest request, Long adminId) {
        Course course = new Course();
        fillCourse(course, request);
        course.setInstructorId(adminId);
        course.setStatus(CourseStatus.ACTIVE);
        return toResponse(courseRepository.save(course), false);
    }

    @Override
    public CourseResponse update(Long courseId, CourseRequest request, Long adminId) {
        Course course = findCourseById(courseId);
        fillCourse(course, request);
        return toResponse(courseRepository.save(course), false);
    }

    @Override
    public void deactivate(Long courseId) {
        Course course = findCourseById(courseId);
        course.setStatus(CourseStatus.INACTIVE);
        courseRepository.save(course);
    }

    @Override
    public SectionResponse addSection(Long courseId, SectionRequest request) {
        Course course = findCourseById(courseId);

        CourseSection section = new CourseSection();
        section.setCourseId(courseId);
        section.setTitle(request.getTitle());
        section.setDuration(request.getDuration());
        section.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        section.setIsFree(request.getIsFree() != null ? request.getIsFree() : false);
        sectionRepository.save(section);

        course.setSectionCount(sectionRepository.countByCourseId(courseId));
        courseRepository.save(course);

        return toSectionResponse(section);
    }

    private Course findCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + id));
    }

    private void fillCourse(Course course, CourseRequest request) {
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setCoverImage(request.getCoverImage());
        course.setPrice(request.getPrice());
        course.setCategory(request.getCategory());
        course.setLevel(request.getLevel());
        course.setValidDays(request.getValidDays());
    }

    private CourseResponse toResponse(Course course, boolean withSections) {
        CourseResponse response = new CourseResponse();
        response.setId(course.getId());
        response.setTitle(course.getTitle());
        response.setDescription(course.getDescription());
        response.setCoverImage(course.getCoverImage());
        response.setPrice(course.getPrice());
        response.setCategory(course.getCategory());
        response.setInstructorId(course.getInstructorId());
        response.setLevel(course.getLevel());
        response.setSectionCount(course.getSectionCount());
        response.setStatus(course.getStatus());
        response.setValidDays(course.getValidDays());
        response.setCreatedAt(course.getCreatedAt());

        response.setSections(withSections
                ? sectionRepository.findByCourseIdOrderBySortOrderAsc(course.getId())
                        .stream().map(this::toSectionResponse).toList()
                : Collections.emptyList());

        return response;
    }

    private SectionResponse toSectionResponse(CourseSection section) {
        SectionResponse response = new SectionResponse();
        response.setId(section.getId());
        response.setTitle(section.getTitle());
        response.setDuration(section.getDuration());
        response.setSortOrder(section.getSortOrder());
        response.setIsFree(section.getIsFree());
        return response;
    }
}
