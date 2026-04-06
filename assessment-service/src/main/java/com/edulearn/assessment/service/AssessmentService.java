package com.edulearn.assessment.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.edulearn.assessment.entity.Attempt;
import com.edulearn.assessment.entity.Question;
import com.edulearn.assessment.entity.Quiz;

public interface AssessmentService {

    // ==================== QUIZ MANAGEMENT ====================
    Quiz createQuiz(Quiz quiz);

    Optional<Quiz> getQuizById(Integer quizId);

    List<Quiz> getQuizzesByCourse(Integer courseId);

    Quiz updateQuiz(Integer quizId, Quiz quiz);

    void deleteQuiz(Integer quizId);

    void publishQuiz(Integer quizId);

    // ==================== QUESTION MANAGEMENT ====================
    Question addQuestion(Integer quizId, Question question);

    List<Question> getQuestionsByQuiz(Integer quizId);

    Question updateQuestion(Integer questionId, Question question);

    void deleteQuestion(Integer questionId);

    // ==================== ATTEMPT MANAGEMENT ====================
    Attempt startAttempt(Integer quizId, Integer studentId);

    Attempt submitAttempt(Integer attemptId, Map<Integer, String> studentAnswers);

    List<Attempt> getAttemptsByStudent(Integer studentId);

    List<Attempt> getAttemptsByQuiz(Integer quizId);

    Optional<Attempt> getBestScore(Integer studentId, Integer quizId);

    Optional<Attempt> getAttemptById(Integer attemptId);
}

