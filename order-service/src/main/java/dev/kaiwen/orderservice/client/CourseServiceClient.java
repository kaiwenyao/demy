package dev.kaiwen.orderservice.client;

import dev.kaiwen.common.dto.CourseInternalResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "course-service")
public interface CourseServiceClient {

    @GetMapping("/internal/courses/{id}")
    CourseInternalResponse getCourseById(@PathVariable("id") Long id);
}
