package dev.kaiwen.enrollmentservice.service;

import dev.kaiwen.common.message.OrderPaidMessage;
import dev.kaiwen.common.response.PageDto;
import dev.kaiwen.enrollmentservice.dto.EnrollmentVo;

import java.util.List;

public interface EnrollmentService {

    /**
     * 根据订单支付消息创建选课记录
     */
    void createFromOrder(OrderPaidMessage message);

    /**
     * 分页查询我的课表
     */
    PageDto<EnrollmentVo> getMyEnrollments(Long userId, int page, int size);

    /**
     * 查询我最近正在学习的课程
     */
    List<EnrollmentVo> getMyCurrentLearning(Long userId);

    /**
     * 根据课程 id 查询指定课程的学习状态
     */
    EnrollmentVo getEnrollmentByCourseId(Long userId, Long courseId);

    /**
     * 删除课表中的某课程
     */
    void deleteEnrollment(Long userId, Long courseId);
}
