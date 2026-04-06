package com.edulearn.assessment.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.edulearn.assessment.entity.Attempt;
import com.edulearn.assessment.entity.Question;
import com.edulearn.assessment.entity.Quiz;
import com.edulearn.assessment.service.AssessmentService;

@RestController
@RequestMapping("/api/assessments")
@Tag(name = "Assessment Service", description = "API for managing quizzes, questions, and student attempts with auto-grading")
public class AssessmentController {

    @Autowired
    private AssessmentService assessmentService;

    // ==================== QUIZ ENDPOINTS ====================

    @PostMapping("/quiz")
    @Operation(summary = "Create a new quiz", description = "Creates a new quiz with initial configuration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Quiz created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid quiz data")
    })
    public ResponseEntity<Quiz> createQuiz(@RequestBody Quiz quiz) {
        Quiz createdQuiz = assessmentService.createQuiz(quiz);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuiz);
    }

    @GetMapping("/quiz/{quizId}")
    @Operation(summary = "Get quiz by ID", description = "Retrieves a specific quiz by its unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Quiz found and returned"),
        @ApiResponse(responseCode = "404", description = "Quiz not found")
    })
    public ResponseEntity<Quiz> getQuizById(@PathVariable @Parameter(description = "Quiz unique identifier") Integer quizId) {
        return assessmentService.getQuizById(quizId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/course/{courseId}/quizzes")
    @Operation(summary = "Get all quizzes for a course", description = "Retrieves all quizzes associated with a specific course")
    @ApiResponse(responseCode = "200", description = "List of quizzes (empty list if none found)")
    public ResponseEntity<List<Quiz>> getQuizzesByCourse(@PathVariable @Parameter(description = "Course unique identifier") Integer courseId) {
        List<Quiz> quizzes = assessmentService.getQuizzesByCourse(courseId);
        return ResponseEntity.ok(quizzes);
    }

    @PutMapping("/quiz/{quizId}")
    @Operation(summary = "Update an existing quiz", description = "Updates quiz details like title, description, time limit, passing score, and max attempts")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Quiz updated successfully"),
        @ApiResponse(responseCode = "404", description = "Quiz not found")
    })
    public ResponseEntity<Quiz> updateQuiz(@PathVariable @Parameter(description = "Quiz unique identifier") Integer quizId, @RequestBody Quiz quiz) {
        try {
            Quiz updatedQuiz = assessmentService.updateQuiz(quizId, quiz);
            return ResponseEntity.ok(updatedQuiz);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/quiz/{quizId}")
    @Operation(summary = "Delete a quiz", description = "Deletes a quiz and all associated questions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Quiz deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Quiz not found")
    })
    public ResponseEntity<Void> deleteQuiz(@PathVariable @Parameter(description = "Quiz unique identifier") Integer quizId) {
        assessmentService.deleteQuiz(quizId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/quiz/{quizId}/publish")
    @Operation(summary = "Publish a quiz", description = "Makes a quiz available for students to take")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Quiz published successfully"),
        @ApiResponse(responseCode = "404", description = "Quiz not found")
    })
    public ResponseEntity<Void> publishQuiz(@PathVariable @Parameter(description = "Quiz unique identifier") Integer quizId) {
        try {
            assessmentService.publishQuiz(quizId);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // ==================== QUESTION ENDPOINTS ====================

    @PostMapping("/quiz/{quizId}/question")
    @Operation(summary = "Add a question to a quiz", description = "Adds a new question (MCQ_SINGLE, TRUE_FALSE, or MCQ_MULTI) to a quiz")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Question added successfully"),
        @ApiResponse(responseCode = "404", description = "Quiz not found")
    })
    public ResponseEntity<Question> addQuestion(@PathVariable @Parameter(description = "Quiz unique identifier") Integer quizId, @RequestBody Question question) {
        try {
            Question addedQuestion = assessmentService.addQuestion(quizId, question);
            return ResponseEntity.status(HttpStatus.CREATED).body(addedQuestion);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/quiz/{quizId}/questions")
    @Operation(summary = "Get all questions for a quiz", description = "Retrieves all questions for a quiz ordered by their index")
    @ApiResponse(responseCode = "200", description = "List of questions (empty list if none found)")
    public ResponseEntity<List<Question>> getQuestionsByQuiz(@PathVariable @Parameter(description = "Quiz unique identifier") Integer quizId) {
        List<Question> questions = assessmentService.getQuestionsByQuiz(quizId);
        return ResponseEntity.ok(questions);
    }

    @PutMapping("/question/{questionId}")
    @Operation(summary = "Update a question", description = "Updates question details like text, type, options, and correct answer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Question updated successfully"),
        @ApiResponse(responseCode = "404", description = "Question not found")
    })
    public ResponseEntity<Question> updateQuestion(@PathVariable @Parameter(description = "Question unique identifier") Integer questionId, @RequestBody Question question) {
        try {
            Question updatedQuestion = assessmentService.updateQuestion(questionId, question);
            return ResponseEntity.ok(updatedQuestion);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/question/{questionId}")
    @Operation(summary = "Delete a question", description = "Removes a question from a quiz")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Question deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Question not found")
    })
    public ResponseEntity<Void> deleteQuestion(@PathVariable @Parameter(description = "Question unique identifier") Integer questionId) {
        assessmentService.deleteQuestion(questionId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // ==================== ATTEMPT ENDPOINTS ====================

    @PostMapping("/quiz/{quizId}/start")
    @Operation(summary = "Start a quiz attempt", description = "Creates a new attempt for a student to take a quiz. Starts a timer based on quiz time limit.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Attempt started successfully"),
        @ApiResponse(responseCode = "400", description = "Maximum attempts reached or invalid request")
    })
    public ResponseEntity<Attempt> startAttempt(
            @PathVariable @Parameter(description = "Quiz unique identifier") Integer quizId,
            @RequestBody @Parameter(description = "Request body containing studentId") Map<String, Integer> body) {
        Integer studentId = body.get("studentId");
        try {
            Attempt attempt = assessmentService.startAttempt(quizId, studentId);
            return ResponseEntity.status(HttpStatus.CREATED).body(attempt);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/attempt/{attemptId}/submit")
    @Operation(summary = "Submit quiz attempt with answers", description = "Submits student answers for auto-grading. Calculates score, determines pass/fail, and stores the attempt result.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Attempt submitted and graded successfully"),
        @ApiResponse(responseCode = "404", description = "Attempt not found")
    })
    public ResponseEntity<Attempt> submitAttempt(
            @PathVariable @Parameter(description = "Attempt unique identifier") Integer attemptId,
            @RequestBody @Parameter(description = "Student answers as map of questionId -> answer") Map<Integer, String> studentAnswers) {
        try {
            Attempt submittedAttempt = assessmentService.submitAttempt(attemptId, studentAnswers);
            return ResponseEntity.ok(submittedAttempt);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/student/{studentId}/attempts")
    @Operation(summary = "Get all attempts by a student", description = "Retrieves all quiz attempts made by a specific student across all quizzes")
    @ApiResponse(responseCode = "200", description = "List of student attempts")
    public ResponseEntity<List<Attempt>> getAttemptsByStudent(@PathVariable @Parameter(description = "Student unique identifier") Integer studentId) {
        List<Attempt> attempts = assessmentService.getAttemptsByStudent(studentId);
        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/quiz/{quizId}/attempts")
    @Operation(summary = "Get all attempts for a quiz", description = "Retrieves all attempts made by all students for a specific quiz")
    @ApiResponse(responseCode = "200", description = "List of attempts for the quiz")
    public ResponseEntity<List<Attempt>> getAttemptsByQuiz(@PathVariable @Parameter(description = "Quiz unique identifier") Integer quizId) {
        List<Attempt> attempts = assessmentService.getAttemptsByQuiz(quizId);
        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/student/{studentId}/quiz/{quizId}/best")
    @Operation(summary = "Get best score for a student on a quiz", description = "Retrieves the highest scoring attempt made by a student on a specific quiz")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Best attempt found"),
        @ApiResponse(responseCode = "404", description = "No attempts found")
    })
    public ResponseEntity<Attempt> getBestScore(
            @PathVariable @Parameter(description = "Student unique identifier") Integer studentId,
            @PathVariable @Parameter(description = "Quiz unique identifier") Integer quizId) {
        return assessmentService.getBestScore(studentId, quizId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/attempt/{attemptId}")
    @Operation(summary = "Get attempt by ID", description = "Retrieves a specific quiz attempt with all submission details and score")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Attempt found and returned"),
        @ApiResponse(responseCode = "404", description = "Attempt not found")
    })
    public ResponseEntity<Attempt> getAttemptById(@PathVariable @Parameter(description = "Attempt unique identifier") Integer attemptId) {
        return assessmentService.getAttemptById(attemptId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}

