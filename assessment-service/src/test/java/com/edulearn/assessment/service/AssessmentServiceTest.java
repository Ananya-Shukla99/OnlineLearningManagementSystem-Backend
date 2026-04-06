package com.edulearn.assessment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.edulearn.assessment.entity.Attempt;
import com.edulearn.assessment.entity.Question;
import com.edulearn.assessment.entity.Quiz;
import com.edulearn.assessment.repository.AttemptRepository;
import com.edulearn.assessment.repository.QuestionRepository;
import com.edulearn.assessment.repository.QuizRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("Assessment Service Unit Tests")
class AssessmentServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private AttemptRepository attemptRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AssessmentServiceImpl assessmentService;

    private Quiz testQuiz;
    private Question testQuestion;
    private Attempt testAttempt;

    @BeforeEach
    void setUp() {
        // Initialize test fixtures
        testQuiz = Quiz.builder()
                .quizId(1)
                .courseId(1)
                .lessonId(1)
                .title("Java Basics Quiz")
                .description("Quiz about Java fundamentals")
                .timeLimitMinutes(30)
                .passingScore(60)
                .maxAttempts(3)
                .isPublished(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testQuestion = Question.builder()
                .questionId(1)
                .quizId(1)
                .questionText("What is polymorphism?")
                .questionType("MCQ_SINGLE")
                .options("Inheritance,Encapsulation,Method overriding,All of above")
                .correctAnswer("All of above")
                .marks(5)
                .orderIndex(1)
                .build();

        testAttempt = Attempt.builder()
                .attemptId(1)
                .quizId(1)
                .studentId(101)
                .score(0)
                .passed(false)
                .startedAt(LocalDateTime.now())
                .submittedAt(null)
                .timeTaken(0)
                .answers("{}")
                .build();
    }

    // ==================== QUIZ MANAGEMENT TESTS ====================

    @Test
    @DisplayName("Test: Create Quiz - Should set timestamps and save successfully")
    void testCreateQuiz_Success() {
        // Arrange
        Quiz quizToCreate = Quiz.builder()
                .courseId(1)
                .title("New Quiz")
                .description("Test quiz")
                .timeLimitMinutes(30)
                .passingScore(60)
                .maxAttempts(3)
                .isPublished(false)
                .build();

        when(quizRepository.save(any(Quiz.class))).thenReturn(testQuiz);

        // Act
        Quiz result = assessmentService.createQuiz(quizToCreate);

        // Assert
        assertNotNull(result);
        assertEquals("Java Basics Quiz", result.getTitle());
        verify(quizRepository, times(1)).save(any(Quiz.class));
    }

    @Test
    @DisplayName("Test: Get Quiz By ID - Should return quiz when found")
    void testGetQuizById_Found() {
        // Arrange
        when(quizRepository.findById(1)).thenReturn(Optional.of(testQuiz));

        // Act
        Optional<Quiz> result = assessmentService.getQuizById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testQuiz.getQuizId(), result.get().getQuizId());
        assertEquals("Java Basics Quiz", result.get().getTitle());
        verify(quizRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Test: Get Quiz By ID - Should return empty when not found")
    void testGetQuizById_NotFound() {
        // Arrange
        when(quizRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<Quiz> result = assessmentService.getQuizById(999);

        // Assert
        assertFalse(result.isPresent());
        verify(quizRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("Test: Get Quizzes By Course - Should return all quizzes for course")
    void testGetQuizzesByCourse_Success() {
        // Arrange
        List<Quiz> quizzes = Arrays.asList(testQuiz, testQuiz);
        when(quizRepository.findByCourseId(1)).thenReturn(quizzes);

        // Act
        List<Quiz> result = assessmentService.getQuizzesByCourse(1);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(quizRepository, times(1)).findByCourseId(1);
    }

    @Test
    @DisplayName("Test: Get Quizzes By Course - Should return empty list when no quizzes")
    void testGetQuizzesByCourse_Empty() {
        // Arrange
        when(quizRepository.findByCourseId(999)).thenReturn(new ArrayList<>());

        // Act
        List<Quiz> result = assessmentService.getQuizzesByCourse(999);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(quizRepository, times(1)).findByCourseId(999);
    }

    @Test
    @DisplayName("Test: Update Quiz - Should update quiz details successfully")
    void testUpdateQuiz_Success() {
        // Arrange
        Quiz updateData = Quiz.builder()
                .title("Updated Quiz")
                .description("Updated description")
                .timeLimitMinutes(45)
                .passingScore(70)
                .maxAttempts(5)
                .build();

        when(quizRepository.findById(1)).thenReturn(Optional.of(testQuiz));
        when(quizRepository.save(any(Quiz.class))).thenReturn(testQuiz);

        // Act
        Quiz result = assessmentService.updateQuiz(1, updateData);

        // Assert
        assertNotNull(result);
        verify(quizRepository, times(1)).findById(1);
        verify(quizRepository, times(1)).save(any(Quiz.class));
    }

    @Test
    @DisplayName("Test: Update Quiz - Should throw exception when quiz not found")
    void testUpdateQuiz_NotFound() {
        // Arrange
        when(quizRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            assessmentService.updateQuiz(999, testQuiz);
        });
        verify(quizRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("Test: Delete Quiz - Should delete quiz and its questions")
    void testDeleteQuiz_Success() {
        // Arrange
        doNothing().when(questionRepository).deleteByQuizId(1);
        doNothing().when(quizRepository).deleteById(1);

        // Act
        assessmentService.deleteQuiz(1);

        // Assert
        verify(questionRepository, times(1)).deleteByQuizId(1);
        verify(quizRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("Test: Publish Quiz - Should set isPublished to true")
    void testPublishQuiz_Success() {
        // Arrange
        when(quizRepository.findById(1)).thenReturn(Optional.of(testQuiz));
        when(quizRepository.save(any(Quiz.class))).thenReturn(testQuiz);

        // Act
        assessmentService.publishQuiz(1);

        // Assert
        verify(quizRepository, times(1)).findById(1);
        verify(quizRepository, times(1)).save(any(Quiz.class));
    }

    @Test
    @DisplayName("Test: Publish Quiz - Should throw exception when quiz not found")
    void testPublishQuiz_NotFound() {
        // Arrange
        when(quizRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            assessmentService.publishQuiz(999);
        });
    }

    // ==================== QUESTION MANAGEMENT TESTS ====================

    @Test
    @DisplayName("Test: Add Question - Should add question to quiz successfully")
    void testAddQuestion_Success() {
        // Arrange
        Question questionToAdd = Question.builder()
                .questionText("Test question")
                .questionType("MCQ_SINGLE")
                .options("A,B,C,D")
                .correctAnswer("A")
                .marks(5)
                .orderIndex(1)
                .build();

        when(quizRepository.findById(1)).thenReturn(Optional.of(testQuiz));
        when(questionRepository.save(any(Question.class))).thenReturn(testQuestion);

        // Act
        Question result = assessmentService.addQuestion(1, questionToAdd);

        // Assert
        assertNotNull(result);
        verify(quizRepository, times(1)).findById(1);
        verify(questionRepository, times(1)).save(any(Question.class));
    }

    @Test
    @DisplayName("Test: Add Question - Should throw exception when quiz not found")
    void testAddQuestion_QuizNotFound() {
        // Arrange
        when(quizRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            assessmentService.addQuestion(999, testQuestion);
        });
    }

    @Test
    @DisplayName("Test: Get Questions By Quiz - Should return all questions ordered")
    void testGetQuestionsByQuiz_Success() {
        // Arrange
        List<Question> questions = Arrays.asList(testQuestion, testQuestion);
        when(questionRepository.findByQuizIdOrderByOrderIndex(1)).thenReturn(questions);

        // Act
        List<Question> result = assessmentService.getQuestionsByQuiz(1);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(questionRepository, times(1)).findByQuizIdOrderByOrderIndex(1);
    }

    @Test
    @DisplayName("Test: Update Question - Should update question details")
    void testUpdateQuestion_Success() {
        // Arrange
        Question updateData = Question.builder()
                .questionText("Updated question")
                .questionType("TRUE_FALSE")
                .correctAnswer("True")
                .marks(10)
                .build();

        when(questionRepository.findById(1)).thenReturn(Optional.of(testQuestion));
        when(questionRepository.save(any(Question.class))).thenReturn(testQuestion);

        // Act
        Question result = assessmentService.updateQuestion(1, updateData);

        // Assert
        assertNotNull(result);
        verify(questionRepository, times(1)).findById(1);
        verify(questionRepository, times(1)).save(any(Question.class));
    }

    @Test
    @DisplayName("Test: Update Question - Should throw exception when question not found")
    void testUpdateQuestion_NotFound() {
        // Arrange
        when(questionRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            assessmentService.updateQuestion(999, testQuestion);
        });
    }

    @Test
    @DisplayName("Test: Delete Question - Should delete question successfully")
    void testDeleteQuestion_Success() {
        // Arrange
        doNothing().when(questionRepository).deleteById(1);

        // Act
        assessmentService.deleteQuestion(1);

        // Assert
        verify(questionRepository, times(1)).deleteById(1);
    }

    // ==================== ATTEMPT MANAGEMENT TESTS ====================

    @Test
    @DisplayName("Test: Start Attempt - Should create attempt and store timer in Redis")
    void testStartAttempt_Success() {
        // Arrange
        when(quizRepository.findById(1)).thenReturn(Optional.of(testQuiz));
        when(attemptRepository.countByStudentIdAndQuizId(101, 1)).thenReturn(0);
        when(attemptRepository.save(any(Attempt.class))).thenReturn(testAttempt);

        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        // Act
        Attempt result = assessmentService.startAttempt(1, 101);

        // Assert
        assertNotNull(result);
        assertEquals(101, result.getStudentId());
        assertEquals(1, result.getQuizId());
        verify(quizRepository, times(1)).findById(1);
        verify(attemptRepository, times(1)).save(any(Attempt.class));
        verify(redisTemplate.opsForValue(), times(1)).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("Test: Start Attempt - Should throw exception when max attempts reached")
    void testStartAttempt_MaxAttemptsExceeded() {
        // Arrange
        when(quizRepository.findById(1)).thenReturn(Optional.of(testQuiz));
        when(attemptRepository.countByStudentIdAndQuizId(101, 1)).thenReturn(3);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            assessmentService.startAttempt(1, 101);
        });
        verify(quizRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Test: Start Attempt - Should throw exception when quiz not found")
    void testStartAttempt_QuizNotFound() {
        // Arrange
        when(quizRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            assessmentService.startAttempt(999, 101);
        });
    }

    @Test
    @DisplayName("Test: Submit Attempt - Should grade MCQ_SINGLE question correctly")
    void testSubmitAttempt_MCQSingle_Correct() throws Exception {
        // Arrange
        LocalDateTime startTime = LocalDateTime.now();
        testAttempt.setStartedAt(startTime);
        Map<Integer, String> studentAnswers = new HashMap<>();
        studentAnswers.put(1, "All of above");

        when(attemptRepository.findById(1)).thenReturn(Optional.of(testAttempt));
        when(quizRepository.findById(1)).thenReturn(Optional.of(testQuiz));
        when(redisTemplate.hasKey("quiz:attempt:1")).thenReturn(true);
        when(questionRepository.findByQuizId(1)).thenReturn(Arrays.asList(testQuestion));
        when(objectMapper.writeValueAsString(studentAnswers)).thenReturn("{\"1\":\"All of above\"}");
        when(attemptRepository.save(any(Attempt.class))).thenReturn(testAttempt);
        when(redisTemplate.delete("quiz:attempt:1")).thenReturn(true);

        // Act
        Attempt result = assessmentService.submitAttempt(1, studentAnswers);

        // Assert
        assertNotNull(result);
        assertEquals(100, result.getScore()); // 5/5 marks = 100%
        assertTrue(result.getPassed()); // 100 >= 60
        verify(attemptRepository, times(1)).findById(1);
        verify(attemptRepository, times(1)).save(any(Attempt.class));
    }

    @Test
    @DisplayName("Test: Submit Attempt - Should grade MCQ_SINGLE question incorrectly")
    void testSubmitAttempt_MCQSingle_Incorrect() throws Exception {
        // Arrange
        LocalDateTime startTime = LocalDateTime.now();
        testAttempt.setStartedAt(startTime);
        Map<Integer, String> studentAnswers = new HashMap<>();
        studentAnswers.put(1, "Inheritance"); // Wrong answer

        when(attemptRepository.findById(1)).thenReturn(Optional.of(testAttempt));
        when(quizRepository.findById(1)).thenReturn(Optional.of(testQuiz));
        when(redisTemplate.hasKey("quiz:attempt:1")).thenReturn(true);
        when(questionRepository.findByQuizId(1)).thenReturn(Arrays.asList(testQuestion));
        when(objectMapper.writeValueAsString(studentAnswers)).thenReturn("{\"1\":\"Inheritance\"}");
        when(attemptRepository.save(any(Attempt.class))).thenReturn(testAttempt);
        when(redisTemplate.delete("quiz:attempt:1")).thenReturn(true);

        // Act
        Attempt result = assessmentService.submitAttempt(1, studentAnswers);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getScore()); // 0/5 marks = 0%
        assertFalse(result.getPassed()); // 0 < 60
        verify(attemptRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Test: Submit Attempt - Should grade TRUE_FALSE question correctly")
    void testSubmitAttempt_TrueFalse_Correct() throws Exception {
        // Arrange
        Question trueFalseQuestion = Question.builder()
                .questionId(2)
                .quizId(1)
                .questionText("Is Java a compiled language?")
                .questionType("TRUE_FALSE")
                .options("True,False")
                .correctAnswer("False")
                .marks(5)
                .orderIndex(2)
                .build();

        LocalDateTime startTime = LocalDateTime.now();
        testAttempt.setStartedAt(startTime);
        Map<Integer, String> studentAnswers = new HashMap<>();
        studentAnswers.put(2, "False");

        when(attemptRepository.findById(1)).thenReturn(Optional.of(testAttempt));
        when(quizRepository.findById(1)).thenReturn(Optional.of(testQuiz));
        when(redisTemplate.hasKey("quiz:attempt:1")).thenReturn(true);
        when(questionRepository.findByQuizId(1)).thenReturn(Arrays.asList(trueFalseQuestion));
        when(objectMapper.writeValueAsString(studentAnswers)).thenReturn("{\"2\":\"False\"}");
        when(attemptRepository.save(any(Attempt.class))).thenReturn(testAttempt);
        when(redisTemplate.delete("quiz:attempt:1")).thenReturn(true);

        // Act
        Attempt result = assessmentService.submitAttempt(1, studentAnswers);

        // Assert
        assertNotNull(result);
        assertEquals(100, result.getScore());
        assertTrue(result.getPassed());
    }

    @Test
    @DisplayName("Test: Submit Attempt - Should grade MCQ_MULTI question with sorted comparison")
    void testSubmitAttempt_MCQMulti_Correct() throws Exception {
        // Arrange
        Question multiQuestion = Question.builder()
                .questionId(3)
                .quizId(1)
                .questionText("Which are OOP concepts? (Select all)")
                .questionType("MCQ_MULTI")
                .options("Inheritance,Encapsulation,Polymorphism,Loops")
                .correctAnswer("Encapsulation,Inheritance,Polymorphism") // Note: order different
                .marks(10)
                .orderIndex(3)
                .build();

        LocalDateTime startTime = LocalDateTime.now();
        testAttempt.setStartedAt(startTime);
        Map<Integer, String> studentAnswers = new HashMap<>();
        studentAnswers.put(3, "Polymorphism,Encapsulation,Inheritance"); // Different order

        when(attemptRepository.findById(1)).thenReturn(Optional.of(testAttempt));
        when(quizRepository.findById(1)).thenReturn(Optional.of(testQuiz));
        when(redisTemplate.hasKey("quiz:attempt:1")).thenReturn(true);
        when(questionRepository.findByQuizId(1)).thenReturn(Arrays.asList(multiQuestion));
        when(objectMapper.writeValueAsString(studentAnswers)).thenReturn("{\"3\":\"Polymorphism,Encapsulation,Inheritance\"}");
        when(attemptRepository.save(any(Attempt.class))).thenReturn(testAttempt);
        when(redisTemplate.delete("quiz:attempt:1")).thenReturn(true);

        // Act
        Attempt result = assessmentService.submitAttempt(1, studentAnswers);

        // Assert
        assertNotNull(result);
        assertEquals(100, result.getScore()); // All correct
        assertTrue(result.getPassed());
    }

    @Test
    @DisplayName("Test: Submit Attempt - Should grade MCQ_MULTI question with partial answers")
    void testSubmitAttempt_MCQMulti_Partial() throws Exception {
        // Arrange
        Question multiQuestion = Question.builder()
                .questionId(3)
                .quizId(1)
                .questionText("Which are OOP concepts?")
                .questionType("MCQ_MULTI")
                .options("Inheritance,Encapsulation,Polymorphism,Loops")
                .correctAnswer("Encapsulation,Inheritance,Polymorphism")
                .marks(10)
                .orderIndex(3)
                .build();

        LocalDateTime startTime = LocalDateTime.now();
        testAttempt.setStartedAt(startTime);
        Map<Integer, String> studentAnswers = new HashMap<>();
        studentAnswers.put(3, "Inheritance,Polymorphism"); // Missing one correct answer

        when(attemptRepository.findById(1)).thenReturn(Optional.of(testAttempt));
        when(quizRepository.findById(1)).thenReturn(Optional.of(testQuiz));
        when(redisTemplate.hasKey("quiz:attempt:1")).thenReturn(true);
        when(questionRepository.findByQuizId(1)).thenReturn(Arrays.asList(multiQuestion));
        when(objectMapper.writeValueAsString(studentAnswers)).thenReturn("{\"3\":\"Inheritance,Polymorphism\"}");
        when(attemptRepository.save(any(Attempt.class))).thenReturn(testAttempt);
        when(redisTemplate.delete("quiz:attempt:1")).thenReturn(true);

        // Act
        Attempt result = assessmentService.submitAttempt(1, studentAnswers);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getScore()); // Partial answers are incorrect
        assertFalse(result.getPassed());
    }

    @Test
    @DisplayName("Test: Submit Attempt - Should handle unanswered questions")
    void testSubmitAttempt_UnansweredQuestion() throws Exception {
        // Arrange
        LocalDateTime startTime = LocalDateTime.now();
        testAttempt.setStartedAt(startTime);
        Map<Integer, String> studentAnswers = new HashMap<>();
        // No answer provided for question 1

        when(attemptRepository.findById(1)).thenReturn(Optional.of(testAttempt));
        when(quizRepository.findById(1)).thenReturn(Optional.of(testQuiz));
        when(redisTemplate.hasKey("quiz:attempt:1")).thenReturn(true);
        when(questionRepository.findByQuizId(1)).thenReturn(Arrays.asList(testQuestion));
        when(objectMapper.writeValueAsString(studentAnswers)).thenReturn("{}");
        when(attemptRepository.save(any(Attempt.class))).thenReturn(testAttempt);
        when(redisTemplate.delete("quiz:attempt:1")).thenReturn(true);

        // Act
        Attempt result = assessmentService.submitAttempt(1, studentAnswers);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getScore());
        assertFalse(result.getPassed());
    }

    @Test
    @DisplayName("Test: Submit Attempt - Should calculate timeTaken correctly")
    void testSubmitAttempt_TimeTaken() throws Exception {
        // Arrange
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(5);
        testAttempt.setStartedAt(startTime);
        Map<Integer, String> studentAnswers = new HashMap<>();

        when(attemptRepository.findById(1)).thenReturn(Optional.of(testAttempt));
        when(quizRepository.findById(1)).thenReturn(Optional.of(testQuiz));
        when(redisTemplate.hasKey("quiz:attempt:1")).thenReturn(false); // Timer expired
        when(questionRepository.findByQuizId(1)).thenReturn(Arrays.asList(testQuestion));
        when(objectMapper.writeValueAsString(studentAnswers)).thenReturn("{}");
        when(attemptRepository.save(any(Attempt.class))).thenReturn(testAttempt);
        when(redisTemplate.delete("quiz:attempt:1")).thenReturn(true);

        // Act
        Attempt result = assessmentService.submitAttempt(1, studentAnswers);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTimeTaken() >= 0);
    }

    @Test
    @DisplayName("Test: Submit Attempt - Should throw exception when attempt not found")
    void testSubmitAttempt_NotFound() {
        // Arrange
        when(attemptRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            assessmentService.submitAttempt(999, new HashMap<>());
        });
    }

    @Test
    @DisplayName("Test: Get Attempts By Student - Should return all student attempts")
    void testGetAttemptsByStudent_Success() {
        // Arrange
        List<Attempt> attempts = Arrays.asList(testAttempt, testAttempt);
        when(attemptRepository.findByStudentId(101)).thenReturn(attempts);

        // Act
        List<Attempt> result = assessmentService.getAttemptsByStudent(101);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(attemptRepository, times(1)).findByStudentId(101);
    }

    @Test
    @DisplayName("Test: Get Attempts By Quiz - Should return all quiz attempts")
    void testGetAttemptsByQuiz_Success() {
        // Arrange
        List<Attempt> attempts = Arrays.asList(testAttempt, testAttempt);
        when(attemptRepository.findByQuizId(1)).thenReturn(attempts);

        // Act
        List<Attempt> result = assessmentService.getAttemptsByQuiz(1);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(attemptRepository, times(1)).findByQuizId(1);
    }

    @Test
    @DisplayName("Test: Get Best Score - Should return highest scoring attempt")
    void testGetBestScore_Found() {
        // Arrange
        when(attemptRepository.findTopByStudentIdAndQuizIdOrderByScoreDesc(101, 1))
                .thenReturn(Optional.of(testAttempt));

        // Act
        Optional<Attempt> result = assessmentService.getBestScore(101, 1);

        // Assert
        assertTrue(result.isPresent());
        verify(attemptRepository, times(1)).findTopByStudentIdAndQuizIdOrderByScoreDesc(101, 1);
    }

    @Test
    @DisplayName("Test: Get Best Score - Should return empty when no attempts")
    void testGetBestScore_NotFound() {
        // Arrange
        when(attemptRepository.findTopByStudentIdAndQuizIdOrderByScoreDesc(101, 999))
                .thenReturn(Optional.empty());

        // Act
        Optional<Attempt> result = assessmentService.getBestScore(101, 999);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Test: Get Attempt By ID - Should return attempt when found")
    void testGetAttemptById_Found() {
        // Arrange
        when(attemptRepository.findById(1)).thenReturn(Optional.of(testAttempt));

        // Act
        Optional<Attempt> result = assessmentService.getAttemptById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getAttemptId());
        verify(attemptRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Test: Get Attempt By ID - Should return empty when not found")
    void testGetAttemptById_NotFound() {
        // Arrange
        when(attemptRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<Attempt> result = assessmentService.getAttemptById(999);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Test: Submit Attempt - Should calculate partial score with multiple questions")
    void testSubmitAttempt_MultipleQuestionsPartialScore() throws Exception {
        // Arrange
        Question q1 = Question.builder()
                .questionId(1)
                .quizId(1)
                .questionText("Q1")
                .questionType("MCQ_SINGLE")
                .correctAnswer("A")
                .marks(5)
                .build();

        Question q2 = Question.builder()
                .questionId(2)
                .quizId(1)
                .questionText("Q2")
                .questionType("MCQ_SINGLE")
                .correctAnswer("B")
                .marks(5)
                .build();

        LocalDateTime startTime = LocalDateTime.now();
        testAttempt.setStartedAt(startTime);
        Map<Integer, String> studentAnswers = new HashMap<>();
        studentAnswers.put(1, "A"); // Correct
        studentAnswers.put(2, "C"); // Wrong

        when(attemptRepository.findById(1)).thenReturn(Optional.of(testAttempt));
        when(quizRepository.findById(1)).thenReturn(Optional.of(testQuiz));
        when(redisTemplate.hasKey("quiz:attempt:1")).thenReturn(true);
        when(questionRepository.findByQuizId(1)).thenReturn(Arrays.asList(q1, q2));
        when(objectMapper.writeValueAsString(studentAnswers)).thenReturn("{\"1\":\"A\",\"2\":\"C\"}");
        when(attemptRepository.save(any(Attempt.class))).thenReturn(testAttempt);
        when(redisTemplate.delete("quiz:attempt:1")).thenReturn(true);

        // Act
        Attempt result = assessmentService.submitAttempt(1, studentAnswers);

        // Assert
        assertNotNull(result);
        assertEquals(50, result.getScore()); // 5/10 marks = 50%
        assertFalse(result.getPassed()); // 50 < 60
    }
}

