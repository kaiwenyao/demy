package dev.kaiwen.enrollmentservice.service.impl;

import dev.kaiwen.common.exception.ResourceNotFoundException;
import dev.kaiwen.common.message.OrderPaidMessage;
import dev.kaiwen.common.response.PageDto;
import dev.kaiwen.enrollmentservice.dto.EnrollmentVo;
import dev.kaiwen.enrollmentservice.entity.Enrollment;
import dev.kaiwen.enrollmentservice.entity.EnrollmentStatus;
import dev.kaiwen.enrollmentservice.repository.EnrollmentRepository;
import dev.kaiwen.enrollmentservice.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentServiceImpl implements EnrollmentService {

    private static final int CURRENT_LEARNING_LIMIT = 10;

    private final EnrollmentRepository enrollmentRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createFromOrder(OrderPaidMessage message) {
        // 防止重复消费
        if (enrollmentRepository.existsByUserIdAndCourseId(
                message.getUserId(), message.getCourseId())) {
            log.warn("Enrollment already exists, skip: {}", message);
            return;
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setUserId(message.getUserId());
        enrollment.setCourseId(message.getCourseId());
        enrollment.setStatus(EnrollmentStatus.NOT_STARTED);
        enrollment.setLearnedSections(0);

        // 计算过期时间
        if (message.getValidDays() != null) {
            enrollment.setExpireTime(
                    java.time.LocalDateTime.now()
                            .plusDays(message.getValidDays())
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
            );
        }

        enrollmentRepository.save(enrollment);
        log.info("Enrollment created for user {} course {}",
                message.getUserId(), message.getCourseId());
    }

    @Override
    public PageDto<EnrollmentVo> getMyEnrollments(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Enrollment> result = enrollmentRepository.findByUserIdOrderByUpdatedAtDesc(userId, pageable);
        List<EnrollmentVo> content = result.getContent().stream()
                .map(EnrollmentVo::from)
                .collect(Collectors.toList());
        return PageDto.of(content, result.getTotalElements(), page, size);
    }

    @Override
    public List<EnrollmentVo> getMyCurrentLearning(Long userId) {
        Pageable pageable = PageRequest.of(0, CURRENT_LEARNING_LIMIT);
        List<Enrollment> enrollments = enrollmentRepository
                .findByUserIdAndStatusOrderByLatestLearnTimeDesc(userId, EnrollmentStatus.IN_PROGRESS, pageable);
        return enrollments.stream()
                .map(EnrollmentVo::from)
                .collect(Collectors.toList());
    }

    @Override
    public EnrollmentVo getEnrollmentByCourseId(Long userId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found for course: " + courseId));
        return EnrollmentVo.from(enrollment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEnrollment(Long userId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found for course: " + courseId));
        enrollmentRepository.delete(enrollment);
    }
}
