-- =============================================================================
-- SIMATS Campus Event Registration System - Database Schema (schema.sql)
-- Target RDBMS: MySQL / PostgreSQL / SQLite / Oracle
-- Student: Thejas Balaji (Register Number: 192565092)
-- Course: Programming in Java (CO4 - GUI & Event-Driven Architecture)
-- =============================================================================

-- 1. Create Events Master Table
CREATE TABLE IF NOT EXISTS campus_events (
    event_id INT AUTO_INCREMENT PRIMARY KEY,
    event_code VARCHAR(20) NOT NULL UNIQUE,
    event_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    max_capacity INT DEFAULT 50,
    event_date DATE NOT NULL,
    venue VARCHAR(100) NOT NULL,
    coordinator_name VARCHAR(100)
);

-- 2. Create Student Registrations Table
CREATE TABLE IF NOT EXISTS event_registrations (
    registration_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20) NOT NULL,
    student_name VARCHAR(100) NOT NULL,
    department VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    event_name VARCHAR(100) NOT NULL,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    attendance_status VARCHAR(20) DEFAULT 'REGISTERED',
    
    -- Business Constraint: A student cannot register for the same event twice!
    CONSTRAINT uk_student_event UNIQUE (student_id, event_name)
);

-- 3. Composite Index for performant filter queries by Event and Department
CREATE INDEX idx_registrations_event ON event_registrations (event_name);
CREATE INDEX idx_registrations_dept ON event_registrations (department);

-- 4. Sample Seed Data for Campus Events Master
INSERT INTO campus_events (event_code, event_name, category, max_capacity, event_date, venue, coordinator_name)
VALUES
    ('EVT-HACK', 'Hackathon 2026', 'Technical', 50, '2026-12-10', 'Main Computing Lab 3', 'Dr. S. Ramanujan'),
    ('EVT-CODE', 'Code Marathon', 'Technical', 40, '2026-12-11', 'Turing Auditorium', 'Prof. Ada Lovelace'),
    ('EVT-ROBO', 'RoboWars Challenge', 'Robotics', 30, '2026-12-12', 'Mechanical Indoor Arena', 'Dr. Nikola Tesla'),
    ('EVT-WEBD', 'Web Dev Blitz', 'Design', 45, '2026-12-13', 'Design Studio B', 'Prof. Tim Berners-Lee'),
    ('EVT-PAPR', 'Paper Presentation', 'Academic', 60, '2026-12-14', 'Seminar Hall 1', 'Dr. C.V. Raman');

-- 5. Sample Initial Registrations
INSERT INTO event_registrations (student_id, student_name, department, email, event_name, attendance_status)
VALUES
    ('192565092', 'Thejas Balaji', 'Computer Science', '192565092@simats.edu.in', 'Hackathon 2026', 'CONFIRMED'),
    ('192565093', 'Karthik Raja', 'Information Tech', 'karthik@simats.edu.in', 'Code Marathon', 'REGISTERED'),
    ('192565094', 'Ananya Sharma', 'Electronics Eng', 'ananya@simats.edu.in', 'RoboWars Challenge', 'REGISTERED');

-- Verification Queries
-- SELECT * FROM event_registrations WHERE event_name = 'Hackathon 2026';
-- SELECT event_name, COUNT(*) AS total_registered FROM event_registrations GROUP BY event_name;
