package dev.kaiwen.enrollmentservice.service.impl;

import dev.kaiwen.common.exception.ResourceNotFoundException;
import dev.kaiwen.common.response.PageDto;
import dev.kaiwen.enrollmentservice.dto.EnrollmentVo;
import dev.kaiwen.enrollmentservice.entity.Enrollment;
import dev.kaiwen.enrollmentservice.entity.EnrollmentStatus;
import dev.kaiwen.enrollmentservice.repository.EnrollmentRepository;
import dev.kaiwen.enrollmentservice.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private static final int CURRENT_LEARNING_LIMIT = 10;

    private final EnrollmentRepository enrollmentRepository;

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
