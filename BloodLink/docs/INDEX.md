# BloodLink Project - Complete Documentation Index

## 📚 Documentation Overview

Welcome to BloodLink! This document serves as a central hub for all project documentation. Start here to understand the project structure and navigate to relevant documentation.

---

## 🗂️ Quick Navigation

### 1. **Getting Started**
- **First Time?** Start with [SETUP_GUIDE.md](#setup-guide) below
- **Want to Run Locally?** Follow [Quick Start](#quick-start-guide)
- **Need to Deploy?** See [Deployment Guide](#deployment-guide)

### 2. **Understanding the Project**
- **Architecture**: [ARCHITECTURE.md](ARCHITECTURE.md)
- **Database Design**: [DATABASE_SCHEMA.md](#database-documentation)
- **API Reference**: [API_DOCUMENTATION.md](#api-documentation)
- **Code Examples**: [IMPLEMENTATION_GUIDE.md](IMPLEMENTATION_GUIDE.md)

### 3. **Development**
- **OOP Principles**: [OOP_GUIDE.md](#oop-principles-guide)
- **Design Patterns**: [ARCHITECTURE.md](ARCHITECTURE.md#-design-patterns-used)
- **Code Standards**: [CODING_STANDARDS.md](#coding-standards)

---

## 📖 Detailed Documentation

### Main README
**File**: [README.md](../README.md)

Complete project overview including:
- Project description and goals
- Technology stack
- Project structure
- Feature list
- Setup instructions
- API endpoints summary

**Read this first** to get an overview of the project.

---

### Architecture & Design
**File**: [ARCHITECTURE.md](ARCHITECTURE.md)

Comprehensive guide covering:
- System architecture with diagrams
- Layered architecture explanation
- MVC pattern implementation
- All design patterns used (Repository, DTO, Factory, Observer, Strategy)
- OOP principles implementation (Abstraction, Encapsulation, Inheritance, Polymorphism)
- SOLID principles with code examples
- Data flow diagrams
- Authentication & authorization flow
- Database relationships
- Component interaction diagrams

**Read this** to understand how the application is structured and how components interact.

---

### Implementation Guide
**File**: [IMPLEMENTATION_GUIDE.md](IMPLEMENTATION_GUIDE.md)

Detailed code examples showing:
- Complete DonorService implementation with explanations
- Complete DonorController REST endpoints
- Exception handling patterns
- Service-to-service communication
- DTO patterns
- OOP principles applied in real code
- Testing examples
- Best practices applied

**Read this** for actual code examples and implementation patterns.

---

### Database Schema
**File**: [DATABASE_SCHEMA.sql](DATABASE_SCHEMA.sql)

Complete SQL script including:
- All table definitions
- Relationships (1:1, 1:N, M:N)
- Indexes for performance
- Sample data for testing
- Constraints and validations

**Use this** to:
- Create the database
- Understand data model
- Add sample data
- See all tables and relationships

---

### Deployment Guide
**File**: [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)

Step-by-step deployment instructions for:
- Local development setup (Windows, Linux, Mac)
- Docker deployment
- Docker Compose setup
- AWS EC2 deployment
- Google Cloud Run
- Heroku deployment
- Nginx reverse proxy configuration
- Environment variables
- Performance tuning
- Security checklist
- Troubleshooting

**Use this** to deploy the application to various environments.

---

## 🚀 Quick Start Guide

### 5-Minute Setup

#### Prerequisites
```
✓ Java 17+
✓ Maven 3.8+
✓ MySQL 8.0+
✓ Git
```

#### Step 1: Database Setup
```sql
CREATE DATABASE bloodlink_db CHARACTER SET utf8mb4;
CREATE USER 'bloodlink'@'localhost' IDENTIFIED BY 'BloodLink@123';
GRANT ALL ON bloodlink_db.* TO 'bloodlink'@'localhost';
```

#### Step 2: Clone & Configure
```bash
git clone https://github.com/yourusername/bloodlink.git
cd BloodLink

# Edit: src/main/resources/application.properties
# Update database credentials
```

#### Step 3: Build & Run
```bash
mvn clean install
mvn spring-boot:run
```

#### Step 4: Access Application
- **Web**: http://localhost:8080
- **API**: http://localhost:8080/api/donors

---

## 📁 Project Structure

```
BloodLink/
├── src/main/java/com/bloodlink/
│   ├── entity/                  # JPA Entities (OOP Principles)
│   ├── repository/              # Data Access Layer (Repository Pattern)
│   ├── service/                 # Business Logic Layer
│   ├── controller/              # REST API Controllers
│   ├── dto/                     # Data Transfer Objects (DTO Pattern)
│   ├── security/                # JWT & Authentication
│   ├── websocket/               # WebSocket Configuration
│   ├── ai/                      # AI/ML Features
│   ├── util/                    # Utility Classes
│   ├── exception/               # Custom Exceptions
│   ├── config/                  # Spring Configuration
│   └── BloodLinkApplication.java    # Main Application
│
├── src/main/resources/
│   ├── application.properties   # Configuration
│   ├── templates/               # HTML Templates
│   └── static/                  # CSS, JS, Images
│
├── docs/
│   ├── ARCHITECTURE.md          # Architecture & Design
│   ├── IMPLEMENTATION_GUIDE.md  # Code Examples
│   ├── DATABASE_SCHEMA.sql      # Database Setup
│   ├── DEPLOYMENT_GUIDE.md      # Deployment Instructions
│   ├── API_DOCUMENTATION.md     # API Reference
│   └── ... (other docs)
│
├── pom.xml                      # Maven Dependencies
├── README.md                    # Project Overview
└── Dockerfile                   # Docker Configuration
```

---

## 🎯 Key Features by Component

### Entity Layer (OOP Principles)
```
✓ Abstract User class (ABSTRACTION)
✓ Donor extends User (INHERITANCE)
✓ Patient extends User (INHERITANCE)
✓ Polymorphic methods (POLYMORPHISM)
✓ Encapsulated validation (ENCAPSULATION)
```

### Repository Layer (Data Access)
```
✓ Spring Data JPA repositories
✓ Custom query methods
✓ Location-based searches
✓ Complex joins and filtering
```

### Service Layer (Business Logic)
```
✓ Authentication service
✓ Donor management
✓ Patient management
✓ Blood request processing
✓ Smart donor matching (AI)
✓ Chat and messaging
✓ Notification system
```

### Controller Layer (REST API)
```
✓ RESTful endpoints
✓ JWT authentication
✓ Request validation
✓ Error handling
✓ DTO conversion
```

### Security Layer
```
✓ JWT token generation
✓ Spring Security integration
✓ Role-based authorization
✓ Password encryption (BCrypt)
```

### Communication Layer
```
✓ WebSocket for real-time chat
✓ Message persistence
✓ Seen/Unseen status
```

---

## 🔧 Development Workflow

### 1. **Understanding the Code**
- Start with entity classes to understand data model
- Read service classes to understand business logic
- Study controller classes for API design
- Check tests for expected behavior

### 2. **Adding New Feature**
1. Create/modify entity in `entity/` folder
2. Create repository method in `repository/` folder
3. Add business logic in `service/` folder
4. Create REST endpoint in `controller/` folder
5. Write tests in `test/` folder
6. Update documentation

### 3. **Running Tests**
```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# Coverage report
mvn jacoco:report
```

### 4. **Code Quality**
```bash
# Static analysis
mvn checkstyle:check

# FindBugs
mvn findbugs:check
```

---

## 📝 API Endpoints Summary

### Authentication
```
POST   /api/auth/register          - Register new user
POST   /api/auth/login             - User login
POST   /api/auth/refresh-token     - Refresh JWT token
POST   /api/auth/logout            - Logout user
```

### Donor Management
```
GET    /api/donors                       - Get all donors
GET    /api/donors/{id}                  - Get donor details
PUT    /api/donors/{id}                  - Update profile
GET    /api/donors/search?bg=O+          - Search by blood group
GET    /api/donors/nearby?lat=...&lon=.. - Find nearby donors
PUT    /api/donors/{id}/availability     - Set availability
GET    /api/donors/{id}/donations        - Donation history
```

### Patient Management
```
GET    /api/patients                     - Get all patients
GET    /api/patients/{id}                - Get patient details
PUT    /api/patients/{id}                - Update profile
```

### Blood Requests
```
POST   /api/requests                     - Create request
GET    /api/requests/{id}                - Get request details
PUT    /api/requests/{id}/accept         - Accept request
PUT    /api/requests/{id}/decline        - Decline request
PUT    /api/requests/{id}/complete       - Complete request
```

### Chat & Messaging
```
POST   /api/chats/start                  - Start new chat
GET    /api/chats                        - Get user's chats
GET    /api/chats/{id}/messages         - Get messages
POST   /api/messages                     - Send message
WS     /ws/chat/{chatId}                 - WebSocket chat
```

**For complete API documentation**, see [API_DOCUMENTATION.md](API_DOCUMENTATION.md)

---

## 🏛️ OOP Principles Applied

### 1. Abstraction
- Abstract `User` class defines common interface
- Abstract methods force subclass implementation
- Hides complex implementation details

### 2. Encapsulation
- Private fields with controlled access
- Business logic validation in entity methods
- Atomic operations (e.g., `setAvailabilityStatus()`)

### 3. Inheritance
- `Donor` extends `User`
- `Patient` extends `User`
- Reuses common functionality

### 4. Polymorphism
- Runtime polymorphism with `getUserRole()` override
- Different behavior for `Donor` vs `Patient`
- List<User> can contain both types

### SOLID Principles
- **S**ingle Responsibility: Each service handles one concern
- **O**pen/Closed: Open for extension via interfaces
- **L**iskov Substitution: Donor/Patient substitute for User
- **I**nterface Segregation: Specific interfaces (DonorService, etc.)
- **D**ependency Inversion: Depend on abstractions

---

## 🧪 Testing Guide

### Unit Testing
```java
@SpringBootTest
class DonorServiceTest {
    @Test
    void testRegisterDonor() { ... }
}
```

### Integration Testing
```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
class DonorControllerTest {
    @Test
    void testGetAllDonors() { ... }
}
```

### Running Tests
```bash
mvn test              # Run all unit tests
mvn verify           # Run all tests including integration
mvn test -Dtest=DonorServiceTest  # Run specific test
```

---

## 📊 Database Relationships

```
User (Abstract)
├── Donor
│   ├── 1:N → DonationHistory
│   ├── 1:N → BloodRequest
│   ├── 1:N → DonorReview
│   └── 1:1 → MedicalRecord
│
└── Patient
    ├── 1:N → BloodRequest
    ├── 1:N → MedicalRecord
    └── 1:N → DonorReview

Chat (1:N Message)
├── Initiator (User)
└── Recipient (User)

BloodRequest
├── Patient (N:1)
└── Donor (N:1)
```

---

## 🔐 Security Features

### Implemented
- ✅ JWT Authentication (24-hour tokens)
- ✅ Spring Security integration
- ✅ Role-based authorization (@PreAuthorize)
- ✅ Password encryption (BCrypt)
- ✅ CORS configuration
- ✅ Input validation
- ✅ Exception handling

### To Implement
- 🔄 HTTPS/SSL configuration
- 🔄 Rate limiting
- 🔄 CSRF protection
- 🔄 OAuth2 integration

---

## 📦 Dependencies Included

**Key Dependencies**:
- Spring Boot 3.1.5
- Spring Security
- Spring Data JPA
- Hibernate 6.2
- MySQL Connector 8.0
- JWT (jjwt) 0.12.3
- Lombok
- ModelMapper

**Full list**: See [pom.xml](../pom.xml)

---

## 🚀 Performance Considerations

### Database Optimization
- Indexed frequently searched columns
- Query optimization with joins
- Connection pooling (HikariCP)
- Lazy loading for relationships

### Application Optimization
- DTO pattern to reduce payload
- Pagination for large datasets
- Caching layer (optional Redis)
- Batch processing

---

## 📞 Getting Help

1. **Documentation**: Check [README.md](../README.md) and docs/ folder
2. **Architecture**: See [ARCHITECTURE.md](ARCHITECTURE.md)
3. **Code Examples**: View [IMPLEMENTATION_GUIDE.md](IMPLEMENTATION_GUIDE.md)
4. **Deployment**: Follow [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)
5. **Issues**: Check GitHub Issues or contact support

---

## 📝 Coding Standards

### Naming Conventions
- **Classes**: PascalCase (User, DonorService)
- **Methods**: camelCase (getUserRole, findNearbyDonors)
- **Constants**: UPPER_SNAKE_CASE (MAX_ATTEMPTS)
- **Variables**: camelCase (donorId, isAvailable)

### Documentation
- All public methods have JavaDoc
- Complex logic has inline comments
- Classes document their responsibility

### Structure
- One class per file
- Logical method organization
- Proper import organization

---

## 🎓 Learning Resources

1. **Spring Framework**: https://spring.io/
2. **Hibernate/JPA**: https://hibernate.org/
3. **JWT**: https://jwt.io/
4. **Design Patterns**: https://refactoring.guru/
5. **SOLID Principles**: https://www.baeldung.com/

---

## ✅ Checklist for Getting Started

- [ ] Read README.md for project overview
- [ ] Review ARCHITECTURE.md for system design
- [ ] Follow DEPLOYMENT_GUIDE.md for local setup
- [ ] Read IMPLEMENTATION_GUIDE.md for code patterns
- [ ] Check DATABASE_SCHEMA.sql for data model
- [ ] Run application and verify it works
- [ ] Review entity classes and OOP principles
- [ ] Study service layer implementations
- [ ] Explore controller endpoints
- [ ] Run tests and understand test patterns

---

## 📅 Release History

**Version 1.0.0** (Current)
- Initial release
- Core features implemented
- Full documentation
- Production ready

---

**Last Updated**: 2024
**Author**: BloodLink Development Team
**License**: MIT

For more information, visit the project repository or contact support@bloodlink.com

