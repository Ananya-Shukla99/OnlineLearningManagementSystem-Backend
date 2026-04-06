package com.edulearn.assessment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.edulearn.assessment.entity.Attempt;
import com.edulearn.assessment.entity.Question;
import com.edulearn.assessment.entity.Quiz;
import com.edulearn.assessment.service.AssessmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@WebMvcTest(
        controllers = AssessmentController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class,
                RedisAutoConfiguration.class,
                RedisRepositoriesAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("Assessment Controller Tests")
class AssessmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AssessmentService assessmentService;

    // Required: prevents JwtAuthenticationFilter from failing to load in @WebMvcTest context
    @MockBean
    private com.edulearn.assessment.security.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.edulearn.assessment.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    private Quiz testQuiz;
    private Question testQuestion;
    private Attempt testAttempt;

    @BeforeEach
    void setUp() {
        // Register JavaTimeModule so LocalDateTime fields serialize correctly in tests
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

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
                .score(85)
                .passed(true)
                .startedAt(LocalDateTime.now().minusMinutes(10))
                .submittedAt(LocalDateTime.now())
                .timeTaken(600)
                .answers("{}")
                .build();
    }

    // ==================== QUIZ ENDPOINTS TESTS ====================

    @Test
    @DisplayName("Test: POST /api/assessments/quiz - Create Quiz")
    void testCreateQuiz_Success() throws Exception {
        when(assessmentService.createQuiz(any(Quiz.class))).thenReturn(testQuiz);

        mockMvc.perform(post("/api/assessments/quiz")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testQuiz)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quizId").value(1))
                .andExpect(jsonPath("$.title").value("Java Basics Quiz"));

        verify(assessmentService, times(1)).createQuiz(any(Quiz.class));
    }

    @Test
    @DisplayName("Test: GET /api/assessments/quiz/{quizId} - Get Quiz By ID")
    void testGetQuizById_Success() throws Exception {
        when(assessmentService.getQuizById(1)).thenReturn(Optional.of(testQuiz));

        mockMvc.perform(get("/api/assessments/quiz/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quizId").value(1))
                .andExpect(jsonPath("$.title").value("Java Basics Quiz"));

        verify(assessmentService, times(1)).getQuizById(1);
    }

    @Test
    @DisplayName("Test: GET /api/assessments/quiz/{quizId} - Quiz Not Found")
    void testGetQuizById_NotFound() throws Exception {
        when(assessmentService.getQuizById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/assessments/quiz/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(assessmentService, times(1)).getQuizById(999);
    }

    @Test
    @DisplayName("Test: GET /api/assessments/course/{courseId}/quizzes - Get Quizzes By Course")
    void testGetQuizzesByCourse_Success() throws Exception {
        List<Quiz> quizzes = Arrays.asList(testQuiz, testQuiz);
        when(assessmentService.getQuizzesByCourse(1)).thenReturn(quizzes);

        mockMvc.perform(get("/api/assessments/course/1/quizzes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quizId").value(1))
                .andExpect(jsonPath("$.length()").value(2));

        verify(assessmentService, times(1)).getQuizzesByCourse(1);
    }

    @Test
    @DisplayName("Test: GET Quizzes - Empty list response")
    void testGetQuizzesByCourse_EmptyList() throws Exception {
        when(assessmentService.getQuizzesByCourse(999)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/assessments/course/999/quizzes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(assessmentService, times(1)).getQuizzesByCourse(999);
    }

    @Test
    @DisplayName("Test: PUT /api/assessments/quiz/{quizId} - Update Quiz Success")
    void testUpdateQuiz_Success() throws Exception {
        Quiz updatedQuiz = Quiz.builder()
                .quizId(1)
                .title("Updated Quiz")
                .build();

        when(assessmentService.updateQuiz(eq(1), any(Quiz.class))).thenReturn(testQuiz);

        mockMvc.perform(put("/api/assessments/quiz/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedQuiz)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quizId").value(1));

        verify(assessmentService, times(1)).updateQuiz(eq(1), any(Quiz.class));
    }

    @Test
    @DisplayName("Test: PUT /api/assessments/quiz/{quizId} - Update Quiz Not Found")
    void testUpdateQuiz_NotFound() throws Exception {
        // FIX: Added missing negative test - controller returns 404 when service throws
        when(assessmentService.updateQuiz(eq(999), any(Quiz.class)))
                .thenThrow(new RuntimeException("Quiz not found with ID: 999"));

        mockMvc.perform(put("/api/assessments/quiz/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testQuiz)))
                .andExpect(status().isNotFound());

        verify(assessmentService, times(1)).updateQuiz(eq(999), any(Quiz.class));
    }

    @Test
    @DisplayName("Test: DELETE /api/assessments/quiz/{quizId} - Delete Quiz Success")
    void testDeleteQuiz_Success() throws Exception {
        doNothing().when(assessmentService).deleteQuiz(1);

        mockMvc.perform(delete("/api/assessments/quiz/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(assessmentService, times(1)).deleteQuiz(1);
    }

    @Test
    @DisplayName("Test: DELETE /api/assessments/quiz/{quizId} - Delete Quiz Throws 500")
    void testDeleteQuiz_ServiceThrows() throws Exception {
        // FIX: Controller has no try-catch for deleteQuiz — unhandled exception returns 500
        doThrow(new RuntimeException("Quiz not found")).when(assessmentService).deleteQuiz(999);

        mockMvc.perform(delete("/api/assessments/quiz/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(assessmentService, times(1)).deleteQuiz(999);
    }

    @Test
    @DisplayName("Test: POST /api/assessments/quiz/{quizId}/publish - Publish Quiz Success")
    void testPublishQuiz_Success() throws Exception {
        doNothing().when(assessmentService).publishQuiz(1);

        mockMvc.perform(post("/api/assessments/quiz/1/publish")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(assessmentService, times(1)).publishQuiz(1);
    }

    @Test
    @DisplayName("Test: POST /api/assessments/quiz/{quizId}/publish - Publish Quiz Not Found")
    void testPublishQuiz_NotFound() throws Exception {
        // FIX: Added missing negative test for publishQuiz 404 path
        doThrow(new RuntimeException("Quiz not found with ID: 999"))
                .when(assessmentService).publishQuiz(999);

        mockMvc.perform(post("/api/assessments/quiz/999/publish")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(assessmentService, times(1)).publishQuiz(999);
    }

    // ==================== QUESTION ENDPOINTS TESTS ====================

    @Test
    @DisplayName("Test: POST /api/assessments/quiz/{quizId}/question - Add Question Success")
    void testAddQuestion_Success() throws Exception {
        when(assessmentService.addQuestion(eq(1), any(Question.class))).thenReturn(testQuestion);

        mockMvc.perform(post("/api/assessments/quiz/1/question")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testQuestion)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.questionId").value(1))
                .andExpect(jsonPath("$.questionText").value("What is polymorphism?"));

        verify(assessmentService, times(1)).addQuestion(eq(1), any(Question.class));
    }

    @Test
    @DisplayName("Test: POST /api/assessments/quiz/{quizId}/question - Add Question Quiz Not Found")
    void testAddQuestion_QuizNotFound() throws Exception {
        // FIX: Added missing negative test — controller returns 404 when quiz doesn't exist
        when(assessmentService.addQuestion(eq(999), any(Question.class)))
                .thenThrow(new RuntimeException("Quiz not found with ID: 999"));

        mockMvc.perform(post("/api/assessments/quiz/999/question")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testQuestion)))
                .andExpect(status().isNotFound());

        verify(assessmentService, times(1)).addQuestion(eq(999), any(Question.class));
    }

    @Test
    @DisplayName("Test: GET /api/assessments/quiz/{quizId}/questions - Get Questions By Quiz")
    void testGetQuestionsByQuiz_Success() throws Exception {
        List<Question> questions = Arrays.asList(testQuestion, testQuestion);
        when(assessmentService.getQuestionsByQuiz(1)).thenReturn(questions);

        mockMvc.perform(get("/api/assessments/quiz/1/questions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].questionId").value(1))
                .andExpect(jsonPath("$.length()").value(2));

        verify(assessmentService, times(1)).getQuestionsByQuiz(1);
    }

    @Test
    @DisplayName("Test: PUT /api/assessments/question/{questionId} - Update Question Success")
    void testUpdateQuestion_Success() throws Exception {
        when(assessmentService.updateQuestion(eq(1), any(Question.class))).thenReturn(testQuestion);

        mockMvc.perform(put("/api/assessments/question/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testQuestion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionId").value(1));

        verify(assessmentService, times(1)).updateQuestion(eq(1), any(Question.class));
    }

    @Test
    @DisplayName("Test: PUT /api/assessments/question/{questionId} - Update Question Not Found")
    void testUpdateQuestion_NotFound() throws Exception {
        // FIX: Added missing negative test — controller returns 404 when service throws
        when(assessmentService.updateQuestion(eq(999), any(Question.class)))
                .thenThrow(new RuntimeException("Question not found with ID: 999"));

        mockMvc.perform(put("/api/assessments/question/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testQuestion)))
                .andExpect(status().isNotFound());

        verify(assessmentService, times(1)).updateQuestion(eq(999), any(Question.class));
    }

    @Test
    @DisplayName("Test: DELETE /api/assessments/question/{questionId} - Delete Question Success")
    void testDeleteQuestion_Success() throws Exception {
        doNothing().when(assessmentService).deleteQuestion(1);

        mockMvc.perform(delete("/api/assessments/question/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(assessmentService, times(1)).deleteQuestion(1);
    }

    @Test
    @DisplayName("Test: DELETE /api/assessments/question/{questionId} - Delete Question Throws 500")
    void testDeleteQuestion_ServiceThrows() throws Exception {
        // FIX: Controller has no try-catch for deleteQuestion — unhandled exception returns 500
        doThrow(new RuntimeException("Question not found")).when(assessmentService).deleteQuestion(999);

        mockMvc.perform(delete("/api/assessments/question/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(assessmentService, times(1)).deleteQuestion(999);
    }

    // ==================== ATTEMPT ENDPOINTS TESTS ====================

    @Test
    @DisplayName("Test: POST /api/assessments/quiz/{quizId}/start - Start Attempt Success")
    void testStartAttempt_Success() throws Exception {
        Map<String, Integer> body = new HashMap<>();
        body.put("studentId", 101);

        when(assessmentService.startAttempt(1, 101)).thenReturn(testAttempt);

        mockMvc.perform(post("/api/assessments/quiz/1/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.attemptId").value(1))
                .andExpect(jsonPath("$.studentId").value(101));

        verify(assessmentService, times(1)).startAttempt(1, 101);
    }

    @Test
    @DisplayName("Test: POST /api/assessments/quiz/{quizId}/start - Max Attempts Exceeded")
    void testStartAttempt_MaxAttemptsExceeded() throws Exception {
        // FIX: Use doThrow for cleaner stubbing of void-like exception paths
        Map<String, Integer> body = new HashMap<>();
        body.put("studentId", 101);

        when(assessmentService.startAttempt(1, 101))
                .thenThrow(new RuntimeException("Maximum attempts reached for this quiz"));

        mockMvc.perform(post("/api/assessments/quiz/1/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        verify(assessmentService, times(1)).startAttempt(1, 101);
    }

    @Test
    @DisplayName("Test: POST /api/assessments/attempt/{attemptId}/submit - Submit Attempt Success")
    void testSubmitAttempt_Success() throws Exception {
        Map<Integer, String> answers = new HashMap<>();
        answers.put(1, "All of above");

        when(assessmentService.submitAttempt(eq(1), any(Map.class))).thenReturn(testAttempt);

        mockMvc.perform(post("/api/assessments/attempt/1/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answers)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attemptId").value(1))   // FIX: Added stronger assertion
                .andExpect(jsonPath("$.score").value(85))
                .andExpect(jsonPath("$.passed").value(true));

        verify(assessmentService, times(1)).submitAttempt(eq(1), any(Map.class));
    }

    @Test
    @DisplayName("Test: POST /api/assessments/attempt/{attemptId}/submit - Attempt Not Found")
    void testSubmitAttempt_NotFound() throws Exception {
        Map<Integer, String> answers = new HashMap<>();

        when(assessmentService.submitAttempt(eq(999), any(Map.class)))
                .thenThrow(new RuntimeException("Attempt not found with ID: 999"));

        mockMvc.perform(post("/api/assessments/attempt/999/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answers)))
                .andExpect(status().isNotFound());

        verify(assessmentService, times(1)).submitAttempt(eq(999), any(Map.class));
    }

    @Test
    @DisplayName("Test: GET /api/assessments/student/{studentId}/attempts - Get Attempts By Student")
    void testGetAttemptsByStudent_Success() throws Exception {
        List<Attempt> attempts = Arrays.asList(testAttempt, testAttempt);
        when(assessmentService.getAttemptsByStudent(101)).thenReturn(attempts);

        mockMvc.perform(get("/api/assessments/student/101/attempts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].attemptId").value(1))
                .andExpect(jsonPath("$[0].studentId").value(101))  // FIX: Stronger assertion
                .andExpect(jsonPath("$.length()").value(2));

        verify(assessmentService, times(1)).getAttemptsByStudent(101);
    }

    @Test
    @DisplayName("Test: GET Student Attempts - Empty list")
    void testGetAttemptsByStudent_EmptyList() throws Exception {
        when(assessmentService.getAttemptsByStudent(999)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/assessments/student/999/attempts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(assessmentService, times(1)).getAttemptsByStudent(999);
    }

    @Test
    @DisplayName("Test: GET /api/assessments/quiz/{quizId}/attempts - Get Attempts By Quiz")
    void testGetAttemptsByQuiz_Success() throws Exception {
        List<Attempt> attempts = Arrays.asList(testAttempt);
        when(assessmentService.getAttemptsByQuiz(1)).thenReturn(attempts);

        mockMvc.perform(get("/api/assessments/quiz/1/attempts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quizId").value(1))
                .andExpect(jsonPath("$[0].attemptId").value(1))    // FIX: Stronger assertion
                .andExpect(jsonPath("$[0].studentId").value(101))  // FIX: Stronger assertion
                .andExpect(jsonPath("$.length()").value(1));

        verify(assessmentService, times(1)).getAttemptsByQuiz(1);
    }

    @Test
    @DisplayName("Test: GET /api/assessments/student/{studentId}/quiz/{quizId}/best - Get Best Score")
    void testGetBestScore_Success() throws Exception {
        when(assessmentService.getBestScore(101, 1)).thenReturn(Optional.of(testAttempt));

        mockMvc.perform(get("/api/assessments/student/101/quiz/1/best")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attemptId").value(1))  // FIX: Added stronger assertion
                .andExpect(jsonPath("$.score").value(85))
                .andExpect(jsonPath("$.passed").value(true));

        verify(assessmentService, times(1)).getBestScore(101, 1);
    }

    @Test
    @DisplayName("Test: GET /api/assessments/student/{studentId}/quiz/{quizId}/best - Not Found")
    void testGetBestScore_NotFound() throws Exception {
        when(assessmentService.getBestScore(999, 999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/assessments/student/999/quiz/999/best")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(assessmentService, times(1)).getBestScore(999, 999);
    }

    @Test
    @DisplayName("Test: GET /api/assessments/attempt/{attemptId} - Get Attempt By ID Success")
    void testGetAttemptById_Success() throws Exception {
        when(assessmentService.getAttemptById(1)).thenReturn(Optional.of(testAttempt));

        mockMvc.perform(get("/api/assessments/attempt/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attemptId").value(1))
                .andExpect(jsonPath("$.studentId").value(101));

        verify(assessmentService, times(1)).getAttemptById(1);
    }

    @Test
    @DisplayName("Test: GET /api/assessments/attempt/{attemptId} - Attempt Not Found")
    void testGetAttemptById_NotFound() throws Exception {
        when(assessmentService.getAttemptById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/assessments/attempt/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(assessmentService, times(1)).getAttemptById(999);
    }
}