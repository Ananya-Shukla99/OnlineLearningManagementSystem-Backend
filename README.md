# OnlineLearningManagementSystem-Backend
An online learning management system. EduLearn - Learn Anytime, Grow Everywhere.

## Overview

EduLearn is a scalable, secure, and modular Learning Management System built using a microservices architecture. The backend is designed to support modern educational platforms with independent services, asynchronous communication, and strong security boundaries.

This system separates core business domains into individual microservices, allowing independent development, deployment, and scaling.

---

## Architecture

The backend follows a **microservices-based architecture** with the following key components:

* **API Gateway**

  * Central entry point for all client requests
  * Handles routing, authentication, and CORS configuration

* **Service Discovery (Eureka)**

  * Enables dynamic registration and discovery of services

* **Microservices (Domain-Based)**

  * Each service is independently deployable
  * Each service owns its database

* **Database per Service**

  * MySQL used for persistence
  * Ensures data isolation and loose coupling

* **Message Broker (RabbitMQ)**

  * Enables asynchronous communication between services
  * Used for event-driven workflows like enrollment and notifications

* **Authentication & Authorization**

  * JWT-based security
  * Token validation handled at the API Gateway level

* **Payment Integration**

  * Razorpay used for secure course payments

---

## Microservices Breakdown

Each microservice follows a layered architecture:

* **Controller Layer** – REST API endpoints
* **Service Layer** – Business logic
* **Repository Layer** – Database interaction (JPA)
* **Entity Layer** – Domain models

Typical services include:

* Authentication Service
* User Service
* Course Service
* Enrollment Service
* Payment Service
* Notification Service
* Content/Lesson Service
* Gateway Service

---

## Key Features

* Domain-driven microservices design
* Independent scaling of services
* Secure JWT-based authentication
* Asynchronous event-driven communication
* Fault isolation between services
* External payment integration
* Clean layered architecture for maintainability

---

## Technology Stack

* **Backend Framework:** Spring Boot
* **Architecture:** Microservices
* **Security:** Spring Security + JWT
* **Service Discovery:** Eureka
* **API Gateway:** Spring Cloud Gateway
* **Database:** MySQL
* **Messaging:** RabbitMQ
* **Build Tool:** Maven / Gradle
* **Payment Gateway:** Razorpay

---

## Project Structure

Each microservice follows a consistent structure:

```
service-name/
│── controller/
│── service/
│── repository/
│── entity/
│── config/
│── dto/
│── exception/
│── application.yml
```

---

## Setup and Installation

### Prerequisites

* Java 17+
* Maven or Gradle
* MySQL
* RabbitMQ
* Eureka Server running

---

### Steps to Run

1. Clone the repository

   ```
   git clone <repository-url>
   ```

2. Configure databases for each microservice in `application.yml`

3. Start infrastructure services:

   * Eureka Server
   * RabbitMQ
   * MySQL

4. Start services in order:

   * API Gateway
   * Core microservices

5. Verify services are registered in Eureka dashboard

---

## Authentication Flow

1. User logs in via API Gateway
2. Authentication service validates credentials
3. JWT token is generated
4. Token is sent to client
5. API Gateway validates token for all protected routes
6. Requests are forwarded to respective services

---

## Core Workflows

### Instructor Workflow

1. Create and manage courses
2. Upload lessons and resources
3. Publish courses

### Student Workflow

1. Register and log in
2. Browse available courses
3. Make payment via Razorpay
4. Enroll and access course content
5. Track progress

---

## Backend Testing

### Testing Strategy

The backend uses multiple levels of testing:

#### 1. Unit Testing

* Tests individual service methods
* Uses JUnit and Mockito

#### 2. Integration Testing

* Tests interaction between layers (Controller → Service → Repository)
* Uses Spring Boot Test

#### 3. API Testing

* Validates REST endpoints
* Tools:

  * Postman
  * Swagger (if enabled)

---

### Running Tests

Using Maven:

```
mvn test
```

Using Gradle:

```
gradle test
```

---

### Example Test Cases

* Authentication:

  * Valid login returns JWT
  * Invalid credentials return error

* Course Service:

  * Create course
  * Fetch published courses

* Enrollment:

  * Enrollment after successful payment
  * Access restriction for non-enrolled users

* Payment:

  * Payment success callback handling
  * Enrollment trigger via RabbitMQ event

---

## Error Handling

* Centralized exception handling using `@ControllerAdvice`
* Custom exception classes for business logic errors
* Standard HTTP status codes used

---

## Security

* JWT-based stateless authentication
* API Gateway enforces token validation
* Role-based access control (Student, Instructor, Admin)

---

## Future Enhancements

* Analytics and reporting service
* AI-based course recommendations
* Real-time notifications (WebSocket)
* Containerization with Docker
* CI/CD pipeline integration

---

## Contribution Guidelines

1. Create a feature branch
2. Commit changes with clear messages
3. Push and create a pull request
4. Ensure all tests pass

---
