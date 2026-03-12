package dev.kaiwen.courseservice.mapper;

import dev.kaiwen.courseservice.dto.CourseResponse;
import dev.kaiwen.courseservice.entity.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    @Mapping(target = "sections", ignore = true)
    CourseResponse courseToResponse(Course course);
}

