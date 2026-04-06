# Assessment Service

A comprehensive Spring Boot microservice for managing quizzes, questions, and student attempts with **automatic grading** capabilities in the Online Learning Management System (OLMS).

## Overview

The Assessment Service handles:
- **Quiz Management** - Create, update, publish, and delete quizzes
- **Question Management** - Add, edit, and organize quiz questions
- **Auto-Grading** - Automatic scoring for MCQ_SINGLE, MCQ_MULTI, and TRUE_FALSE questions
- **Attempt Tracking** - Track student quiz attempts with time limits
- **Score Analytics** - Get best scores and attempt history

## Features

### Core Features
-  **Quiz CRUD Operations** - Full lifecycle management
-  **Question Management** - Support for multiple question types
-  **Auto-Grading Engine** - Instant result calculation
-  **Attempt Management** - Start, submit, and track attempts
-  **Time Management** - Redis-based TTL for attempt timeouts
-  **Cascading Delete** - Questions deleted with quiz
-  **Best Score Tracking** - Get highest score per student/quiz
-  **Multi-attempt Support** - Configurable max attempts per quiz

###  Quality Assurance
-  **64 Comprehensive Tests** - Unit, Integration, and Repository tests
-  **100% API Coverage** - All endpoints tested
-  **Auto-Grading Tests** - MCQ_SINGLE, MCQ_MULTI, TRUE_FALSE
-  **H2 Database** - In-memory testing database

## Architecture

```
assessment-service/
├── src/
│   ├── main/
│   │   ├── java/com/edulearn/assessment/
│   │   │   ├── AssessmentServiceApplication.java     (Main class)
│   │   │   ├── controller/
│   │   │   │   └── AssessmentController.java         (REST endpoints)
│   │   │   ├── service/
│   │   │   │   ├── AssessmentService.java           (Interface)
│   │   │   │   └── AssessmentServiceImpl.java        (Implementation)
│   │   │   ├── entity/
│   │   │   │   ├── Quiz.java
│   │   │   │   ├── Question.java
│   │   │   │   └── Attempt.java
│   │   │   ├── repository/
│   │   │   │   ├── QuizRepository.java
│   │   │   │   ├── QuestionRepository.java
│   │   │   │   └── AttemptRepository.java
│   │   │   └── config/
│   │   │       └── RedisConfig.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/edulearn/assessment/
│           ├── service/
│           │   └── AssessmentServiceImplTest.java
│           ├── controller/
│           │   └── AssessmentControllerTest.java
│           ├── repository/
│           │   └── RepositoryTest.java
│           └── AssessmentServiceIntegrationTest.java
├── pom.xml
├── mvnw
├── mvnw.cmd
├── TEST-DOCUMENTATION.md
├── QUICK_TEST_GUIDE.md
└── README.md
```

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: MySQL (production), H2 (testing)
- **Cache**: Redis
- **Testing**: JUnit 5, Mockito, Spring Test
- **Build**: Maven

## Prerequisites

- Java 17+
- MySQL 8.0+
- Redis (optional, for production)
- Maven 3.6+

## Installation

### 1. Clone or Download
```bash
cd assessment-service
```

### 2. Build the Project
```bash
mvn clean install
```

### 3. Configure Database
Create MySQL database:
```sql
CREATE DATABASE assessment_service_db;
```

Update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/assessment_service_db
spring.datasource.username=root
spring.datasource.password=root
```

### 4. Run Tests
```bash
mvn clean test
```

Expected output:
```
Tests run: 64, Failures: 0, Errors: 0
```

### 5. Start the Service
```bash
mvn spring-boot:run
```
or
```bash
java -jar target/assessment-service-0.0.1-SNAPSHOT.jar
```

Service runs on: `http://localhost:8083`

## API Endpoints

### Quiz Management

#### Create Quiz
```http
POST /api/assessments/quiz
Content-Type: application/json

{
  "courseId": 1,
  "lessonId": 1,
  "title": "Java Basics",
  "description": "Test your Java knowledge",
  "timeLimitMinutes": 30,
  "passingScore": 70,
  "maxAttempts": 3,
  "isPublished": false
}
```

#### Get Quiz
```http
GET /api/assessments/quiz/{quizId}
```

#### Get Quizzes by Course
```http
GET /api/assessments/course/{courseId}/quizzes
```

#### Update Quiz
```http
PUT /api/assessments/quiz/{quizId}
Content-Type: application/json

{
  "title": "Updated Title",
  "description": "Updated Description",
  "timeLimitMinutes": 45,
  "passingScore": 75,
  "maxAttempts": 5
}
```

#### Delete Quiz
```http
DELETE /api/assessments/quiz/{quizId}
```

#### Publish Quiz
```http
POST /api/assessments/quiz/{quizId}/publish
```

### Question Management

#### Add Question
```http
POST /api/assessments/quiz/{quizId}/question
Content-Type: application/json

{
  "questionText": "What is the capital of France?",
  "questionType": "MCQ_SINGLE",
  "options": "Paris,London,Berlin,Madrid",
  "correctAnswer": "Paris",
  "marks": 1,
  "orderIndex": 1
}
```

Question Types:
- `MCQ_SINGLE` - Single choice (case-insensitive match)
- `MCQ_MULTI` - Multiple choice (comma-separated, order-independent)
- `TRUE_FALSE` - Boolean question

#### Get Questions
```http
GET /api/assessments/quiz/{quizId}/questions
```

#### Update Question
```http
PUT /api/assessments/question/{questionId}
Content-Type: application/json

{
  "questionText": "Updated question",
  "correctAnswer": "Updated Answer",
  "marks": 2
}
```

#### Delete Question
```http
DELETE /api/assessments/question/{questionId}
```

### Attempt Management

#### Start Attempt
```http
POST /api/assessments/quiz/{quizId}/start
Content-Type: application/json

{
  "studentId": 101
}
```

Response:
```json
{
  "attemptId": 1,
  "quizId": 1,
  "studentId": 101,
  "score": 0,
  "passed": false,
  "startedAt": "2026-04-05T10:30:00"
}
```

#### Submit Attempt
```http
POST /api/assessments/attempt/{attemptId}/submit
Content-Type: application/json

{
  "1": "Paris",
  "2": "Option A,Option B",
  "3": "true"
}
```

Response:
```json
{
  "attemptId": 1,
  "quizId": 1,
  "studentId": 101,
  "score": 85,
  "passed": true,
  "startedAt": "2026-04-05T10:30:00",
  "submittedAt": "2026-04-05T10:45:00",
  "timeTaken": 900
}
```

#### Get Student Attempts
```http
GET /api/assessments/student/{studentId}/attempts
```

#### Get Quiz Attempts
```http
GET /api/assessments/quiz/{quizId}/attempts
```

#### Get Best Score
```http
GET /api/assessments/student/{studentId}/quiz/{quizId}/best
```

#### Get Attempt Details
```http
GET /api/assessments/attempt/{attemptId}
```

## Auto-Grading Logic

### Scoring Algorithm
```
For each question:
  if (studentAnswer matches correctAnswer):
    earnedMarks += question.marks
  
totalScore = (earnedMarks / totalMarks) × 100
passed = (totalScore >= quiz.passingScore)
```

### Question Type Handling

#### MCQ_SINGLE
- Case-insensitive exact match
- Example: "Paris" == "paris" ✓

#### MCQ_MULTI
- Split by comma, sort, and compare
- Order-independent matching
- Example: "Option A,Option B" == "Option B,Option A" ✓

#### TRUE_FALSE
- Boolean values as strings
- Case-insensitive match
- Example: "true" == "TRUE" ✓

### Example: Score Calculation
Quiz with 3 marks total:
- Question 1 (1 mark): Student correct → 1 mark
- Question 2 (2 marks): Student incorrect → 0 marks
- Total: 1/3 marks = 33% score

## Testing

### Run All Tests
```bash
mvn clean test
```

### Run Specific Test Class
```bash
mvn test -Dtest=AssessmentServiceImplTest
mvn test -Dtest=AssessmentControllerTest
mvn test -Dtest=RepositoryTest
mvn test -Dtest=AssessmentServiceIntegrationTest
```

### Generate Test Report
```bash
mvn surefire-report:report
open target/site/surefire-report.html
```

## Test Coverage

| Component | Tests | Type |
|-----------|-------|------|
| AssessmentService | 25 | Unit |
| Controller | 18 | Integration |
| Repository | 14 | DataJPA |
| End-to-End | 7 | Integration |
| **Total** | **64** | **Mixed** |

### Test Scenarios

✅ **Quiz Management**
- Create, read, update, delete quizzes
- Publish quizzes
- List quizzes by course

 **Question Management**
- Add questions to quiz
- Retrieve ordered questions
- Update and delete questions

 **Attempt Management**
- Start new attempts
- Track max attempts
- Auto-grade MCQ_SINGLE answers
- Auto-grade MCQ_MULTI answers
- Track best scores

## Configuration

### Application Properties
```properties
# Server
server.port=8083

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/assessment_service_db
spring.datasource.username=root
spring.datasource.password=root

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Redis
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.timeout=60000ms
```

### Environment Variables
```bash
export MYSQL_URL=jdbc:mysql://localhost:3306/assessment_service_db
export MYSQL_USER=root
export MYSQL_PASSWORD=root
export REDIS_HOST=localhost
export REDIS_PORT=6379
```

## Dependencies

```xml
<!-- Spring Boot -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Database -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
</dependency>

<!-- Cache -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- Testing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

## Troubleshooting

### MySQL Connection Error
```
Error: No connection to jdbc:mysql://localhost:3306/assessment_service_db
```
**Solution**: 
1. Ensure MySQL is running
2. Check credentials in application.properties
3. Verify database exists: `CREATE DATABASE assessment_service_db;`

### Redis Connection Error
```
Error: Unable to connect to Redis at localhost:6379
```
**Solution**:
- Tests mock Redis, no setup needed
- For production, start Redis: `redis-server`

### Port Already in Use
```
Error: Address already in use: 8083
```
**Solution**:
1. Change port in application.properties: `server.port=8084`
2. Or kill process: `lsof -i :8083` (Linux/Mac)

### Test Compilation Error
```
[ERROR] Quiz.java uses unchecked or unsafe operations
```
**Solution**: Ensure Java 17+ is configured in pom.xml

## Database Schema

### QUIZZES Table
```sql
CREATE TABLE quizzes (
  quiz_id INT AUTO_INCREMENT PRIMARY KEY,
  course_id INT NOT NULL,
  lesson_id INT,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  time_limit_minutes INT NOT NULL,
  passing_score INT NOT NULL,
  max_attempts INT NOT NULL,
  is_published BOOLEAN DEFAULT FALSE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME
);
```

### QUESTIONS Table
```sql
CREATE TABLE questions (
  question_id INT AUTO_INCREMENT PRIMARY KEY,
  quiz_id INT NOT NULL,
  question_text TEXT NOT NULL,
  question_type VARCHAR(50) NOT NULL,
  options TEXT,
  correct_answer TEXT NOT NULL,
  marks INT NOT NULL,
  order_index INT NOT NULL,
  FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id) ON DELETE CASCADE
);
```

### ATTEMPTS Table
```sql
CREATE TABLE attempts (
  attempt_id INT AUTO_INCREMENT PRIMARY KEY,
  quiz_id INT NOT NULL,
  student_id INT NOT NULL,
  score INT NOT NULL,
  passed BOOLEAN DEFAULT FALSE,
  started_at DATETIME NOT NULL,
  submitted_at DATETIME,
  time_taken INT,
  answers LONGTEXT,
  FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id) ON DELETE CASCADE
);
```

## Performance Considerations

- **Indexing**: Added on `quiz_id`, `student_id`, `course_id`
- **Query Optimization**: OrderBy used in repository queries
- **Caching**: Redis for attempt timers
- **Batch Operations**: Cascade delete for efficiency

## Security Notes

- Implement authentication/authorization in gateway
- Add rate limiting for attempt endpoints
- Validate quiz ownership before modifications
- Encrypt sensitive data in transit

## Contributing

1. Create feature branch
2. Write tests first
3. Implement feature
4. Ensure all tests pass
5. Submit pull request
