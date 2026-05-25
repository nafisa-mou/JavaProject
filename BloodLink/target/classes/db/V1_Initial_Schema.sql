-- ============================================================
-- BloodLink Database Schema
-- MySQL 8.0+
-- Created: 2026-05-25
-- Purpose: Complete schema for Blood Donor & Patient Platform
-- ============================================================

-- Create Database
CREATE DATABASE IF NOT EXISTS bloodlink_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE bloodlink_db;

-- ============================================================
-- 1. USERS TABLE (Base class with Single Table Inheritance)
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dtype VARCHAR(31) NOT NULL COMMENT 'Discriminator: Donor, Patient, Admin',
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    gender ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL,
    age INT NOT NULL,
    
    -- Address Information
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50),
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    
    -- Account Status
    is_active BOOLEAN DEFAULT TRUE,
    is_verified BOOLEAN DEFAULT FALSE,
    verification_token VARCHAR(255),
    verification_token_expiry TIMESTAMP,
    
    -- Security & Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    
    -- Donor-specific fields (Nullable)
    blood_group VARCHAR(5) COMMENT 'O+, O-, A+, A-, B+, B-, AB+, AB-',
    is_available BOOLEAN DEFAULT FALSE,
    last_donation_date DATE,
    total_donations INT DEFAULT 0 COMMENT 'Track donation count for eligibility',
    
    -- Patient-specific fields (Nullable)
    medical_condition_history TEXT,
    emergency_contact_name VARCHAR(100),
    emergency_contact_number VARCHAR(20),
    
    -- Indexes
    INDEX idx_email (email),
    INDEX idx_dtype (dtype),
    INDEX idx_is_active (is_active),
    INDEX idx_blood_group (blood_group),
    INDEX idx_location (latitude, longitude),
    INDEX idx_created_at (created_at),
    
    CONSTRAINT chk_age CHECK (age >= 18 AND age <= 120),
    CONSTRAINT chk_blood_group CHECK (blood_group IN ('O+', 'O-', 'A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 2. MEDICAL RECORDS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS medical_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    
    -- Vital Signs
    systolic_bp INT COMMENT 'Systolic blood pressure (mmHg)',
    diastolic_bp INT COMMENT 'Diastolic blood pressure (mmHg)',
    heart_rate INT COMMENT 'Beats per minute',
    temperature DECIMAL(5, 2) COMMENT 'Celsius',
    weight DECIMAL(7, 2) COMMENT 'Kilograms',
    height DECIMAL(5, 2) COMMENT 'Centimeters',
    
    -- Lab Results
    hemoglobin DECIMAL(5, 2) COMMENT 'g/dL for donors',
    hematocrit DECIMAL(5, 2) COMMENT 'Percentage',
    platelet_count INT COMMENT 'Per microL',
    
    -- Tests & Results
    blood_type_verified BOOLEAN DEFAULT FALSE,
    hiv_test_result VARCHAR(20) COMMENT 'NEGATIVE, POSITIVE, UNKNOWN',
    hiv_test_date DATE,
    hepatitis_b_result VARCHAR(20),
    hepatitis_b_date DATE,
    hepatitis_c_result VARCHAR(20),
    hepatitis_c_date DATE,
    syphilis_result VARCHAR(20),
    syphilis_date DATE,
    
    -- Clinical Information
    allergies TEXT,
    chronic_conditions TEXT,
    current_medications TEXT,
    recent_surgeries TEXT,
    risk_factors TEXT,
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    recorded_by_id BIGINT,
    
    -- Indexes
    INDEX idx_user_id (user_id),
    INDEX idx_recorded_by (recorded_by_id),
    INDEX idx_created_at (created_at),
    
    CONSTRAINT fk_user_medical FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_recorded_by FOREIGN KEY (recorded_by_id) 
        REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 3. BLOOD REQUESTS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS blood_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id BIGINT NOT NULL,
    donor_id BIGINT,
    
    -- Request Details
    blood_group VARCHAR(5) NOT NULL,
    units_needed INT NOT NULL DEFAULT 1,
    units_received INT DEFAULT 0,
    
    -- Priority & Urgency
    emergency_level ENUM('ROUTINE', 'URGENT', 'CRITICAL', 'LIFE_THREATENING') 
        DEFAULT 'ROUTINE',
    status ENUM('PENDING', 'ACCEPTED', 'DECLINED', 'COMPLETED', 'EXPIRED') 
        DEFAULT 'PENDING',
    
    -- Timestamps
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP,
    completed_at TIMESTAMP,
    expires_at TIMESTAMP COMMENT 'Typically 24 hours after request',
    
    -- Additional Info
    reason_for_blood TEXT,
    hospital_name VARCHAR(100),
    hospital_address TEXT,
    contact_person_name VARCHAR(100),
    contact_person_phone VARCHAR(20),
    
    -- Decline/Completion Info
    decline_reason TEXT,
    completion_notes TEXT,
    test_results TEXT,
    
    -- Indexes
    INDEX idx_patient_id (patient_id),
    INDEX idx_donor_id (donor_id),
    INDEX idx_blood_group (blood_group),
    INDEX idx_status (status),
    INDEX idx_emergency_level (emergency_level),
    INDEX idx_requested_at (requested_at),
    INDEX idx_expires_at (expires_at),
    
    CONSTRAINT fk_patient_blood_req FOREIGN KEY (patient_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_donor_blood_req FOREIGN KEY (donor_id) 
        REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_units CHECK (units_needed > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 4. DONATION HISTORY TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS donation_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    donor_id BIGINT NOT NULL,
    blood_request_id BIGINT,
    
    -- Donation Details
    donation_date DATE NOT NULL,
    units_donated INT NOT NULL DEFAULT 1,
    blood_type_donated VARCHAR(5),
    
    -- Health Screening
    weight_at_donation DECIMAL(7, 2),
    hemoglobin_at_donation DECIMAL(5, 2),
    blood_pressure_at_donation VARCHAR(20),
    
    -- Post-Donation
    complications TEXT,
    notes TEXT,
    next_eligible_date DATE COMMENT 'Typically 56 days after donation',
    
    -- Audit
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    recorded_by_id BIGINT,
    
    -- Indexes
    INDEX idx_donor_id (donor_id),
    INDEX idx_donation_date (donation_date),
    INDEX idx_next_eligible_date (next_eligible_date),
    INDEX idx_blood_request_id (blood_request_id),
    
    CONSTRAINT fk_donor_donation FOREIGN KEY (donor_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_request_donation FOREIGN KEY (blood_request_id) 
        REFERENCES blood_requests(id) ON DELETE SET NULL,
    CONSTRAINT fk_recorded_donation FOREIGN KEY (recorded_by_id) 
        REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 5. DONOR REVIEWS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS donor_reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    donor_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    
    -- Review Content
    rating INT NOT NULL COMMENT 'Rating from 1-5',
    comment TEXT,
    
    -- Review Criteria
    responsiveness_rating INT,
    reliability_rating INT,
    professionalism_rating INT,
    
    -- Review Details
    review_type ENUM('DONATION', 'INTERACTION', 'GENERAL') DEFAULT 'GENERAL',
    is_anonymous BOOLEAN DEFAULT FALSE,
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Indexes
    INDEX idx_donor_id (donor_id),
    INDEX idx_reviewer_id (reviewer_id),
    INDEX idx_rating (rating),
    INDEX idx_created_at (created_at),
    
    CONSTRAINT fk_donor_review FOREIGN KEY (donor_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviewer FOREIGN KEY (reviewer_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_rating CHECK (rating >= 1 AND rating <= 5),
    CONSTRAINT chk_criteria_rating CHECK (
        (responsiveness_rating IS NULL OR (responsiveness_rating >= 1 AND responsiveness_rating <= 5)) AND
        (reliability_rating IS NULL OR (reliability_rating >= 1 AND reliability_rating <= 5)) AND
        (professionalism_rating IS NULL OR (professionalism_rating >= 1 AND professionalism_rating <= 5))
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 6. CHATS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS chats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_one_id BIGINT NOT NULL,
    user_two_id BIGINT NOT NULL,
    
    -- Chat Status
    status ENUM('ACTIVE', 'ARCHIVED', 'BLOCKED') DEFAULT 'ACTIVE',
    
    -- Chat Statistics
    total_messages INT DEFAULT 0,
    unread_messages INT DEFAULT 0,
    last_message_at TIMESTAMP,
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Indexes
    INDEX idx_user_one (user_one_id),
    INDEX idx_user_two (user_two_id),
    INDEX idx_status (status),
    INDEX idx_last_message (last_message_at),
    UNIQUE KEY unique_chat (user_one_id, user_two_id),
    
    CONSTRAINT fk_user_one_chat FOREIGN KEY (user_one_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_two_chat FOREIGN KEY (user_two_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_different_users CHECK (user_one_id != user_two_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 7. MESSAGES TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    chat_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    
    -- Message Content
    content VARCHAR(5000) NOT NULL,
    message_type ENUM('TEXT', 'IMAGE', 'FILE', 'SYSTEM') DEFAULT 'TEXT',
    
    -- Message Status
    is_seen BOOLEAN DEFAULT FALSE,
    is_deleted BOOLEAN DEFAULT FALSE,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    seen_at TIMESTAMP,
    edited_at TIMESTAMP,
    deleted_at TIMESTAMP,
    
    -- Indexes
    INDEX idx_chat_id (chat_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_created_at (created_at),
    INDEX idx_is_seen (is_seen),
    
    CONSTRAINT fk_chat_message FOREIGN KEY (chat_id) 
        REFERENCES chats(id) ON DELETE CASCADE,
    CONSTRAINT fk_sender_message FOREIGN KEY (sender_id) 
        REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 8. NOTIFICATIONS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    
    -- Notification Content
    title VARCHAR(255) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    notification_type ENUM(
        'WELCOME',
        'BLOOD_REQUEST_RECEIVED',
        'REQUEST_ACCEPTED',
        'REQUEST_DECLINED',
        'REQUEST_COMPLETED',
        'NEW_MESSAGE',
        'DONOR_AVAILABLE',
        'USER_ONLINE',
        'USER_OFFLINE',
        'SYSTEM_ALERT',
        'APPOINTMENT_REMINDER',
        'DONATION_REMINDER'
    ) DEFAULT 'SYSTEM_ALERT',
    
    -- Notification Status
    is_read BOOLEAN DEFAULT FALSE,
    
    -- Related Entity
    related_entity_id BIGINT COMMENT 'ID of related blood request, chat, etc.',
    related_entity_type VARCHAR(50) COMMENT 'BloodRequest, Chat, Message, etc.',
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP,
    
    -- Indexes
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_type (notification_type),
    INDEX idx_created_at (created_at),
    
    CONSTRAINT fk_user_notification FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 9. AUDIT LOG TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    
    -- Action Details
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL COMMENT 'User, BloodRequest, Donation, etc.',
    entity_id BIGINT,
    
    -- Change Details
    old_value TEXT,
    new_value TEXT,
    
    -- Request Details
    http_method VARCHAR(10),
    endpoint VARCHAR(255),
    ip_address VARCHAR(45) COMMENT 'Supports IPv6',
    user_agent TEXT,
    
    -- Status
    status_code INT,
    error_message TEXT,
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Indexes
    INDEX idx_user_id (user_id),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_action (action),
    INDEX idx_created_at (created_at),
    
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 10. REFRESH TOKENS TABLE (For JWT Refresh Token Management)
-- ============================================================
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at),
    
    CONSTRAINT fk_user_refresh FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================

-- Location-based queries
CREATE SPATIAL INDEX idx_location_spatial ON users(POINT(latitude, longitude));

-- Composite indexes for common queries
CREATE INDEX idx_blood_available ON users(blood_group, is_available, is_active);
CREATE INDEX idx_donor_eligible ON users(last_donation_date, is_active) 
    WHERE dtype = 'Donor';

-- Message search optimization
CREATE FULLTEXT INDEX idx_message_search ON messages(content);

-- Audit trail queries
CREATE INDEX idx_audit_timerange ON audit_log(created_at, user_id);

-- ============================================================
-- VIEWS FOR COMMON QUERIES
-- ============================================================

-- View: All Active Donors
CREATE OR REPLACE VIEW v_active_donors AS
SELECT 
    id,
    email,
    full_name,
    phone_number,
    blood_group,
    city,
    state,
    latitude,
    longitude,
    is_available,
    total_donations,
    last_donation_date,
    created_at
FROM users
WHERE dtype = 'Donor' AND is_active = TRUE;

-- View: All Active Patients
CREATE OR REPLACE VIEW v_active_patients AS
SELECT 
    id,
    email,
    full_name,
    phone_number,
    city,
    state,
    latitude,
    longitude,
    medical_condition_history,
    emergency_contact_name,
    emergency_contact_number,
    created_at
FROM users
WHERE dtype = 'Patient' AND is_active = TRUE;

-- View: Pending Blood Requests
CREATE OR REPLACE VIEW v_pending_requests AS
SELECT 
    br.id,
    br.blood_group,
    br.units_needed,
    br.emergency_level,
    br.status,
    br.requested_at,
    br.expires_at,
    p.full_name AS patient_name,
    p.city AS patient_city,
    p.latitude AS patient_latitude,
    p.longitude AS patient_longitude
FROM blood_requests br
JOIN users p ON br.patient_id = p.id
WHERE br.status = 'PENDING' AND br.is_active = TRUE;

-- ============================================================
-- STORED PROCEDURES
-- ============================================================

DELIMITER //

-- Procedure: Mark expired requests
CREATE PROCEDURE IF NOT EXISTS mark_expired_requests()
BEGIN
    UPDATE blood_requests
    SET status = 'EXPIRED'
    WHERE status = 'PENDING' 
    AND expires_at < NOW()
    AND expires_at IS NOT NULL;
END//

-- Procedure: Check donor eligibility (56-day rule)
CREATE PROCEDURE IF NOT EXISTS check_donor_eligibility(IN donor_id BIGINT, OUT is_eligible BOOLEAN)
BEGIN
    DECLARE days_since_donation INT;
    
    SELECT DATEDIFF(CURDATE(), MAX(donation_date)) INTO days_since_donation
    FROM donation_history
    WHERE donor_id = donor_id;
    
    IF days_since_donation IS NULL OR days_since_donation >= 56 THEN
        SET is_eligible = TRUE;
    ELSE
        SET is_eligible = FALSE;
    END IF;
END//

-- Procedure: Calculate donor reliability score
CREATE PROCEDURE IF NOT EXISTS calculate_donor_score(IN donor_id BIGINT, OUT score INT)
BEGIN
    DECLARE total_donations INT;
    DECLARE avg_rating DECIMAL(3,2);
    DECLARE calculated_score INT;
    
    SELECT COUNT(*) INTO total_donations
    FROM donation_history
    WHERE donor_id = donor_id;
    
    SELECT AVG(rating) INTO avg_rating
    FROM donor_reviews
    WHERE donor_id = donor_id;
    
    SET avg_rating = COALESCE(avg_rating, 0);
    SET calculated_score = LEAST(100, FLOOR((total_donations * 5) + (avg_rating * 20)));
    SET score = calculated_score;
END//

DELIMITER ;

-- ============================================================
-- CLEANUP & VERIFICATION
-- ============================================================

-- Verify all tables created successfully
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    DATA_LENGTH,
    INDEX_LENGTH
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'bloodlink_db'
ORDER BY TABLE_NAME;

-- Verify all indexes
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    SEQ_IN_INDEX,
    COLUMN_NAME
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'bloodlink_db'
ORDER BY TABLE_NAME, INDEX_NAME;

-- ============================================================
-- END OF SCHEMA
-- ============================================================
