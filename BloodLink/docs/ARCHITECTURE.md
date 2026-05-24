# BloodLink Architecture & Design Patterns

## 📐 System Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                     CLIENT LAYER                             │
│  (HTML/CSS/JavaScript + WebSocket Client)                   │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP/WebSocket
┌──────────────────────────▼──────────────────────────────────┐
│            REST CONTROLLER LAYER                             │
│  (AuthController, DonorController, PatientController, etc.)  │
│  Responsibilities:                                           │
│  - Handle HTTP requests/responses                            │
│  - Request validation                                        │
│  - Authorization checks                                      │
│  - JSON serialization/deserialization                        │
└──────────────────────────┬──────────────────────────────────┘
                           │ Dependency Injection
┌──────────────────────────▼──────────────────────────────────┐
│          SERVICE/BUSINESS LOGIC LAYER                        │
│  (DonorService, PatientService, BloodRequestService, etc.)   │
│  Responsibilities:                                           │
│  - Business logic implementation                             │
│  - Data validation                                           │
│  - Transaction management                                    │
│  - AI algorithms                                             │
│  - Notification logic                                        │
└──────────────────────────┬──────────────────────────────────┘
                           │ Uses
┌──────────────────────────▼──────────────────────────────────┐
│      REPOSITORY/DATA ACCESS LAYER                            │
│  (UserRepository, DonorRepository, PatientRepository, etc.)   │
│  Responsibilities:                                           │
│  - Database queries (CRUD)                                   │
│  - Custom query methods                                      │
│  - Transaction handling                                      │
└──────────────────────────┬──────────────────────────────────┘
                           │ Mapped to
┌──────────────────────────▼──────────────────────────────────┐
│           ENTITY LAYER (JPA ENTITIES)                        │
│  (User, Donor, Patient, BloodRequest, etc.)                 │
│  Responsibilities:                                           │
│  - Data model definition                                     │
│  - ORM annotations                                           │
│  - Relationships definition                                  │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
                    ┌─────────────┐
                    │   MYSQL DB  │
                    └─────────────┘
```

---

## 🎯 Design Patterns Used

### 1. **MVC (Model-View-Controller) Pattern**

- **Model**: JPA Entities (User, Donor, Patient, etc.)
- **View**: HTML/CSS/JavaScript templates
- **Controller**: REST Controllers handling HTTP requests

### 2. **Repository Pattern**

Abstracts data access logic and provides a collection-like interface.

```java
// Example: Repository abstraction
public interface DonorRepository extends JpaRepository<Donor, Long> {
    List<Donor> findByBloodGroupAndIsAvailableTrue(String bloodGroup);
}

// Service uses repository
@Service
public class DonorService {
    @Autowired
    private DonorRepository donorRepository;
    
    public List<Donor> findAvailableDonors(String bloodGroup) {
        return donorRepository.findByBloodGroupAndIsAvailableTrue(bloodGroup);
    }
}
```

### 3. **Service Locator Pattern**

Used through Spring Dependency Injection

```java
@Service
public class BloodRequestService {
    @Autowired
    private BloodRequestRepository requestRepo;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private EmailService emailService;
    
    // Services automatically injected
}
```

### 4. **DTO (Data Transfer Object) Pattern**

Separates internal representation from external API

```java
// Internal entity
public class Donor extends User { ... }

// External DTO
@Data
public class DonorDTO extends UserDTO {
    private String bloodGroup;
    private Boolean isAvailable;
    private Integer totalDonations;
}

// Conversion in service
public DonorDTO getDonorDTO(Donor donor) {
    return modelMapper.map(donor, DonorDTO.class);
}
```

### 5. **Singleton Pattern**

Spring beans are singletons by default

```java
@Service
public class DonorService {  // Singleton
    // Single instance shared across application
}
```

### 6. **Factory Pattern**

Factory methods for creating domain objects

```java
@Service
public class BloodRequestFactory {
    public BloodRequest createRequest(Patient patient, Donor donor, int units) {
        BloodRequest request = new BloodRequest();
        request.setPatient(patient);
        request.setDonor(donor);
        request.setUnits(units);
        request.setStatus(RequestStatus.PENDING);
        return request;
    }
}
```

### 7. **Observer Pattern**

Event-driven notifications

```java
@Service
public class NotificationService {
    public void notifyDonor(BloodRequest request) {
        // Observer notifies donor of new request
    }
}
```

### 8. **Strategy Pattern**

Different matching algorithms

```java
public interface DonorMatchingStrategy {
    List<Donor> matchDonors(Patient patient);
}

@Component
public class LocationBasedMatchingStrategy implements DonorMatchingStrategy {
    @Override
    public List<Donor> matchDonors(Patient patient) {
        // Location-based matching logic
    }
}

@Component
public class BloodGroupMatchingStrategy implements DonorMatchingStrategy {
    @Override
    public List<Donor> matchDonors(Patient patient) {
        // Blood group based matching logic
    }
}
```

---

## 🏛️ OOP Principles Implementation

### **1. ABSTRACTION**

Hiding complex implementation details behind simple interfaces

```java
// Abstract User class
public abstract class User {
    private Long userId;
    private String email;
    
    // Abstract methods - subclasses must implement
    public abstract String getUserRole();
    public abstract String getDisplayInfo();
    
    // Concrete method - same for all users
    public void updateLocation(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
```

### **2. ENCAPSULATION**

Bundling data and methods, hiding internal details

```java
public class Donor extends User {
    // Private field - cannot access directly
    private String bloodGroup;
    private Boolean isAvailable;
    
    // Controlled access through methods
    public void setAvailabilityStatus(Boolean available) {
        if (isVerified && isActive) {  // Validation
            this.isAvailable = available;
        }
    }
    
    // Computed property
    public boolean isEligibleForDonation() {
        LocalDate minDate = LocalDate.now().minusDays(56);
        return lastDonationDate == null || lastDonationDate.isBefore(minDate);
    }
}
```

### **3. INHERITANCE**

Code reuse through class hierarchies

```java
// Base class
public abstract class User { ... }

// Derived classes
public class Donor extends User {
    private String bloodGroup;
    
    @Override
    public String getUserRole() {
        return "DONOR";
    }
}

public class Patient extends User {
    private String requiredBloodGroup;
    
    @Override
    public String getUserRole() {
        return "PATIENT";
    }
}
```

### **4. POLYMORPHISM**

Objects of different types responding to same message

```java
// Polymorphic usage
List<User> users = new ArrayList<>();
users.add(new Donor(...));
users.add(new Patient(...));

// Runtime polymorphism
for (User user : users) {
    System.out.println(user.getUserRole());  // Different output for each
    System.out.println(user.getDisplayInfo());
}
```

---

## 💪 SOLID Principles

### **S - Single Responsibility Principle**

Each class has one reason to change

```java
// ✅ GOOD: One responsibility each
@Service
public class DonorService {
    // Only handles donor operations
    public Donor registerDonor(RegisterRequest request) { ... }
}

@Service
public class NotificationService {
    // Only handles notifications
    public void sendNotification(User user, String message) { ... }
}

// ❌ BAD: Multiple responsibilities
@Service
public class UserService {
    // Handles users, donations, notifications, chat, etc.
}
```

### **O - Open/Closed Principle**

Open for extension, closed for modification

```java
// ✅ GOOD: Can extend without modifying
public interface DonorMatchingStrategy {
    List<Donor> matchDonors(Patient patient);
}

@Component
public class AIBasedMatcher implements DonorMatchingStrategy {
    @Override
    public List<Donor> matchDonors(Patient patient) { ... }
}

@Component
public class LocationBasedMatcher implements DonorMatchingStrategy {
    @Override
    public List<Donor> matchDonors(Patient patient) { ... }
}
```

### **L - Liskov Substitution Principle**

Derived classes must be substitutable for base class

```java
// ✅ GOOD: Donor and Patient can substitute User
public void processUser(User user) {
    user.updateLocation(10.5, 20.3);  // Works for Donor or Patient
    String role = user.getUserRole();
}

// Usage
User donor = new Donor(...);
User patient = new Patient(...);
processUser(donor);      // Works
processUser(patient);    // Works
```

### **I - Interface Segregation Principle**

Many specific interfaces better than general one

```java
// ✅ GOOD: Specific interfaces
public interface Donor Service {
    void registerDonor(RegisterRequest request);
    void setAvailability(Long donorId, Boolean available);
}

public interface DonorSearchService {
    List<Donor> searchDonors(DonorSearchCriteria criteria);
}

public interface DonorRatingService {
    void rateDonor(Long donorId, int rating);
}

// ❌ BAD: One large interface
public interface UserService {
    void registerDonor(...);
    void registerPatient(...);
    List<Donor> searchDonors(...);
    void rateDonor(...);
    // Many more methods
}
```

### **D - Dependency Inversion Principle**

Depend on abstractions, not concretions

```java
// ✅ GOOD: Depends on abstraction
@Service
public class BloodRequestService {
    @Autowired
    private BloodRequestRepository requestRepo;  // Abstraction
    
    public void createRequest(BloodRequest request) {
        requestRepo.save(request);
    }
}

// ❌ BAD: Depends on concrete class
@Service
public class BloodRequestService {
    private BloodRequestDAO dao = new BloodRequestDAO();  // Concrete
}
```

---

## 🔄 Data Flow Examples

### **Example 1: Create Blood Request**

```
1. Client submits form
   ↓
2. PatientController.createRequest() receives request
   ↓
3. Controller validates input and calls BloodRequestService
   ↓
4. Service applies business logic:
   - Validates patient eligibility
   - Validates donor availability
   - Creates BloodRequest entity
   ↓
5. Service calls BloodRequestRepository.save()
   ↓
6. Repository persists to database
   ↓
7. Service sends notifications to donor
   ↓
8. Controller returns response with request ID
   ↓
9. Client receives response and updates UI
```

### **Example 2: Search Donors**

```
1. Client searches with criteria (blood group, location)
   ↓
2. SearchController.searchDonors() receives query
   ↓
3. Controller calls DonorService.searchDonors()
   ↓
4. Service determines matching strategy:
   - LocationBasedMatcher
   - BloodGroupMatcher
   - AIBasedMatcher
   ↓
5. Service calls DonorRepository.findBestMatchDonors()
   ↓
6. Repository executes JPQL/SQL query
   ↓
7. Service applies post-processing:
   - Calculates distances
   - Scores donors
   - Ranks results
   ↓
8. Service converts entities to DTOs
   ↓
9. Controller returns list of DonorDTOs
   ↓
10. Client displays results on UI
```

---

## 🧩 Component Interaction

### Request Flow Diagram

```
┌────────────┐
│  Browser   │
└─────┬──────┘
      │ HTTP Request (JSON)
      ▼
┌─────────────────────────────────────┐
│    Spring Dispatcher Servlet         │
└─────────────────────────────────────┘
      │ Route to matching controller
      ▼
┌─────────────────────────────────────┐
│  @RestController                     │
│  - Input validation                 │
│  - JWT verification                 │
└─────────────────────────────────────┘
      │ Call service method
      ▼
┌─────────────────────────────────────┐
│  @Service                            │
│  - Business logic                   │
│  - Transaction management           │
│  - Cross-cutting concerns           │
└─────────────────────────────────────┘
      │ Use repositories
      ▼
┌─────────────────────────────────────┐
│  Repository Interface               │
│  - Query methods                    │
└─────────────────────────────────────┘
      │ Execute queries
      ▼
┌─────────────────────────────────────┐
│  Hibernate ORM                       │
│  - Map entities to DB               │
└─────────────────────────────────────┘
      │ SQL queries
      ▼
┌─────────────────────────────────────┐
│  MySQL Database                      │
│  - Persist/Retrieve data            │
└─────────────────────────────────────┘
      │ Return result set
      ▼
[Process reverses back to client]
```

---

## 🔐 Authentication & Authorization Flow

```
1. User submits login credentials
   ↓
2. AuthController.login() receives credentials
   ↓
3. AuthService.authenticate() verifies:
   - Email exists
   - Password matches (BCrypt)
   ↓
4. JwtTokenProvider.generateToken() creates JWT:
   - Subject: email
   - Claims: role
   - Expiration: 24 hours
   ↓
5. AuthResponse returned with token
   ↓
6. Client stores token in localStorage
   ↓
7. For subsequent requests:
   - Client includes: Authorization: Bearer <token>
   ↓
8. JwtAuthenticationFilter intercepts request
   ↓
9. Filter validates token using JwtTokenProvider
   ↓
10. Filter sets SecurityContext with user details
    ↓
11. Controller method executes with authenticated user
    ↓
12. @PreAuthorize("hasRole('DONOR')") checks authorization
```

---

## 📊 Database Relationships

### Relationship Diagram

```
          ┌─────────────────┐
          │      User       │
          │   (Abstract)    │
          └────────┬────────┘
                   │
        ┌──────────┴──────────┐
        ▼                     ▼
   ┌────────┐          ┌──────────┐
   │ Donor  │          │ Patient  │
   └────┬───┘          └────┬─────┘
        │                   │
        ├─── 1:N ───────→ DonationHistory
        ├─── 1:N ───────→ BloodRequest (as donor)
        ├─── 1:N ───────→ DonorReview (as donor)
        ├─── 1:1 ───────→ MedicalRecord
        │
        ├─── M:M (via Chat) ───→ Patient
        │
        └─── 1:N ───────→ Message (as sender)

        ├─── 1:N ───────→ BloodRequest (as patient)
        ├─── 1:N ───────→ MedicalRecord (as patient)
        ├─── 1:N ───────→ DonorReview (as patient)
        │
        └─── 1:N ───────→ Message (as sender)

Chat
 ├─── 1:N ───────→ Message
 └─── M:M ───────→ User (initiator, recipient)

User
 ├─── 1:N ───────→ Chat
 ├─── 1:N ───────→ Message
 └─── 1:N ───────→ Notification
```

---

## 🎓 Learning Path

1. **Start with basics**: Understand User, Donor, Patient entities
2. **Learn repositories**: See how data is accessed
3. **Study services**: Business logic implementation
4. **Understand controllers**: API endpoints
5. **WebSocket**: Real-time communication
6. **AI matching**: Advanced algorithm
7. **Security**: JWT and Spring Security

---

## 📚 Additional Resources

- [Spring Framework Documentation](https://spring.io/docs)
- [Hibernate/JPA Guide](https://hibernate.org/)
- [JWT Best Practices](https://tools.ietf.org/html/rfc7519)
- [SOLID Principles](https://en.wikipedia.org/wiki/SOLID)
- [Design Patterns](https://refactoring.guru/design-patterns)

