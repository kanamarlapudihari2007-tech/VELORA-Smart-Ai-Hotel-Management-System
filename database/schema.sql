-- ============================================================
-- AI Smart Hotel Management System - Database Schema
-- Database: hotel_db
-- Target RDBMS: MySQL 5.7+ / 8.0+
-- ============================================================

CREATE DATABASE IF NOT EXISTS hotel_db;
USE hotel_db;

-- Disable Foreign Key checks temporarily for clean setup
SET FOREIGN_KEY_CHECKS = 0;

-- Drop existing tables if re-initialization is required
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS tasks;
DROP TABLE IF EXISTS complaints;
DROP TABLE IF EXISTS service_requests;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS room_types;
DROP TABLE IF EXISTS staff;
DROP TABLE IF EXISTS guests;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 1. USERS TABLE
-- Core table storing authentication credentials & system role
-- ============================================================
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, -- Stores BCrypt / Hashed passwords
    email VARCHAR(100) NOT NULL UNIQUE,
    role ENUM('GUEST', 'STAFF', 'MANAGER') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 2. GUESTS TABLE
-- Profile information specific to Guests (1-to-1 with users)
-- ============================================================
CREATE TABLE guests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    id_proof VARCHAR(100),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_guest_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 3. STAFF TABLE
-- Profile & department assignment for Staff (1-to-1 with users)
-- ============================================================
CREATE TABLE staff (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    category ENUM('Housekeeping', 'Maintenance', 'Reception', 'Food Service', 'Security') NOT NULL,
    work_status ENUM('AVAILABLE', 'BUSY', 'OFF_DUTY') DEFAULT 'AVAILABLE',
    performance_credits INT NOT NULL DEFAULT 100,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_staff_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 4. ROOM TYPES TABLE
-- Room categories, nightly rates, and bed capacity
-- ============================================================
CREATE TABLE room_types (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type_name VARCHAR(50) NOT NULL UNIQUE,
    price_per_night DECIMAL(10, 2) NOT NULL,
    description TEXT,
    capacity INT NOT NULL DEFAULT 2
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 5. ROOMS TABLE
-- Individual hotel rooms and their current operational status
-- ============================================================
CREATE TABLE rooms (
    id INT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(10) NOT NULL UNIQUE,
    room_type_id INT NOT NULL,
    floor_number INT NOT NULL,
    status ENUM('AVAILABLE', 'RESERVED', 'OCCUPIED', 'CLEANING', 'MAINTENANCE') DEFAULT 'AVAILABLE',
    CONSTRAINT fk_room_type FOREIGN KEY (room_type_id) REFERENCES room_types(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 6. BOOKINGS TABLE
-- Reservation records linking guests to specific rooms
-- ============================================================
CREATE TABLE bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    booking_code VARCHAR(20) NOT NULL UNIQUE,
    guest_id INT NOT NULL,
    room_id INT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    status ENUM('RESERVED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED') DEFAULT 'RESERVED',
    total_amount DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_guest FOREIGN KEY (guest_id) REFERENCES guests(id) ON DELETE RESTRICT,
    CONSTRAINT fk_booking_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 7. PAYMENTS TABLE
-- Transaction records linked to bookings
-- ============================================================
CREATE TABLE payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(50) DEFAULT 'CARD',
    payment_status ENUM('PENDING', 'COMPLETED', 'FAILED') DEFAULT 'PENDING',
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 8. SERVICE REQUESTS TABLE
-- Guest requested services processed by AI understanding
-- ============================================================
CREATE TABLE service_requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    room_id INT NOT NULL,
    request_type VARCHAR(50) NOT NULL,
    raw_text TEXT NOT NULL,
    extracted_items TEXT,
    priority ENUM('LOW', 'NORMAL', 'HIGH', 'URGENT') DEFAULT 'NORMAL',
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sr_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    CONSTRAINT fk_sr_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 9. COMPLAINTS TABLE
-- Guest complaints classified automatically by AI
-- ============================================================
CREATE TABLE complaints (
    id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    room_id INT NOT NULL,
    raw_text TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    priority ENUM('LOW', 'NORMAL', 'HIGH', 'URGENT') DEFAULT 'NORMAL',
    short_description VARCHAR(255),
    status ENUM('PENDING', 'IN_PROGRESS', 'RESOLVED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_complaint_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    CONSTRAINT fk_complaint_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 10. TASKS TABLE
-- Tasks created dynamically for Staff based on Requests/Complaints
-- ============================================================
CREATE TABLE tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    task_type ENUM('SERVICE_REQUEST', 'COMPLAINT', 'ROOM_CLEANING') NOT NULL,
    reference_id INT NOT NULL,
    assigned_staff_id INT,
    priority ENUM('LOW', 'NORMAL', 'HIGH', 'URGENT') DEFAULT 'NORMAL',
    status ENUM('ASSIGNED', 'IN_PROGRESS', 'COMPLETED') DEFAULT 'ASSIGNED',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_task_staff FOREIGN KEY (assigned_staff_id) REFERENCES staff(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 11. NOTIFICATIONS TABLE
-- Real-time notification messages for Users
-- ============================================================
CREATE TABLE notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================================
-- SEED DATA (INITIAL TESTING DATA)
-- ============================================================

-- Insert Room Types
INSERT INTO room_types (type_name, price_per_night, description, capacity) VALUES
('Standard Single', 1200.00, 'Comfortable single bed room with high-speed WiFi and work desk.', 1),
('Deluxe Double', 2500.00, 'Spacious king-size bed room with city view, mini fridge, and smart TV.', 2),
('Executive Suite', 5000.00, 'Luxury suite with separate living room, balcony, jacuzzi, and complimentary breakfast.', 4);

-- Insert Sample Rooms
INSERT INTO rooms (room_number, room_type_id, floor_number, status) VALUES
('101', 1, 1, 'AVAILABLE'),
('102', 1, 1, 'AVAILABLE'),
('201', 2, 2, 'AVAILABLE'),
('204', 2, 2, 'OCCUPIED'),
('301', 3, 3, 'AVAILABLE');

-- Insert Initial Users (Passwords stored as plain for reference in Stage 2; Stage 4 will hash them)
-- 1. Manager Account
INSERT INTO users (username, password, email, role) VALUES 
('admin', 'admin123', 'admin@smarthotel.com', 'MANAGER');

-- 2. Staff Accounts (Covering All 5 Sectors)
INSERT INTO users (username, password, email, role) VALUES 
('john_housekeeping', 'staff123', 'john@smarthotel.com', 'STAFF'),
('mike_maintenance', 'staff123', 'mike@smarthotel.com', 'STAFF'),
('david_foodservice', 'staff123', 'david@smarthotel.com', 'STAFF'),
('sarah_reception', 'staff123', 'sarah@smarthotel.com', 'STAFF'),
('robert_security', 'staff123', 'robert@smarthotel.com', 'STAFF');

-- 3. Guest Account
INSERT INTO users (username, password, email, role) VALUES 
('alex_guest', 'guest123', 'alex@gmail.com', 'GUEST');

-- Insert Staff Details (All 5 Sectors)
INSERT INTO staff (user_id, full_name, phone, category, work_status, performance_credits) VALUES
((SELECT id FROM users WHERE username = 'john_housekeeping'), 'John Doe', '9876543210', 'Housekeeping', 'AVAILABLE', 100),
((SELECT id FROM users WHERE username = 'mike_maintenance'), 'Mike Smith', '9876543211', 'Maintenance', 'AVAILABLE', 100),
((SELECT id FROM users WHERE username = 'david_foodservice'), 'David Miller', '9876543212', 'Food Service', 'AVAILABLE', 100),
((SELECT id FROM users WHERE username = 'sarah_reception'), 'Sarah Jenkins', '9876543213', 'Reception', 'AVAILABLE', 100),
((SELECT id FROM users WHERE username = 'robert_security'), 'Robert Taylor', '9876543214', 'Security', 'AVAILABLE', 100);

-- Insert Guest Details
INSERT INTO guests (user_id, full_name, phone, id_proof, address) VALUES
((SELECT id FROM users WHERE username = 'alex_guest'), 'Alex Johnson', '9123456789', 'AADHAAR-1234-5678-9012', '123 Park Avenue, NY');
