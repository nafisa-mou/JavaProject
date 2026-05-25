# BloodLink - Complete Implementation Guide

## Project Overview

BloodLink is a **comprehensive AI-powered Blood Donor & Patient Connection Platform** that connects blood donors with patients in need through real-time matching, WebSocket-based communication, and intelligent recommendations.

### Key Statistics
- **47 Java Classes** (Entities, Services, Controllers, Security, Config)
- **60+ REST API Endpoints** across 8 controllers
- **9 Service Layer Classes** with 3,300+ lines of business logic
- **10 JPA Repository Interfaces** with 100+ custom @Query methods
- **Full-Stack Tech Stack**: Spring Boot 3.1.5, MySQL 8.0, JWT, WebSocket, Swagger/OpenAPI
- **Production-Ready**: Docker deployment, comprehensive testing, monitoring/metrics

## Part 1: Backend Setup

### 1.1 Database Setup

#### Option A: MySQL CLI
```bash
# Login to MySQL
mysql -u root -p

# Create database
CREATE DATABASE bloodlink_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bloodlink_db;

# Run schema script
source src/main/resources/db/V1_Initial_Schema.sql;

# Insert sample data
source src/main/resources/db/V2_Sample_Data.sql;

# Verify
SHOW TABLES;
SELECT COUNT(*) FROM users;
```

#### Option B: Docker Compose
```bash
# Start MySQL and PhpMyAdmin
docker-compose -f docker-compose.yml -p bloodlink up -d --profile dev-admin

# Access PhpMyAdmin: http://localhost:8081
# MySQL will automatically initialize with SQL scripts from docker-compose volume

# Stop services
docker-compose -p bloodlink down
```

#### Database Structure
- **users** (Single Table Inheritance): Stores Donor, Patient, Admin accounts
- **blood_requests**: Blood request lifecycle (PENDING → ACCEPTED → COMPLETED)
- **medical_records**: Patient medical history and conditions
- **donation_history**: Donor donation records with 56-day eligibility tracking
- **chats**: Chat conversations between users
- **messages**: Chat messages with delivery/read tracking
- **notifications**: System notifications for users
- **donor_reviews**: Donor reliability ratings and reviews
- **refresh_tokens**: JWT refresh token storage for token rotation
- **audit_log**: System audit trail for admin activities

### 1.2 Application Configuration

Edit `src/main/resources/application.properties`:

```properties
# Database Connection
spring.datasource.url=jdbc:mysql://localhost:3306/bloodlink_db?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=your_mysql_password

# JWT Configuration - Change in production!
app.jwt.secret=YourVeryLongSecureSecretKeyWith256BitsForProductionUse

# Email Service (Optional for password reset)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password

# Base URL for email links
app.base-url=http://localhost:3000
```

### 1.3 Build and Run Backend

```bash
# Navigate to project
cd BloodLink

# Build with Maven
./mvnw clean package -DskipTests

# Run application
./mvnw spring-boot:run

# Application starts on http://localhost:8080

# Verify running
curl http://localhost:8080/actuator/health
```

### 1.4 Access API Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **Actuator Health**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics
- **Prometheus**: http://localhost:8080/actuator/prometheus

## Part 2: Frontend Setup

### 2.1 Install Dependencies

```bash
cd frontend

# Install Node packages
npm install

# If using npm version < 7, you may need:
npm ci
```

### 2.2 Configure Frontend

Create `.env` file in frontend directory:

```
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_WS_URL=ws://localhost:8080/ws/chat
REACT_APP_WS_NOTIFY_URL=ws://localhost:8080/ws/notify
```

### 2.3 Start Development Server

```bash
# Start React development server
npm start

# Frontend runs on http://localhost:3000
```

### 2.4 Available Components

- **LoginComponent**: User login with JWT token management
- **RegisterDonorComponent**: Donor registration with location services
- **DonorSearchComponent**: Find donors by blood group and geolocation
- **BloodRequestComponent**: Create and manage blood requests
- **ChatComponent**: Real-time messaging via WebSocket
- **NotificationComponent**: Real-time notifications display

## Part 3: Testing

### 3.1 Run Unit and Integration Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=AuthControllerTest

# Run with code coverage
./mvnw clean test jacoco:report
```

### 3.2 Test Sample Data

**Donor Accounts:**
- john.doe@example.com / password123 (O+)
- sarah.smith@example.com / password123 (A+)

**Patient Accounts:**
- alice.thompson@example.com / password123

**Test Blood Request:**
- Create request for O+ blood group
- System will match with available donors

### 3.3 Manual Testing

```bash
# Register Donor
curl -X POST http://localhost:8080/api/auth/register-donor \
  -H "Content-Type: application/json" \
  -d '{
    "firstName":"John",
    "lastName":"Doe",
    "email":"john@example.com",
    "password":"secure123",
    "bloodGroup":"O+",
    "city":"New York",
    "state":"NY"
  }'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email":"john@example.com",
    "password":"secure123"
  }'

# Get Donors
curl http://localhost:8080/api/donors \
  -H "Authorization: Bearer {jwtToken}"

# Create Blood Request
curl -X POST http://localhost:8080/api/blood-requests \
  -H "Authorization: Bearer {patientToken}" \
  -H "Content-Type: application/json" \
  -d '{
    "bloodGroup":"O+",
    "unitsNeeded":2,
    "emergencyLevel":"URGENT",
    "medicalReason":"Emergency Surgery"
  }'
```

## Part 4: Docker Deployment

### 4.1 Build Docker Image

```bash
# Build image
docker build -t bloodlink:1.0.0 .

# Tag for registry
docker tag bloodlink:1.0.0 your-registry/bloodlink:1.0.0

# Push to registry (optional)
docker push your-registry/bloodlink:1.0.0
```

### 4.2 Run with Docker Compose

```bash
# Start full stack (MySQL + Spring Boot app)
docker-compose -f docker-compose.yml -p bloodlink up -d --profile full-stack

# View logs
docker-compose -p bloodlink logs -f bloodlink-app

# Access application
# - Backend: http://localhost:8080
# - Swagger: http://localhost:8080/swagger-ui.html

# Stop services
docker-compose -p bloodlink down

# Stop and remove volumes (full cleanup)
docker-compose -p bloodlink down -v
```

### 4.3 Docker Compose Profiles

```bash
# Start only MySQL
docker-compose up -d

# Start MySQL + PhpMyAdmin for database management
docker-compose up -d --profile dev-admin
# Access PhpMyAdmin at http://localhost:8081

# Start full stack (MySQL + Spring Boot app)
docker-compose up -d --profile full-stack
```

## Part 5: API Endpoints Reference

### Authentication Endpoints
```
POST   /api/auth/register-donor        - Register new donor
POST   /api/auth/register-patient      - Register new patient
POST   /api/auth/login                 - Login (returns JWT token)
POST   /api/auth/refresh-token         - Refresh expiring JWT token
POST   /api/auth/change-password/{id}  - Change password
POST   /api/auth/forgot-password       - Request password reset
POST   /api/auth/reset-password        - Complete password reset
POST   /api/auth/logout                - Logout (client-side token removal)
```

### Donor Endpoints
```
GET    /api/donors                     - Get all donors
GET    /api/donors/{id}                - Get donor profile
GET    /api/donors/search?bg=O+        - Search by blood group
GET    /api/donors/nearby?lat=40.7&lon=-74.0&radius=50 - Find nearby donors
GET    /api/donors/{id}/profile        - Get full donor profile with ratings
GET    /api/donors/{id}/score          - Get reliability score (0-100)
GET    /api/donors/{id}/eligible       - Check donation eligibility
PUT    /api/donors/{id}                - Update donor profile (authenticated)
PUT    /api/donors/{id}/availability   - Set availability status
POST   /api/donors/{id}/donation       - Record donation (admin only)
```

### Blood Request Endpoints
```
GET    /api/blood-requests/pending     - Get pending requests
GET    /api/blood-requests/critical    - Get critical requests
GET    /api/blood-requests/{id}        - Get request details
GET    /api/blood-requests/expired     - Get expired requests
GET    /api/blood-requests/{id}/suitable-donors - Find matching donors
GET    /api/blood-requests/{id}/priority-score  - Calculate priority
POST   /api/blood-requests             - Create blood request
PUT    /api/blood-requests/{id}/accept - Accept request as donor
PUT    /api/blood-requests/{id}/decline- Decline request
PUT    /api/blood-requests/{id}/complete-Complete request
GET    /api/blood-requests/statistics  - Get aggregate statistics
```

### Chat & Messaging Endpoints
```
GET    /api/chats                      - Get user's chats
GET    /api/chats/{id}                 - Get chat details
POST   /api/chats/start                - Start new chat
PUT    /api/chats/{id}/archive         - Archive chat
PUT    /api/chats/{id}/block           - Block user chat
GET    /api/messages/chat/{chatId}     - Get chat messages
POST   /api/messages                   - Send message
PUT    /api/messages/seen              - Mark as seen
GET    /api/messages/unread-count      - Get unread message count
DELETE /api/messages/{id}              - Delete message
GET    /api/messages/search?q=text     - Search messages
```

### Notification Endpoints
```
GET    /api/notifications              - Get notifications
GET    /api/notifications/unread       - Get unread notifications
PUT    /api/notifications/{id}/read    - Mark as read
PUT    /api/notifications/read-all     - Mark all as read
DELETE /api/notifications/{id}         - Delete notification
GET    /api/notifications/type/{type}  - Get by type
```

### Admin Endpoints (Admin role required)
```
GET    /api/admin/dashboard            - Admin dashboard stats
GET    /api/admin/users                - Get all users (paginated)
GET    /api/admin/users/{id}           - Get user details
PUT    /api/admin/users/{id}/status    - Update user status
DELETE /api/admin/users/{id}           - Delete user
GET    /api/admin/system-health        - System health check
GET    /api/admin/logs                 - Get application logs
```

## Part 6: WebSocket Connections

### Chat WebSocket

**Connect:**
```javascript
const socket = new SockJS('http://localhost:8080/ws/chat');
const stompClient = Stomp.over(socket);
stompClient.connect({ Authorization: `Bearer ${token}` }, onConnect);
```

**Subscribe to chat messages:**
```javascript
stompClient.subscribe(`/user/${userId}/queue/chat`, (message) => {
  const chatMessage = JSON.parse(message.body);
  console.log('Received:', chatMessage);
});
```

**Send chat message:**
```javascript
stompClient.send('/app/chat.sendMessage', {}, JSON.stringify({
  chatId: 123,
  content: 'Hello, are you available?',
  senderId: userId
}));
```

**Destinations:**
- `/app/chat.sendMessage` - Send chat message
- `/app/chat.startChat` - Initiate chat
- `/app/status.online` - Set online status
- `/app/status.offline` - Set offline status
- `/user/{userId}/queue/chat` - Receive messages
- `/topic/notifications` - Broadcast notifications

## Part 7: Security Best Practices

### JWT Token Security
```properties
# Production checklist:
1. Use strong secret key (256+ bits): generate with OpenSSL
   openssl rand -base64 32
2. Enable HTTPS/TLS in production
3. Set appropriate token expiration times
4. Implement token refresh mechanism (already implemented)
5. Store tokens securely in frontend (localStorage or secure cookie)
```

### Database Security
```sql
-- Create non-root user with limited permissions
CREATE USER 'bloodlink_user'@'localhost' IDENTIFIED BY 'strong_password';
GRANT SELECT, INSERT, UPDATE, DELETE ON bloodlink_db.* TO 'bloodlink_user'@'localhost';
FLUSH PRIVILEGES;

-- Enable SSL for database connections
-- Use connection pool authentication
```

### API Security
```properties
# Spring Security Configuration (already configured)
- CSRF protection disabled for stateless REST API
- CORS configuration restricted to known origins
- HTTP Basic auth disabled (JWT only)
- @PreAuthorize annotations enforce role-based access
- Password encryption with BCrypt (strength 12)
```

## Part 8: Monitoring and Metrics

### Actuator Endpoints

Access monitoring data at:
```
http://localhost:8080/actuator/health
http://localhost:8080/actuator/metrics
http://localhost:8080/actuator/prometheus
```

### Key Metrics

- `bloodlink.requests.created` - Total blood requests
- `bloodlink.requests.accepted` - Accepted requests
- `bloodlink.donors.registered` - Total donors
- `bloodlink.donations.recorded` - Total donations
- `bloodlink.messages.created` - Chat messages
- `bloodlink.auth.attempts` - Authentication attempts (success/failure)

### Custom Metrics Usage

```java
@Autowired
private MetricsUtil metricsUtil;

// Track blood request creation
metricsUtil.incrementBloodRequestsCreated();

// Track donation
metricsUtil.incrementDonations();

// Track performance
long startTime = System.currentTimeMillis();
searchDonors();
metricsUtil.recordDonorSearchTime(System.currentTimeMillis() - startTime);
```

## Part 9: Troubleshooting

### MySQL Connection Error
```
Error: Cannot get a connection
Solution:
1. Verify MySQL is running: sudo service mysql status
2. Check credentials in application.properties
3. Ensure database exists: SHOW DATABASES;
4. Check HikariCP pool settings
```

### JWT Token Expired
```
Error: 401 Unauthorized - token expired
Solution:
1. Frontend automatically calls /api/auth/refresh-token
2. If refresh fails, user redirected to login
3. Check JWT expiration settings in application.properties
```

### WebSocket Connection Failed
```
Error: WebSocket connection failed
Solution:
1. Ensure backend is running and WebSocket enabled
2. Check JWT token is valid
3. Verify SockJS fallback: spring.websocket.servlet.prefix=/ws
4. Check browser console for errors
```

### CORS Error
```
Error: Access to XMLHttpRequest blocked by CORS policy
Solution:
1. Verify frontend origin in application.properties
2. Ensure CORS configuration includes your origin
3. Check that credentials are enabled if needed
```

## Part 10: Production Deployment

### Environment Variables
```bash
# Set before deployment
export DB_HOST=your-mysql-host
export DB_USERNAME=bloodlink_user
export DB_PASSWORD=strong_db_password
export JWT_SECRET=your-strong-256-bit-secret
export MAIL_HOST=your-smtp-server
export MAIL_USERNAME=your-email
export MAIL_PASSWORD=your-app-password
export APP_BASE_URL=https://bloodlink.example.com
```

### Docker Production Deployment
```bash
# Build multi-stage image
docker build -t bloodlink:latest .

# Run with environment variables
docker run -d \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql-host:3306/bloodlink_db \
  -e SPRING_DATASOURCE_USERNAME=${DB_USERNAME} \
  -e SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD} \
  -e APP_JWT_SECRET=${JWT_SECRET} \
  -p 8080:8080 \
  bloodlink:latest
```

### Kubernetes Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: bloodlink-app
spec:
  replicas: 3
  selector:
    matchLabels:
      app: bloodlink
  template:
    metadata:
      labels:
        app: bloodlink
    spec:
      containers:
      - name: bloodlink
        image: your-registry/bloodlink:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            configMapKeyRef:
              name: bloodlink-config
              key: db-url
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: bloodlink-secrets
              key: db-password
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
```

## Summary

You now have a complete production-ready BloodLink platform with:
- ✅ Complete backend API with 60+ endpoints
- ✅ Full-featured React frontend with 5+ components
- ✅ Real-time WebSocket communication
- ✅ JWT-based security with role-based access control
- ✅ Comprehensive testing with integration test classes
- ✅ Docker containerization for easy deployment
- ✅ Monitoring and metrics via Actuator
- ✅ Email service for notifications
- ✅ Swagger/OpenAPI API documentation
- ✅ Complete admin dashboard

## Next Steps

1. **Customize Domain Model**: Extend entities with additional fields specific to your use case
2. **Implement Advanced Matching**: Enhance DonorMatchingService with ML algorithms
3. **Add Payment Integration**: Integrate with Stripe/PayPal for premium features
4. **Deploy to Cloud**: AWS, Azure, or GCP (Kubernetes recommended)
5. **Setup CI/CD**: GitHub Actions, Jenkins, or GitLab CI
6. **Monitor Production**: Setup ELK stack or Datadog for logs/metrics
7. **Scale Database**: Implement read replicas, sharding for high traffic

---

**Happy coding! BloodLink is ready to save lives.** 🩸
