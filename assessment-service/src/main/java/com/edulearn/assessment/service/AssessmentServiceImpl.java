package com.edulearn.assessment.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edulearn.assessment.entity.Attempt;
import com.edulearn.assessment.entity.Question;
import com.edulearn.assessment.entity.Quiz;
import com.edulearn.assessment.repository.AttemptRepository;
import com.edulearn.assessment.repository.QuestionRepository;
import com.edulearn.assessment.repository.QuizRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AssessmentServiceImpl implements AssessmentService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AttemptRepository attemptRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== QUIZ MANAGEMENT ====================

    @Override
    public Quiz createQuiz(Quiz quiz) {

        quiz.setCreatedAt(LocalDateTime.now());
        quiz.setUpdatedAt(LocalDateTime.now());
        return quizRepository.save(quiz);
    }

    @Override
    public Optional<Quiz> getQuizById(Integer quizId) {
        return quizRepository.findById(quizId);
    }

    @Override
    public List<Quiz> getQuizzesByCourse(Integer courseId) {
        return quizRepository.findByCourseId(courseId);
    }

    @Override
    public Quiz updateQuiz(Integer quizId, Quiz quiz) {
        Quiz existingQuiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with ID: " + quizId));

        existingQuiz.setTitle(quiz.getTitle());
        existingQuiz.setDescription(quiz.getDescription());
        existingQuiz.setTimeLimitMinutes(quiz.getTimeLimitMinutes());
        existingQuiz.setPassingScore(quiz.getPassingScore());
        existingQuiz.setMaxAttempts(quiz.getMaxAttempts());
        existingQuiz.setUpdatedAt(LocalDateTime.now());

        return quizRepository.save(existingQuiz);
    }

    @Override
    @Transactional
    public void deleteQuiz(Integer quizId) {
        // Delete all questions first
        questionRepository.deleteByQuizId(quizId);
        // Then delete the quiz
        quizRepository.deleteById(quizId);
    }

    @Override
    public void publishQuiz(Integer quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with ID: " + quizId));
        quiz.setIsPublished(true);
        quiz.setUpdatedAt(LocalDateTime.now());
        quizRepository.save(quiz);
    }

    // ==================== QUESTION MANAGEMENT ====================

    @Override
    public Question addQuestion(Integer quizId, Question question) {
        // Verify quiz exists
        quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with ID: " + quizId));

        question.setQuizId(quizId);
        return questionRepository.save(question);
    }

    @Override
    public List<Question> getQuestionsByQuiz(Integer quizId) {
        return questionRepository.findByQuizIdOrderByOrderIndex(quizId);
    }

    @Override
    public Question updateQuestion(Integer questionId, Question question) {
        Question existingQuestion = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with ID: " + questionId));

        existingQuestion.setQuestionText(question.getQuestionText());
        existingQuestion.setQuestionType(question.getQuestionType());
        existingQuestion.setOptions(question.getOptions());
        existingQuestion.setCorrectAnswer(question.getCorrectAnswer());
        existingQuestion.setMarks(question.getMarks());
        existingQuestion.setOrderIndex(question.getOrderIndex());

        return questionRepository.save(existingQuestion);
    }

    @Override
    public void deleteQuestion(Integer questionId) {
        questionRepository.deleteById(questionId);
    }

    // ==================== ATTEMPT MANAGEMENT ====================

    @Override
    public Attempt startAttempt(Integer quizId, Integer studentId) {
        // Step 1: Load quiz
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with ID: " + quizId));

        // Step 2: Count existing attempts
        int attemptCount = attemptRepository.countByStudentIdAndQuizId(studentId, quizId);

        // Step 3: Check max attempts
        if (attemptCount >= quiz.getMaxAttempts()) {
            throw new RuntimeException("Maximum attempts reached for this quiz");
        }

        // Step 4 & 5: Create and save attempt
        Attempt attempt = Attempt.builder()
                .quizId(quizId)
                .studentId(studentId)
                .score(0)
                .passed(false)
                .startedAt(LocalDateTime.now())
                .build();

        Attempt savedAttempt = attemptRepository.save(attempt);

        // Step 6: Store start time in Redis with TTL
        String redisKey = "quiz:attempt:" + savedAttempt.getAttemptId();
        String redisValue = savedAttempt.getStartedAt().toString();
        long ttlSeconds = quiz.getTimeLimitMinutes() * 60;

        redisTemplate.opsForValue().set(redisKey, redisValue, ttlSeconds, java.util.concurrent.TimeUnit.SECONDS);

        // Step 7: Return attempt
        return savedAttempt;
    }

    @Override
    public Attempt submitAttempt(Integer attemptId, Map<Integer, String> studentAnswers) {
        // Step 1: Load attempt
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found with ID: " + attemptId));

        // Step 2: Load quiz
        Quiz quiz = quizRepository.findById(attempt.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        // Step 3: Check Redis timer (if expired, that's OK, proceed anyway)
        String redisKey = "quiz:attempt:" + attemptId;
        Boolean timerExists = redisTemplate.hasKey(redisKey);
        // If timerExists is false, the timer has expired (but we still grade)

        // Step 4: Load all questions for this quiz
        List<Question> allQuestions = questionRepository.findByQuizId(attempt.getQuizId());

        // Step 5: Calculate total possible marks
        int totalMarks = allQuestions.stream()
                .mapToInt(Question::getMarks)
                .sum();

        // Step 6: AUTO-GRADING LOGIC - Calculate marks earned
        int marksEarned = 0;

        for (Question question : allQuestions) {
            Integer questionId = question.getQuestionId();
            String studentAnswer = studentAnswers.getOrDefault(questionId, "");

            if (studentAnswer == null || studentAnswer.trim().isEmpty()) {
                // Student didn't answer - 0 marks
                continue;
            }

            String correctAnswer = question.getCorrectAnswer();
            String questionType = question.getQuestionType();

            boolean isCorrect = false;

            if ("MCQ_SINGLE".equals(questionType) || "TRUE_FALSE".equals(questionType)) {
                // Exact match required
                isCorrect = studentAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
            } else if ("MCQ_MULTI".equals(questionType)) {
                // Multiple correct answers - split both by comma and compare
                List<String> studentAnswerList = Arrays.stream(studentAnswer.split(","))
                        .map(String::trim)
                        .sorted()
                        .collect(Collectors.toList());

                List<String> correctAnswerList = Arrays.stream(correctAnswer.split(","))
                        .map(String::trim)
                        .sorted()
                        .collect(Collectors.toList());

                isCorrect = studentAnswerList.equals(correctAnswerList);
            }

            if (isCorrect) {
                marksEarned += question.getMarks();
            }
        }

        // Step 7: Calculate score percentage
        int scorePercentage = totalMarks > 0 ? Math.round((float) marksEarned * 100 / totalMarks) : 0;

        // Step 8 & 9: Set score and passed flag
        attempt.setScore(scorePercentage);
        attempt.setPassed(scorePercentage >= quiz.getPassingScore());

        // Step 10: Set submission time
        attempt.setSubmittedAt(LocalDateTime.now());

        // Step 11: Calculate time taken in seconds
        long secondsTaken = ChronoUnit.SECONDS.between(attempt.getStartedAt(), attempt.getSubmittedAt());
        attempt.setTimeTaken((int) secondsTaken);

        // Step 12: Convert answers to JSON
        try {
            String answersJson = objectMapper.writeValueAsString(studentAnswers);
            attempt.setAnswers(answersJson);
        } catch (Exception e) {
            attempt.setAnswers("{}");
        }

        // Step 13: Delete Redis key
        redisTemplate.delete(redisKey);

        // Step 14: Save and return
        return attemptRepository.save(attempt);
    }

    @Override
    public List<Attempt> getAttemptsByStudent(Integer studentId) {
        return attemptRepository.findByStudentId(studentId);
    }

    @Override
    public List<Attempt> getAttemptsByQuiz(Integer quizId) {
        return attemptRepository.findByQuizId(quizId);
    }

    @Override
    public Optional<Attempt> getBestScore(Integer studentId, Integer quizId) {
        return attemptRepository.findTopByStudentIdAndQuizIdOrderByScoreDesc(studentId, quizId);
    }

    @Override
    public Optional<Attempt> getAttemptById(Integer attemptId) {
        return attemptRepository.findById(attemptId);
    }
}

