package com.edulearn.assessment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.edulearn.assessment.entity.Question;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {

    List<Question> findByQuizId(Integer quizId);

    List<Question> findByQuizIdOrderByOrderIndex(Integer quizId);

    int countByQuizId(Integer quizId);

    void deleteByQuizId(Integer quizId);
}

