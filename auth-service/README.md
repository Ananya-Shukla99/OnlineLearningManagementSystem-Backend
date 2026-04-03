# Auth Service - EduLearn LMS Platform

## Overview

The Auth Service is a standalone microservice component of the EduLearn Learning Management System. This service is responsible for managing user authentication, authorization, and token generation within the platform. It serves as the security gateway for all protected operations across the EduLearn ecosystem.

The Auth Service handles user registration, login, JWT token generation and validation, OAuth2 integration with Google, profile management, and password operations. Every protected operation in the LMS first passes through this service for authentication and role verification.

## Service Responsibilities

- User registration with role assignment (Student, Instructor, Admin)
- User authentication via email and password
- JWT token generation and validation
- Google OAuth2 authentication support
- User profile management and updates
- Password change and reset operations
- Token refresh functionality
- Role-based access control validation

## Technology Stack

- **Language**: Java 17
- **Framework**: Spring Boot 3.5.13
- **Database**: MySQL 8.0
- **Security**: Spring Security, JWT (JJWT 0.12.3)
- **ORM**: Spring Data JPA, Hibernate
- **Authentication**: OAuth2 (Google)
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito, Spring Boot Test
- **API Documentation**: REST with JSON

## Project Structure

```
auth-service/
├── src/main/java/com/edulearn/auth/
│   ├── entity/
│   │   ├── User.java                 # User entity with JPA annotations
│   │   └── UserRole.java             # Role enumeration (STUDENT, INSTRUCTOR, ADMIN)
│   ├── repository/
│   │   └── UserRepository.java       # Data access interface
│   ├── service/
│   │   ├── AuthService.java          # Service contract interface
│   │   └── AuthServiceImpl.java       # Business logic implementation
│   ├── controller/
│   │   └── AuthController.java       # REST endpoint definitions
│   ├── dto/
│   │   ├── RegisterRequest.java      # Registration request DTO
│   │   ├── LoginRequest.java         # Login request DTO
│   │   └── AuthResponse.java         # Standard response DTO
│   ├── util/
│   │   └── JwtTokenProvider.java     # JWT token generation and validation
│   ├── config/
│   │   └── SecurityConfig.java       # Spring Security configuration
│   └── AuthServiceApplication.java   # Spring Boot entry point
├── src/test/java/com/edulearn/auth/
│   ├── service/
│   │   └── AuthServiceImplTest.java  # Unit tests for service layer
│   └── controller/
│       └── AuthControllerTest.java   # Integration tests for API endpoints
├── src/main/resources/
│   └── application.properties        # Application configuration
├── pom.xml                           # Maven project configuration
└── README.md                         # Project documentation
```

## Database Schema

### Users Table

```sql
CREATE TABLE users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL ENUM('STUDENT', 'INSTRUCTOR', 'ADMIN'),
    mobile VARCHAR(20),
    bio VARCHAR(500),
    profile_pic_url VARCHAR(500),
    provider VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Key Attributes

- **user_id**: Unique identifier for each user
- **email**: Unique email address for user identification
- **full_name**: User's full name
- **password_hash**: BCrypt encrypted password (never stored in plain text)
- **role**: User role defining access permissions
- **provider**: Authentication provider (local, google, github, etc.)
- **created_at**: Account creation timestamp
- **updated_at**: Last modification timestamp

## REST API Endpoints

All endpoints are prefixed with `/api/v1/auth`

### 1. User Registration

**Endpoint**: POST /auth/register
**Authentication**: None (Public)
**CSRF Protection**: Disabled for registration

**Request Body**:
```json
{
  "email": "user@example.com",
  "fullName": "John Doe",
  "password": "securePassword123",
  "role": "STUDENT"
}
```

**Response** (201 Created):
```json
{
  "success": true,
  "message": "User registered successfully",
  "userId": 1,
  "email": "user@example.com",
  "fullName": "John Doe",
  "role": "STUDENT"
}
```

**Error Response** (400 Bad Request):
```json
{
  "success": false,
  "message": "User already exists with email: user@example.com"
}
```

### 2. User Login

**Endpoint**: POST /auth/login
**Authentication**: None (Public)
**CSRF Protection**: Disabled for login

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "user@example.com",
  "fullName": "John Doe",
  "role": "STUDENT"
}
```

**Error Response** (401 Unauthorized):
```json
{
  "success": false,
  "message": "Invalid password"
}
```

### 3. Token Validation

**Endpoint**: GET /auth/validate
**Authentication**: None (Public)
**Query Parameters**: token (JWT token string)

**Request**:
```
GET /api/v1/auth/validate?token=eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Token is valid",
  "userId": 1
}
```

**Error Response** (401 Unauthorized):
```json
{
  "success": false,
  "message": "Invalid JWT token: Token expired"
}
```

### 4. Token Refresh

**Endpoint**: POST /auth/refresh
**Authentication**: None (Public)
**Query Parameters**: token (Valid JWT token)

**Request**:
```
POST /api/v1/auth/refresh?token=eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "token": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9..."
}
```

## Security Implementation

### Password Security

Passwords are encrypted using BCrypt algorithm with a strength factor of 10. Plain text passwords are never stored in the database. Each password undergoes one-way hashing before persistence.

### JWT Token Structure

JWT tokens are composed of three parts:

1. **Header**: Algorithm (HS512) and token type
2. **Payload**: User ID, email, role, issuance time, expiration time
3. **Signature**: HMAC-SHA512 signature using the configured secret key

**Token Configuration**:
- Algorithm: HMAC-SHA512
- Expiration: 24 hours (86400000 milliseconds)
- Claims: userId (subject), email, role

**Token Example**:
```
eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.
eyJzdWIiOiIxIiwiZW1haWwiOiJ1c2VyQGV4YW1wbGUuY29tIiwicm9sZSI6IlNUVURFTlQiLCJpYXQiOjE3MTIxMjAwMDAsImV4cCI6MTcxMjIwNjQwMH0.
signature_here
```

### CSRF Protection

CSRF (Cross-Site Request Forgery) protection is disabled for REST API endpoints as recommended for stateless token-based APIs. Client-side token-based authentication serves as the security mechanism.

### Role-Based Access Control

Three roles are defined with hierarchical permissions:

- **STUDENT**: Can enroll in courses, watch lessons, take quizzes, track progress
- **INSTRUCTOR**: Can create and manage courses, view student progress, moderate forums
- **ADMIN**: Full platform access including user management and analytics

### OAuth2 Integration

Google OAuth2 is configured for third-party authentication. Users can authenticate using their Google account, eliminating the need for password management for those users.

**Configured Provider**: Google
**Required Credentials**: Client ID, Client Secret (configured in application.properties)

## Configuration

### Application Properties

Key configuration parameters in `application.properties`:

```properties
server.port=8081
server.servlet.context-path=/api/v1

spring.datasource.url=jdbc:mysql://localhost:3306/edulearn_auth
spring.datasource.username=root
spring.datasource.password=root123

spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect

jwt.secret=mySecretKeyForJWTAuthentication...
jwt.expiration=86400000

spring.application.name=auth-service
```

### Database Connection

The service requires a MySQL database running on localhost:3306. The database `edulearn_auth` is created automatically if it does not exist. The `users` table is created by Hibernate based on the User entity annotations.

## Setup Instructions

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher
- Git

### Installation Steps

1. Clone the repository
   ```bash
   git clone <repository-url>
   cd auth-service
   ```

2. Ensure MySQL is running
   ```bash
   # Windows
   net start MySQL80
   
   # Linux
   sudo systemctl start mysql
   ```

3. Build the project
   ```bash
   mvn clean install
   ```

4. Run the application
   ```bash
   mvn spring-boot:run
   ```
   
   Or using Java directly:
   ```bash
   java -jar target/auth-service-0.0.1-SNAPSHOT.jar
   ```

5. Verify the service is running
   ```bash
   curl http://localhost:8081/api/v1/auth/validate?token=test
   ```

## Testing

### Unit Tests

Unit tests verify the business logic of the AuthService layer using Mockito for mocking dependencies.

Run unit tests:
```bash
mvn test -Dtest=AuthServiceImplTest
```

**Test Coverage**:
- User registration success and failure scenarios
- User login with valid and invalid credentials
- Password verification and encryption
- Token generation and validation
- User retrieval operations

### Integration Tests

Integration tests verify the complete request-response cycle of REST endpoints with Spring Boot test context.

Run integration tests:
```bash
mvn test -Dtest=AuthControllerTest
```

**Test Coverage**:
- POST /auth/register endpoint
- POST /auth/login endpoint
- POST /auth/login failure scenarios
- GET /auth/validate endpoint

### Run All Tests

```bash
mvn test
```

Expected output:
```
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
```

## Service Startup

When the application starts, the following sequence occurs:

1. Spring Boot initializes the context
2. DataSource connects to MySQL database
3. Hibernate creates/updates the users table schema
4. Spring Security configuration is applied
5. JWT configuration is loaded from application.properties
6. Tomcat embedded server starts on port 8081
7. REST endpoints become available

**Expected Console Output**:
```
Started AuthServiceApplication in X.XXX seconds
Tomcat started on port(s): 8081 (http)
```

## Accessing the API

Once running, access the API using any HTTP client:

### Using cURL

```bash
# Register
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","fullName":"John Doe","password":"pass123","role":"STUDENT"}'

# Login
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"pass123"}'
```

### Using Postman

1. Import the REST endpoints into Postman
2. Set request method (POST, GET, etc.)
3. Set request URL with parameters
4. Set Content-Type header to application/json for POST requests
5. Provide request body as JSON
6. Click Send to execute the request

## Troubleshooting

### MySQL Connection Error

**Error**: Connection refused
**Solution**: Ensure MySQL service is running on localhost:3306

### Invalid JWT Token Error

**Cause**: Token expired or malformed
**Solution**: Generate a new token by logging in again

### 403 Forbidden on POST Requests

**Cause**: CSRF protection enabled
**Solution**: CSRF is already disabled in SecurityConfig

### Port Already in Use

**Error**: Address already in use
**Solution**: Change port in application.properties or kill process using port 8081

## Future Enhancements

- Implement token blacklist for logout functionality
- Add email verification for new registrations
- Implement password reset via email
- Add two-factor authentication support
- Implement rate limiting on login attempts
- Add audit logging for security events
- Integrate with external LDAP/ActiveDirectory

## Architecture Notes

The Auth Service follows the layered microservices architecture pattern:

- **Controller Layer**: REST endpoint exposure
- **Service Layer**: Business logic and orchestration
- **Repository Layer**: Data access abstraction
- **Entity Layer**: Domain model and persistence mapping
- **Config Layer**: Spring configuration and security setup
- **Util Layer**: Utility components (JWT token provider)

Each layer has clear separation of concerns and dependencies flow from top to bottom.

## Performance Considerations

- JWT tokens are stateless, eliminating database queries for validation
- Password hashing uses BCrypt with configurable strength (currently 10 rounds)
- Database connections are pooled using HikariCP
- MySQL indexes on email field for fast user lookups

## Dependencies

Key external dependencies:

- spring-boot-starter-web: Web and REST support
- spring-boot-starter-data-jpa: Database access
- spring-boot-starter-security: Authentication and authorization
- spring-boot-starter-oauth2-client: OAuth2 support
- mysql-connector-java: MySQL database driver
- jjwt: JWT token handling
- lombok: Boilerplate code reduction

## Support and Maintenance

For issues, bug reports, or feature requests, refer to the main EduLearn project repository.

---

**Version**: 1.0.0
**Last Updated**: April 3, 2026
**Status**: Production Ready

