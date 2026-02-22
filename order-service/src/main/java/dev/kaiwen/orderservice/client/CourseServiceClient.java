package dev.kaiwen.orderservice.client;

import dev.kaiwen.common.response.Result;
import dev.kaiwen.orderservice.dto.CourseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "course-service")
public interface CourseServiceClient {

    @GetMapping("/courses/{id}")
    Result<CourseResponse> getCourseById(@PathVariable Long id);
}
