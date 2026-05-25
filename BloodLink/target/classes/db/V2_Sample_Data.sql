-- ============================================================
-- BloodLink Sample/Test Data
-- MySQL 8.0+
-- Created: 2026-05-25
-- Purpose: Populate database with realistic test data for development
-- ============================================================

USE bloodlink_db;

-- ============================================================
-- SAMPLE DATA: USERS (Donors)
-- ============================================================

INSERT INTO users (
    dtype, email, password_hash, full_name, phone_number, 
    gender, age, city, state, latitude, longitude, 
    is_active, is_verified, blood_group, is_available, 
    total_donations, created_at, last_login_at
) VALUES
-- Donor 1: John Doe (O+ Blood - Universal Donor)
(
    'Donor',
    'john.donor@bloodlink.com',
    '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86AGR0wqaS8',
    'John Doe',
    '+1-555-0101',
    'MALE',
    32,
    'New York',
    'NY',
    40.7128,
    -74.0060,
    TRUE,
    TRUE,
    'O+',
    TRUE,
    5,
    '2024-06-15 08:30:00',
    '2026-05-24 14:22:00'
),

-- Donor 2: Sarah Smith (A+ Blood)
(
    'Donor',
    'sarah.smith@bloodlink.com',
    '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86AGR0wqaS8',
    'Sarah Smith',
    '+1-555-0102',
    'FEMALE',
    28,
    'Los Angeles',
    'CA',
    34.0522,
    -118.2437,
    TRUE,
    TRUE,
    'A+',
    TRUE,
    3,
    '2024-07-20 10:15:00',
    '2026-05-23 09:45:00'
),

-- Donor 3: Michael Johnson (B+ Blood)
(
    'Donor',
    'michael.johnson@bloodlink.com',
    '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86AGR0wqaS8',
    'Michael Johnson',
    '+1-555-0103',
    'MALE',
    45,
    'Chicago',
    'IL',
    41.8781,
    -87.6298,
    TRUE,
    TRUE,
    'B+',
    FALSE,
    12,
    '2024-05-10 14:00:00',
    '2026-05-20 11:30:00'
),

-- Donor 4: Emily Davis (AB- Blood - Rare)
(
    'Donor',
    'emily.davis@bloodlink.com',
    '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86AGR0wqaS8',
    'Emily Davis',
    '+1-555-0104',
    'FEMALE',
    26,
    'Houston',
    'TX',
    29.7604,
    -95.3698,
    TRUE,
    TRUE,
    'AB-',
    TRUE,
    1,
    '2024-08-05 09:20:00',
    '2026-05-24 16:50:00'
),

-- Donor 5: Robert Williams (O- Blood - Universal Donor)
(
    'Donor',
    'robert.williams@bloodlink.com',
    '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86AGR0wqaS8',
    'Robert Williams',
    '+1-555-0105',
    'MALE',
    55,
    'Phoenix',
    'AZ',
    33.4484,
    -112.0742,
    TRUE,
    TRUE,
    'O-',
    TRUE,
    18,
    '2023-12-01 11:40:00',
    '2026-05-25 08:15:00'
),

-- Donor 6: Jennifer Brown (A- Blood)
(
    'Donor',
    'jennifer.brown@bloodlink.com',
    '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86AGR0wqaS8',
    'Jennifer Brown',
    '+1-555-0106',
    'FEMALE',
    35,
    'Philadelphia',
    'PA',
    39.9526,
    -75.1652,
    TRUE,
    TRUE,
    'A-',
    TRUE,
    7,
    '2024-04-18 13:25:00',
    '2026-05-22 15:10:00'
);

-- ============================================================
-- SAMPLE DATA: USERS (Patients)
-- ============================================================

INSERT INTO users (
    dtype, email, password_hash, full_name, phone_number, 
    gender, age, city, state, latitude, longitude, 
    is_active, is_verified, medical_condition_history,
    emergency_contact_name, emergency_contact_number, created_at, last_login_at
) VALUES
-- Patient 1: Alice Thompson
(
    'Patient',
    'alice.thompson@bloodlink.com',
    '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86AGR0wqaS8',
    'Alice Thompson',
    '+1-555-0201',
    'FEMALE',
    42,
    'New York',
    'NY',
    40.7150,
    -74.0050,
    TRUE,
    TRUE,
    'Type 2 Diabetes, Hypertension',
    'James Thompson',
    '+1-555-0210',
    '2024-09-10 10:30:00',
    '2026-05-24 12:45:00'
),

-- Patient 2: David Martinez
(
    'Patient',
    'david.martinez@bloodlink.com',
    '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86AGR0wqaS8',
    'David Martinez',
    '+1-555-0202',
    'MALE',
    58,
    'Los Angeles',
    'CA',
    34.0510,
    -118.2440,
    TRUE,
    TRUE,
    'Heart Disease, High Cholesterol',
    'Maria Martinez',
    '+1-555-0211',
    '2024-10-05 14:15:00',
    '2026-05-23 10:20:00'
),

-- Patient 3: Rachel Green
(
    'Patient',
    'rachel.green@bloodlink.com',
    '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86AGR0wqaS8',
    'Rachel Green',
    '+1-555-0203',
    'FEMALE',
    31,
    'Chicago',
    'IL',
    41.8800,
    -87.6280,
    TRUE,
    TRUE,
    'Anemia, Iron Deficiency',
    'Steven Green',
    '+1-555-0212',
    '2024-11-20 09:45:00',
    '2026-05-25 07:30:00'
);

-- ============================================================
-- SAMPLE DATA: MEDICAL RECORDS
-- ============================================================

INSERT INTO medical_records (
    user_id, systolic_bp, diastolic_bp, heart_rate, temperature, 
    weight, height, hemoglobin, hematocrit, platelet_count,
    blood_type_verified, hiv_test_result, hiv_test_date,
    hepatitis_b_result, hepatitis_b_date,
    allergies, chronic_conditions, current_medications,
    created_at, recorded_by_id
) VALUES
-- Medical record for John Doe (Donor)
(
    1, 120, 80, 72, 37.0,
    75.5, 180.0, 14.5, 42.0, 250000,
    TRUE, 'NEGATIVE', '2026-05-01',
    'NEGATIVE', '2026-05-01',
    'Penicillin', 'None', 'Multivitamin',
    '2026-05-15 08:30:00', NULL
),

-- Medical record for Alice Thompson (Patient)
(
    7, 145, 92, 78, 37.2,
    72.0, 165.0, 12.0, 38.0, 220000,
    TRUE, 'NEGATIVE', '2025-11-15',
    'NEGATIVE', '2025-11-15',
    'Aspirin', 'Type 2 Diabetes, Hypertension', 'Metformin, Lisinopril',
    '2026-05-10 14:20:00', NULL
),

-- Medical record for Rachel Green (Patient)
(
    9, 118, 78, 75, 36.9,
    62.0, 168.0, 10.5, 32.0, 210000,
    TRUE, 'NEGATIVE', '2026-04-20',
    'NEGATIVE', '2026-04-20',
    'None', 'Anemia, Iron Deficiency', 'Iron Supplement',
    '2026-05-12 11:15:00', NULL
);

-- ============================================================
-- SAMPLE DATA: BLOOD REQUESTS
-- ============================================================

INSERT INTO blood_requests (
    patient_id, donor_id, blood_group, units_needed, units_received,
    emergency_level, status, requested_at, expires_at,
    reason_for_blood, hospital_name, hospital_address,
    contact_person_name, contact_person_phone
) VALUES
-- Blood Request 1: Pending (CRITICAL)
(
    7, NULL, 'O+', 2, 0,
    'CRITICAL', 'PENDING', '2026-05-25 06:00:00', '2026-05-26 06:00:00',
    'Emergency surgery due to trauma',
    'New York Presbyterian Hospital',
    '622 West 168th Street, New York, NY 10032',
    'Dr. Sarah Chen',
    '+1-212-305-2500'
),

-- Blood Request 2: Accepted
(
    8, 1, 'A+', 1, 1,
    'URGENT', 'ACCEPTED', '2026-05-24 10:30:00', '2026-05-25 10:30:00',
    'Surgical preparation',
    'Cedars-Sinai Medical Center',
    '8700 Beverly Boulevard, Los Angeles, CA 90048',
    'Dr. Michael Torres',
    '+1-310-423-3000'
),

-- Blood Request 3: Completed
(
    9, 2, 'O-', 1, 1,
    'ROUTINE', 'COMPLETED', '2026-05-20 14:15:00', '2026-05-21 14:15:00',
    'Anemia treatment transfusion',
    'Northwestern Memorial Hospital',
    '251 East Huron Street, Chicago, IL 60611',
    'Dr. James Park',
    '+1-312-926-2000'
),

-- Blood Request 4: Pending (ROUTINE)
(
    7, NULL, 'AB+', 3, 0,
    'ROUTINE', 'PENDING', '2026-05-24 16:45:00', '2026-05-25 16:45:00',
    'Scheduled surgical procedure',
    'New York Presbyterian Hospital',
    '622 West 168th Street, New York, NY 10032',
    'Dr. Sarah Chen',
    '+1-212-305-2500'
);

-- ============================================================
-- SAMPLE DATA: DONATION HISTORY
-- ============================================================

INSERT INTO donation_history (
    donor_id, blood_request_id, donation_date, units_donated,
    blood_type_donated, weight_at_donation, hemoglobin_at_donation,
    blood_pressure_at_donation, notes, next_eligible_date,
    recorded_at, recorded_by_id
) VALUES
-- Donation 1: John Doe
(
    1, 2, '2026-05-24', 1, 'O+', 75.0, 14.5,
    '120/80', 'Smooth donation process', '2026-07-19',
    '2026-05-24 11:00:00', NULL
),

-- Donation 2: Sarah Smith
(
    2, 3, '2026-05-20', 1, 'A+', 68.0, 13.8,
    '118/75', 'No complications', '2026-07-15',
    '2026-05-20 14:30:00', NULL
),

-- Donation 3: Robert Williams
(
    5, NULL, '2026-03-15', 1, 'O-', 82.0, 15.2,
    '125/82', 'Donor is experienced', '2026-05-10',
    '2026-03-15 10:15:00', NULL
),

-- Donation 4: Jennifer Brown
(
    6, NULL, '2026-04-22', 1, 'A-', 70.0, 13.5,
    '119/76', 'Regular donor', '2026-06-17',
    '2026-04-22 13:45:00', NULL
);

-- ============================================================
-- SAMPLE DATA: DONOR REVIEWS
-- ============================================================

INSERT INTO donor_reviews (
    donor_id, reviewer_id, rating, comment,
    responsiveness_rating, reliability_rating, professionalism_rating,
    review_type, is_anonymous, created_at
) VALUES
-- Review 1: John Doe from patient Alice Thompson
(
    1, 7, 5, 'John was very professional and responsive. Highly recommended!',
    5, 5, 5, 'DONATION', FALSE, '2026-05-24 12:30:00'
),

-- Review 2: Sarah Smith from patient David Martinez
(
    2, 8, 4, 'Great donor, arrived on time. Very helpful.',
    4, 4, 5, 'INTERACTION', FALSE, '2026-05-20 15:45:00'
),

-- Review 3: Robert Williams from patient Rachel Green
(
    5, 9, 5, 'Robert is an excellent donor. Reliable and always available.',
    5, 5, 5, 'DONATION', FALSE, '2026-05-21 10:20:00'
);

-- ============================================================
-- SAMPLE DATA: CHATS
-- ============================================================

INSERT INTO chats (
    user_one_id, user_two_id, status, total_messages,
    unread_messages, last_message_at, created_at
) VALUES
-- Chat 1: John Doe (Donor) and Alice Thompson (Patient)
(
    1, 7, 'ACTIVE', 5, 1, '2026-05-25 09:30:00', '2026-05-24 10:00:00'
),

-- Chat 2: Sarah Smith (Donor) and David Martinez (Patient)
(
    2, 8, 'ACTIVE', 3, 0, '2026-05-23 14:15:00', '2026-05-22 11:00:00'
),

-- Chat 3: Robert Williams (Donor) and Rachel Green (Patient)
(
    5, 9, 'ACTIVE', 8, 2, '2026-05-25 07:45:00', '2026-05-20 13:30:00'
);

-- ============================================================
-- SAMPLE DATA: MESSAGES
-- ============================================================

INSERT INTO messages (
    chat_id, sender_id, content, message_type,
    is_seen, created_at, seen_at
) VALUES
-- Messages in Chat 1
(
    1, 1, 'Hi Alice, I received your blood request. I am available to donate.',
    'TEXT', TRUE, '2026-05-24 10:05:00', '2026-05-24 10:10:00'
),
(
    1, 7, 'Thank you so much John! We really need the blood by tomorrow.',
    'TEXT', TRUE, '2026-05-24 10:15:00', '2026-05-24 10:20:00'
),
(
    1, 1, 'No problem, I can come to the hospital at 11 AM tomorrow.',
    'TEXT', TRUE, '2026-05-24 10:25:00', '2026-05-24 10:30:00'
),
(
    1, 7, 'Perfect! That works great. See you tomorrow!',
    'TEXT', TRUE, '2026-05-24 10:35:00', '2026-05-24 10:40:00'
),
(
    1, 7, 'Thank you again for helping us!',
    'TEXT', FALSE, '2026-05-25 09:30:00', NULL
),

-- Messages in Chat 2
(
    2, 2, 'Hi David, I can help with the blood transfusion.',
    'TEXT', TRUE, '2026-05-22 11:10:00', '2026-05-22 11:15:00'
),
(
    2, 8, 'That is great news Sarah. When can you come?',
    'TEXT', TRUE, '2026-05-22 11:20:00', '2026-05-22 11:25:00'
),
(
    2, 2, 'I am available on Monday morning.',
    'TEXT', TRUE, '2026-05-23 14:15:00', '2026-05-23 14:20:00'
),

-- Messages in Chat 3
(
    3, 5, 'Hi Rachel, I saw your blood request. Happy to help.',
    'TEXT', TRUE, '2026-05-20 13:35:00', '2026-05-20 13:40:00'
),
(
    3, 9, 'Robert, thank you so much! Your help means a lot to us.',
    'TEXT', TRUE, '2026-05-20 13:45:00', '2026-05-20 13:50:00'
),
(
    3, 5, 'When do you need the blood?',
    'TEXT', TRUE, '2026-05-20 13:55:00', '2026-05-20 14:00:00'
),
(
    3, 9, 'As soon as possible. Tomorrow if possible.',
    'TEXT', TRUE, '2026-05-20 14:05:00', '2026-05-20 14:10:00'
),
(
    3, 5, 'I will be at the hospital at 10 AM tomorrow.',
    'TEXT', TRUE, '2026-05-20 14:15:00', '2026-05-20 14:20:00'
),
(
    3, 9, 'Great! Thank you Robert. You are a lifesaver!',
    'TEXT', TRUE, '2026-05-20 14:25:00', '2026-05-20 14:30:00'
),
(
    3, 5, 'Happy to help. See you tomorrow!',
    'TEXT', TRUE, '2026-05-21 08:00:00', '2026-05-21 08:05:00'
),
(
    3, 9, 'See you tomorrow! Thank you again!',
    'TEXT', FALSE, '2026-05-25 07:45:00', NULL
);

-- ============================================================
-- SAMPLE DATA: NOTIFICATIONS
-- ============================================================

INSERT INTO notifications (
    user_id, title, message, notification_type,
    is_read, related_entity_id, related_entity_type, created_at, read_at
) VALUES
-- Notifications for John Doe
(
    1, 'New Blood Request', 'Alice Thompson needs O+ blood urgently!',
    'BLOOD_REQUEST_RECEIVED', TRUE, 1, 'BloodRequest',
    '2026-05-25 06:05:00', '2026-05-25 06:10:00'
),
(
    1, 'Chat Message', 'Alice Thompson: Thank you for helping us!',
    'NEW_MESSAGE', TRUE, 1, 'Chat',
    '2026-05-25 09:35:00', '2026-05-25 09:40:00'
),

-- Notifications for Alice Thompson
(
    7, 'Donor Available', 'John Doe is available to donate!',
    'DONOR_AVAILABLE', TRUE, 1, 'BloodRequest',
    '2026-05-24 10:02:00', '2026-05-24 10:05:00'
),
(
    7, 'Request Accepted', 'Your blood request has been accepted by John Doe!',
    'REQUEST_ACCEPTED', TRUE, 1, 'BloodRequest',
    '2026-05-24 10:05:00', '2026-05-24 10:10:00'
),

-- Notifications for Rachel Green
(
    9, 'New Blood Request Match', 'Robert Williams can help with your blood request!',
    'DONOR_AVAILABLE', TRUE, 4, 'BloodRequest',
    '2026-05-20 13:30:00', '2026-05-20 13:35:00'
),
(
    9, 'User Online', 'Robert Williams is now online',
    'USER_ONLINE', FALSE, 5, 'User',
    '2026-05-25 07:40:00', NULL
);

-- ============================================================
-- SAMPLE DATA: REFRESH TOKENS
-- ============================================================

INSERT INTO refresh_tokens (
    user_id, token, expires_at, created_at, revoked_at
) VALUES
-- Refresh token for John Doe
(
    1, 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huLmRvbm9yQGJsb29kbGluay5jb20iLCJ1c2VySWQiOjEsInJvbGUiOiJET05PUiIsImlhdCI6MTcxNjU2MDAwMCwiZXhwIjoxNzE3MTY0ODAwfQ.sample_signature',
    '2026-06-01 08:00:00', '2026-05-25 08:00:00', NULL
),

-- Refresh token for Alice Thompson
(
    7, 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhbGljZS50aG9tcHNvbkBibG9vZGxpbmsuY29tIiwidXNlcklkIjo3LCJyb2xlIjoiUEFUSUVOVCIsImlhdCI6MTcxNjU2MDAwMCwiZXhwIjoxNzE3MTY0ODAwfQ.sample_signature',
    '2026-06-01 12:00:00', '2026-05-25 12:00:00', NULL
);

-- ============================================================
-- DATA VERIFICATION QUERIES
-- ============================================================

-- Verify donor count
SELECT COUNT(*) as total_donors FROM users WHERE dtype = 'Donor';

-- Verify patient count
SELECT COUNT(*) as total_patients FROM users WHERE dtype = 'Patient';

-- Verify blood request count by status
SELECT status, COUNT(*) as count FROM blood_requests GROUP BY status;

-- Verify message count
SELECT COUNT(*) as total_messages FROM messages;

-- Verify unread messages
SELECT COUNT(*) as unread_messages FROM messages WHERE is_seen = FALSE;

-- Verify notification count
SELECT COUNT(*) as total_notifications FROM notifications;

-- ============================================================
-- END OF SAMPLE DATA
-- ============================================================
