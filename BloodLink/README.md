# BloodLink - AI-Powered Blood Donor & Patient Connection Platform

## 🏥 Project Overview

**BloodLink** is a complete, production-ready, full-stack web application built with enterprise-level architecture and OOP principles. It connects blood donors with patients in need of blood transfusions using intelligent AI-based matching.

## 📋 Table of Contents
1. [Technology Stack](#technology-stack)
2. [Project Structure](#project-structure)
3. [Database Design](#database-design)
4. [Architecture & OOP Principles](#architecture--oop-principles)
5. [Setup & Installation](#setup--installation)
6. [API Endpoints](#api-endpoints)
7. [Features](#features)
8. [Deployment](#deployment)

---

## 🛠️ Technology Stack

### Backend
- **Framework**: Java 17 + Spring Boot 3.1.5
- **Database**: MySQL 8.0+
- **ORM**: Hibernate/JPA
- **Build Tool**: Maven
- **Authentication**: JWT + Spring Security
- **Real-time Communication**: WebSocket
- **API Documentation**: Swagger/OpenAPI

### Frontend
- **HTML5**, **CSS3**, **JavaScript (ES6+)**
- **Responsive Design**: Bootstrap 5
- **Real-time Updates**: WebSocket Client
- **AJAX**: For seamless data loading

### DevOps
- **Containerization**: Docker
- **Server**: Tomcat (embedded in Spring Boot)
- **Version Control**: Git

---

## 📁 Project Structure

```
BloodLink/
├── src/
│   ├── main/
│   │   ├── java/com/bloodlink/
│   │   │   ├── entity/               # JPA Entities with OOP principles
│   │   │   │   ├── User.java         # Abstract base class (INHERITANCE)
│   │   │   │   ├── Donor.java        # Extends User (POLYMORPHISM)
│   │   │   │   ├── Patient.java      # Extends User (POLYMORPHISM)
│   │   │   │   ├── BloodRequest.java
│   │   │   │   ├── Chat.java
│   │   │   │   ├── Message.java
│   │   │   │   ├── Notification.java
│   │   │   │   ├── DonationHistory.java
│   │   │   │   ├── MedicalRecord.java
│   │   │   │   └── DonorReview.java
│   │   │   ├── repository/           # Spring Data JPA Repositories
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── DonorRepository.java
│   │   │   │   ├── PatientRepository.java
│   │   │   │   ├── BloodRequestRepository.java
│   │   │   │   ├── ChatRepository.java
│   │   │   │   ├── MessageRepository.java
│   │   │   │   ├── NotificationRepository.java
│   │   │   │   ├── DonationHistoryRepository.java
│   │   │   │   ├── MedicalRecordRepository.java
│   │   │   │   └── DonorReviewRepository.java
│   │   │   ├── service/              # Business Logic Layer
│   │   │   │   ├── AuthService.java              (Authentication)
│   │   │   │   ├── DonorService.java             (Donor operations)
│   │   │   │   ├── PatientService.java           (Patient operations)
│   │   │   │   ├── BloodRequestService.java      (Request management)
│   │   │   │   ├── ChatService.java              (Chat operations)
│   │   │   │   ├── MessageService.java           (Message operations)
│   │   │   │   ├── NotificationService.java      (Notifications)
│   │   │   │   ├── DonorMatchingService.java     (AI-based matching)
│   │   │   │   └── UserService.java              (User management)
│   │   │   ├── controller/           # REST Controllers (API Layer)
│   │   │   │   ├── AuthController.java           (Auth endpoints)
│   │   │   │   ├── DonorController.java          (Donor endpoints)
│   │   │   │   ├── PatientController.java        (Patient endpoints)
│   │   │   │   ├── BloodRequestController.java   (Request endpoints)
│   │   │   │   ├── ChatController.java           (Chat endpoints)
│   │   │   │   ├── NotificationController.java   (Notification endpoints)
│   │   │   │   └── SearchController.java         (Search endpoints)
│   │   │   ├── dto/                  # Data Transfer Objects
│   │   │   │   ├── UserDTO.java
│   │   │   │   ├── AuthDTO.java
│   │   │   │   ├── BloodRequestDTO.java
│   │   │   │   └── ... (other DTOs)
│   │   │   ├── security/             # Security & JWT
│   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── CustomUserDetailsService.java
│   │   │   ├── websocket/            # WebSocket Configuration
│   │   │   │   ├── WebSocketConfig.java
│   │   │   │   ├── ChatHandler.java
│   │   │   │   └── WebSocketMessage.java
│   │   │   ├── ai/                   # AI/ML Features
│   │   │   │   ├── DonorMatcher.java
│   │   │   │   └── RecommendationEngine.java
│   │   │   ├── util/                 # Utility Classes
│   │   │   │   ├── ValidationUtil.java
│   │   │   │   ├── EmailService.java
│   │   │   │   └── LocationUtil.java
│   │   │   ├── exception/            # Custom Exceptions
│   │   │   │   └── BloodLinkExceptions.java
│   │   │   ├── config/               # Spring Configuration
│   │   │   │   ├── AppConfig.java
│   │   │   │   └── WebConfig.java
│   │   │   └── BloodLinkApplication.java     # Main Application
│   │   ├── resources/
│   │   │   ├── application.properties        # Configuration
│   │   │   ├── templates/                    # Thymeleaf templates
│   │   │   │   ├── index.html
│   │   │   │   ├── donor-dashboard.html
│   │   │   │   ├── patient-dashboard.html
│   │   │   │   └── ... (other HTML templates)
│   │   │   └── static/                       # Static resources
│   │   │       ├── css/
│   │   │       │   ├── style.css
│   │   │       │   └── dashboard.css
│   │   │       ├── js/
│   │   │       │   ├── app.js
│   │   │       │   ├── chat.js
│   │   │       │   └── websocket-client.js
│   │   │       └── img/
│   │   │           └── (images)
│   └── test/
│       └── java/com/bloodlink/     # Unit & Integration Tests
├── docs/
│   ├── DATABASE_SCHEMA.md
│   ├── API_DOCUMENTATION.md
│   ├── DEPLOYMENT_GUIDE.md
│   └── ARCHITECTURE.md
├── pom.xml                         # Maven dependencies
├── Dockerfile                      # Docker configuration
├── .gitignore
└── README.md
```

---

## 🗄️ Database Design

### Entity Relationship Diagram

```
User (Abstract)
├── Donor
│   ├── DonationHistory (1:N)
│   ├── BloodRequest (1:N)
│   ├── DonorReview (1:N)
│   └── MedicalRecord (1:1)
│
├── Patient
│   ├── BloodRequest (1:N)
│   ├── MedicalRecord (1:N)
│   └── DonorReview (1:N)
│
├── Chat (M:M)
│   └── Message (1:N)
│
└── Notification (1:N)
```

### Key Tables

1. **users** - Base user table with inheritance
   - Fields: userId, fullName, email, phoneNumber, password, city, latitude, longitude, etc.
   - Inheritance: Single table with discriminator column (user_type)

2. **donors** - Donor-specific data
   - Blood group, availability, donation history, ratings

3. **patients** - Patient-specific data
   - Required blood group, hospital info, urgency level

4. **blood_requests** - Blood request management
   - Patient ID, Donor ID, status, urgency level, timestamps

5. **chats** - Chat conversations
   - Initiator ID, Recipient ID, status

6. **messages** - Individual messages
   - Chat ID, Sender ID, content, seen status

7. **notifications** - User notifications
   - User ID, type, read status

8. **donation_history** - Historical donation records
   - Donor ID, donation date, test results

9. **medical_records** - Medical information
   - Test results, health status

10. **donor_reviews** - Donor ratings and reviews
    - Donor ID, Patient ID, rating, comment

---

## 🏗️ Architecture & OOP Principles

### Layered Architecture

```
┌─────────────────────────────────────┐
│   REST API Layer (Controllers)      │  HTTP requests/responses
├─────────────────────────────────────┤
│   Business Logic Layer (Services)   │  Core application logic
├─────────────────────────────────────┤
│   Data Access Layer (Repositories)  │  Database operations
├─────────────────────────────────────┤
│   Entity Layer (JPA Entities)       │  Database models
└─────────────────────────────────────┘
```

### OOP Principles Implementation

#### 1. **ABSTRACTION**
```java
// Abstract User class defines common properties
public abstract class User {
    private Long userId;
    private String email;
    public abstract String getUserRole();
    public abstract String getDisplayInfo();
}
```

#### 2. **INHERITANCE**
```java
// Donor extends User
public class Donor extends User {
    private String bloodGroup;
    @Override
    public String getUserRole() { return "DONOR"; }
}

// Patient extends User
public class Patient extends User {
    private String requiredBloodGroup;
    @Override
    public String getUserRole() { return "PATIENT"; }
}
```

#### 3. **POLYMORPHISM**
```java
// Different implementations for same interface
List<User> users = new ArrayList<>();
users.add(new Donor(...));
users.add(new Patient(...));

for (User user : users) {
    System.out.println(user.getUserRole());  // DONOR or PATIENT
}
```

#### 4. **ENCAPSULATION**
```java
// Private fields with public getters/setters
public class Donor {
    private String bloodGroup;
    private Boolean isAvailable;
    
    public void setAvailabilityStatus(Boolean available) {
        if (isVerified && isActive) {
            this.isAvailable = available;  // Validation
        }
    }
}
```

#### 5. **SOLID Principles**

- **S** - Single Responsibility: Each service class handles one concern
- **O** - Open/Closed: Services are open for extension via interfaces
- **L** - Liskov Substitution: Donor and Patient are substitutable for User
- **I** - Interface Segregation: Specific interfaces for different features
- **D** - Dependency Inversion: Services depend on abstractions (repositories)

---

## ⚙️ Setup & Installation

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0+
- Node.js (optional, for frontend development)

### Step 1: Clone Repository
```bash
git clone <repository-url>
cd BloodLink
```

### Step 2: Configure Database
```sql
CREATE DATABASE bloodlink_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bloodlink_db;
```

### Step 3: Update Configuration
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bloodlink_db
spring.datasource.username=root
spring.datasource.password=your_password
app.jwt.secret=your-256-bit-secret-key
```

### Step 4: Build Project
```bash
mvn clean install
```

### Step 5: Run Application
```bash
mvn spring-boot:run
```

Application will start on `http://localhost:8080`

---

## 📡 API Endpoints

### Authentication
```
POST   /api/auth/register          - Register new user
POST   /api/auth/login             - User login (returns JWT token)
POST   /api/auth/refresh-token     - Refresh JWT token
POST   /api/auth/logout            - Logout user
POST   /api/auth/change-password   - Change password
```

### Donor Operations
```
GET    /api/donors                       - Get all donors
GET    /api/donors/{id}                  - Get donor details
PUT    /api/donors/{id}                  - Update donor profile
GET    /api/donors/search?bg=O+          - Search donors by blood group
GET    /api/donors/nearby?lat=...&lon=.. - Find nearby donors
GET    /api/donors/{id}/donations       - Donation history
```

### Patient Operations
```
GET    /api/patients                      - Get all patients
GET    /api/patients/{id}                 - Get patient details
PUT    /api/patients/{id}                 - Update patient profile
GET    /api/patients/{id}/requests       - Patient's requests
```

### Blood Requests
```
POST   /api/requests                      - Create blood request
GET    /api/requests/{id}                 - Get request details
PUT    /api/requests/{id}/accept         - Accept request (Donor)
PUT    /api/requests/{id}/decline        - Decline request (Donor)
PUT    /api/requests/{id}/complete       - Complete request
GET    /api/requests/pending             - Get pending requests
```

### Chat & Messages
```
POST   /api/chats/start                   - Start new chat
GET    /api/chats                         - Get user's chats
GET    /api/chats/{id}/messages          - Get messages in chat
POST   /api/messages                      - Send message
PUT    /api/messages/{id}/mark-seen      - Mark message as seen
```

### Notifications
```
GET    /api/notifications                 - Get user notifications
PUT    /api/notifications/{id}/mark-read - Mark as read
DELETE /api/notifications/{id}            - Delete notification
```

---

## ✨ Key Features

### 1. **User Management**
- ✅ Two-role system (Donor, Patient)
- ✅ JWT Authentication
- ✅ Email verification
- ✅ Profile management
- ✅ Location tracking

### 2. **Donor Features**
- ✅ Register with blood group
- ✅ Set availability status
- ✅ Manage donation history
- ✅ Accept/Decline requests
- ✅ View ratings and reviews

### 3. **Patient Features**
- ✅ Search donors by criteria
- ✅ Send blood requests
- ✅ Track request status
- ✅ Rate and review donors
- ✅ Emergency level indication

### 4. **Smart Search & Filtering**
- ✅ Search by blood group
- ✅ Location-based filtering
- ✅ Availability status
- ✅ Distance calculation
- ✅ Rating/frequency sorting

### 5. **Real-time Communication**
- ✅ WebSocket-based chat
- ✅ Seen/Unseen status
- ✅ Notification system
- ✅ Online/Offline indicators

### 6. **AI Features**
- ✅ Smart donor matching algorithm
- ✅ Priority scoring system
- ✅ Donor ranking by reliability
- ✅ Urgency-based matching

### 7. **Medical Records**
- ✅ Health information storage
- ✅ Test results management
- ✅ Eligibility assessment
- ✅ Risk evaluation

---

## 🔐 Security Features

- **JWT Authentication**: Stateless, token-based authentication
- **Spring Security**: Role-based authorization
- **Password Encryption**: BCrypt hashing
- **Input Validation**: XSS and SQL injection protection
- **CORS Configuration**: Safe cross-origin requests
- **HTTPS Support**: SSL/TLS configuration ready

---

## 🚀 Deployment

### Docker Deployment
```bash
docker build -t bloodlink:latest .
docker run -p 8080:8080 bloodlink:latest
```

### AWS EC2 Deployment
1. Create EC2 instance with Ubuntu
2. Install Java 17, MySQL, Maven
3. Clone repository
4. Configure application properties
5. Run `mvn spring-boot:run`
6. Set up Nginx reverse proxy

### Cloud Platforms
- AWS Elastic Beanstalk
- Google Cloud Run
- Microsoft Azure App Service
- Heroku

---

## 📊 Database Setup & Sample Data

### Create Schema
```bash
mysql -u root -p < database-schema.sql
```

### Sample Data Loading
SQL script creates tables and loads initial data for testing.

---

## 🧪 Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

### Test Coverage
```bash
mvn jacoco:report
```

---

## 📈 Performance Optimization

- Database indexing on frequently searched columns
- Connection pooling with HikariCP
- Lazy loading for relationships
- Caching layer for frequently accessed data
- Request pagination for large datasets

---

## 🐛 Troubleshooting

### Database Connection Issues
```bash
# Verify MySQL is running
mysql -u root -p -e "SELECT 1"
```

### JWT Token Errors
- Ensure `app.jwt.secret` is set in properties
- Check token expiration in AuthResponse

### WebSocket Connection Issues
- Enable WebSocket in browser
- Check server logs for connection errors

---

## 📚 Additional Documentation

- [Database Schema Documentation](docs/DATABASE_SCHEMA.md)
- [API Endpoint Documentation](docs/API_DOCUMENTATION.md)
- [Architecture & Design Patterns](docs/ARCHITECTURE.md)
- [Deployment Guides](docs/DEPLOYMENT_GUIDE.md)

---

## 👨‍💻 Development Guidelines

1. **Code Style**: Follow Google Java Style Guide
2. **Naming Conventions**: Use camelCase for variables, PascalCase for classes
3. **Documentation**: Write JavaDoc for public methods
4. **Testing**: Aim for 80%+ code coverage
5. **Git Commits**: Use conventional commit messages

---

## 📄 License

This project is licensed under the MIT License - see LICENSE.md for details.

---

## 👥 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

---

## 📞 Support & Contact

- **Email**: support@bloodlink.com
- **Issues**: GitHub Issues
- **Documentation**: Wiki

---

**Created with ❤️ for the Blood Donation Community**

Last Updated: 2024
