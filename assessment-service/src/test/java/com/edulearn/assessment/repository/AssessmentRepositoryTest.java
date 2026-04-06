package com.edulearn.assessment.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.edulearn.assessment.entity.Attempt;
import com.edulearn.assessment.entity.Question;
import com.edulearn.assessment.entity.Quiz;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Assessment Repository Tests")
class AssessmentRepositoryTest {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AttemptRepository attemptRepository;

    private Quiz testQuiz;
    private Question testQuestion;
    private Attempt testAttempt;

    @BeforeEach
    void setUp() {
        // Clear existing data
        attemptRepository.deleteAll();
        questionRepository.deleteAll();
        quizRepository.deleteAll();

        // Create test quiz
        testQuiz = Quiz.builder()
                .courseId(1)
                .lessonId(1)
                .title("Java Basics")
                .description("Java fundamentals test")
                .timeLimitMinutes(30)
                .passingScore(60)
                .maxAttempts(3)
                .isPublished(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testQuiz = quizRepository.save(testQuiz);

        // Create test question
        testQuestion = Question.builder()
                .quizId(testQuiz.getQuizId())
                .questionText("What is OOP?")
                .questionType("MCQ_SINGLE")
                .options("Object oriented programming,Object object programming,None of above")
                .correctAnswer("Object oriented programming")
                .marks(5)
                .orderIndex(1)
                .build();
        testQuestion = questionRepository.save(testQuestion);

        // Create test attempt
        testAttempt = Attempt.builder()
                .quizId(testQuiz.getQuizId())
                .studentId(101)
                .score(85)
                .passed(true)
                .startedAt(LocalDateTime.now().minusMinutes(10))
                .submittedAt(LocalDateTime.now())
                .timeTaken(600)
                .answers("{\"1\":\"Object oriented programming\"}")
                .build();
        testAttempt = attemptRepository.save(testAttempt);
    }

    // ==================== QUIZ REPOSITORY TESTS ====================

    @Test
    @DisplayName("Test: Save and retrieve Quiz")
    void testSaveAndRetrieveQuiz() {
        // Act
        Optional<Quiz> result = quizRepository.findById(testQuiz.getQuizId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Java Basics", result.get().getTitle());
        assertEquals(60, result.get().getPassingScore());
    }

    @Test
    @DisplayName("Test: Find Quizzes by Course ID")
    void testFindQuizzesByCourseId() {
        // Arrange - Create another quiz for same course
        Quiz quiz2 = Quiz.builder()
                .courseId(1) // Same course
                .lessonId(2)
                .title("Python Basics")
                .timeLimitMinutes(45)
                .passingScore(70)
                .maxAttempts(2)
                .isPublished(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        quizRepository.save(quiz2);

        // Act
        List<Quiz> result = quizRepository.findByCourseId(1);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Test: Find Quizzes by Course ID - Empty result")
    void testFindQuizzesByCourseId_Empty() {
        // Act
        List<Quiz> result = quizRepository.findByCourseId(999);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test: Update Quiz")
    void testUpdateQuiz() {
        // Arrange
        testQuiz.setTitle("Updated Title");
        testQuiz.setPassingScore(75);
        testQuiz.setUpdatedAt(LocalDateTime.now());

        // Act
        Quiz result = quizRepository.save(testQuiz);

        // Assert
        assertEquals("Updated Title", result.getTitle());
        assertEquals(75, result.getPassingScore());
    }

    @Test
    @DisplayName("Test: Delete Quiz")
    void testDeleteQuiz() {
        // Act
        quizRepository.deleteById(testQuiz.getQuizId());

        // Assert
        assertFalse(quizRepository.findById(testQuiz.getQuizId()).isPresent());
    }

    // ==================== QUESTION REPOSITORY TESTS ====================

    @Test
    @DisplayName("Test: Save and retrieve Question")
    void testSaveAndRetrieveQuestion() {
        // Act
        Optional<Question> result = questionRepository.findById(testQuestion.getQuestionId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals("What is OOP?", result.get().getQuestionText());
        assertEquals("MCQ_SINGLE", result.get().getQuestionType());
    }

    @Test
    @DisplayName("Test: Find Questions by Quiz ID ordered by OrderIndex")
    void testFindQuestionsByQuizIdOrderedByOrderIndex() {
        // Arrange - Create another question with higher order index
        Question q2 = Question.builder()
                .quizId(testQuiz.getQuizId())
                .questionText("What is inheritance?")
                .questionType("TRUE_FALSE")
                .options("True,False")
                .correctAnswer("True")
                .marks(5)
                .orderIndex(2)
                .build();
        questionRepository.save(q2);

        // Act
        List<Question> result = questionRepository.findByQuizIdOrderByOrderIndex(testQuiz.getQuizId());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getOrderIndex());
        assertEquals(2, result.get(1).getOrderIndex());
    }

    @Test
    @DisplayName("Test: Delete Questions by Quiz ID")
    void testDeleteQuestionsByQuizId() {
        // Act
        questionRepository.deleteByQuizId(testQuiz.getQuizId());

        // Assert
        List<Question> result = questionRepository.findByQuizIdOrderByOrderIndex(testQuiz.getQuizId());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test: Delete single Question")
    void testDeleteQuestion() {
        // Act
        questionRepository.deleteById(testQuestion.getQuestionId());

        // Assert
        assertFalse(questionRepository.findById(testQuestion.getQuestionId()).isPresent());
    }

    // ==================== ATTEMPT REPOSITORY TESTS ====================

    @Test
    @DisplayName("Test: Save and retrieve Attempt")
    void testSaveAndRetrieveAttempt() {
        // Act
        Optional<Attempt> result = attemptRepository.findById(testAttempt.getAttemptId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(101, result.get().getStudentId());
        assertEquals(85, result.get().getScore());
        assertTrue(result.get().getPassed());
    }

    @Test
    @DisplayName("Test: Find Attempts by Student ID")
    void testFindAttemptsByStudentId() {
        // Arrange - Create another attempt for same student
        Attempt attempt2 = Attempt.builder()
                .quizId(testQuiz.getQuizId())
                .studentId(101) // Same student
                .score(90)
                .passed(true)
                .startedAt(LocalDateTime.now().minusMinutes(5))
                .submittedAt(LocalDateTime.now())
                .timeTaken(300)
                .answers("{}")
                .build();
        attemptRepository.save(attempt2);

        // Act
        List<Attempt> result = attemptRepository.findByStudentId(101);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Test: Find Attempts by Quiz ID")
    void testFindAttemptsByQuizId() {
        // Arrange - Create another attempt for same quiz
        Attempt attempt2 = Attempt.builder()
                .quizId(testQuiz.getQuizId()) // Same quiz
                .studentId(102) // Different student
                .score(75)
                .passed(true)
                .startedAt(LocalDateTime.now().minusMinutes(8))
                .submittedAt(LocalDateTime.now())
                .timeTaken(480)
                .answers("{}")
                .build();
        attemptRepository.save(attempt2);

        // Act
        List<Attempt> result = attemptRepository.findByQuizId(testQuiz.getQuizId());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Test: Count Attempts by Student and Quiz ID")
    void testCountAttemptsByStudentAndQuizId() {
        // Act
        int count = attemptRepository.countByStudentIdAndQuizId(101, testQuiz.getQuizId());

        // Assert
        assertEquals(1, count);
    }

    @Test
    @DisplayName("Test: Count Attempts - No matching attempts")
    void testCountAttempts_NoMatching() {
        // Act
        int count = attemptRepository.countByStudentIdAndQuizId(999, 999);

        // Assert
        assertEquals(0, count);
    }

    @Test
    @DisplayName("Test: Find top attempt by Student and Quiz ordered by Score descending")
    void testFindTopAttemptByStudentAndQuizOrderByScore() {
        // Arrange - Create multiple attempts with different scores
        Attempt attempt2 = Attempt.builder()
                .quizId(testQuiz.getQuizId())
                .studentId(101) // Same student
                .score(70)
                .passed(true)
                .startedAt(LocalDateTime.now().minusMinutes(20))
                .submittedAt(LocalDateTime.now().minusMinutes(15))
                .timeTaken(300)
                .answers("{}")
                .build();
        attemptRepository.save(attempt2);

        // Act
        Optional<Attempt> result = attemptRepository
                .findTopByStudentIdAndQuizIdOrderByScoreDesc(101, testQuiz.getQuizId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(85, result.get().getScore()); // Should return highest score
    }

    @Test
    @DisplayName("Test: Find top attempt - No matching attempt")
    void testFindTopAttempt_NoMatching() {
        // Act
        Optional<Attempt> result = attemptRepository
                .findTopByStudentIdAndQuizIdOrderByScoreDesc(999, 999);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Test: Delete Attempt")
    void testDeleteAttempt() {
        // Act
        attemptRepository.deleteById(testAttempt.getAttemptId());

        // Assert
        assertFalse(attemptRepository.findById(testAttempt.getAttemptId()).isPresent());
    }

    @Test
    @DisplayName("Test: Update Attempt score and passed status")
    void testUpdateAttemptScoreAndStatus() {
        // Arrange
        testAttempt.setScore(95);
        testAttempt.setPassed(true);

        // Act
        Attempt result = attemptRepository.save(testAttempt);

        // Assert
        assertEquals(95, result.getScore());
        assertTrue(result.getPassed());
    }

    @Test
    @DisplayName("Test: Attempt with all fields populated")
    void testAttemptWithAllFields() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.now().minusHours(1);
        LocalDateTime endTime = LocalDateTime.now();

        Attempt newAttempt = Attempt.builder()
                .quizId(testQuiz.getQuizId())
                .studentId(102)
                .score(88)
                .passed(true)
                .startedAt(startTime)
                .submittedAt(endTime)
                .timeTaken(3600)
                .answers("{\"1\":\"Answer1\",\"2\":\"Answer2\"}")
                .build();

        // Act
        Attempt result = attemptRepository.save(newAttempt);

        // Assert
        assertNotNull(result.getAttemptId());
        assertEquals(102, result.getStudentId());
        assertEquals(88, result.getScore());
        assertNotNull(result.getStartedAt());
        assertNotNull(result.getSubmittedAt());
        assertEquals(3600, result.getTimeTaken());
        assertTrue(result.getAnswers().contains("Answer1"));
    }
}

