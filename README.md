# Payment Service

Payment and Subscription Management Service for EduLearn LMS using Razorpay Payment Gateway.

## Features

- Razorpay payment gateway integration
- HMAC-SHA256 signature verification
- Subscription management (FREE/MONTHLY/ANNUAL)
- Payment status tracking (PENDING/SUCCESS/FAILED/REFUNDED)
- JWT authentication
- Swagger/OpenAPI documentation

## Prerequisites

- Java 17 or higher
- MySQL 8.0+
- Maven 3.6+
- Razorpay Account (free test account at https://razorpay.com)

## Setup

1. Clone the repository
2. Copy `.env.example` to `.env` and configure your database and Razorpay credentials
3. Build the project:
   ```bash
   .\mvnw.cmd clean install
   ```

## Running the Service

```bash
.\mvnw.cmd spring-boot:run
```

Service runs on `http://localhost:8085` (configurable via SERVER_PORT in .env)

## Database

- Creates automatic MySQL database: `edulearn_payment`
- Tables: `payments`, `subscriptions`

## API Documentation

Swagger UI: `http://localhost:8085/api/v1/swagger-ui.html`

## Key Endpoints

### Payment Management
- `POST /api/v1/payments/create-order` - Create Razorpay order
- `POST /api/v1/payments/verify` - Verify payment with signature
- `GET /api/v1/payments/student/{studentId}` - Get payment history
- `POST /api/v1/payments/refund/{paymentId}` - Refund payment

### Subscription Management
- `POST /api/v1/subscriptions/subscribe` - Subscribe to plan
- `DELETE /api/v1/subscriptions/cancel/{studentId}` - Cancel subscription
- `GET /api/v1/subscriptions/status/{studentId}` - Check subscription status
- `GET /api/v1/subscriptions/student/{studentId}` - Get subscription details

## Environment Variables

```env
SERVER_PORT=8085
DB_URL=jdbc:mysql://localhost:3306/edulearn_payment?...
DB_USERNAME=root
DB_PASSWORD=root123
RAZORPAY_KEY_ID=rzp_test_XXXXX
RAZORPAY_KEY_SECRET=XXXXX
JWT_SECRET=your_secret_key
JWT_EXPIRATION=86400000
```

## Testing

1. Use Postman or Swagger UI to test endpoints
2. Razorpay Test Mode: Use test credentials to avoid real payments
3. Test Card: 4111 1111 1111 1111

## Architecture

- **Entity**: Payment, Subscription
- **Repository**: PaymentRepository, SubscriptionRepository
- **Service**: PaymentService (Interface), PaymentServiceImpl (Implementation)
- **Controller**: PaymentController

## Security

- JWT Bearer token authentication
- HMAC-SHA256 signature verification for Razorpay
- Role-based access control (STUDENT, INSTRUCTOR, ADMIN)

## Dependencies

- Spring Boot 3.2.0
- Spring Data JPA
- MySQL Connector
- Razorpay Java SDK 1.4.3
- Spring Security
- JWT (JJWT)
- SpringDoc OpenAPI (Swagger)

## Project Structure

```
payment-service/
├── pom.xml
├── .env
├── .env.example
├── mvnw
├── mvnw.cmd
├── README.md
└── src/
    ├── main/
    │   ├── java/com/edulearn/payment/
    │   │   ├── PaymentServiceApplication.java
    │   │   ├── Payment.java
    │   │   ├── Subscription.java
    │   │   ├── PaymentRepository.java
    │   │   ├── SubscriptionRepository.java
    │   │   ├── PaymentService.java
    │   │   ├── PaymentServiceImpl.java
    │   │   └── PaymentController.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/edulearn/payment/
```

## Next Steps

1. Configure `.env` file with database and Razorpay credentials
2. Run `.\mvnw.cmd spring-boot:run`
3. Access Swagger UI at `http://localhost:8085/api/v1/swagger-ui.html`
4. Start making payment API calls

## Troubleshooting

### Port already in use
- Change `SERVER_PORT` in `.env` file

### Database connection failed
- Verify MySQL is running
- Check credentials in `.env`
- Ensure database user has proper permissions

### Razorpay integration failed
- Verify API keys are correct
- Use test keys (rzp_test_*) for development
- Check Razorpay dashboard for API key status
