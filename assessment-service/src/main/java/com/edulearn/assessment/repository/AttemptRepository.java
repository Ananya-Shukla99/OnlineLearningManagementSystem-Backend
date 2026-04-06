package com.edulearn.assessment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.edulearn.assessment.entity.Attempt;

@Repository
public interface AttemptRepository extends JpaRepository<Attempt, Integer> {

    List<Attempt> findByStudentId(Integer studentId);

    List<Attempt> findByQuizId(Integer quizId);

    List<Attempt> findByStudentIdAndQuizId(Integer studentId, Integer quizId);

    int countByStudentIdAndQuizId(Integer studentId, Integer quizId);

    Optional<Attempt> findTopByStudentIdAndQuizIdOrderByScoreDesc(Integer studentId, Integer quizId);
}

