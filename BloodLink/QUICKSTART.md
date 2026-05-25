# BloodLink Quick Start Guide

## 🚀 Get Started in 5 Minutes

### Option 1: Docker Compose (Recommended for Development)

```bash
# Navigate to project root
cd BloodLink

# Start MySQL with sample data
docker-compose up -d mysql

# Verify MySQL is running
docker-compose logs mysql

# MySQL will be available at:
# Host: localhost
# Port: 3306
# Username: root
# Password: root_password
# Database: bloodlink_db
```

### Option 2: Manual MySQL Setup

#### Windows
```batch
# Download MySQL 8.0 from mysql.com
# Install and start MySQL Service

# Open Command Prompt
cd "C:\Program Files\MySQL\MySQL Server 8.0\bin"

# Connect to MySQL
mysql -u root -p

# In MySQL prompt:
mysql> SOURCE C:\path\to\V1_Initial_Schema.sql;
mysql> SOURCE C:\path\to\V2_Sample_Data.sql;
```

#### macOS/Linux
```bash
# Install MySQL (if not already installed)
# macOS: brew install mysql
# Linux: apt-get install mysql-server

# Start MySQL
mysql.server start

# Connect
mysql -u root

# In MySQL prompt:
mysql> SOURCE /path/to/V1_Initial_Schema.sql;
mysql> SOURCE /path/to/V2_Sample_Data.sql;
```

### Option 3: MySQL Workbench (GUI)

1. Open MySQL Workbench
2. Create connection to localhost:3306
3. Connect with username: root, password: root_password
4. File → Open SQL Script → Select `V1_Initial_Schema.sql`
5. Execute (Ctrl+Shift+Enter)
6. Repeat for `V2_Sample_Data.sql`

---

## 🗄️ Database Location

Database schema files are in:
```
BloodLink/
├── src/main/resources/db/
│   ├── V1_Initial_Schema.sql    # Database schema (tables, indexes, views, procedures)
│   └── V2_Sample_Data.sql        # Sample test data (donors, patients, requests, etc.)
├── DATABASE_SETUP.md              # Detailed setup documentation
├── docker-compose.yml             # Docker configuration
└── Dockerfile                      # Spring Boot Docker image
```

---

## 📊 Database Contents

### After Running Scripts, You'll Have:

✅ **10 Database Tables**
- users, medical_records, blood_requests, donation_history, donor_reviews
- chats, messages, notifications, audit_log, refresh_tokens

✅ **6 Sample Donors**
- John Doe (O+), Sarah Smith (A+), Michael Johnson (B+)
- Emily Davis (AB-), Robert Williams (O-), Jennifer Brown (A-)

✅ **3 Sample Patients**
- Alice Thompson, David Martinez, Rachel Green

✅ **4 Blood Requests**
- 1 Critical Pending, 1 Urgent Accepted, 1 Routine Completed, 1 Routine Pending

✅ **13 Chat Messages**
- Between donors and patients coordinating donations

✅ **Indexes & Stored Procedures**
- Location-based search indexes
- Donor eligibility check (56-day rule)
- Reliability score calculation

---

## 🔗 Verify Database Setup

### Connect to Database

```bash
# Using MySQL CLI
mysql -h localhost -u root -p bloodlink_db

# Or with Docker
docker exec -it bloodlink-mysql mysql -u root -proot_password bloodlink_db
```

### Check Tables

```sql
-- Show all tables
SHOW TABLES;

-- Count records in each table
SELECT 'Users' as table_name, COUNT(*) as count FROM users
UNION ALL SELECT 'Blood Requests', COUNT(*) FROM blood_requests
UNION ALL SELECT 'Messages', COUNT(*) FROM messages
UNION ALL SELECT 'Chats', COUNT(*) FROM chats;

-- Show sample donors
SELECT id, full_name, blood_group, is_available FROM users WHERE dtype = 'Donor';

-- Show pending blood requests
SELECT id, blood_group, emergency_level, status FROM blood_requests WHERE status = 'PENDING';
```

---

## 🏃 Running Spring Boot Application

### Prerequisites
- Java 17+
- Maven 3.9+

### Run Application

```bash
# Navigate to project directory
cd BloodLink

# Build and run
mvn clean spring-boot:run

# Application will start at:
# http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
# WebSocket: ws://localhost:8080/ws/chat
```

### Update Database Connection (if needed)

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bloodlink_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root_password
```

---

## 🧪 Test Database

### Test Sample Data

```bash
# Get all donors
curl http://localhost:8080/api/donors

# Get all patients
curl http://localhost:8080/api/patients

# Get blood requests
curl http://localhost:8080/api/requests

# Login with test user (sample donor)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.donor@bloodlink.com",
    "password": "password123"
  }'

# View sample chat
curl http://localhost:8080/api/chats

# View sample messages
curl http://localhost:8080/api/messages
```

---

## 🔍 View Sample Data

### MySQL CLI Examples

```sql
-- Show all active donors
SELECT * FROM v_active_donors;

-- Show all active patients
SELECT * FROM v_active_patients;

-- Show pending blood requests
SELECT * FROM v_pending_requests;

-- Show all messages between two users
SELECT * FROM messages 
WHERE chat_id IN (
  SELECT id FROM chats 
  WHERE (user_one_id = 1 AND user_two_id = 7) 
     OR (user_one_id = 7 AND user_two_id = 1)
);

-- Show donor reliability scores
CALL calculate_donor_score(1, @score);
SELECT @score as donor_reliability_score;

-- Check donor eligibility (56-day rule)
CALL check_donor_eligibility(1, @eligible);
SELECT @eligible as is_eligible_to_donate;

-- Show all notifications for a user
SELECT * FROM notifications WHERE user_id = 7 ORDER BY created_at DESC;
```

---

## 📱 Test User Credentials

Use these credentials to login and test the API:

### Donors
```
Email: john.donor@bloodlink.com
Password: (hashed, use /api/auth/login endpoint)
```

```
Email: sarah.smith@bloodlink.com
Email: michael.johnson@bloodlink.com
Email: emily.davis@bloodlink.com
Email: robert.williams@bloodlink.com
Email: jennifer.brown@bloodlink.com
```

### Patients
```
Email: alice.thompson@bloodlink.com
Email: david.martinez@bloodlink.com
Email: rachel.green@bloodlink.com
```

> **Note:** All sample users have the same password hash. To set a specific password:
> 1. Use the `/api/auth/register-donor` or `/api/auth/register-patient` endpoint
> 2. Or manually update the password_hash in the database

---

## 🐳 Docker Commands

```bash
# Start only database
docker-compose up mysql

# Start database with PhpMyAdmin admin UI
docker-compose --profile dev-admin up mysql phpmyadmin

# Start full stack (database + app)
docker-compose --profile full-stack up

# Stop all services
docker-compose down

# View logs
docker-compose logs mysql

# Execute MySQL command in container
docker exec -it bloodlink-mysql mysql -u root -proot_password bloodlink_db

# Backup database from Docker
docker exec bloodlink-mysql mysqldump -u root -proot_password bloodlink_db > backup.sql

# Restore database from backup
docker exec -i bloodlink-mysql mysql -u root -proot_password bloodlink_db < backup.sql
```

---

## 🌐 PhpMyAdmin (Optional)

If using Docker with dev-admin profile:
- URL: http://localhost:8081
- Server: mysql
- Username: root
- Password: root_password

---

## 🔧 Troubleshooting

### MySQL Connection Error
```
Error: "Can't connect to MySQL server on 'localhost:3306'"

Solution:
1. Verify MySQL is running: docker ps
2. Check credentials in application.properties
3. Restart container: docker-compose restart mysql
```

### Port Already in Use
```
Error: "Port 3306 already in use"

Solution:
1. Kill existing MySQL: lsof -ti:3306 | xargs kill -9
2. Or change port in docker-compose.yml: ports: ["3307:3306"]
```

### Sample Data Not Loading
```
Solution:
1. Verify V1_Initial_Schema.sql ran first
2. Check MySQL logs: docker-compose logs mysql
3. Manually run: docker exec bloodlink-mysql mysql -u root -proot_password bloodlink_db < /path/to/V2_Sample_Data.sql
```

### Stored Procedures Not Working
```
Solution:
1. Verify DELIMITER is properly set
2. Check error: CALL mark_expired_requests(); SHOW ERRORS;
3. Drop and recreate: DROP PROCEDURE IF EXISTS mark_expired_requests;
```

---

## 📋 Database Schema Quick Reference

```
users (10 users: 6 donors + 3 patients + 1 admin)
├── medical_records (3 records)
├── blood_requests (4 requests)
│   └── donation_history (4 donations)
│       └── donor_reviews (3 reviews)
├── chats (3 chats)
│   └── messages (13 messages)
├── notifications (6 notifications)
├── audit_log (empty, logs at runtime)
└── refresh_tokens (2 tokens)
```

---

## 🚀 Next Steps

1. ✅ Database setup complete
2. ✅ Sample data loaded
3. 🔄 **Run Spring Boot application**
   ```bash
   mvn clean spring-boot:run
   ```
4. 🔄 **Test REST API endpoints**
   ```bash
   curl http://localhost:8080/api/donors
   ```
5. 🔄 **Connect to WebSocket**
   ```javascript
   const ws = new SockJS('http://localhost:8080/ws/chat');
   const stompClient = Stomp.over(ws);
   stompClient.connect({}, onConnect);
   ```
6. 🔄 **Integrate with frontend**
   - React, Vue, or Angular client
   - Use JWT tokens from /api/auth/login
7. 🔄 **Run integration tests**
   ```bash
   mvn test
   ```

---

## 📚 Documentation

- [DATABASE_SETUP.md](DATABASE_SETUP.md) - Detailed database setup guide
- [SECURITY_GUIDE.md](SECURITY_GUIDE.md) - Authentication & authorization
- [WEBSOCKET_GUIDE.md](WEBSOCKET_GUIDE.md) - Real-time messaging setup
- [API Documentation](http://localhost:8080/swagger-ui.html) - Auto-generated Swagger UI (after running app)

---

## 📞 Support

For issues or questions:
1. Check [DATABASE_SETUP.md](DATABASE_SETUP.md) troubleshooting section
2. Review MySQL error logs: `docker-compose logs mysql`
3. Check Spring Boot logs: `docker-compose logs app`
4. Inspect database: `mysql -h localhost -u root -p bloodlink_db`

---

**Created:** May 25, 2026  
**Status:** ✅ Production-Ready Database Schema  
**Sample Data:** ✅ Ready for Development & Testing
