# BloodLink - Deployment & Setup Guide

## 🚀 Quick Start Guide

### Prerequisites
- **Java 17 or higher**: Download from [oracle.com](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.8+**: Download from [maven.apache.org](https://maven.apache.org/download.cgi)
- **MySQL 8.0+**: Download from [mysql.com](https://www.mysql.com/downloads/)
- **Git**: Download from [git-scm.com](https://git-scm.com/)
- **Postman** (optional): For API testing

---

## ⚡ 5-Minute Setup

### Step 1: Create Database
```bash
# Open MySQL command line
mysql -u root -p

# Create database
CREATE DATABASE bloodlink_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'bloodlink_user'@'localhost' IDENTIFIED BY 'BloodLink@123';
GRANT ALL PRIVILEGES ON bloodlink_db.* TO 'bloodlink_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### Step 2: Clone & Setup Project
```bash
# Clone repository
git clone https://github.com/yourusername/bloodlink.git
cd BloodLink

# Configure application properties
# Edit: src/main/resources/application.properties

spring.datasource.url=jdbc:mysql://localhost:3306/bloodlink_db
spring.datasource.username=bloodlink_user
spring.datasource.password=BloodLink@123
app.jwt.secret=your-super-secret-key-min-256-bits-long-change-in-production
```

### Step 3: Build & Run
```bash
# Build project
mvn clean install

# Run application
mvn spring-boot:run

# Or run JAR directly
java -jar target/bloodlink-1.0.0.jar
```

### Step 4: Verify Application
```
✓ Open browser: http://localhost:8080
✓ API Ready: http://localhost:8080/api/donors
✓ Database connected and initialized automatically
```

---

## 📋 Detailed Installation Guide

### Windows Setup

#### 1. Install Java 17
```batch
# Download and install from oracle.com
# Verify installation
java -version
javac -version

# Set JAVA_HOME environment variable
setx JAVA_HOME "C:\Program Files\Java\jdk-17"
```

#### 2. Install Maven
```batch
# Download from maven.apache.org
# Extract to: C:\Program Files\Apache\maven

# Add to System Environment Variables
# M2_HOME = C:\Program Files\Apache\maven
# Add to PATH: %M2_HOME%\bin

# Verify
mvn -version
```

#### 3. Install MySQL
```batch
# Download MySQL Community Server from mysql.com
# Run installer and follow wizard
# Default port: 3306
# Create root user with password

# Verify
mysql --version
```

#### 4. Create Database
```batch
# Open Command Prompt as Administrator
mysql -u root -p

> CREATE DATABASE bloodlink_db CHARACTER SET utf8mb4;
> EXIT;
```

#### 5. Build & Run BloodLink
```batch
cd C:\path\to\BloodLink

# Build
mvn clean install

# Run
mvn spring-boot:run
```

---

### Linux/Mac Setup

#### 1. Install Java 17
```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install openjdk-17-jdk

# macOS (using Homebrew)
brew install openjdk@17
```

#### 2. Install Maven
```bash
# Ubuntu/Debian
sudo apt-get install maven

# macOS
brew install maven

# Verify
mvn -version
```

#### 3. Install MySQL
```bash
# Ubuntu/Debian
sudo apt-get install mysql-server

# macOS
brew install mysql

# Start MySQL
sudo systemctl start mysql    # Linux
brew services start mysql     # macOS
```

#### 4. Create Database
```bash
mysql -u root -p

> CREATE DATABASE bloodlink_db CHARACTER SET utf8mb4;
> CREATE USER 'bloodlink'@'localhost' IDENTIFIED BY 'BloodLink@123';
> GRANT ALL PRIVILEGES ON bloodlink_db.* TO 'bloodlink'@'localhost';
> FLUSH PRIVILEGES;
> EXIT;
```

#### 5. Build & Run
```bash
cd /path/to/BloodLink
mvn clean install
mvn spring-boot:run
```

---

## 🐳 Docker Deployment

### Using Docker Compose

Create `docker-compose.yml`:
```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: bloodlink_db
      MYSQL_USER: bloodlink_user
      MYSQL_PASSWORD: BloodLink@123
      MYSQL_ROOT_PASSWORD: root_password
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  bloodlink:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/bloodlink_db
      SPRING_DATASOURCE_USERNAME: bloodlink_user
      SPRING_DATASOURCE_PASSWORD: BloodLink@123
    depends_on:
      - mysql

volumes:
  mysql_data:
```

Create `Dockerfile`:
```dockerfile
FROM openjdk:17-slim

COPY target/bloodlink-1.0.0.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
```

Run:
```bash
docker-compose up --build
```

---

## ☁️ Cloud Deployment

### AWS EC2 Deployment

#### 1. Create EC2 Instance
```bash
# Launch Ubuntu 22.04 instance
# Security group: Allow ports 22, 80, 443, 8080
# Create key pair for SSH access
```

#### 2. Connect & Setup
```bash
# SSH into instance
ssh -i your-key.pem ubuntu@your-instance-ip

# Update system
sudo apt-get update && sudo apt-get upgrade -y

# Install dependencies
sudo apt-get install -y openjdk-17-jdk maven mysql-server git

# Clone repository
git clone https://github.com/yourusername/bloodlink.git
cd BloodLink
```

#### 3. Configure Database
```bash
# Start MySQL
sudo systemctl start mysql
sudo mysql < docs/DATABASE_SCHEMA.sql

# Create user
sudo mysql -e "CREATE USER 'bloodlink'@'localhost' IDENTIFIED BY 'password';"
sudo mysql -e "GRANT ALL ON bloodlink_db.* TO 'bloodlink'@'localhost';"
```

#### 4. Build & Run
```bash
mvn clean install
java -jar target/bloodlink-1.0.0.jar --server.port=8080
```

#### 5. Setup Nginx Reverse Proxy
```bash
sudo apt-get install nginx

# Create config file
sudo nano /etc/nginx/sites-available/bloodlink

# Add configuration:
server {
    listen 80;
    server_name your-domain.com;
    
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}

# Enable site
sudo ln -s /etc/nginx/sites-available/bloodlink /etc/nginx/sites-enabled/
sudo systemctl restart nginx
```

---

### Google Cloud Run Deployment

```bash
# Build Docker image
docker build -t bloodlink:latest .

# Tag for Google Container Registry
docker tag bloodlink:latest gcr.io/YOUR-PROJECT-ID/bloodlink:latest

# Push to registry
docker push gcr.io/YOUR-PROJECT-ID/bloodlink:latest

# Deploy to Cloud Run
gcloud run deploy bloodlink \
  --image gcr.io/YOUR-PROJECT-ID/bloodlink:latest \
  --platform managed \
  --region us-central1 \
  --set-env-vars SPRING_DATASOURCE_URL=... \
  --set-env-vars SPRING_DATASOURCE_USERNAME=... \
  --set-env-vars SPRING_DATASOURCE_PASSWORD=...
```

---

### Heroku Deployment

```bash
# Install Heroku CLI
# Create Procfile
echo "web: java -jar target/bloodlink-1.0.0.jar" > Procfile

# Create app
heroku create your-bloodlink-app

# Add MySQL add-on
heroku addons:create cleardb:ignite

# Set environment variables
heroku config:set APP_JWT_SECRET=your-secret

# Deploy
git push heroku main
```

---

## 📊 Environment Variables

### Development
```properties
SPRING_PROFILE_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/bloodlink_db
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=password
APP_JWT_SECRET=dev-secret-key
APP_JWT_EXPIRATION=86400000
```

### Production
```properties
SPRING_PROFILE_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:mysql://prod-db-host:3306/bloodlink_db
SPRING_DATASOURCE_USERNAME=prod_user
SPRING_DATASOURCE_PASSWORD=strong_password
APP_JWT_SECRET=production-secret-key-min-256-bits
APP_JWT_EXPIRATION=86400000
SERVER_SSL_ENABLED=true
SERVER_SSL_KEYSTORE=classpath:keystore.p12
SERVER_SSL_KEYSTORE_PASSWORD=keystorepass
```

---

## 🧪 Testing the Application

### Using cURL

```bash
# Register as Donor
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "John Donor",
    "email": "john@donor.com",
    "phoneNumber": "1234567890",
    "password": "password123",
    "userRole": "DONOR",
    "bloodGroup": "O+"
  }'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@donor.com",
    "password": "password123"
  }'

# Get all donors
curl -X GET http://localhost:8080/api/donors \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Using Postman

1. **Create Collection**: BloodLink API
2. **Create Requests**:
   - POST /auth/register
   - POST /auth/login
   - GET /donors
   - GET /donors/{id}
   - POST /requests
   - etc.

---

## 📈 Performance Tuning

### Database Optimization
```sql
-- Create indexes
CREATE INDEX idx_users_blood_group ON users(blood_group);
CREATE INDEX idx_requests_status ON blood_requests(status);
CREATE INDEX idx_messages_chat ON messages(chat_id);

-- Check slow queries
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;
```

### Application Optimization
```properties
# Connection pooling
spring.datasource.hikari.maximum-pool-size=30
spring.datasource.hikari.minimum-idle=10

# Caching
spring.cache.type=redis
spring.redis.url=redis://localhost:6379

# Lazy loading
spring.jpa.properties.hibernate.enable_lazy_load_no_trans=true
```

---

## 🔒 Security Checklist

- [ ] Change JWT secret to 256+ bit key
- [ ] Use HTTPS in production
- [ ] Enable CORS only for trusted domains
- [ ] Set secure cookies
- [ ] Implement rate limiting
- [ ] Use environment variables for secrets
- [ ] Regular security updates
- [ ] Database backups
- [ ] HTTPS certificates (Let's Encrypt)
- [ ] WAF (Web Application Firewall)

---

## 📋 Troubleshooting

### Port Already in Use
```bash
# Find process using port 8080
lsof -i :8080

# Kill process
kill -9 <PID>

# Or use different port
java -jar -Dserver.port=9090 target/bloodlink-1.0.0.jar
```

### Database Connection Error
```properties
# Verify settings
jdbc:mysql://localhost:3306/bloodlink_db?useSSL=false&serverTimezone=UTC

# Check MySQL status
sudo systemctl status mysql

# Test connection
mysql -u root -p -e "SELECT 1"
```

### JWT Token Issues
```bash
# Check token expiration
# Verify JWT secret is set correctly
# Check Authorization header format: "Bearer <token>"
```

---

## 📞 Support & Resources

- **Documentation**: [GitHub Wiki](https://github.com/yourrepo/wiki)
- **Issues**: [GitHub Issues](https://github.com/yourrepo/issues)
- **Email**: support@bloodlink.com

