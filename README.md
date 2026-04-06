# Enrollment Service

## Overview

Enrollment Service is a Spring Boot microservice responsible for managing student course enrollments in the EduLearn Learning Management System. It provides RESTful APIs for enrollment operations, progress tracking, and enrollment verification with role-based access control.

## Architecture

```
Enrollment Service (Port 8084)
├── Controller Layer (REST Endpoints)
├── Service Layer (Business Logic)
├── Repository Layer (Database Access)
└── Database (MySQL - edulearn_enrollment)
```

## Technology Stack

- **Framework**: Spring Boot 3.5.13
- **Language**: Java 17
- **Database**: MySQL 8.0
- **ORM**: Hibernate with Spring Data JPA
- **Security**: Spring Security with JWT
- **Testing**: JUnit 5 with Mockito
- **Build Tool**: Maven

## Features

### 1. Enrollment Management
- Enroll students in courses
- Unenroll students from courses
- Track enrollment status (ACTIVE, COMPLETED, CANCELLED)
- View student enrollments
- View course enrollments

### 2. Progress Tracking
- Update course progress (0-100%)
- Auto-complete courses at 100% progress
- Mark enrollments as completed
- Track completion dates

### 3. Enrollment Verification
- Check if student is enrolled (for lesson access gate)
- Get total enrollment count per course
- Support for access control integration

### 4. Security
- Role-based access control (STUDENT, INSTRUCTOR, ADMIN)
- JWT token validation
- CORS enabled for cross-origin requests
- Public endpoint for enrollment checks (for lesson access gate)

## API Endpoints

### Public Endpoints (No Authentication Required)

#### Check Enrollment Status
```http
GET /api/v1/enrollments/check?studentId=X&courseId=Y
```
Returns boolean indicating if student is enrolled (used by lesson service for access gate).

### Protected Endpoints (STUDENT Role)

#### Enroll in Course
```http
POST /api/v1/enrollments/enroll
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}

{
  "studentId": 1,
  "courseId": 5
}
```
Response: Returns enrollment object with enrollmentId, enrolledAt, status="ACTIVE".

#### Unenroll from Course
```http
DELETE /api/v1/enrollments/{enrollmentId}
Authorization: Bearer {JWT_TOKEN}
```

### Protected Endpoints (Authenticated Users)

#### Get My Enrollments
```http
GET /api/v1/enrollments/student/{studentId}
Authorization: Bearer {JWT_TOKEN}
```
Returns all enrollments for a student.

#### Update Progress
```http
PUT /api/v1/enrollments/progress
Authorization: Bearer {JWT_TOKEN}

{
  "studentId": 1,
  "courseId": 5,
  "progressPercent": 50
}
```
When progressPercent=100, status automatically set to "COMPLETED".

#### Get Enrollment Count
```http
GET /api/v1/enrollments/count/{courseId}
Authorization: Bearer {JWT_TOKEN}
```
Returns total number of students enrolled in a course.

### Protected Endpoints (INSTRUCTOR/ADMIN Role)

#### Get Course Enrollments
```http
GET /api/v1/enrollments/course/{courseId}
Authorization: Bearer {JWT_TOKEN}
```
Get all student enrollments for a course.

#### Mark Enrollment Complete
```http
PUT /api/v1/enrollments/complete/{enrollmentId}
Authorization: Bearer {JWT_TOKEN}
```
Mark enrollment as completed.

## Database Schema

### Enrollments Table
```sql
CREATE TABLE enrollments (
  enrollment_id INT PRIMARY KEY AUTO_INCREMENT,
  student_id INT NOT NULL,
  course_id INT NOT NULL,
  enrolled_at DATETIME NOT NULL,
  completed_at DATETIME,
  status VARCHAR(20) NOT NULL,
  progress_percent INT DEFAULT 0,
  certificate_issued BOOLEAN DEFAULT FALSE
);
```

## Configuration

### application.properties

```properties
server.port=8084
server.servlet.context-path=/api/v1

spring.datasource.url=jdbc:mysql://localhost:3306/edulearn_enrollment
spring.datasource.username=root
spring.datasource.password=root123

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

### JWT Configuration
- Secret Key: `mySecretKeyForJWTAuthenticationInEduLearnMicroserviceArchitecture2024SuperSecureKey123!@#`
- Token Expiration: 24 hours
- Algorithm: HS256

## Running the Service

### Prerequisites
- Java 17
- Maven 3.6+
- MySQL 8.0+

### Build
```bash
cd enrollment-service
./mvnw clean package -DskipTests
```

### Run
```bash
./mvnw spring-boot:run
```

### Access Swagger UI
```
http://localhost:8084/api/v1/swagger-ui.html
```

## Project Structure

```
enrollment-service/
├── src/
│   ├── main/
│   │   ├── java/com/edulearn/enrollment/
│   │   │   ├── controller/
│   │   │   │   └── EnrollmentController.java
│   │   │   ├── service/
│   │   │   │   ├── EnrollmentService.java
│   │   │   │   └── EnrollmentServiceImpl.java
│   │   │   ├── repository/
│   │   │   │   └── EnrollmentRepository.java
│   │   │   ├── entity/
│   │   │   │   └── Enrollment.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JwtAuthFilter.java
│   │   │   │   └── JwtUtil.java
│   │   │   └── EnrollmentServiceApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/edulearn/enrollment/
├── pom.xml
└── README.md
```

## Integration with Other Services

### Lesson Service Integration
- Enrollment Service provides `isEnrolled()` method via `@Autowired` injection
- Lesson Service calls this method for access gate verification
- No HTTP/RestTemplate needed - direct Spring bean injection in monolith

### Auth Service Communication
- Enrollment Service validates JWT tokens using the same secret key
- User information extracted from JWT claims
- Role-based authorization on endpoints

## Key Method: isEnrolled()

This is the **critical method** for lesson access control:

```java
@Override
public boolean isEnrolled(Integer studentId, Integer courseId) {
    return enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId);
}
```

**Usage in LessonServiceImpl**:
```java
if (!lesson.getIsPreview()) {
    if (!enrollmentService.isEnrolled(user.getUserId(), lesson.getCourseId())) {
        throw new AccessDeniedException("Please enroll to access this lesson");
    }
}
```

## Error Handling

The service returns standardized error responses:

```json
{
  "success": false,
  "message": "Error description"
}
```

Common errors:
- 409: Student already enrolled in this course
- 404: Enrollment not found
- 403: Insufficient permissions
- 401: Invalid or missing JWT token

## Performance Considerations

1. Database indexes on (studentId, courseId) composite key
2. Connection pooling with HikariCP
3. Query optimization with proper repository methods
4. Spring Data JPA automatic query generation

## Security Features

1. JWT-based authentication
2. Role-based access control (RBAC)
3. CORS configuration
4. SQL injection prevention through parameterized queries
5. XSS protection through JSON responses
6. Public endpoint for lesson access gate verification

## Deployment

### Build Docker Image
```bash
./mvnw clean package
docker build -t enrollment-service:1.0 .
```

### Run in Docker
```bash
docker run -p 8084:8084 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/edulearn_enrollment \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=root123 \
  enrollment-service:1.0
```

## Troubleshooting

### Connection Refused (Port 8084)
- Ensure MySQL is running
- Check if another service is using port 8084
- Verify database configuration

### JWT Token Invalid
- Verify secret key matches other services
- Check token expiration time
- Ensure Authorization header: `Bearer {token}`

### Student Already Enrolled Error
- Student cannot enroll twice in same course
- Check enrollment status (ACTIVE/COMPLETED/CANCELLED)
- Unenroll first before re-enrolling

## Future Enhancements

1. Certificate generation
2. Enrollment expiration policies
3. Bulk enrollments and transfers
4. Enrollment analytics
5. Waitlist management
6. Prerequisites verification

