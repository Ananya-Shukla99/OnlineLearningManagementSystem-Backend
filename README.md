# Progress Service

Student Progress Tracking and Certificate Management Service for EduLearn LMS using Apache PDFBox.

## Features

- Lesson progress tracking (watched seconds)
- Course completion percentage calculation
- Apache PDFBox certificate generation (professional PDF)
- Certificate verification with UUID codes (public endpoint)
- Automatic certificate issuance at 100% completion
- JWT authentication
- Swagger/OpenAPI documentation

## Prerequisites

- Java 17 or higher
- MySQL 8.0+
- Maven 3.6+

## Setup

1. Clone the repository
2. Copy `.env.example` to `.env` and configure your database
3. Build the project:
   ```bash
   .\mvnw.cmd clean install
   ```

## Running the Service

```bash
.\mvnw.cmd spring-boot:run
```

Service runs on `http://localhost:8086` (configurable via SERVER_PORT in .env)

## Database

- Creates automatic MySQL database: `edulearn_progress`
- Tables: `progress`, `certificates`

## API Documentation

Swagger UI: `http://localhost:8086/api/v1/swagger-ui.html`

## Key Endpoints

### Progress Tracking
- `PUT /api/v1/progress/track` - Track lesson watch time
- `PUT /api/v1/progress/complete` - Mark lesson complete
- `GET /api/v1/progress/course` - Get course progress %
- `GET /api/v1/progress/lesson` - Get lesson progress
- `GET /api/v1/progress/all/{studentId}` - Get all progress records

### Certificate Management
- `POST /api/v1/progress/certificates/issue` - Issue certificate
- `GET /api/v1/progress/certificates/student/{studentId}` - Get student certificates
- `GET /api/v1/progress/certificates/verify/{code}` - Verify certificate (PUBLIC - No Auth)

## Environment Variables

```env
SERVER_PORT=8086
DB_URL=jdbc:mysql://localhost:3306/edulearn_progress?...
DB_USERNAME=root
DB_PASSWORD=root123
CERTIFICATE_OUTPUT_PATH=src/main/resources/certificates/
JWT_SECRET=your_secret_key
JWT_EXPIRATION=86400000
```

## Testing

1. Use Postman or Swagger UI to test endpoints
2. Track lesson progress: Call `/track` endpoint
3. Mark lesson complete: Call `/complete` endpoint
4. Check progress: Call `/course` endpoint with studentId and courseId
5. When progress = 100%, certificate auto-generates
6. Verify certificate: Call `/certificates/verify/{code}` (no auth required)

## Certificate Features

- **PDF Generation**: Professional certificate with blue borders and centered text
- **Verification Code**: Unique code in format `EL-YYYY-CAT-XXXXXX`
- **Public Verification**: Anyone can verify certificate with code (no authentication needed)
- **Auto-Issuance**: Automatic certificate generation when course completion hits 100%
- **Storage**: PDFs stored in `src/main/resources/certificates/`

## Architecture

- **Entities**: Progress, Certificate
- **Repositories**: ProgressRepository, CertificateRepository
- **Service**: ProgressService (Interface), ProgressServiceImpl (Implementation with PDFBox)
- **Controller**: ProgressController

## Security

- JWT Bearer token authentication for most endpoints
- Public endpoint for certificate verification (no auth)
- Role-based access control

## Dependencies

- Spring Boot 3.2.0
- Spring Data JPA
- MySQL Connector
- Apache PDFBox 2.0.29
- Spring Security
- JWT (JJWT)
- SpringDoc OpenAPI (Swagger)

## Project Structure

```
progress-service/
├── pom.xml
├── .env
├── .env.example
├── mvnw
├── mvnw.cmd
├── README.md
└── src/
    ├── main/
    │   ├── java/com/edulearn/progress/
    │   │   ├── ProgressServiceApplication.java
    │   │   ├── Progress.java
    │   │   ├── Certificate.java
    │   │   ├── ProgressRepository.java
    │   │   ├── CertificateRepository.java
    │   │   ├── ProgressService.java
    │   │   ├── ProgressServiceImpl.java
    │   │   └── ProgressController.java
    │   └── resources/
    │       ├── application.properties
    │       └── certificates/
    └── test/
        └── java/com/edulearn/progress/
```

## Workflow

1. Student starts watching a lesson
2. Frontend calls `/api/v1/progress/track` every 30 seconds with `watchedSeconds`
3. Service updates progress in database
4. When lesson duration is reached, `isCompleted` is set to true
5. Service calculates course progress percentage
6. If progress == 100%, certificate is automatically generated
7. Certificate PDF is created and saved
8. Certificate record is stored in database
9. Student can view certificate or share verification code

## Certificate Verification Flow

1. Student receives certificate with verification code: `EL-2026-CS-8F2A9C`
2. Student shares code with others
3. Anyone can verify at: `/api/v1/progress/certificates/verify/EL-2026-CS-8F2A9C`
4. System returns certificate details without requiring authentication

## Next Steps

1. Configure `.env` file with database credentials
2. Run `.\mvnw.cmd spring-boot:run`
3. Access Swagger UI at `http://localhost:8086/api/v1/swagger-ui.html`
4. Start tracking student progress

## Troubleshooting

### Port already in use
- Change `SERVER_PORT` in `.env` file

### Database connection failed
- Verify MySQL is running
- Check credentials in `.env`
- Ensure database user has proper permissions

### Certificate not generating
- Check `CERTIFICATE_OUTPUT_PATH` in `.env`
- Ensure directory exists and is writable
- Check logs for PDFBox errors

### PDF not rendering
- Verify PDFBox dependency is installed
- Check certificate path configuration
- Ensure font files are available
