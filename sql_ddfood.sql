-- ==============================================================================
-- DDMoney - Database Schema (MySQL)
-- ==============================================================================
-- Lưu ý: Chạy đoạn script này trong công cụ quản trị MySQL (như Laragon/HeidiSQL/phpMyAdmin)
-- để khởi tạo toàn bộ cấu trúc bảng cho dự án.
-- ==============================================================================

CREATE DATABASE IF NOT EXISTS ddmoney CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ddmoney;

-- 1. Bảng USERS (Quản lý người dùng)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    avatar_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. Bảng WALLETS (Quản lý ví)
-- Đã bổ sung user_id để phân biệt ví của ai
CREATE TABLE IF NOT EXISTS wallets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    name VARCHAR(100) NOT NULL,
    balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    type VARCHAR(20) NOT NULL, -- CASH, BANK, EWALLET, CREDIT
    bank_name VARCHAR(100),
    card_number VARCHAR(20),
    color_hex VARCHAR(10) DEFAULT '#4659A6',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. Bảng CATEGORIES (Quản lý danh mục)
-- Đã bổ sung user_id (Nếu user_id = NULL thì đó là danh mục mặc định của hệ thống)
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    name VARCHAR(100) NOT NULL,
    icon VARCHAR(10) DEFAULT '📦',
    color_hex VARCHAR(10) DEFAULT '#4659A6',
    type VARCHAR(20) NOT NULL, -- INCOME, EXPENSE, DEBT, BOTH
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. Bảng TRANSACTIONS (Quản lý thu chi/giao dịch)
-- Đã bổ sung user_id để phân biệt giao dịch của ai
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    wallet_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    transfer_to_wallet_id BIGINT NULL,
    title VARCHAR(200) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    type VARCHAR(20) NOT NULL, -- INCOME, EXPENSE, TRANSFER, DEBT
    date DATE NOT NULL,
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (wallet_id) REFERENCES wallets(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    FOREIGN KEY (transfer_to_wallet_id) REFERENCES wallets(id) ON DELETE CASCADE
);

-- 5. Bảng BUDGETS (Quản lý ngân sách - Nếu có)
CREATE TABLE IF NOT EXISTS budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    category_id BIGINT NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    month INT NOT NULL,
    year INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    UNIQUE KEY unique_budget (user_id, category_id, month, year)
);

