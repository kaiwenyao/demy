package dev.kaiwen.enrollmentservice.controller;

import dev.kaiwen.common.exception.BadRequestException;
import dev.kaiwen.common.response.PageDto;
import dev.kaiwen.common.response.Result;
import dev.kaiwen.enrollmentservice.dto.EnrollmentVo;
import dev.kaiwen.enrollmentservice.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课表相关接口
 */
@RestController
@RequestMapping("/enrollments")
@RequiredArgsConstructor
@Tag(name = "Enrollment", description = "课表/课程注册管理")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping("/page")
    @Operation(summary = "分页查询我的课表", description = "分页查询当前用户的课表，按更新时间倒序")
    @ApiResponse(responseCode = "200", description = "查询成功")
    @ApiResponse(responseCode = "400", description = "缺少 X-User-Id 请求头")
    public Result<PageDto<EnrollmentVo>> getMyEnrollments(
            @RequestHeader(value = "X-User-Id", required = false)
            @Parameter(description = "当前用户 ID（网关鉴权后注入）", required = true) Long userId,
            @RequestParam(defaultValue = "0")
            @Parameter(description = "页码，从 0 开始") int page,
            @RequestParam(defaultValue = "10")
            @Parameter(description = "每页大小") int size) {
        validateUserId(userId);
        PageDto<EnrollmentVo> result = enrollmentService.getMyEnrollments(userId, page, size);
        return Result.success(result);
    }

    @GetMapping("/now")
    @Operation(summary = "查询最近正在学习的课程", description = "查询当前用户最近正在学习的课程列表（status=1）")
    @ApiResponse(responseCode = "200", description = "查询成功")
    @ApiResponse(responseCode = "400", description = "缺少 X-User-Id 请求头")
    public Result<List<EnrollmentVo>> getMyCurrentLearning(
            @RequestHeader(value = "X-User-Id", required = false)
            @Parameter(description = "当前用户 ID", required = true) Long userId) {
        validateUserId(userId);
        List<EnrollmentVo> result = enrollmentService.getMyCurrentLearning(userId);
        return Result.success(result);
    }

    @GetMapping("/{courseId}")
    @Operation(summary = "查询指定课程的学习状态", description = "根据课程 ID 查询当前用户该课程的学习状态")
    @ApiResponse(responseCode = "200", description = "查询成功")
    @ApiResponse(responseCode = "400", description = "缺少 X-User-Id 请求头")
    @ApiResponse(responseCode = "404", description = "课程不在课表中")
    public Result<EnrollmentVo> getEnrollmentByCourseId(
            @RequestHeader(value = "X-User-Id", required = false)
            @Parameter(description = "当前用户 ID", required = true) Long userId,
            @PathVariable @Parameter(description = "课程 ID") Long courseId) {
        validateUserId(userId);
        EnrollmentVo result = enrollmentService.getEnrollmentByCourseId(userId, courseId);
        return Result.success(result);
    }

    @DeleteMapping("/{courseId}")
    @Operation(summary = "删除课表中的课程", description = "从课表中移除指定课程")
    @ApiResponse(responseCode = "200", description = "删除成功")
    @ApiResponse(responseCode = "400", description = "缺少 X-User-Id 请求头")
    @ApiResponse(responseCode = "404", description = "课程不在课表中")
    public Result<Void> deleteEnrollment(
            @RequestHeader(value = "X-User-Id", required = false)
            @Parameter(description = "当前用户 ID", required = true) Long userId,
            @PathVariable @Parameter(description = "课程 ID") Long courseId) {
        validateUserId(userId);
        enrollmentService.deleteEnrollment(userId, courseId);
        return Result.success();
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new BadRequestException("X-User-Id header is required");
        }
    }
}
