# BloodLink - OOP & SOLID Principles Quick Reference

## 🎯 OOP Principles Cheat Sheet

### 1. ABSTRACTION
**What**: Hide complex implementation, show only essential features
**Why**: Reduces complexity, improves maintainability
**BloodLink Example**:
```java
// Abstract class hides common user properties
public abstract class User {
    // Common properties
    private Long userId;
    private String email;
    
    // Abstract method - subclasses must implement
    public abstract String getUserRole();
}
```

### 2. ENCAPSULATION
**What**: Bundle data and methods, hide internal details
**Why**: Protects data integrity, enables change without breaking code
**BloodLink Example**:
```java
// Encapsulate donor availability logic
public class Donor extends User {
    private Boolean isAvailable;
    
    // Controlled access with validation
    public void setAvailabilityStatus(Boolean available) {
        if (isVerified && isActive) {
            this.isAvailable = available;
        }
    }
}
```

### 3. INHERITANCE
**What**: Create new classes based on existing classes
**Why**: Promotes code reuse and establishes hierarchy
**BloodLink Example**:
```java
// Donor inherits from User
public class Donor extends User {
    private String bloodGroup;
    // Inherits: userId, email, phone, address, etc.
}

// Patient inherits from User
public class Patient extends User {
    private String requiredBloodGroup;
    // Inherits: userId, email, phone, address, etc.
}
```

### 4. POLYMORPHISM
**What**: Objects respond differently to the same message
**Why**: Enables flexible and extensible code
**BloodLink Example**:
```java
// Polymorphic behavior
List<User> users = new ArrayList<>();
users.add(new Donor(...));
users.add(new Patient(...));

for (User user : users) {
    // Different output for each type
    System.out.println(user.getUserRole());  // DONOR or PATIENT
    System.out.println(user.getDisplayInfo());
}
```

---

## 🏛️ SOLID Principles Cheat Sheet

### S - Single Responsibility Principle
**Rule**: A class should have only one reason to change
**BloodLink Example**:
```
✅ GOOD: Separated concerns
DonorService - Only handles donor operations
PatientService - Only handles patient operations
NotificationService - Only handles notifications
BloodRequestService - Only handles blood requests

❌ BAD: Mixed concerns
UserService - Handles donors, patients, notifications, requests
```

### O - Open/Closed Principle
**Rule**: Open for extension, closed for modification
**BloodLink Example**:
```java
// Define interface
public interface DonorMatcher {
    List<Donor> match(Patient patient);
}

// Extend without modifying existing code
public class LocationMatcher implements DonorMatcher {
    @Override
    public List<Donor> match(Patient patient) { ... }
}

public class AIBasedMatcher implements DonorMatcher {
    @Override
    public List<Donor> match(Patient patient) { ... }
}
```

### L - Liskov Substitution Principle
**Rule**: Derived classes must be substitutable for base class
**BloodLink Example**:
```java
// Donor and Patient can substitute User
public void processUser(User user) {
    user.updateProfile(...);  // Works for both
    user.updateLocation(...); // Works for both
    String role = user.getUserRole(); // Different implementation
}

// Usage - both work without changes
processUser(new Donor(...));  // ✅ Works
processUser(new Patient(...))); // ✅ Works
```

### I - Interface Segregation Principle
**Rule**: Many specific interfaces > one general interface
**BloodLink Example**:
```java
// ✅ GOOD: Specific interfaces
public interface DonorRegistration {
    Donor registerDonor(RegisterRequest request);
}

public interface DonorSearch {
    List<Donor> searchByBloodGroup(String group);
}

public interface DonorRating {
    void rateDonor(Long donorId, int rating);
}

// ❌ BAD: One large interface
public interface UserManagement {
    Donor registerDonor(...);
    Patient registerPatient(...);
    List<Donor> searchDonors(...);
    void rateDonor(...);
    // 50+ more methods...
}
```

### D - Dependency Inversion Principle
**Rule**: Depend on abstractions, not concrete implementations
**BloodLink Example**:
```java
// ✅ GOOD: Depends on abstraction (interface)
@Service
public class BloodRequestService {
    @Autowired
    private BloodRequestRepository requestRepo;  // Abstraction
    
    public void saveRequest(BloodRequest request) {
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

## 📊 Principle Application Matrix

| Component | Abstraction | Encapsulation | Inheritance | Polymorphism | S | O | L | I | D |
|-----------|:-----------:|:-------------:|:-----------:|:------------:|:-:|:-:|:-:|:-:|:-:|
| User (abstract) | ✅ | ✅ | ✅ | ✅ | - | - | - | - | - |
| Donor class | ✅ | ✅ | ✅ | ✅ | - | - | - | - | - |
| Patient class | ✅ | ✅ | ✅ | ✅ | - | - | - | - | - |
| DonorService | ✅ | ✅ | - | - | ✅ | ✅ | - | ✅ | ✅ |
| DonorRepository | ✅ | - | - | - | ✅ | ✅ | ✅ | ✅ | ✅ |
| DonorController | - | ✅ | - | - | ✅ | ✅ | ✅ | - | - |

---

## 🔍 Common Patterns in BloodLink

### Repository Pattern
```java
// Abstraction: Interface provides contract
public interface DonorRepository extends JpaRepository<Donor, Long> {
    List<Donor> findByBloodGroup(String bloodGroup);
}

// Service uses abstraction
@Service
public class DonorService {
    @Autowired
    private DonorRepository donorRepository;  // Injected
    
    public List<Donor> getDonors(String bloodGroup) {
        return donorRepository.findByBloodGroup(bloodGroup);
    }
}
```

### Factory Pattern
```java
// Factory creates objects
@Service
public class BloodRequestFactory {
    public BloodRequest createRequest(Patient patient, Donor donor) {
        BloodRequest request = new BloodRequest();
        request.setPatient(patient);
        request.setDonor(donor);
        request.setStatus(PENDING);
        return request;
    }
}
```

### Strategy Pattern
```java
// Strategy interface
public interface DonorMatcher {
    List<Donor> match(Patient patient);
}

// Different implementations
@Component
public class LocationMatcher implements DonorMatcher { ... }

@Component
public class HealthMatcher implements DonorMatcher { ... }

@Component
public class AIBasedMatcher implements DonorMatcher { ... }
```

### Observer Pattern
```java
// Observer notifies about events
@Service
public class NotificationService {
    public void notifyDonorOfRequest(Donor donor, BloodRequest request) {
        // Send notification to donor
    }
}
```

### DTO Pattern
```java
// Entity
public class Donor extends User { ... }

// DTO
public class DonorDTO {
    private String fullName;
    private String bloodGroup;
    private Integer totalDonations;
}

// Conversion in service
public DonorDTO toDonorDTO(Donor donor) {
    return DonorDTO.builder()
        .fullName(donor.getFullName())
        .bloodGroup(donor.getBloodGroup())
        .totalDonations(donor.getTotalDonations())
        .build();
}
```

---

## 🎨 Design Principles Applied

### DRY (Don't Repeat Yourself)
```java
// Base class eliminates duplication
public abstract class User {
    protected void updateLocation(Double lat, Double lon) {
        this.latitude = lat;
        this.longitude = lon;
    }
}

// Both Donor and Patient use it
public class Donor extends User { ... }
public class Patient extends User { ... }
```

### KISS (Keep It Simple, Stupid)
```java
// Simple, clear method
public boolean isDonorEligible(Donor donor) {
    return donor.getLastDonationDate() == null || 
           donor.getLastDonationDate().isBefore(LocalDate.now().minusDays(56));
}

// NOT complex with unnecessary logic
```

### YAGNI (You Aren't Gonna Need It)
- Only implement features that are actually needed
- Don't add "just in case" functionality
- Keep codebase lean and focused

---

## 📈 Architecture Principles

### Layered Architecture
```
Controller Layer → Service Layer → Repository Layer → Entity Layer → Database
    (API)          (Logic)        (Access)          (Models)
```

### Separation of Concerns
```
Entity Layer: Data model
Repository Layer: Database operations
Service Layer: Business logic
Controller Layer: HTTP handling
```

### Dependency Flow
```
Controller → Service → Repository → Entity
   (High)              (Low)
High-level modules depend on abstractions, not low-level details
```

---

## ✅ Code Review Checklist

- [ ] Does the class have a single responsibility?
- [ ] Are fields properly encapsulated (private)?
- [ ] Are related classes in inheritance hierarchy?
- [ ] Can this class be extended without modification?
- [ ] Does it depend on abstractions, not concrete classes?
- [ ] Are business rules hidden in methods?
- [ ] Is the code DRY (no repetition)?
- [ ] Are names clear and descriptive?
- [ ] Is the code testable?
- [ ] Are edge cases handled?

---

## 🚀 Best Practices Summary

1. **Program to Interface**: Use abstractions, not implementations
2. **Encapsulate Variations**: Hide what changes
3. **Favor Composition Over Inheritance**: When appropriate
4. **Dependency Injection**: Spring injects dependencies
5. **Immutable When Possible**: Thread-safe design
6. **Validate Early**: Check preconditions
7. **Handle Exceptions**: Meaningful error messages
8. **Document Intent**: Why, not what
9. **Test Thoroughly**: Unit + integration tests
10. **Refactor Regularly**: Improve code quality

---

## 📚 References

- **Gang of Four Design Patterns**: https://en.wikipedia.org/wiki/Design_Patterns
- **SOLID Principles**: https://en.wikipedia.org/wiki/SOLID
- **Clean Code**: Robert C. Martin
- **Effective Java**: Joshua Bloch
- **Spring Best Practices**: https://spring.io/guides

---

## 🎯 Quick Examples

### Before & After: Applying Principles

#### BEFORE (❌ Poor Design)
```java
class UserManager {
    public void registerDonor(Donor d) { ... }
    public void registerPatient(Patient p) { ... }
    public void searchDonors(String bg) { ... }
    public void rateDonor(long id, int rating) { ... }
    public void sendNotification(User u, String msg) { ... }
    public void saveToDatabase(User u) { ... }
    // 100+ more methods
}
```

#### AFTER (✅ Good Design)
```java
@Service class AuthService { 
    // Authentication logic only 
}

@Service class DonorService { 
    // Donor operations only 
}

@Service class SearchService { 
    // Search logic only 
}

@Service class NotificationService { 
    // Notification logic only 
}
```

---

**Key Takeaway**: Always design with SOLID principles and OOP concepts in mind. Your code will be more maintainable, testable, and scalable.

