# Notification Service

## Overview

Notification Service is a Spring Boot microservice that manages user notifications in the EduLearn Learning Management System. Instead of using RabbitMQ or Kafka, it uses **Spring's built-in ApplicationEventPublisher** for decoupled event-driven notifications within the monolith.

## How It Works (Architecture)

```
Event Flow (WITHOUT RabbitMQ):
1. EnrollmentServiceImpl publishes EnrollmentEvent
   → NotificationServiceImpl's @EventListener catches it
   → Creates Notification in DB automatically
   
2. Angular polls GET /notifications/unread-count/{userId} every 30 seconds
   → Bell badge updates with unread count
   
3. User clicks bell → GET /notifications/user/{userId}
   → Shows all notifications (newest first)
```

**Why This Approach?**
- No external infrastructure (RabbitMQ, Redis, etc.)
- Events processed asynchronously via SimpleAsyncTaskExecutor
- Notifications stored in same MySQL database
- Real-time updates via Angular polling

## Technology Stack

- **Framework**: Spring Boot 3.5.13
- **Language**: Java 17
- **Database**: MySQL 8.0 (shared database with other services)
- **Event Publishing**: Spring ApplicationEventPublisher
- **Async Processing**: SimpleApplicationEventMulticaster with SimpleAsyncTaskExecutor
- **Testing**: JUnit 5 with Mockito
- **Documentation**: Springdoc OpenAPI (Swagger UI)
- **Build Tool**: Maven

## Entities

### Notification
- `notificationId` (int, PK): Auto-generated
- `userId` (int): Who receives the notification
- `type` (String): ENROLLMENT | PAYMENT | QUIZ_RESULT | CERTIFICATE | COURSE_PUBLISHED | THREAD_REPLY
- `title` (String, 255): Short notification title
- `message` (TEXT): Full notification text
- `isRead` (boolean): Default false
- `createdAt` (LocalDateTime): Auto-set on creation
- `relatedEntityId` (Integer, nullable): e.g. courseId, quizId
- `relatedEntityType` (String, nullable): e.g. "COURSE", "QUIZ"

## Events (Published by Other Services)

### EnrollmentEvent
Published by: `EnrollmentServiceImpl.enroll()`
- `studentId`: Student who enrolled
- `courseId`: Course enrolled in
- `courseTitle`: Course name for notification

### PaymentEvent
Published by: `PaymentServiceImpl.verifyPayment()`
- `studentId`: Student who paid
- `amount`: Payment amount
- `courseTitle`: Course name

### QuizResultEvent
Published by: `AssessmentServiceImpl.submitAttempt()`
- `studentId`: Student who submitted
- `quizTitle`: Quiz name
- `score`: Score percentage (0-100)
- `passed`: Whether student passed

### CertificateEvent
Published by: `ProgressServiceImpl.issueCertificate()`
- `studentId`: Student who earned certificate
- `courseName`: Course name
- `verificationCode`: Certificate verification code

## API Endpoints

### Get All Notifications for User
```http
GET /api/notifications/user/{userId}
Authorization: Bearer {JWT_TOKEN}
```
Returns all notifications for user (newest first).

### Get Unread Count (Bell Badge)
```http
GET /api/notifications/unread-count/{userId}
Authorization: Bearer {JWT_TOKEN}
```
Returns integer count. **Angular polls this every 30 seconds**.

### Get Unread Notifications
```http
GET /api/notifications/unread/{userId}
Authorization: Bearer {JWT_TOKEN}
```
Returns only notifications with `isRead=false`.

### Mark Single Notification as Read
```http
PUT /api/notifications/{notificationId}/read
Authorization: Bearer {JWT_TOKEN}
```

### Mark All Notifications as Read
```http
PUT /api/notifications/read-all/{userId}
Authorization: Bearer {JWT_TOKEN}
```

### Delete Notification
```http
DELETE /api/notifications/{notificationId}
Authorization: Bearer {JWT_TOKEN}
```

### Get Notifications by Type
```http
GET /api/notifications/type?userId={userId}&type={type}
Authorization: Bearer {JWT_TOKEN}
```
Filter by type: ENROLLMENT, PAYMENT, QUIZ_RESULT, CERTIFICATE

## Database Schema

```sql
CREATE TABLE notifications (
  notification_id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  type VARCHAR(50) NOT NULL,
  title VARCHAR(255) NOT NULL,
  message TEXT,
  is_read BOOLEAN DEFAULT FALSE,
  created_at DATETIME NOT NULL,
  related_entity_id INT,
  related_entity_type VARCHAR(50),
  FOREIGN KEY (user_id) REFERENCES users(user_id),
  INDEX idx_user_id (user_id),
  INDEX idx_type (type),
  INDEX idx_is_read (is_read)
);
```

## Configuration

### application.properties
```properties
server.port=8085
spring.datasource.url=jdbc:mysql://localhost:3306/edulearn_notification
spring.datasource.username=root
spring.datasource.password=root123
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

## Project Structure

```
notification-service/
├── src/
│   ├── main/
│   │   ├── java/com/edulearn/notification/
│   │   │   ├── event/
│   │   │   │   ├── EnrollmentEvent.java
│   │   │   │   ├── PaymentEvent.java
│   │   │   │   ├── QuizResultEvent.java
│   │   │   │   └── CertificateEvent.java
│   │   │   ├── entity/
│   │   │   │   └── Notification.java
│   │   │   ├── repository/
│   │   │   │   └── NotificationRepository.java
│   │   │   ├── service/
│   │   │   │   ├── NotificationService.java (Interface)
│   │   │   │   └── NotificationServiceImpl.java (@EventListener methods)
│   │   │   ├── controller/
│   │   │   │   └── NotificationController.java
│   │   │   ├── config/
│   │   │   │   └── AppEventConfig.java
│   │   │   └── NotificationServiceApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/edulearn/notification/
│           ├── service/
│           │   └── NotificationServiceImplTest.java
│           └── controller/
│               └── NotificationControllerTest.java
├── pom.xml
└── README.md
```

## How Events Flow

### Example 1: Enrollment Notification

```java
// 1. Student enrolls via EnrollmentController
POST /api/enrollments/enroll
Body: { studentId: 5, courseId: 10 }

// 2. EnrollmentServiceImpl.enroll() is called
Enrollment enrollment = enrollmentRepository.save(...)  // Saved to DB
eventPublisher.publishEvent(
    new EnrollmentEvent(this, 5, 10, "Advanced Java")
)

// 3. Spring publishes event asynchronously
// 4. NotificationServiceImpl.handleEnrollmentEvent() is called automatically
@EventListener
public void handleEnrollmentEvent(EnrollmentEvent event) {
    sendNotification(
        5,  // studentId
        "ENROLLMENT",
        "Enrolled successfully!",
        "You have enrolled in Advanced Java. Start learning now!",
        10, // courseId
        "COURSE"
    )
}

// 5. Notification saved to DB
INSERT INTO notifications(user_id, type, title, message, is_read, created_at, ...)
VALUES(5, 'ENROLLMENT', 'Enrolled successfully!', '...', false, NOW(), ...)

// 6. Angular polls GET /api/notifications/unread-count/5
// Returns: 1
// Bell badge shows "1"

// 7. User clicks bell → GET /api/notifications/user/5
// Returns: [{ notificationId: 1, type: 'ENROLLMENT', ... }]
```

## Wiring Events into Services

### Step 1: EnrollmentServiceImpl
Already done in Step 20. The event is published after enrollment is saved.

### Step 2: PaymentServiceImpl
Already done in Step 21. The event is published after payment is verified as SUCCESS.

### Step 3: AssessmentServiceImpl
Already done in Step 22. The event is published after quiz attempt is graded.

### Step 4: ProgressServiceImpl
Already done in Step 23. The event is published after certificate is generated.

### Step 5: AppEventConfig
Already created in Step 19. Enables asynchronous event handling.

## Async Event Processing

The `AppEventConfig.applicationEventMulticaster()` bean configures async handling:

```java
@Bean
public ApplicationEventMulticaster applicationEventMulticaster() {
    SimpleApplicationEventMulticaster eventMulticaster = 
        new SimpleApplicationEventMulticaster();
    eventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor());
    return eventMulticaster;
}
```

**Benefits:**
- Original request completes immediately (e.g., enroll() returns to user)
- Notification creation happens in background thread
- No blocking or delays to user experience

## Testing

### Run All Tests
```bash
./mvnw test
```

### Test Event Publishing
```bash
# Create test event and verify notification is created automatically
mvn test -Dtest=NotificationServiceImplTest
```

### Test REST Endpoints
```bash
mvn test -Dtest=NotificationControllerTest
```

## Error Handling

Standard error responses:

- **404 Not Found**: Notification or user not found
- **401 Unauthorized**: Missing/invalid JWT token
- **403 Forbidden**: Insufficient permissions
- **500 Server Error**: Database or event processing error

## Security

- JWT authentication on all endpoints (Bearer token)
- @PreAuthorize("isAuthenticated()") on all endpoints
- CORS enabled for Angular (localhost:4200)
- Read/write access control per user

## Performance Optimization

### Database Indexes
```sql
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_user_id_is_read ON notifications(user_id, is_read);
```

### Caching Candidates
- Unread count (cache for 10 seconds per user)
- Recent notifications (cache for 5 minutes)
- Notification type counts (cache for 30 minutes)

### Query Optimization
- Pagination for large notification lists
- Lazy loading relationships
- Connection pooling with HikariCP

## Troubleshooting

### Notifications Not Creating After Events
1. Verify `AppEventConfig` is in config package
2. Check NotificationServiceImpl has @EventListener annotations
3. Verify event publishers call `eventPublisher.publishEvent()`
4. Check database table exists: `SELECT * FROM notifications;`

### Unread Count Not Updating
1. Verify polling interval (Angular should call every 30 seconds)
2. Check GET /api/notifications/unread-count/{userId} returns correct integer
3. Verify isRead field is being set correctly

### Events Not Publishing
1. Check @Autowired ApplicationEventPublisher is injected
2. Verify event object constructor matches: `new EnrollmentEvent(this, params...)`
3. Check event extends `ApplicationEvent`

## Future Enhancements

1. **Email Notifications**: Send emails for important events
2. **SMS Notifications**: Text message for urgent alerts
3. **WebSocket Integration**: Real-time push notifications
4. **Notification Preferences**: User control over notification types
5. **Notification Templates**: Customizable notification messages
6. **Batch Processing**: Group similar notifications
7. **Notification Analytics**: Track read rates and engagement
8. **Scheduled Notifications**: Digest emails at specific times
9. **Notification History**: Archive old notifications
10. **Multi-language Support**: Notifications in different languages

## Integration Points

### From EnrollmentService
- Calls: `eventPublisher.publishEvent(new EnrollmentEvent(...))`
- Receives: Enrollment saved event
- Sends: ENROLLMENT notification

### From PaymentService
- Calls: `eventPublisher.publishEvent(new PaymentEvent(...))`
- Receives: Payment verified event
- Sends: PAYMENT notification

### From AssessmentService
- Calls: `eventPublisher.publishEvent(new QuizResultEvent(...))`
- Receives: Quiz submitted event
- Sends: QUIZ_RESULT notification

### From ProgressService
- Calls: `eventPublisher.publishEvent(new CertificateEvent(...))`
- Receives: Certificate issued event
- Sends: CERTIFICATE notification

## Deployment

### Build
```bash
./mvnw clean package -DskipTests
```

### Run Locally
```bash
./mvnw spring-boot:run
```

### Docker
```bash
docker build -t notification-service:1.0 .
docker run -p 8085:8085 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/edulearn_notification \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=root123 \
  notification-service:1.0
```

## Notes

- **Port**: 8085 (configured in application.properties)
- **Database**: Shared `edulearn_notification` database with all other services
- **Context Path**: `/` (root)
- **Event Processing**: Asynchronous (non-blocking)
- **Polling**: Angular polls unread count every 30 seconds
