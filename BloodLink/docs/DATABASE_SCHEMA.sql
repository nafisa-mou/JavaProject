-- BloodLink Database Schema
-- MySQL Script to create all necessary tables

USE bloodlink_db;

-- ============ USERS TABLE (Base table with Single Table Inheritance) ============
CREATE TABLE users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_type VARCHAR(50) NOT NULL,  -- DONOR, PATIENT (Discriminator)
    
    -- Basic Information
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(15) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    
    -- Personal Information
    gender VARCHAR(50),
    age INT,
    profile_photo_url VARCHAR(500),
    
    -- Location Information
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    full_address VARCHAR(300),
    latitude DOUBLE,
    longitude DOUBLE,
    
    -- Account Status
    is_active BOOLEAN DEFAULT TRUE,
    is_verified BOOLEAN DEFAULT FALSE,
    is_blocked BOOLEAN DEFAULT FALSE,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    
    -- Donor-specific fields (for inheritance)
    blood_group VARCHAR(5),
    is_available BOOLEAN DEFAULT FALSE,
    last_donation_date DATE,
    total_donations INT DEFAULT 0,
    emergency_available BOOLEAN DEFAULT FALSE,
    average_rating DOUBLE DEFAULT 0.0,
    donor_verified BOOLEAN DEFAULT FALSE,
    
    -- Patient-specific fields
    required_blood_group VARCHAR(5),
    hospital_name VARCHAR(200),
    hospital_city VARCHAR(100),
    hospital_address VARCHAR(300),
    hospital_phone_number VARCHAR(20),
    urgency_level VARCHAR(50),
    
    INDEX idx_email (email),
    INDEX idx_blood_group (blood_group),
    INDEX idx_city (city),
    INDEX idx_location (latitude, longitude),
    INDEX idx_user_type (user_type),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============ MEDICAL RECORDS TABLE ============
CREATE TABLE medical_records (
    record_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    donor_id BIGINT UNIQUE,
    patient_id BIGINT,
    
    -- Medical Information
    chronic_diseases TEXT,
    surgical_history TEXT,
    family_history TEXT,
    current_medications TEXT,
    allergies TEXT,
    vaccinations TEXT,
    
    -- Vital Signs
    blood_pressure_systolic INT,
    blood_pressure_diastolic INT,
    pulse_rate INT,
    body_temperature DOUBLE,
    weight DOUBLE,
    height DOUBLE,
    bmi DOUBLE,
    
    -- Lab Test Results
    hemoglobin DOUBLE,
    white_blood_cells DOUBLE,
    platelets DOUBLE,
    blood_group VARCHAR(5),
    rh_factor VARCHAR(10),
    
    -- Lifestyle
    smoker BOOLEAN DEFAULT FALSE,
    alcohol_consumer BOOLEAN DEFAULT FALSE,
    drug_user BOOLEAN DEFAULT FALSE,
    occupation VARCHAR(300),
    hours_exercise_per_week INT,
    
    -- Status
    is_verified BOOLEAN DEFAULT FALSE,
    verified_by_doctor VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_reviewed_at TIMESTAMP,
    
    FOREIGN KEY (donor_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (patient_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_donor_id (donor_id),
    INDEX idx_patient_id (patient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============ BLOOD REQUESTS TABLE ============
CREATE TABLE blood_requests (
    request_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id BIGINT NOT NULL,
    donor_id BIGINT NOT NULL,
    
    blood_group VARCHAR(5) NOT NULL,
    units INT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',  -- PENDING, ACCEPTED, DECLINED, COMPLETED
    urgency_level VARCHAR(50) DEFAULT 'NORMAL',
    reason VARCHAR(500),
    
    -- Notes
    donor_notes VARCHAR(500),
    patient_notes VARCHAR(500),
    admin_notes VARCHAR(500),
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    responded_at TIMESTAMP,
    completed_at TIMESTAMP,
    
    FOREIGN KEY (patient_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (donor_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_patient_id (patient_id),
    INDEX idx_donor_id (donor_id),
    INDEX idx_status (status),
    INDEX idx_blood_group (blood_group),
    INDEX idx_urgency (urgency_level),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============ CHATS TABLE ============
CREATE TABLE chats (
    chat_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    initiator_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    
    status VARCHAR(50) DEFAULT 'ACTIVE',  -- ACTIVE, ARCHIVED, BLOCKED
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_message_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    archived_at TIMESTAMP,
    
    UNIQUE KEY unique_chat (initiator_id, recipient_id),
    FOREIGN KEY (initiator_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (recipient_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_initiator (initiator_id),
    INDEX idx_recipient (recipient_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============ MESSAGES TABLE ============
CREATE TABLE messages (
    message_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    chat_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    
    content LONGTEXT NOT NULL,
    is_seen BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    seen_at TIMESTAMP,
    
    FOREIGN KEY (chat_id) REFERENCES chats(chat_id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_chat_id (chat_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_created_at (created_at),
    INDEX idx_is_seen (is_seen)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============ NOTIFICATIONS TABLE ============
CREATE TABLE notifications (
    notification_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    
    title VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) DEFAULT 'GENERAL',
    is_read BOOLEAN DEFAULT FALSE,
    
    related_entity_id BIGINT,
    related_entity_type VARCHAR(100),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_type (type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============ DONATION HISTORY TABLE ============
CREATE TABLE donation_history (
    donation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    donor_id BIGINT NOT NULL,
    
    donation_date DATE NOT NULL,
    blood_group_donated VARCHAR(5),
    units_donated INT,
    donation_center_name VARCHAR(100),
    donation_center_location VARCHAR(300),
    doctor_name VARCHAR(200),
    donation_successful BOOLEAN DEFAULT TRUE,
    remarks VARCHAR(500),
    status VARCHAR(100),
    
    -- Blood Test Results
    hemoglobin DOUBLE,
    hbsag BOOLEAN,     -- Hepatitis B
    hcvab BOOLEAN,     -- Hepatitis C
    hiv BOOLEAN,
    vdrl BOOLEAN,      -- Syphilis
    
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (donor_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_donor_id (donor_id),
    INDEX idx_donation_date (donation_date),
    INDEX idx_blood_group (blood_group_donated)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============ DONOR REVIEWS TABLE ============
CREATE TABLE donor_reviews (
    review_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    donor_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    request_id BIGINT,
    
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    is_anonymous BOOLEAN DEFAULT FALSE,
    
    -- Aspect Ratings
    reliability_rating INT CHECK (reliability_rating >= 1 AND reliability_rating <= 5),
    communication_rating INT CHECK (communication_rating >= 1 AND communication_rating <= 5),
    professionalism_rating INT CHECK (professionalism_rating >= 1 AND professionalism_rating <= 5),
    
    -- Status
    is_verified BOOLEAN DEFAULT FALSE,
    is_approved BOOLEAN DEFAULT TRUE,
    is_helpful BOOLEAN DEFAULT FALSE,
    is_flagged BOOLEAN DEFAULT FALSE,
    flag_reason VARCHAR(500),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (donor_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (patient_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (request_id) REFERENCES blood_requests(request_id) ON DELETE SET NULL,
    INDEX idx_donor_id (donor_id),
    INDEX idx_patient_id (patient_id),
    INDEX idx_rating (rating),
    INDEX idx_is_approved (is_approved)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============ CREATE INDEXES FOR PERFORMANCE ============
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone_number);
CREATE INDEX idx_users_blood_group ON users(blood_group);
CREATE INDEX idx_users_location ON users(city, state);
CREATE INDEX idx_requests_status ON blood_requests(status);
CREATE INDEX idx_messages_chat ON messages(chat_id);
CREATE INDEX idx_notifications_user ON notifications(user_id);

-- ============ SAMPLE DATA FOR TESTING ============

-- Insert sample donors
INSERT INTO users (user_type, full_name, email, phone_number, password, gender, age, city, state, country, 
                  blood_group, is_available, is_active, is_verified, created_at)
VALUES 
('DONOR', 'John Donor', 'john@donor.com', '1234567890', 'hashed_password_here', 'Male', 28, 'New York', 'NY', 'USA', 
 'O+', TRUE, TRUE, TRUE, NOW()),
('DONOR', 'Sarah Donor', 'sarah@donor.com', '0987654321', 'hashed_password_here', 'Female', 25, 'New York', 'NY', 'USA',
 'AB+', TRUE, TRUE, TRUE, NOW()),
('PATIENT', 'Tom Patient', 'tom@patient.com', '5551234567', 'hashed_password_here', 'Male', 35, 'New York', 'NY', 'USA',
 NULL, FALSE, TRUE, TRUE, NOW());

-- Display schema info
SELECT 'Database schema created successfully!' AS status;
SHOW TABLES;
