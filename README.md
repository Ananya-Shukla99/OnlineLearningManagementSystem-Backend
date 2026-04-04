# Lesson Service

## Overview

Lesson Service is a Spring Boot microservice responsible for managing course lessons and supplementary resources in the EduLearn Learning Management System. It provides RESTful APIs for creating, updating, ordering, and retrieving lessons with role-based access control and enrollment-based access gates.

## Architecture

```
Lesson Service (Port 8083)
├── Controller Layer (REST Endpoints)
├── Service Layer (Business Logic with Access Gate)
├── Repository Layer (Database Access)
├── Integration Layer (EnrollmentService calls)
└── Database (MySQL - edulearn_lesson)
```

## Technology Stack

- **Framework**: Spring Boot 3.5.13
- **Language**: Java 17
- **Database**: MySQL 8.0
- **ORM**: Hibernate with Spring Data JPA
- **Security**: Spring Security with JWT
- **Testing**: JUnit 5 with Mockito (49 comprehensive tests)
- **Documentation**: Springdoc OpenAPI (Swagger UI)
- **Build Tool**: Maven
- **Code Generation**: Lombok

## Features

### 1. Lesson Management
- Create lessons for courses (INSTRUCTOR only)
- Update lesson details (INSTRUCTOR only)
- Delete lessons with cascading resource cleanup (INSTRUCTOR only)
- View lesson details with enrollment verification
- Support for multiple content types (VIDEO, ARTICLE, PDF)

### 2. Lesson Ordering
- Reorder lessons within a course (INSTRUCTOR only)
- Auto-increment orderIndex for proper sequencing
- Transactional reordering to maintain consistency

### 3. Resource Management
- Attach supplementary resources to lessons (INSTRUCTOR only)
- Support resource types: PDF, SLIDES, CODE
- Track file sizes and URLs
- Delete resources (INSTRUCTOR only)

### 4. Preview Lessons
- Mark lessons as preview/free (isPreview=true)
- Access preview lessons without enrollment
- Preview endpoint requires no authentication

### 5. Enrollment-Based Access Gate (CRITICAL FEATURE)
- Check enrollment status before granting access to paid lessons
- Calls EnrollmentService.isEnrolled() via Spring injection
- Throws AccessDeniedException for non-enrolled students
- Transparent access for preview lessons
- Integrated with Lesson Service layer

### 6. Lesson Fields
- Lesson ID (auto-generated)
- Course ID (foreign key)
- Title
- Content Type (VIDEO, ARTICLE, PDF)
- Content URL
- Duration (in minutes)
- Order Index (sequence)
- Description
- isPreview (boolean)

### 7. Resource Fields
- Resource ID (auto-generated)
- Lesson ID (foreign key)
- Name
- File URL
- File Type (PDF, SLIDES, CODE)
- Size (in KB)

### 8. Security
- Role-based access control (INSTRUCTOR, STUDENT, ADMIN)
- JWT token validation
- Enrollment verification for paid lessons
- CORS enabled for cross-origin requests
- CSRF protection on state-changing operations

### 9. API Documentation
- Interactive Swagger UI
- OpenAPI 3.0 specification
- All endpoints documented

## API Endpoints

### Public Endpoints (No Authentication Required)

#### Get Preview Lessons
```http
GET /api/v1/lessons/preview/{courseId}
```
Returns all lessons marked as preview (free) for a course. No authentication needed.

### Protected Endpoints (STUDENT Role)

#### Get Lessons by Course
```http
GET /api/v1/lessons/course/{courseId}
Authorization: Bearer {JWT_TOKEN}
```
Returns all lessons for a course (preview + enrolled paid lessons).

#### Get Lesson by ID (with Access Gate)
```http
GET /api/v1/lessons/{lessonId}
Authorization: Bearer {JWT_TOKEN}
```
Returns lesson details if:
- Lesson is preview (free), OR
- Student is enrolled in the course

Returns 403 Forbidden if student is not enrolled in a paid lesson.

### Protected Endpoints (INSTRUCTOR Role Required)

#### Create Lesson
```http
POST /api/v1/lessons
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}

{
  "courseId": 5,
  "title": "Variables in Java",
  "contentType": "VIDEO",
  "contentUrl": "https://example.com/video.mp4",
  "durationMinutes": 45,
  "orderIndex": 0,
  "description": "Learn about Java variables",
  "isPreview": true
}
```

#### Update Lesson
```http
PUT /api/v1/lessons/{lessonId}
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}

{
  "title": "Updated Title",
  "description": "Updated description",
  "contentType": "ARTICLE",
  "contentUrl": "https://example.com/article",
  "durationMinutes": 30,
  "isPreview": false
}
```

#### Delete Lesson
```http
DELETE /api/v1/lessons/{lessonId}
Authorization: Bearer {JWT_TOKEN}
```
Deletes lesson and all associated resources.

#### Reorder Lessons
```http
PUT /api/v1/lessons/reorder/{courseId}
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}

[1, 3, 2, 4]
```
Reorder lessons by providing list of lesson IDs in new order.

#### Add Resource to Lesson
```http
POST /api/v1/lessons/{lessonId}/resources
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}

{
  "name": "Java Cheat Sheet",
  "fileUrl": "https://example.com/cheatsheet.pdf",
  "fileType": "PDF",
  "sizeKb": 500
}
```

#### Delete Resource
```http
DELETE /api/v1/lessons/resources/{resourceId}
Authorization: Bearer {JWT_TOKEN}
```

## Database Schema

### Lessons Table
```sql
CREATE TABLE lessons (
  lesson_id INT PRIMARY KEY AUTO_INCREMENT,
  course_id INT NOT NULL,
  title VARCHAR(255) NOT NULL,
  content_type VARCHAR(50) NOT NULL,
  content_url VARCHAR(500) NOT NULL,
  duration_minutes INT NOT NULL,
  order_index INT NOT NULL,
  description TEXT,
  is_preview BOOLEAN DEFAULT FALSE,
  FOREIGN KEY (course_id) REFERENCES courses(course_id)
);
```

### Resources Table
```sql
CREATE TABLE resources (
  resource_id INT PRIMARY KEY AUTO_INCREMENT,
  lesson_id INT NOT NULL,
  name VARCHAR(255) NOT NULL,
  file_url VARCHAR(500) NOT NULL,
  file_type VARCHAR(50) NOT NULL,
  size_kb BIGINT,
  FOREIGN KEY (lesson_id) REFERENCES lessons(lesson_id) ON DELETE CASCADE
);
```

## Configuration

### application.properties

```properties
server.port=8083
server.servlet.context-path=/api/v1

spring.datasource.url=jdbc:mysql://localhost:3306/edulearn_lesson
spring.datasource.username=root
spring.datasource.password=root123

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

### JWT Configuration
- Secret Key: `mySecretKeyForJWTAuthenticationInEduLearnMicroserviceArchitecture2024SuperSecureKey123!@#`
- Token Expiration: 24 hours
- Algorithm: HS256

### Environment Variables
All sensitive values use environment variables:
- `SERVER_PORT`: Default 8083
- `DB_URL`: MySQL connection string
- `DB_USERNAME`: Database user
- `DB_PASSWORD`: Database password
- `JWT_SECRET`: JWT signing key

## Running the Service

### Prerequisites
- Java 17
- Maven 3.6+
- MySQL 8.0+
- EnrollmentService running (for access gate)

### Build
```bash
cd lesson-service
./mvnw clean package -DskipTests
```

### Run
```bash
./mvnw spring-boot:run
```

### Access Swagger UI
```
http://localhost:8083/api/v1/swagger-ui.html
```

## Testing

### Run All Tests (49 tests)
```bash
./mvnw test
```

### Run Specific Test Class
```bash
mvn test -Dtest=LessonControllerTest
mvn test -Dtest=LessonServiceImplTest
mvn test -Dtest=JwtUtilTest
```

### Test Coverage

| Test Class | Tests | Coverage |
|-----------|-------|----------|
| LessonServiceImplTest | 19 | All 8 service methods |
| LessonControllerTest | 20 | All 7 endpoints |
| JwtUtilTest | 10 | JWT operations |
| **TOTAL** | **49** | **100% critical paths** |

### Test Results
- All 49 tests passing
- Public endpoints: Pass without authentication
- Protected endpoints: Pass with @WithMockUser(authorities = "INSTRUCTOR")
- Access gate: Correctly rejects non-enrolled students
- Preview lessons: Accessible without enrollment

## Project Structure

```
lesson-service/
├── src/
│   ├── main/
│   │   ├── java/com/edulearn/lesson/
│   │   │   ├── controller/
│   │   │   │   └── LessonController.java
│   │   │   ├── service/
│   │   │   │   ├── LessonService.java (Interface)
│   │   │   │   └── LessonServiceImpl.java
│   │   │   ├── repository/
│   │   │   │   ├── LessonRepository.java
│   │   │   │   └── ResourceRepository.java
│   │   │   ├── entity/
│   │   │   │   ├── Lesson.java
│   │   │   │   └── Resource.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JwtAuthFilter.java
│   │   │   │   └── JwtUtil.java
│   │   │   └── LessonServiceApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/edulearn/lesson/
│           ├── controller/
│           │   └── LessonControllerTest.java (20 tests)
│           └── service/
│               ├── LessonServiceImplTest.java (19 tests)
│               └── JwtUtilTest.java (10 tests)
├── pom.xml
├── .env.example
├── .gitignore
├── README.md
└── TEST-DOCUMENTATION.md
```

## THE ACCESS GATE - MOST CRITICAL FEATURE

### How It Works

In `LessonServiceImpl.getLessonById()`:

```java
@Override
public Lesson getLessonById(Integer lessonId, Integer studentId) {
    Lesson lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new RuntimeException("Lesson not found"));

    // ACCESS GATE: Check if lesson is preview or student is enrolled
    if (!lesson.getIsPreview()) {
        // Lesson is paid - check enrollment
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!enrollmentService.isEnrolled(user.getUserId(), lesson.getCourseId())) {
            throw new AccessDeniedException("Please enroll to access this lesson");
        }
    }
    return lesson;
}
```

### Wiring

```java
@Service
public class LessonServiceImpl implements LessonService {
    
    @Autowired
    private EnrollmentService enrollmentService;  // Direct injection
    
    @Autowired
    private UserRepository userRepository;  // Get user from email
    
    // ... rest of service
}
```

### Test Scenario

1. **Test**: GET /lessons/{paidLessonId} without enrollment
   - Expected: 403 Forbidden
   - Reason: Student not enrolled

2. **Test**: Enroll student via POST /enrollments/enroll
   - Expected: 201 Created with enrollment object

3. **Test**: GET /lessons/{paidLessonId} after enrollment
   - Expected: 200 OK with lesson details
   - Reason: Student now enrolled

## Integration with Other Services

### Enrollment Service (Critical)
- Lesson Service calls `EnrollmentService.isEnrolled()`
- Direct Spring bean injection (no HTTP)
- Used in access gate for paid lessons

### Auth Service Communication
- Lesson Service validates JWT tokens using same secret key
- Instructor role verification at JWT filter level
- User email extracted from JWT claims

### Course Service Reference
- Lesson Service stores courseId (foreign key)
- No direct service calls (data reference only)

## Error Handling

The service returns standardized error responses:

```json
{
  "success": false,
  "message": "Error description"
}
```

Common errors:
- 403: Not enrolled in course (paid lesson)
- 404: Lesson not found
- 403: Insufficient permissions (STUDENT trying to create)
- 401: Invalid or missing JWT token
- 409: Duplicate resource error

## Performance Considerations

1. Database indexes on courseId and orderIndex
2. Lazy loading for resources
3. Connection pooling with HikariCP
4. Query optimization with JPQL
5. Transactional consistency for reordering

## Security Features

1. JWT-based authentication
2. Role-based access control (RBAC)
3. Enrollment-based access gate
4. CORS configuration
5. SQL injection prevention via parameterized queries
6. XSS protection via JSON responses
7. CSRF protection on state-changing operations

## Deployment

### Build Docker Image
```bash
./mvnw clean package
docker build -t lesson-service:1.0 .
```

### Run in Docker
```bash
docker run -p 8083:8083 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/edulearn_lesson \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=root123 \
  -e ENROLLMENT_SERVICE_URL=http://enrollment-service:8084 \
  lesson-service:1.0
```

## Troubleshooting

### Connection Refused (Port 8083)
- Ensure MySQL is running
- Check if another service using port 8083
- Verify application.properties configuration

### JWT Token Invalid
- Verify secret key matches auth-service
- Check token expiration
- Ensure Authorization header: `Bearer {token}`

### 403 Access Denied (Paid Lesson)
- Student not enrolled in course
- Enroll via POST /api/v1/enrollments/enroll first
- Check enrollment status in database

### EnrollmentService Not Found
- Ensure enrollment-service is running on port 8084
- Verify Spring bean injection in LessonServiceImpl

## Future Enhancements

1. Video transcoding and streaming
2. Lesson completion tracking
3. Quiz/assignments integration
4. Interactive code playgrounds
5. Lesson comments and discussions
6. Download resources offline
7. Lesson recommendations
8. Analytics and view tracking

