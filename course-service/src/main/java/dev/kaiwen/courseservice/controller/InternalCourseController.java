package dev.kaiwen.courseservice.controller;

import dev.kaiwen.common.dto.CourseInternalResponse;
import dev.kaiwen.courseservice.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内部接口，仅供 order-service 等通过 Feign 调用。
 * 不对外暴露，Gateway 需屏蔽 /internal/** 路径。
 */
@RestController
@RequestMapping("/internal/courses")
@RequiredArgsConstructor
public class InternalCourseController {

    private final CourseService courseService;

    @GetMapping("/{id}")
    public CourseInternalResponse getById(@PathVariable Long id) {
        return courseService.findInternalById(id);
    }
}
