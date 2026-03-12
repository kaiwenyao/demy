package dev.kaiwen.enrollmentservice.controller;

import dev.kaiwen.common.response.PageDto;
import dev.kaiwen.common.response.Result;
import dev.kaiwen.enrollmentservice.dto.EnrollmentResponse;
import dev.kaiwen.enrollmentservice.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课表相关接口
 */
@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
@Tag(name = "Enrollment", description = "课表/课程注册管理")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping("/page")
    @Operation(summary = "分页查询我的课表", description = "分页查询当前用户的课表，按更新时间倒序")
    @ApiResponse(responseCode = "200", description = "查询成功")
    @ApiResponse(responseCode = "400", description = "缺少 X-User-Id 请求头")
    public Result<PageDto<EnrollmentResponse>> getMyEnrollments(
            @RequestHeader("X-User-Id") @Parameter(description = "当前用户 ID（网关鉴权后注入）") Long userId,
            @ParameterObject Pageable pageable) {
        PageDto<EnrollmentResponse> result = enrollmentService.getMyEnrollments(userId, pageable);
        return Result.success(result);
    }

    @GetMapping("/now")
    @Operation(summary = "查询最近正在学习的课程", description = "查询当前用户最近正在学习的课程列表（status=IN_PROGRESS）")
    @ApiResponse(responseCode = "200", description = "查询成功")
    @ApiResponse(responseCode = "400", description = "缺少 X-User-Id 请求头")
    public Result<List<EnrollmentResponse>> getMyCurrentLearning(
            @RequestHeader("X-User-Id") @Parameter(description = "当前用户 ID") Long userId) {
        List<EnrollmentResponse> result = enrollmentService.getMyCurrentLearning(userId);
        return Result.success(result);
    }

    @GetMapping("/{courseId}")
    @Operation(summary = "查询指定课程的学习状态", description = "根据课程 ID 查询当前用户该课程的学习状态")
    @ApiResponse(responseCode = "200", description = "查询成功")
    @ApiResponse(responseCode = "400", description = "缺少 X-User-Id 请求头")
    @ApiResponse(responseCode = "404", description = "课程不在课表中")
    public Result<EnrollmentResponse> getEnrollmentByCourseId(
            @RequestHeader("X-User-Id") @Parameter(description = "当前用户 ID") Long userId,
            @PathVariable @Parameter(description = "课程 ID") Long courseId) {
        EnrollmentResponse result = enrollmentService.getEnrollmentByCourseId(userId, courseId);
        return Result.success(result);
    }

    @DeleteMapping("/{courseId}")
    @Operation(summary = "删除课表中的课程", description = "从课表中移除指定课程")
    @ApiResponse(responseCode = "200", description = "删除成功")
    @ApiResponse(responseCode = "400", description = "缺少 X-User-Id 请求头")
    @ApiResponse(responseCode = "404", description = "课程不在课表中")
    public Result<Void> deleteEnrollment(
            @RequestHeader("X-User-Id") @Parameter(description = "当前用户 ID") Long userId,
            @PathVariable @Parameter(description = "课程 ID") Long courseId) {
        enrollmentService.deleteEnrollment(userId, courseId);
        return Result.success();
    }
}
