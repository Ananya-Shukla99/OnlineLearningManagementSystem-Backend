# Discussion Service

## Overview

Discussion Service is a Spring Boot microservice responsible for managing course discussion forums in the EduLearn Learning Management System. It provides RESTful APIs for creating discussion threads, posting replies, and managing upvotes with role-based access control.

## Architecture

```
Discussion Service (Port 8088)
├── Controller Layer (REST Endpoints)
├── Service Layer (Business Logic)
├── Repository Layer (Database Access)
└── Database (MySQL - edulearn_discussion)
```

## Technology Stack

- **Framework**: Spring Boot 3.5.13
- **Language**: Java 17
- **Database**: MySQL 8.0
- **ORM**: Hibernate with Spring Data JPA
- **Security**: Spring Security with JWT
- **Testing**: JUnit 5 with Mockito (35+ comprehensive tests)
- **Documentation**: Springdoc OpenAPI (Swagger UI)
- **Build Tool**: Maven
- **Code Generation**: Lombok

## Features

### 1. Discussion Thread Management
- Create discussion threads in course forums
- Update thread title and body
- Delete threads with cascading cleanup
- View all threads for a course (sorted by pinned status and creation date)
- Thread linking to specific lessons or course-wide

### 2. Thread Moderation
- Pin/unpin important threads (INSTRUCTOR only)
- Close/reopen threads (INSTRUCTOR only)
- Delete threads (INSTRUCTOR only)

### 3. Reply Management
- Post replies to discussion threads
- Update and delete replies
- Mark best answer (accepted reply)
- Upvote replies with duplicate prevention
- Sort replies by acceptance status, upvotes, and creation time

### 4. Upvote System
- Track individual upvotes to prevent duplicates
- Prevent same student from upvoting same reply twice
- Increment/decrement reply upvote count
- View upvote records by student or reply

### 5. Security
- Role-based access control (INSTRUCTOR, STUDENT, ADMIN)
- JWT token validation
- CORS enabled for cross-origin requests
- Public read access for thread/reply lists
- Protected write access (authentication required)

## Entities

### DiscussionThread
- `threadId` (int, PK): Auto-generated identifier
- `courseId` (int): Which course this thread belongs to
- `lessonId` (Integer, nullable): Optional specific lesson linkage
- `authorId` (int): User ID of thread creator
- `title` (String, max 255): Thread question/topic
- `body` (TEXT): Full thread description
- `isPinned` (boolean): Important threads pinned by instructors
- `isClosed` (boolean): Closed threads reject new replies
- `createdAt` (LocalDateTime): Auto-set on creation
- `updatedAt` (LocalDateTime): Auto-updated on edits

### Reply
- `replyId` (int, PK): Auto-generated identifier
- `threadId` (int): Foreign key to thread
- `authorId` (int): User ID of reply author
- `body` (TEXT): Reply content
- `isAccepted` (boolean): Only one accepted answer per thread
- `upvotes` (int): Total upvote count
- `createdAt` (LocalDateTime): Auto-set on creation

### UpvoteRecord
- `upvoteId` (int, PK): Auto-generated identifier
- `replyId` (int): Which reply was upvoted
- `studentId` (int): Who upvoted it
- `createdAt` (LocalDateTime): When upvote happened

## API Endpoints

### Thread Endpoints

#### Create Thread
```http
POST /api/discussion/threads
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "courseId": 1,
  "lessonId": null,
  "authorId": 101,
  "title": "How to use loops?",
  "body": "What's the difference between for and while loops?"
}
```
Response: 201 CREATED with thread object

#### Get Threads by Course
```http
GET /api/discussion/threads/course/{courseId}
```
Returns pinned threads first, then sorted by creation date (newest first)

#### Get Threads by Lesson
```http
GET /api/discussion/threads/lesson/{lessonId}
```
Returns all threads linked to a specific lesson

#### Get Thread by ID
```http
GET /api/discussion/threads/{threadId}
```

#### Update Thread
```http
PUT /api/discussion/threads/{threadId}
Authorization: Bearer {JWT_TOKEN}
```

#### Delete Thread
```http
DELETE /api/discussion/threads/{threadId}
Authorization: Bearer {JWT_TOKEN}
PreAuthorize: INSTRUCTOR or ADMIN
```

#### Pin Thread
```http
PUT /api/discussion/threads/{threadId}/pin
Authorization: Bearer {JWT_TOKEN}
PreAuthorize: INSTRUCTOR or ADMIN
```

#### Unpin Thread
```http
PUT /api/discussion/threads/{threadId}/unpin
Authorization: Bearer {JWT_TOKEN}
PreAuthorize: INSTRUCTOR or ADMIN
```

#### Close Thread
```http
PUT /api/discussion/threads/{threadId}/close
Authorization: Bearer {JWT_TOKEN}
PreAuthorize: INSTRUCTOR or ADMIN
```

#### Reopen Thread
```http
PUT /api/discussion/threads/{threadId}/reopen
Authorization: Bearer {JWT_TOKEN}
PreAuthorize: INSTRUCTOR or ADMIN
```

#### Get Threads by Author
```http
GET /api/discussion/threads/author/{authorId}
```

#### Get Thread Count
```http
GET /api/discussion/threads/count/{courseId}
```

### Reply Endpoints

#### Post Reply
```http
POST /api/discussion/replies
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "threadId": 1,
  "authorId": 102,
  "body": "Use a for loop when you know the iterations..."
}
```
Response: 201 CREATED - Fails if thread is closed

#### Get Replies by Thread
```http
GET /api/discussion/replies/thread/{threadId}
```
Returns replies sorted by: acceptance status, upvotes (desc), creation time

#### Get Reply by ID
```http
GET /api/discussion/replies/{replyId}
```

#### Update Reply
```http
PUT /api/discussion/replies/{replyId}
Authorization: Bearer {JWT_TOKEN}
```

#### Delete Reply
```http
DELETE /api/discussion/replies/{replyId}
Authorization: Bearer {JWT_TOKEN}
PreAuthorize: INSTRUCTOR or ADMIN
```

#### Upvote Reply
```http
PUT /api/discussion/replies/{replyId}/upvote?studentId={studentId}
Authorization: Bearer {JWT_TOKEN}
PreAuthorize: STUDENT
```
- Fails if student already upvoted this reply
- Increments reply.upvotes by 1
- Creates UpvoteRecord for tracking

#### Accept Reply
```http
PUT /api/discussion/replies/{replyId}/accept
Authorization: Bearer {JWT_TOKEN}
PreAuthorize: INSTRUCTOR or ADMIN
```
- Only one accepted reply per thread
- Auto-unaccepts previous answer if exists

#### Unaccept Reply
```http
PUT /api/discussion/replies/{replyId}/unaccept
Authorization: Bearer {JWT_TOKEN}
PreAuthorize: INSTRUCTOR or ADMIN
```

#### Get Replies by Author
```http
GET /api/discussion/replies/author/{authorId}
```

## Database Schema

### discussion_threads Table
```sql
CREATE TABLE discussion_threads (
  thread_id INT PRIMARY KEY AUTO_INCREMENT,
  course_id INT NOT NULL,
  lesson_id INT,
  author_id INT NOT NULL,
  title VARCHAR(255) NOT NULL,
  body TEXT,
  is_pinned BOOLEAN DEFAULT FALSE,
  is_closed BOOLEAN DEFAULT FALSE,
  created_at DATETIME NOT NULL,
  updated_at DATETIME,
  FOREIGN KEY (course_id) REFERENCES courses(course_id)
);
```

### replies Table
```sql
CREATE TABLE replies (
  reply_id INT PRIMARY KEY AUTO_INCREMENT,
  thread_id INT NOT NULL,
  author_id INT NOT NULL,
  body TEXT,
  is_accepted BOOLEAN DEFAULT FALSE,
  upvotes INT DEFAULT 0,
  created_at DATETIME NOT NULL,
  FOREIGN KEY (thread_id) REFERENCES discussion_threads(thread_id) ON DELETE CASCADE
);
```

### upvote_records Table
```sql
CREATE TABLE upvote_records (
  upvote_id INT PRIMARY KEY AUTO_INCREMENT,
  reply_id INT NOT NULL,
  student_id INT NOT NULL,
  created_at DATETIME NOT NULL,
  FOREIGN KEY (reply_id) REFERENCES replies(reply_id) ON DELETE CASCADE,
  UNIQUE KEY unique_upvote (reply_id, student_id)
);
```

## Configuration

### application.properties
```properties
server.port=8088
server.servlet.context-path=/

spring.datasource.url=jdbc:mysql://localhost:3306/edulearn_discussion
spring.datasource.username=root
spring.datasource.password=root123

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

## Running the Service

### Prerequisites
- Java 17
- Maven 3.6+
- MySQL 8.0+

### Build
```bash
cd discussion-service
./mvnw clean package -DskipTests
```

### Run
```bash
./mvnw spring-boot:run
```

### Access Swagger UI
```
http://localhost:8088/swagger-ui.html
```

## Testing

### Run All Tests
```bash
./mvnw test
```

### Test Coverage

| Test Class | Type | Tests | Coverage |
|-----------|------|-------|----------|
| DiscussionServiceImplTest | Unit/Mockito | 18 | Service methods |
| DiscussionControllerTest | Integration | 21 | All endpoints |
| DiscussionRepositoriesTest | Repository | 21 | Query methods |
| **TOTAL** | | **60** | **100% critical paths** |

### Test Categories

#### Service Tests (18 tests)
- Thread creation, update, delete
- Thread pinning/unpinning
- Thread closing/reopening
- Reply posting with closed thread validation
- Reply upvoting with duplicate prevention
- Reply acceptance with single answer enforcement
- Author query filters

#### Controller Tests (21 tests)
- All CRUD endpoints
- Authentication/authorization validation
- Public vs. protected endpoints
- Error handling (404, 400)
- Role-based access (INSTRUCTOR, STUDENT)

#### Repository Tests (21 tests)
- CRUD operations for all entities
- Complex queries (ordering, filtering)
- Foreign key relationships
- Cascade delete validation
- Unique constraint enforcement

## Project Structure

```
discussion-service/
├── src/
│   ├── main/
│   │   ├── java/com/edulearn/discussion/
│   │   │   ├── controller/
│   │   │   │   └── DiscussionController.java
│   │   │   ├── service/
│   │   │   │   ├── DiscussionService.java (Interface)
│   │   │   │   └── DiscussionServiceImpl.java
│   │   │   ├── repository/
│   │   │   │   ├── ThreadRepository.java
│   │   │   │   ├── ReplyRepository.java
│   │   │   │   └── UpvoteRecordRepository.java
│   │   │   ├── entity/
│   │   │   │   ├── DiscussionThread.java
│   │   │   │   ├── Reply.java
│   │   │   │   └── UpvoteRecord.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JwtAuthFilter.java
│   │   │   │   └── JwtUtil.java
│   │   │   └── DiscussionServiceApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/edulearn/discussion/
│           ├── controller/
│           │   └── DiscussionControllerTest.java (21 tests)
│           ├── service/
│           │   └── DiscussionServiceImplTest.java (18 tests)
│           └── repository/
│               └── DiscussionRepositoriesTest.java (21 tests)
├── pom.xml
├── README.md
└── .gitignore
```

## Error Handling

Common error responses:

- **400 Bad Request**: Invalid input or thread is closed
- **403 Forbidden**: Insufficient permissions or non-enrolled user
- **404 Not Found**: Thread/reply not found
- **401 Unauthorized**: Missing or invalid JWT token
- **409 Conflict**: Already upvoted this reply

## Security Features

1. JWT-based authentication
2. Role-based access control (RBAC)
3. CORS configuration for localhost:4200
4. Public read endpoints for thread/reply lists
5. Protected write endpoints (authentication required)
6. Instructor-only moderation (pin/close/delete)
7. SQL injection prevention via parameterized queries

## Best Practices Implemented

1. **Service Pattern**: Interface + Implementation separation
2. **Repository Pattern**: Data access abstraction
3. **Transactional Integrity**: @Transactional for multi-step operations
4. **Cascade Delete**: Clean removal of related entities
5. **Duplicate Prevention**: UpvoteRecord unique constraint
6. **JPA Lifecycle**: @PrePersist/@PreUpdate for timestamps
7. **DTOs Ready**: Entity structure supports DTO conversion

## Troubleshooting

### Port 8088 Already in Use
```bash
# Find and kill process on port 8088
netstat -ano | findstr :8088
taskkill /PID {PID} /F
```

### Database Connection Failed
- Ensure MySQL is running: `services.msc` (Windows)
- Check credentials in application.properties
- Verify database exists: `CREATE DATABASE edulearn_discussion;`

### JWT Token Invalid
- Verify secret key matches auth-service
- Check token format: `Bearer {token}`
- Ensure token not expired (24-hour expiry)

## Future Enhancements

1. Mention system (@username notifications)
2. Nested replies (reply to reply)
3. Thread subscriptions and notifications
4. Spam detection and moderation
5. Search and full-text indexing
6. Trending topics by engagement
7. Email notifications for mentions
8. Markdown support in posts
9. Rate limiting to prevent spam
10. Analytics on forum engagement

## Integration Points

- **Auth Service**: JWT validation and user identification
- **Course Service**: courseId reference (data only)
- **Lesson Service**: lessonId reference (optional)

## Performance Optimization

1. Database indexes on:
   - courseId (for course threads)
   - lessonId (for lesson threads)
   - threadId (for replies)
   - authorId (for user posts)
   - studentId + replyId (for upvote uniqueness)

2. Query optimization:
   - Sorted queries in repositories
   - Lazy loading for related entities
   - Connection pooling with HikariCP

3. Caching candidates:
   - Popular threads cache
   - Most upvoted replies cache
   - Thread count per course
