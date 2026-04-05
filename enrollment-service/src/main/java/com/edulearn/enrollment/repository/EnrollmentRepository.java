package com.edulearn.enrollment.repository;

import com.edulearn.enrollment.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudentId(Long studentId);

    List<Enrollment> findByCourseId(Integer courseId);

    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Integer courseId);

    boolean existsByStudentIdAndCourseId(Long studentId, Integer courseId);

    int countByCourseId(Integer courseId);
}

