# Spring Security Configuration Guide

## Overview

BloodLink implements enterprise-grade security using Spring Security 6 with JWT (JSON Web Tokens) for stateless authentication. This provides secure API access while maintaining REST principles.

## Architecture

```
┌─────────────────────────────────────────────────┐
│         Client (Browser/Mobile)                 │
│  Stores JWT in localStorage / sessionStorage    │
└──────────────┬──────────────────────────────────┘
               │
               │ 1. POST /api/auth/login
               │    { email, password }
               ↓
┌──────────────────────────────────────────────────────────┐
│         AuthController                                   │
└──────────────┬───────────────────────────────────────────┘
               │
               │ 2. authService.login(email, password)
               ↓
┌──────────────────────────────────────────────────────────┐
│         DaoAuthenticationProvider                        │
│  (Part of Spring Security)                              │
│  - Loads UserDetails via CustomUserDetailsService       │
│  - Validates password with BCryptPasswordEncoder        │
└──────────────┬───────────────────────────────────────────┘
               │
               │ 3. If valid, generate JWT token
               ↓
┌──────────────────────────────────────────────────────────┐
│         JwtTokenProvider                                 │
│  - Signs JWT with HMAC-SHA512                           │
│  - Sets email & role claims                             │
│  - Sets 24-hour expiration                              │
└──────────────┬───────────────────────────────────────────┘
               │
               │ 4. Return JWT in AuthResponse
               ↓
┌─────────────────────────────────────────────┐
│    Client receives & stores token           │
│    Authorization: Bearer <jwt-token>        │
└──────────────┬──────────────────────────────┘
               │
               │ 5. Subsequent requests
               │    GET /api/donors
               │    Header: Authorization: Bearer <token>
               ↓
┌──────────────────────────────────────────────────────────┐
│         JwtAuthenticationFilter                          │
│  (Spring filter chain)                                   │
│  1. Extract token from Authorization header             │
│  2. Validate signature & expiration                      │
│  3. Extract email & role from token                      │
│  4. Load UserDetails                                     │
│  5. Create UsernamePasswordAuthenticationToken           │
│  6. Set in SecurityContext                               │
└──────────────┬───────────────────────────────────────────┘
               │
               │ 6. Continue to endpoint
               ↓
┌──────────────────────────────────────────────────────────┐
│         SecurityConfig - Authorize Request              │
│  @PreAuthorize checks role/permissions                   │
│  - hasRole('DONOR')                                      │
│  - hasRole('PATIENT')                                    │
│  - authenticated()                                       │
└──────────────┬───────────────────────────────────────────┘
               │
               │ 7. If authorized, execute endpoint
               ↓
┌──────────────────────────────────────────────┐
│    DonorController.getAllDonors()             │
│    Returns data to client                     │
└──────────────────────────────────────────────┘
```

## Configuration Components

### 1. SecurityConfig.java

Main Spring Security configuration with:

**Features:**
- Password encoding: BCrypt with strength 12
- CORS configuration for frontend origins
- HTTP security rules and authorization
- JWT authentication filter integration
- Session management (stateless)
- Exception handling

**Endpoint Access Rules:**

```
PUBLIC ENDPOINTS (No authentication required):
- POST   /api/auth/login
- POST   /api/auth/register-donor
- POST   /api/auth/register-patient
- POST   /api/auth/refresh-token
- POST   /api/auth/forgot-password
- POST   /api/auth/reset-password
- GET    /api/auth/health
- /swagger-ui/**
- /actuator/**

DONOR ENDPOINTS (Requires DONOR role):
- GET    /api/donors/*               (DONOR, PATIENT, ADMIN)
- PUT    /api/donors/*               (DONOR, ADMIN)
- POST   /api/donors/*               (ADMIN)

PATIENT ENDPOINTS (Requires PATIENT role):
- GET    /api/patients/*             (PATIENT, ADMIN)
- PUT    /api/patients/*             (PATIENT, ADMIN)
- POST   /api/patients/*             (PATIENT, ADMIN)

BLOOD REQUEST ENDPOINTS:
- GET    /api/requests/*             (DONOR, PATIENT, ADMIN)
- POST   /api/requests/*             (DONOR, PATIENT, ADMIN)
- PUT    /api/requests/*             (DONOR, PATIENT, ADMIN)

CHAT ENDPOINTS:
- /api/chats/**                       (Authenticated)
- /api/messages/**                    (Authenticated)

NOTIFICATION ENDPOINTS:
- /api/notifications/**               (Authenticated)

ADMIN ENDPOINTS:
- /api/admin/**                       (ADMIN only)
```

### 2. CustomUserDetailsService.java

Implements `UserDetailsService` interface:

**Methods:**
- `loadUserByUsername(email)` - Load user by email from database
- `loadUserById(userId)` - Load user by ID (alternative)

**Returns:**
- UserDetails with email, password hash, authorities
- Checks if user account is active
- Maps user role to Spring Security authorities (ROLE_DONOR, ROLE_PATIENT, ROLE_ADMIN)

### 3. JwtAuthenticationFilter.java

Extends `OncePerRequestFilter`:

**Flow:**
1. Extract JWT token from Authorization header
2. Validate token signature & expiration
3. Extract email & role claims
4. Load user details via CustomUserDetailsService
5. Create UsernamePasswordAuthenticationToken
6. Set in SecurityContext
7. Continue to endpoint

**Error Handling:**
- Invalid token: Skip authentication (non-authenticated request)
- Expired token: Skip authentication
- Missing token: Skip authentication

### 4. JwtAuthenticationEntryPoint.java

Handles unauthorized access (HTTP 401):

**Response:**
```json
{
  "success": false,
  "error": "UNAUTHORIZED",
  "message": "Full authentication is required to access this resource",
  "status": 401,
  "timestamp": "2026-05-25T14:30:00",
  "path": "/api/donors"
}
```

### 5. JwtAccessDeniedHandler.java

Handles access denied (HTTP 403):

**Response:**
```json
{
  "success": false,
  "error": "FORBIDDEN",
  "message": "You do not have permission to access this resource",
  "status": 403,
  "timestamp": "2026-05-25T14:30:00",
  "path": "/api/admin/users"
}
```

## Authentication Flow

### 1. User Registration

```bash
POST /api/auth/register-donor
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "SecurePass123!",
  "phoneNumber": "+1234567890",
  "fullName": "John Doe",
  "gender": "MALE",
  "age": 30,
  "city": "New York",
  "state": "NY",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "bloodGroup": "O+"
}

Response:
{
  "success": true,
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "userId": 1,
  "email": "john@example.com",
  "fullName": "John Doe",
  "userRole": "DONOR",
  "expiresIn": 86400000
}
```

### 2. User Login

```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "SecurePass123!"
}

Response:
{
  "success": true,
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "userId": 1,
  "email": "john@example.com",
  "userRole": "DONOR",
  "expiresIn": 86400000
}
```

### 3. Use Token in Requests

```bash
GET /api/donors
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...

Response:
{
  "success": true,
  "count": 15,
  "data": [...]
}
```

### 4. Token Refresh

When token approaches expiration:

```bash
POST /api/auth/refresh-token
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}

Response:
{
  "success": true,
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "expiresIn": 86400000
}
```

## JWT Token Structure

```
Token: eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c

Structure:
- Header (alg, typ): {"alg":"HS512","typ":"JWT"}
- Payload (claims): {"email":"john@example.com","userRole":"DONOR","exp":1234567890,"iat":1234567000}
- Signature: HMACSHA512(header.payload, secret)

JWT Claims:
- email: User's email address
- userRole: User's role (DONOR, PATIENT, ADMIN)
- exp: Token expiration (24 hours from creation)
- iat: Token issued at timestamp
```

## Role-Based Access Control

### @PreAuthorize Annotations

```java
// Public endpoint - no authentication
@GetMapping("/health")
public ResponseEntity<?> health() { ... }

// Requires any authentication
@GetMapping("/donors")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<?> getDonors() { ... }

// Requires DONOR role
@GetMapping("/donors/{id}/profile")
@PreAuthorize("hasRole('DONOR')")
public ResponseEntity<?> getDonorProfile(@PathVariable Long id) { ... }

// Requires PATIENT role
@PostMapping("/requests")
@PreAuthorize("hasRole('PATIENT')")
public ResponseEntity<?> createRequest(@RequestBody BloodRequestDto req) { ... }

// Requires ADMIN role
@PostMapping("/donors/{id}/donation")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> recordDonation(@PathVariable Long id) { ... }

// Multiple roles allowed
@GetMapping("/requests")
@PreAuthorize("hasAnyRole('DONOR', 'PATIENT', 'ADMIN')")
public ResponseEntity<?> getRequests() { ... }

// Custom expression
@PutMapping("/profile/{id}")
@PreAuthorize("@userService.isOwner(#id, authentication.principal.userId)")
public ResponseEntity<?> updateProfile(@PathVariable Long id) { ... }
```

### Role Hierarchy

```
ADMIN > DONOR, PATIENT
```

Admin users have access to all endpoints. Donors and Patients have specific endpoint access.

## Security Best Practices

### 1. Password Security
✅ **BCrypt with strength 12:** Passwords hashed with 2^12 iterations
✅ **Never store plaintext passwords**
✅ **Minimum length validation** (enforced via ConstraintValidator)

### 2. Token Security
✅ **HMAC-SHA512 signature:** Prevents token tampering
✅ **24-hour expiration:** Reduces exposure if token leaked
✅ **Bearer scheme:** Standard JWT transmission method
✅ **Refresh tokens:** Separate long-lived tokens for refreshing access

### 3. CORS Security
✅ **Whitelist specific origins** (not wildcard)
✅ **Restrict to allowed HTTP methods** (GET, POST, etc.)
✅ **Control exposed headers** (only necessary ones)
✅ **Preflight cache timeout** (12 hours)

### 4. Endpoint Security
✅ **Principle of least privilege:** Users have minimal required permissions
✅ **Public/private endpoints clearly defined**
✅ **Admin endpoints heavily restricted**
✅ **Method-level security** (@PreAuthorize) enforced

### 5. Session Security
✅ **Stateless sessions** (no server-side session storage)
✅ **No JSESSIONID cookies** (inappropriate for REST)
✅ **No CSRF protection needed** (stateless JWT)
✅ **Each request is independent**

### 6. HTTP Security
✅ **HTTPS enforcement** (configure in production)
✅ **Secure header management** (HSTS, X-Frame-Options, etc.)
✅ **Content-Type validation** (application/json required)
✅ **Request size limits** (prevent DoS attacks)

## Error Handling

### HTTP Status Codes

| Code | Scenario | Handler |
|------|----------|---------|
| 200 | Success | Response returned |
| 201 | Resource created | Created response returned |
| 400 | Bad request | Validation errors |
| 401 | Unauthorized | JwtAuthenticationEntryPoint |
| 403 | Forbidden | JwtAccessDeniedHandler |
| 404 | Not found | ResourceNotFoundException |
| 500 | Server error | Global exception handler |

### Error Response Format

```json
{
  "success": false,
  "error": "UNAUTHORIZED",
  "message": "Full authentication is required",
  "status": 401,
  "timestamp": "2026-05-25T14:30:00",
  "path": "/api/donors"
}
```

## Configuration Properties

Add to `application.properties`:

```properties
# JWT Configuration
app.jwt.secret=<your-256-bit-secret-key>
app.jwt.expiration=86400000
app.jwt.refresh-token-expiration=604800000

# Security
spring.security.user.name=admin
spring.security.user.password=admin123

# CORS
spring.web.cors.allowed-origins=http://localhost:3000,http://localhost:8080,http://localhost:4200
spring.web.cors.allowed-methods=GET,POST,PUT,DELETE
spring.web.cors.allowed-headers=*
spring.web.cors.allow-credentials=true
```

## Testing Security

### Bypass Authentication (Development Only)

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class SecurityTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(roles = "DONOR")
    void testDonorEndpoint() throws Exception {
        mockMvc.perform(get("/api/donors"))
            .andExpect(status().isOk());
    }
}
```

### Test with JWT Token

```java
@Test
void testWithJwtToken() throws Exception {
    String token = generateTestToken("DONOR");
    
    mockMvc.perform(get("/api/donors")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
}
```

## Troubleshooting

### Issue: 401 Unauthorized on protected endpoint

**Solution:**
1. Verify token is in Authorization header
2. Check token format: "Bearer <token>"
3. Verify token hasn't expired
4. Check token signature validation

### Issue: 403 Forbidden (insufficient permissions)

**Solution:**
1. Verify user has required role
2. Check @PreAuthorize expression
3. Verify role is correctly mapped (ROLE_ prefix)
4. Check SecurityContext has correct authorities

### Issue: CORS error from frontend

**Solution:**
1. Verify origin is in allowed list
2. Check preflight request succeeds (OPTIONS)
3. Verify CORS headers in response
4. Check browser console for specific error

## Files

- `SecurityConfig.java` - Main security configuration
- `CustomUserDetailsService.java` - User details loading
- `JwtAuthenticationFilter.java` - JWT token validation
- `JwtAuthenticationEntryPoint.java` - Unauthorized handler
- `JwtAccessDeniedHandler.java` - Forbidden handler
- `JwtTokenProvider.java` - JWT generation/validation (util package)

## Production Checklist

- [ ] Use HTTPS (TLS 1.2+)
- [ ] Store JWT secret securely (environment variable/vault)
- [ ] Configure appropriate token expiration (balance security/UX)
- [ ] Enable HSTS headers
- [ ] Set secure cookie flags
- [ ] Implement rate limiting
- [ ] Log security events
- [ ] Monitor for suspicious patterns
- [ ] Regular security updates
- [ ] Penetration testing
