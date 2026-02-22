package dev.kaiwen.courseservice.controller;

import dev.kaiwen.common.response.PageDto;
import dev.kaiwen.common.response.Result;
import dev.kaiwen.courseservice.dto.*;
import dev.kaiwen.courseservice.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Tag(name = "Course", description = "课程管理")
public class CourseController {

    private final CourseService courseService;

    // ===== 公开接口 =====

    @GetMapping
    @Operation(summary = "分页查询课程")
    public Result<PageDto<CourseResponse>> findAll(
            @RequestParam(required = false) @Parameter(description = "分类筛选") String category,
            @ParameterObject Pageable pageable) {
        return Result.success(courseService.findAll(category, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询课程详情")
    public Result<CourseResponse> findById(@PathVariable @Parameter(description = "课程 ID") Long id) {
        return Result.success(courseService.findById(id));
    }

    // ===== 管理员接口 =====

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "创建课程（管理员）")
    public Result<CourseResponse> create(
            @RequestBody @Valid CourseRequest request,
            @RequestHeader("X-User-Id") Long adminId,
            @RequestHeader("X-User-Role") String role) {
        checkAdmin(role);
        return Result.success(courseService.create(request, adminId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新课程（管理员）")
    public Result<CourseResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid CourseRequest request,
            @RequestHeader("X-User-Id") Long adminId,
            @RequestHeader("X-User-Role") String role) {
        checkAdmin(role);
        return Result.success(courseService.update(id, request, adminId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "下架课程（管理员）")
    public Result<Void> deactivate(
            @PathVariable Long id,
            @RequestHeader("X-User-Role") String role) {
        checkAdmin(role);
        courseService.deactivate(id);
        return Result.success();
    }

    @PostMapping("/{id}/sections")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "添加小节（管理员）")
    public Result<SectionResponse> addSection(
            @PathVariable Long id,
            @RequestBody @Valid SectionRequest request,
            @RequestHeader("X-User-Role") String role) {
        checkAdmin(role);
        return Result.success(courseService.addSection(id, request));
    }

    // ===== 私有方法 =====

    private void checkAdmin(String role) {
        if (!"ROLE_ADMIN".equals(role)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Admin access required");
        }
    }
}
