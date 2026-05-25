# BloodLink Database Setup Guide

## Overview

This guide explains how to set up and initialize the BloodLink MySQL database with proper schema, indexes, and sample data for development.

## Prerequisites

- **MySQL 8.0+** installed and running
- **MySQL Command Line Client** or **MySQL Workbench**
- Network access to MySQL server (localhost:3306)
- Administrative privileges to create databases

## Database Structure

The BloodLink database consists of 10 main tables with relationships, constraints, and optimized indexes:

```
┌─────────────────────────────────────────────────────────┐
│                     USERS (Single Table                 │
│                     Inheritance)                        │
│  - Donor (DTYPE='Donor')                                │
│  - Patient (DTYPE='Patient')                            │
│  - Admin (DTYPE='Admin')                                │
└──────────────┬──────────────────────────────────────────┘
               │
    ┌──────────┼──────────┬──────────────┐
    │          │          │              │
    ↓          ↓          ↓              ↓
MEDICAL_    BLOOD_      DONATION_     DONOR_
RECORDS     REQUESTS    HISTORY       REVIEWS
    │          │
    │          └─ BLOOD_REQUESTS ← Donor → Patient
    │
CHATS ←──── Bidirectional Relationship
    │
    ↓
MESSAGES

NOTIFICATIONS ──→ Links to any entity
AUDIT_LOG ───────→ Tracks all changes
REFRESH_TOKENS ──→ JWT token management
```

## Setup Instructions

### Option 1: Using MySQL Command Line

#### Step 1: Create Database & Run Schema

```bash
# Connect to MySQL
mysql -h localhost -u root -p

# Then in MySQL prompt:
mysql> SOURCE /path/to/V1_Initial_Schema.sql;
```

#### Step 2: Load Sample Data

```bash
# In MySQL prompt:
mysql> SOURCE /path/to/V2_Sample_Data.sql;

# Verify data loaded:
mysql> USE bloodlink_db;
mysql> SELECT COUNT(*) FROM users;
mysql> SELECT COUNT(*) FROM blood_requests;
mysql> SELECT COUNT(*) FROM messages;
```

### Option 2: Using MySQL Workbench

1. **Open MySQL Workbench**
2. **Create new connection** (localhost:3306)
3. **Connect** to MySQL
4. **File → Open SQL Script**
5. **Select V1_Initial_Schema.sql**
6. **Execute** (Ctrl+Shift+Enter or Execute button)
7. **Repeat for V2_Sample_Data.sql**

### Option 3: Using Docker Compose (Recommended for Development)

```bash
# In project root directory, run:
docker-compose up -d mysql

# Wait for MySQL to start, then:
docker exec mysql mysql -u root -proot_password < src/main/resources/db/V1_Initial_Schema.sql
docker exec mysql mysql -u root -proot_password < src/main/resources/db/V2_Sample_Data.sql
```

### Option 4: Spring Boot Auto-Configuration

Update `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bloodlink_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=create-drop
```

Spring Boot will automatically create the schema on first run using Hibernate.

## Database Tables Reference

### 1. USERS Table

```sql
-- Single Table Inheritance Pattern
users (
  id BIGINT PRIMARY KEY,
  dtype VARCHAR(31) -- 'Donor', 'Patient', or 'Admin'
  email VARCHAR(100) UNIQUE,
  password_hash VARCHAR(255),
  full_name VARCHAR(100),
  phone_number VARCHAR(20),
  gender ENUM('MALE', 'FEMALE', 'OTHER'),
  age INT,
  
  -- Location
  city VARCHAR(50),
  state VARCHAR(50),
  latitude DECIMAL(10, 8),
  longitude DECIMAL(11, 8),
  
  -- Status
  is_active BOOLEAN,
  is_verified BOOLEAN,
  
  -- Donor-specific (nullable)
  blood_group VARCHAR(5),
  is_available BOOLEAN,
  last_donation_date DATE,
  total_donations INT,
  
  -- Patient-specific (nullable)
  medical_condition_history TEXT,
  emergency_contact_name VARCHAR(100),
  emergency_contact_number VARCHAR(20)
)
```

### 2. MEDICAL_RECORDS Table

Tracks vital signs, lab results, and medical history for users.

```sql
medical_records (
  id BIGINT PRIMARY KEY,
  user_id BIGINT FOREIGN KEY,
  
  -- Vitals
  systolic_bp INT,
  diastolic_bp INT,
  heart_rate INT,
  temperature DECIMAL(5, 2),
  weight DECIMAL(7, 2),
  height DECIMAL(5, 2),
  
  -- Lab Results
  hemoglobin DECIMAL(5, 2),
  hematocrit DECIMAL(5, 2),
  platelet_count INT,
  
  -- Test Results
  hiv_test_result VARCHAR(20),
  hepatitis_b_result VARCHAR(20),
  hepatitis_c_result VARCHAR(20),
  syphilis_result VARCHAR(20)
)
```

### 3. BLOOD_REQUESTS Table

Manages blood requests from patients to donors.

```sql
blood_requests (
  id BIGINT PRIMARY KEY,
  patient_id BIGINT FOREIGN KEY,
  donor_id BIGINT FOREIGN KEY (nullable),
  
  blood_group VARCHAR(5),
  units_needed INT,
  units_received INT,
  
  emergency_level ENUM('ROUTINE', 'URGENT', 'CRITICAL', 'LIFE_THREATENING'),
  status ENUM('PENDING', 'ACCEPTED', 'DECLINED', 'COMPLETED', 'EXPIRED'),
  
  requested_at TIMESTAMP,
  accepted_at TIMESTAMP,
  completed_at TIMESTAMP,
  expires_at TIMESTAMP (typically 24 hours)
)
```

### 4. DONATION_HISTORY Table

Records each donation made by a donor.

```sql
donation_history (
  id BIGINT PRIMARY KEY,
  donor_id BIGINT FOREIGN KEY,
  blood_request_id BIGINT FOREIGN KEY (nullable),
  
  donation_date DATE,
  units_donated INT,
  weight_at_donation DECIMAL(7, 2),
  hemoglobin_at_donation DECIMAL(5, 2),
  blood_pressure_at_donation VARCHAR(20),
  
  next_eligible_date DATE (56-day rule)
)
```

### 5. DONOR_REVIEWS Table

Allows patients/admins to review donors after donations.

```sql
donor_reviews (
  id BIGINT PRIMARY KEY,
  donor_id BIGINT FOREIGN KEY,
  reviewer_id BIGINT FOREIGN KEY,
  
  rating INT (1-5),
  comment TEXT,
  
  responsiveness_rating INT (1-5),
  reliability_rating INT (1-5),
  professionalism_rating INT (1-5),
  
  review_type ENUM('DONATION', 'INTERACTION', 'GENERAL')
)
```

### 6. CHATS Table

Manages one-to-one conversations between users.

```sql
chats (
  id BIGINT PRIMARY KEY,
  user_one_id BIGINT FOREIGN KEY,
  user_two_id BIGINT FOREIGN KEY,
  
  status ENUM('ACTIVE', 'ARCHIVED', 'BLOCKED'),
  total_messages INT,
  unread_messages INT,
  last_message_at TIMESTAMP
)
```

### 7. MESSAGES Table

Stores individual messages in chats.

```sql
messages (
  id BIGINT PRIMARY KEY,
  chat_id BIGINT FOREIGN KEY,
  sender_id BIGINT FOREIGN KEY,
  
  content VARCHAR(5000),
  message_type ENUM('TEXT', 'IMAGE', 'FILE', 'SYSTEM'),
  
  is_seen BOOLEAN,
  is_deleted BOOLEAN,
  
  created_at TIMESTAMP,
  seen_at TIMESTAMP,
  edited_at TIMESTAMP,
  deleted_at TIMESTAMP
)
```

### 8. NOTIFICATIONS Table

Stores notifications for users.

```sql
notifications (
  id BIGINT PRIMARY KEY,
  user_id BIGINT FOREIGN KEY,
  
  title VARCHAR(255),
  message VARCHAR(1000),
  notification_type ENUM(...),
  
  is_read BOOLEAN,
  related_entity_id BIGINT,
  related_entity_type VARCHAR(50)
)
```

### 9. AUDIT_LOG Table

Tracks all user actions and entity changes.

```sql
audit_log (
  id BIGINT PRIMARY KEY,
  user_id BIGINT FOREIGN KEY (nullable),
  
  action VARCHAR(100),
  entity_type VARCHAR(50),
  entity_id BIGINT,
  
  old_value TEXT,
  new_value TEXT,
  
  http_method VARCHAR(10),
  endpoint VARCHAR(255),
  ip_address VARCHAR(45),
  status_code INT,
  
  created_at TIMESTAMP
)
```

### 10. REFRESH_TOKENS Table

Manages JWT refresh tokens.

```sql
refresh_tokens (
  id BIGINT PRIMARY KEY,
  user_id BIGINT FOREIGN KEY UNIQUE,
  
  token VARCHAR(255) UNIQUE,
  expires_at TIMESTAMP,
  revoked_at TIMESTAMP (nullable)
)
```

## Sample Data Included

### Donors (6)
- John Doe (O+, 5 donations, highly reliable)
- Sarah Smith (A+, 3 donations)
- Michael Johnson (B+, 12 donations, currently unavailable)
- Emily Davis (AB-, 1 donation, rare blood type)
- Robert Williams (O-, 18 donations, very experienced)
- Jennifer Brown (A-, 7 donations)

### Patients (3)
- Alice Thompson (Diabetes, Hypertension)
- David Martinez (Heart Disease, High Cholesterol)
- Rachel Green (Anemia, Iron Deficiency)

### Blood Requests (4)
- 1 CRITICAL PENDING (Alice needs O+)
- 1 URGENT ACCEPTED (David, by John Doe)
- 1 ROUTINE COMPLETED (Rachel, by Sarah Smith)
- 1 ROUTINE PENDING (Alice needs AB+)

### Donations (4)
- John Doe donated to Alice's request
- Sarah Smith donated to Rachel's request
- Robert Williams has 1 regular donation
- Jennifer Brown has 1 regular donation

### Chats & Messages (3 active chats, 13 messages)
- Chat between John Doe and Alice Thompson
- Chat between Sarah Smith and David Martinez
- Chat between Robert Williams and Rachel Green

### Notifications (6)
- Blood request alerts
- Donor availability notifications
- Chat message notifications
- User online/offline status

## Indexes for Performance

The schema includes optimized indexes for common queries:

### Location-Based Queries
```sql
-- Spatial index for geographic searches
SPATIAL INDEX idx_location_spatial ON users(POINT(latitude, longitude))
```

### Blood Availability
```sql
-- Find available donors with specific blood group
CREATE INDEX idx_blood_available ON users(blood_group, is_available, is_active)
```

### Donor Eligibility
```sql
-- Check 56-day donation rule
CREATE INDEX idx_donor_eligible ON users(last_donation_date, is_active)
    WHERE dtype = 'Donor'
```

### Message Search
```sql
-- Full-text search in messages
FULLTEXT INDEX idx_message_search ON messages(content)
```

## Views for Common Queries

### v_active_donors
Lists all active donors with their blood groups and location.

```sql
SELECT * FROM v_active_donors;
```

### v_active_patients
Lists all active patients with their medical history.

```sql
SELECT * FROM v_active_patients;
```

### v_pending_requests
Shows pending blood requests with patient and urgency information.

```sql
SELECT * FROM v_pending_requests;
```

## Stored Procedures

### mark_expired_requests()
Automatically expires blood requests that haven't been accepted within 24 hours.

```sql
CALL mark_expired_requests();
```

### check_donor_eligibility(donor_id, is_eligible)
Checks if a donor is eligible to donate (56-day rule).

```sql
CALL check_donor_eligibility(1, @is_eligible);
SELECT @is_eligible;
```

### calculate_donor_score(donor_id, score)
Calculates a donor's reliability score (0-100) based on donations and reviews.

```sql
CALL calculate_donor_score(1, @score);
SELECT @score;
```

## Verification Queries

### Check Total Records

```sql
-- Verify all data loaded
SELECT 'Users' as entity, COUNT(*) as count FROM users
UNION ALL
SELECT 'Blood Requests', COUNT(*) FROM blood_requests
UNION ALL
SELECT 'Donations', COUNT(*) FROM donation_history
UNION ALL
SELECT 'Messages', COUNT(*) FROM messages
UNION ALL
SELECT 'Chats', COUNT(*) FROM chats;
```

### Find Blood Request Status

```sql
SELECT status, COUNT(*) as count FROM blood_requests GROUP BY status;
```

### Find Unread Messages

```sql
SELECT COUNT(*) as unread_count FROM messages WHERE is_seen = FALSE;
```

### Find Active Donors

```sql
SELECT * FROM v_active_donors WHERE is_available = TRUE;
```

### Find Critical Requests

```sql
SELECT * FROM v_pending_requests WHERE emergency_level IN ('CRITICAL', 'LIFE_THREATENING');
```

## Configuration for Spring Boot

### application.properties

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/bloodlink_db?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# Connection Pool
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
```

## Troubleshooting

### Issue: "Access denied for user 'root'@'localhost'"

**Solution:**
- Verify MySQL server is running
- Check username/password in connection string
- Ensure user has privileges: `GRANT ALL PRIVILEGES ON bloodlink_db.* TO 'root'@'localhost';`

### Issue: "Database 'bloodlink_db' doesn't exist"

**Solution:**
- Run V1_Initial_Schema.sql first (creates database)
- Verify script ran without errors

### Issue: "Foreign key constraint fails"

**Solution:**
- Ensure tables are created in correct order (V1 before V2)
- Verify parent records exist before inserting child records
- Check InnoDB engine is enabled: `SHOW ENGINES;`

### Issue: "Duplicate entry for key 'unique_chat'"

**Solution:**
- Clear existing data: `TRUNCATE TABLE chats;`
- Or use `INSERT IGNORE` for test data

## Backup & Restoration

### Backup Database

```bash
mysqldump -u root -p bloodlink_db > bloodlink_backup.sql
```

### Restore from Backup

```bash
mysql -u root -p bloodlink_db < bloodlink_backup.sql
```

## Performance Optimization Tips

1. **Add indexes** on frequently searched columns
2. **Partition** large tables (donation_history, messages) by date
3. **Archive** old records to separate tables
4. **Use query caching** for read-heavy operations
5. **Monitor slow queries**: Enable `slow_query_log`

## Next Steps

1. ✅ Database schema created with all tables
2. ✅ Sample data loaded for development/testing
3. Next: Run Spring Boot application
   ```bash
   mvn clean spring-boot:run
   ```
4. Next: Test REST API endpoints
5. Next: Verify WebSocket connections

---

## File References

- **V1_Initial_Schema.sql** - Complete schema with 10 tables, indexes, views, procedures
- **V2_Sample_Data.sql** - Test data for 6 donors, 3 patients, 4 blood requests, etc.

## Contact & Support

For questions about database setup, refer to:
- [SECURITY_GUIDE.md](SECURITY_GUIDE.md) - Authentication/Authorization
- [WEBSOCKET_GUIDE.md](WEBSOCKET_GUIDE.md) - Real-time features
- Application logs in `logs/bloodlink.log`

---

**Last Updated:** May 25, 2026
**Schema Version:** 1.0
**Sample Data Version:** 1.0
