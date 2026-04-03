# Course Service

## Overview

Course Service is a Spring Boot microservice responsible for managing online courses in the EduLearn Learning Management System. It provides RESTful APIs for creating, updating, publishing, searching, and retrieving courses with role-based access control.

## Architecture

```
Course Service (Port 8082)
├── Controller Layer (REST Endpoints)
├── Service Layer (Business Logic)
├── Repository Layer (Database Access)
└── Database (MySQL - edulearn_course)
```

## Technology Stack

- **Framework**: Spring Boot 3.5.13
- **Language**: Java 21
- **Database**: MySQL 8.0
- **ORM**: Hibernate with Spring Data JPA
- **Security**: Spring Security with JWT
- **Documentation**: Springdoc OpenAPI (Swagger UI)
- **Testing**: JUnit 5 with Mockito
- **Build Tool**: Maven

## Features

### 1. Course Management
- Create courses (Instructor only)
- Update course details (Instructor only)
- Publish courses for public viewing (Instructor only)
- Delete courses (Instructor only)
- View course details

### 2. Course Discovery
- Browse all published courses
- Search courses by keyword (title and description)
- Filter courses by category
- Filter courses by instructor
- View featured courses (top 6 most recent)
- Get courses by difficulty level

### 3. Course Fields
- Course ID (auto-generated)
- Title
- Description
- Category (e.g., Programming, Design, Business)
- Level (Beginner, Intermediate, Advanced)
- Price
- Instructor ID
- Thumbnail URL
- Total Duration (in minutes)
- Publication Status
- Created Date
- Language

### 4. Security
- Role-based access control (INSTRUCTOR, STUDENT, ADMIN)
- JWT token validation
- CORS enabled for cross-origin requests
- CSRF protection disabled for API endpoints

### 5. API Documentation
- Interactive Swagger UI
- OpenAPI 3.0 specification
- All endpoints documented with descriptions and response codes

## API Endpoints

### Public Endpoints (No Authentication Required)

#### Get All Published Courses
```http
GET /api/v1/courses
```
Returns all published courses available to students.

#### Get Course by ID
```http
GET /api/v1/courses/{id}
```
Retrieve details of a specific course.

#### Search Courses
```http
GET /api/v1/courses/search?keyword={keyword}
```
Search courses by keyword in title and description.

#### Get Courses by Category
```http
GET /api/v1/courses/category/{category}
```
Filter courses by category.

#### Get Featured Courses
```http
GET /api/v1/courses/featured
```
Get top 6 most recently published courses.

#### Get Courses by Instructor
```http
GET /api/v1/courses/instructor/{instructorId}
```
Get all published courses by a specific instructor.

### Protected Endpoints (INSTRUCTOR Role Required)

#### Create Course
```http
POST /api/v1/courses
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}

{
  "title": "Java Programming",
  "description": "Learn Java from scratch",
  "category": "Programming",
  "level": "Beginner",
  "price": 49.99,
  "instructorId": 1,
  "totalDuration": 120,
  "language": "English"
}
```

#### Update Course
```http
PUT /api/v1/courses/{id}
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}
```

#### Publish Course
```http
PUT /api/v1/courses/{id}/publish
Authorization: Bearer {JWT_TOKEN}
```
Make a draft course visible to students.

#### Delete Course
```http
DELETE /api/v1/courses/{id}
Authorization: Bearer {JWT_TOKEN}
```

### Admin Endpoints (ADMIN Role Required)

#### Get All Courses (Including Unpublished)
```http
GET /api/v1/courses/all
Authorization: Bearer {JWT_TOKEN}
```

## Database Schema

### Courses Table
```sql
CREATE TABLE courses (
  course_id INT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  category VARCHAR(100) NOT NULL,
  level VARCHAR(50) NOT NULL,
  price DOUBLE NOT NULL,
  instructor_id INT NOT NULL,
  thumbnail_url VARCHAR(500),
  total_duration INT NOT NULL,
  is_published BOOLEAN DEFAULT FALSE,
  created_at DATE NOT NULL,
  language VARCHAR(50)
);
```

## Configuration

### application.properties

```properties
server.port=8082
server.servlet.context-path=/api/v1

spring.datasource.url=jdbc:mysql://localhost:3306/edulearn_course
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
- Java 21
- Maven 3.6+
- MySQL 8.0+

### Build
```bash
cd course-service
./mvnw clean package -DskipTests
```

### Run
```bash
./mvnw spring-boot:run
```

### Access Swagger UI
```
http://localhost:8082/api/v1/swagger-ui.html
```

## Testing

### Run Tests
```bash
./mvnw test
```

### Test Coverage
- 11 Controller Tests (REST endpoints)
- 16 Service Tests (business logic)
- Total: 27 test cases

### Test Results
- Public endpoints: Pass without authentication
- Protected endpoints: Pass with @WithMockUser(roles = "INSTRUCTOR")
- Error handling: All exception cases covered

## Project Structure

```
course-service/
├── src/
│   ├── main/
│   │   ├── java/com/edulearn/course/
│   │   │   ├── controller/
│   │   │   │   └── CourseController.java
│   │   │   ├── service/
│   │   │   │   ├── CourseService.java (Interface)
│   │   │   │   └── CourseServiceImpl.java
│   │   │   ├── repository/
│   │   │   │   └── CourseRepository.java
│   │   │   ├── entity/
│   │   │   │   └── Course.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── JwtAuthenticationFilter.java
│   │   │   └── CourseServiceApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/edulearn/course/
│           ├── controller/
│           │   └── CourseControllerTest.java
│           └── service/
│               └── CourseServiceImplTest.java
├── pom.xml
└── README.md
```

## Integration with Other Services

### Auth Service Communication
- Course Service validates JWT tokens using the same secret key
- Instructor role verification happens at the JWT filter level
- User information extracted from JWT claims

### Future Integrations
- Enrollment Service: Track student enrollments
- Payment Service: Handle course payments
- Rating Service: Manage course reviews and ratings
- Notification Service: Send course updates to students

## Error Handling

The service returns standardized error responses:

```json
{
  "success": false,
  "message": "Error description",
  "timestamp": "2026-04-03T10:30:00"
}
```

## Performance Considerations

1. Database indexes on frequently queried columns (title, category, instructor_id)
2. Pagination support for large result sets
3. Connection pooling with HikariCP
4. Query optimization with proper JPA relationships

## Security Features

1. JWT-based authentication
2. Role-based access control (RBAC)
3. CORS configuration for cross-origin requests
4. CSRF protection disabled (token-based API)
5. SQL injection prevention through parameterized queries
6. XSS protection through JSON responses only

## Deployment

### Build Docker Image
```bash
./mvnw clean package
docker build -t course-service:1.0 .
```

### Run in Docker
```bash
docker run -p 8082:8082 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/edulearn_course \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=root123 \
  course-service:1.0
```

## Troubleshooting

### Connection Refused (Port 8082)
- Ensure MySQL is running
- Check if another service is using port 8082
- Verify application.properties database configuration

### JWT Token Invalid
- Verify the secret key matches auth-service
- Check token expiration time
- Ensure Authorization header format: `Bearer {token}`

### 403 Forbidden Error
- User doesn't have required role
- JWT token not included in Authorization header
- Token has expired

## Future Enhancements

1. Course prerequisites and dependencies
2. Course progress tracking
3. Course certificates
4. Bulk course imports
5. Course versioning
6. Advanced search with filters
7. Course recommendations
8. Analytics and reporting

```

