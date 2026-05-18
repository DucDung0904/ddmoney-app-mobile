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
    google_id   VARCHAR(100),
    avatar_url  VARCHAR(255),
    provider    VARCHAR(20)  NOT NULL DEFAULT 'LOCAL',
    enabled     TINYINT(1)   NOT NULL DEFAULT 1,
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email    (email),
    UNIQUE KEY uk_google_id (google_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
   

-- 2. Bảng WALLETS (Quản lý ví)
-- Đã bổ sung user_id để phân biệt ví của ai
CREATE TABLE IF NOT EXISTS wallets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    name VARCHAR(100) NOT NULL,
    balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    type VARCHAR(20) NOT NULL, -- CASH, BANK, EWALLET, CREDIT_CARD, SAVINGS, INVESTMENT
    bank_name VARCHAR(100),
    card_number VARCHAR(20),
    color_hex VARCHAR(10) DEFAULT '#4659A6',
    is_active BOOLEAN DEFAULT TRUE,
    is_included_in_total BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Wallet migration: new columns for production wallet system
SET @ddl = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'wallets' AND COLUMN_NAME = 'icon') = 0, 'ALTER TABLE wallets ADD COLUMN icon VARCHAR(50) DEFAULT ''wallet''', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'wallets' AND COLUMN_NAME = 'currency') = 0, 'ALTER TABLE wallets ADD COLUMN currency VARCHAR(10) DEFAULT ''VND''', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'wallets' AND COLUMN_NAME = 'is_default') = 0, 'ALTER TABLE wallets ADD COLUMN is_default BOOLEAN DEFAULT FALSE', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'wallets' AND COLUMN_NAME = 'is_archived') = 0, 'ALTER TABLE wallets ADD COLUMN is_archived BOOLEAN DEFAULT FALSE', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'wallets' AND COLUMN_NAME = 'is_included_in_total') = 0, 'ALTER TABLE wallets ADD COLUMN is_included_in_total BOOLEAN DEFAULT TRUE', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'wallets' AND COLUMN_NAME = 'sort_order') = 0, 'ALTER TABLE wallets ADD COLUMN sort_order INT DEFAULT 0', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'wallets' AND COLUMN_NAME = 'credit_limit') = 0, 'ALTER TABLE wallets ADD COLUMN credit_limit DECIMAL(18,2) NULL', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'wallets' AND COLUMN_NAME = 'current_debt') = 0, 'ALTER TABLE wallets ADD COLUMN current_debt DECIMAL(18,2) NULL', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'wallets' AND COLUMN_NAME = 'billing_day') = 0, 'ALTER TABLE wallets ADD COLUMN billing_day INT NULL', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'wallets' AND COLUMN_NAME = 'payment_due_day') = 0, 'ALTER TABLE wallets ADD COLUMN payment_due_day INT NULL', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE wallets SET type = 'CREDIT_CARD' WHERE type = 'CREDIT';

-- 3. Bảng CATEGORIES (Quản lý danh mục)
-- Đã bổ sung user_id (Nếu user_id = NULL thì đó là danh mục mặc định của hệ thống)
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL, -- NULL = danh mục mặc định global, khác NULL = danh mục riêng của user
    parent_id BIGINT NULL,
    name VARCHAR(100) NOT NULL,
    icon VARCHAR(64) DEFAULT 'category', -- Material icon key, ví dụ: home_repair_service
    color_hex VARCHAR(10) DEFAULT '#4659A6',
    type VARCHAR(20) NOT NULL, -- INCOME, EXPENSE, DEBT, BOTH
    is_default BOOLEAN DEFAULT FALSE,
    is_editable BOOLEAN DEFAULT TRUE,
    is_deletable BOOLEAN DEFAULT TRUE,
    is_deleted BOOLEAN DEFAULT FALSE,
    sort_order INT NOT NULL DEFAULT 10000,
    seed_version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_categories_user (user_id),
    INDEX idx_categories_parent (parent_id),
    INDEX idx_categories_type (type),
    INDEX idx_categories_visible (is_deleted, user_id, type)
);

-- Migration an toàn cho database đã tạo bằng phiên bản SQL cũ.
-- CREATE TABLE IF NOT EXISTS không tự thêm cột mới, nên cần chạy các ALTER dưới đây
-- trước khi seed danh mục mặc định 1001..1036.
ALTER TABLE categories MODIFY COLUMN icon VARCHAR(64) DEFAULT 'category';

SET @ddl = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'categories' AND COLUMN_NAME = 'parent_id') = 0,
    'ALTER TABLE categories ADD COLUMN parent_id BIGINT NULL AFTER user_id',
    'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'categories' AND COLUMN_NAME = 'is_editable') = 0,
    'ALTER TABLE categories ADD COLUMN is_editable BOOLEAN DEFAULT TRUE AFTER is_default',
    'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'categories' AND COLUMN_NAME = 'is_deletable') = 0,
    'ALTER TABLE categories ADD COLUMN is_deletable BOOLEAN DEFAULT TRUE AFTER is_editable',
    'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'categories' AND COLUMN_NAME = 'is_deleted') = 0,
    'ALTER TABLE categories ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE AFTER is_deletable',
    'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'categories' AND COLUMN_NAME = 'sort_order') = 0,
    'ALTER TABLE categories ADD COLUMN sort_order INT NOT NULL DEFAULT 10000 AFTER is_deleted',
    'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'categories' AND COLUMN_NAME = 'seed_version') = 0,
    'ALTER TABLE categories ADD COLUMN seed_version INT NOT NULL DEFAULT 1 AFTER sort_order',
    'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'categories' AND COLUMN_NAME = 'updated_at') = 0,
    'ALTER TABLE categories ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at',
    'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'categories' AND INDEX_NAME = 'idx_categories_user') = 0,
    'CREATE INDEX idx_categories_user ON categories(user_id)',
    'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'categories' AND INDEX_NAME = 'idx_categories_parent') = 0,
    'CREATE INDEX idx_categories_parent ON categories(parent_id)',
    'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'categories' AND INDEX_NAME = 'idx_categories_type') = 0,
    'CREATE INDEX idx_categories_type ON categories(type)',
    'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'categories' AND INDEX_NAME = 'idx_categories_visible') = 0,
    'CREATE INDEX idx_categories_visible ON categories(is_deleted, user_id, type)',
    'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'categories'
       AND COLUMN_NAME = 'parent_id'
       AND REFERENCED_TABLE_NAME = 'categories') = 0,
    'ALTER TABLE categories ADD CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL',
    'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE categories
SET is_editable = CASE WHEN is_default = TRUE THEN FALSE ELSE TRUE END,
    is_deletable = CASE WHEN is_default = TRUE THEN FALSE ELSE TRUE END
WHERE seed_version = 1;

-- Default categories global cho toàn hệ thống.
-- user_id = NULL, is_default = TRUE, không cho edit/delete.
-- ID cố định 1001..1036 để app local và MySQL backend dùng cùng category_id.
INSERT INTO categories (
    id, user_id, parent_id, name, icon, color_hex, type,
    is_default, is_editable, is_deletable, is_deleted, sort_order, seed_version
) VALUES
    (1001, NULL, NULL, 'Ăn uống', 'restaurant', '#E24B4A', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 10, 2),
    (1002, NULL, 1001, 'Ăn sáng', 'breakfast', '#EB7070', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 11, 2),
    (1003, NULL, 1001, 'Cafe', 'coffee', '#8A5606', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 12, 2),
    (1004, NULL, 1001, 'Nhà hàng', 'local_dining', '#E24B4A', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 13, 2),

    (1005, NULL, NULL, 'Di chuyển', 'directions_car', '#185FA5', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 20, 2),
    (1006, NULL, 1005, 'Xăng xe', 'local_gas_station', '#378ADD', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 21, 2),
    (1007, NULL, 1005, 'Taxi / Grab', 'local_taxi', '#185FA5', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 22, 2),
    (1008, NULL, 1005, 'Bảo dưỡng xe', 'car_repair', '#0C447C', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 23, 2),

    (1009, NULL, NULL, 'Mua sắm', 'shopping_bag', '#EF9F27', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 30, 2),
    (1010, NULL, 1009, 'Đồ dùng cá nhân', 'shopping_bag', '#F3BC56', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 31, 2),
    (1011, NULL, 1009, 'Đồ gia dụng', 'chair', '#EF9F27', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 32, 2),
    (1012, NULL, 1009, 'Quần áo', 'checkroom', '#8A5606', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 33, 2),

    (1013, NULL, NULL, 'Hóa đơn', 'receipt_long', '#1D9E75', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 40, 2),
    (1014, NULL, 1013, 'Điện', 'electric_bolt', '#EF9F27', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 41, 2),
    (1015, NULL, 1013, 'Nước', 'water_drop', '#378ADD', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 42, 2),
    (1016, NULL, 1013, 'Internet', 'wifi', '#185FA5', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 43, 2),
    (1017, NULL, 1013, 'Điện thoại', 'phone_android', '#1D9E75', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 44, 2),

    (1018, NULL, NULL, 'Nhà cửa', 'house', '#639922', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 50, 2),
    (1019, NULL, 1018, 'Tiền thuê nhà', 'home_work', '#639922', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 51, 2),
    (1020, NULL, 1018, 'Sửa chữa nhà', 'home_repair_service', '#3D5E14', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 52, 2),

    (1021, NULL, NULL, 'Giải trí', 'sports_esports', '#F3BC56', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 60, 2),
    (1022, NULL, 1021, 'Xem phim', 'movie', '#EF9F27', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 61, 2),
    (1023, NULL, 1021, 'Du lịch', 'flight_takeoff', '#378ADD', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 62, 2),
    (1024, NULL, 1021, 'Game / App', 'sports_esports', '#8A5606', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 63, 2),

    (1025, NULL, NULL, 'Sức khỏe', 'local_hospital', '#639922', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 70, 2),
    (1026, NULL, 1025, 'Thuốc', 'medication', '#82BB44', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 71, 2),
    (1027, NULL, 1025, 'Khám bệnh', 'local_hospital', '#3D5E14', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 72, 2),

    (1028, NULL, NULL, 'Giáo dục', 'school', '#1D9E75', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 80, 2),
    (1029, NULL, 1028, 'Học phí', 'school', '#1D9E75', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 81, 2),
    (1030, NULL, 1028, 'Sách / Tài liệu', 'menu_book', '#0F5C43', 'EXPENSE', TRUE, FALSE, FALSE, FALSE, 82, 2),

    (1031, NULL, NULL, 'Thu nhập', 'payments', '#639922', 'INCOME', TRUE, FALSE, FALSE, FALSE, 90, 2),
    (1032, NULL, 1031, 'Lương', 'payments', '#639922', 'INCOME', TRUE, FALSE, FALSE, FALSE, 91, 2),
    (1033, NULL, 1031, 'Thưởng', 'celebration', '#82BB44', 'INCOME', TRUE, FALSE, FALSE, FALSE, 92, 2),
    (1034, NULL, 1031, 'Freelance', 'work', '#378ADD', 'INCOME', TRUE, FALSE, FALSE, FALSE, 93, 2),
    (1035, NULL, 1031, 'Quà tặng', 'card_giftcard', '#F3BC56', 'INCOME', TRUE, FALSE, FALSE, FALSE, 94, 2),
    (1036, NULL, 1031, 'Đầu tư', 'trending_up', '#EF9F27', 'INCOME', TRUE, FALSE, FALSE, FALSE, 95, 2)
ON DUPLICATE KEY UPDATE
    user_id = VALUES(user_id),
    parent_id = VALUES(parent_id),
    name = VALUES(name),
    icon = VALUES(icon),
    color_hex = VALUES(color_hex),
    type = VALUES(type),
    is_default = VALUES(is_default),
    is_editable = VALUES(is_editable),
    is_deletable = VALUES(is_deletable),
    is_deleted = VALUES(is_deleted),
    sort_order = VALUES(sort_order),
    seed_version = VALUES(seed_version);

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

-- 5. Bảng BUDGETS (Ngân sách chính)
-- Logic nghiệp vụ:
-- - Ngân sách KHÔNG trừ tiền trong ví, chỉ dùng để so sánh với transactions type = EXPENSE.
-- - category_id = NULL + scope = 'ALL_CATEGORIES' nghĩa là ngân sách áp dụng cho tất cả danh mục chi tiêu.
-- - wallet_id = NULL + wallet_scope = 'ALL_WALLETS' nghĩa là ngân sách áp dụng cho tất cả ví active của user.
-- - CUSTOM period không nên repeat. Repeat chỉ hợp lệ với WEEK, MONTH, QUARTER, YEAR.
CREATE TABLE IF NOT EXISTS budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    category_id BIGINT NULL,
    wallet_id BIGINT NULL,
    scope VARCHAR(30) NOT NULL DEFAULT 'ALL_CATEGORIES', -- CATEGORY, ALL_CATEGORIES
    wallet_scope VARCHAR(30) NOT NULL DEFAULT 'ALL_WALLETS', -- ONE_WALLET, ALL_WALLETS
    period_type VARCHAR(20) NOT NULL DEFAULT 'MONTH', -- WEEK, MONTH, QUARTER, YEAR, CUSTOM
    repeat_type VARCHAR(20) NOT NULL DEFAULT 'NONE', -- NONE, WEEKLY, MONTHLY, QUARTERLY, YEARLY
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Generated keys giúp unique constraint xử lý NULL như một giá trị thật.
    category_key BIGINT GENERATED ALWAYS AS (COALESCE(category_id, -1)) STORED,
    wallet_key BIGINT GENERATED ALWAYS AS (COALESCE(wallet_id, -1)) STORED,

    CONSTRAINT fk_budgets_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_budgets_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    CONSTRAINT fk_budgets_wallet FOREIGN KEY (wallet_id) REFERENCES wallets(id) ON DELETE RESTRICT,
    CONSTRAINT chk_budgets_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_budgets_date_range CHECK (start_date <= end_date),
    CONSTRAINT chk_budgets_scope CHECK (scope IN ('CATEGORY', 'ALL_CATEGORIES')),
    CONSTRAINT chk_budgets_wallet_scope CHECK (wallet_scope IN ('ONE_WALLET', 'ALL_WALLETS')),
    CONSTRAINT chk_budgets_period_type CHECK (period_type IN ('WEEK', 'MONTH', 'QUARTER', 'YEAR', 'CUSTOM')),
    CONSTRAINT chk_budgets_repeat_type CHECK (repeat_type IN ('NONE', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY')),
    CONSTRAINT chk_budgets_scope_category_consistency CHECK (
        (scope = 'CATEGORY' AND category_id IS NOT NULL)
        OR (scope = 'ALL_CATEGORIES' AND category_id IS NULL)
    ),
    CONSTRAINT chk_budgets_scope_wallet_consistency CHECK (
        (wallet_scope = 'ONE_WALLET' AND wallet_id IS NOT NULL)
        OR (wallet_scope = 'ALL_WALLETS' AND wallet_id IS NULL)
    ),
    CONSTRAINT chk_budgets_repeat_consistency CHECK (
        (repeat_type = 'NONE')
        OR (period_type IN ('WEEK', 'MONTH', 'QUARTER', 'YEAR') AND repeat_type <> 'NONE')
    ),

    UNIQUE KEY uk_budget_identity (
        user_id, category_key, wallet_key, start_date, end_date, active
    ),
    INDEX idx_budgets_user_active (user_id, active),
    INDEX idx_budgets_period (user_id, start_date, end_date),
    INDEX idx_budgets_category (category_id),
    INDEX idx_budgets_wallet (wallet_id)
);

-- Migration an toàn cho database đã tạo bằng schema ngân sách cũ (month/year + budget_categories).
-- CREATE TABLE IF NOT EXISTS không tự thêm cột mới, nên cần bổ sung các ALTER dưới đây.
SET @ddl = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'budgets' AND COLUMN_NAME = 'category_id') = 0, 'ALTER TABLE budgets ADD COLUMN category_id BIGINT NULL AFTER amount', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'budgets' AND COLUMN_NAME = 'wallet_id') = 0, 'ALTER TABLE budgets ADD COLUMN wallet_id BIGINT NULL AFTER category_id', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'budgets' AND COLUMN_NAME = 'scope') = 0, 'ALTER TABLE budgets ADD COLUMN scope VARCHAR(30) NOT NULL DEFAULT ''ALL_CATEGORIES'' AFTER wallet_id', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'budgets' AND COLUMN_NAME = 'wallet_scope') = 0, 'ALTER TABLE budgets ADD COLUMN wallet_scope VARCHAR(30) NOT NULL DEFAULT ''ALL_WALLETS'' AFTER scope', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'budgets' AND COLUMN_NAME = 'period_type') = 0, 'ALTER TABLE budgets ADD COLUMN period_type VARCHAR(20) NOT NULL DEFAULT ''MONTH'' AFTER wallet_scope', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'budgets' AND COLUMN_NAME = 'repeat_type') = 0, 'ALTER TABLE budgets ADD COLUMN repeat_type VARCHAR(20) NOT NULL DEFAULT ''NONE'' AFTER period_type', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'budgets' AND COLUMN_NAME = 'start_date') = 0, 'ALTER TABLE budgets ADD COLUMN start_date DATE NULL AFTER repeat_type', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'budgets' AND COLUMN_NAME = 'end_date') = 0, 'ALTER TABLE budgets ADD COLUMN end_date DATE NULL AFTER start_date', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'budgets' AND COLUMN_NAME = 'active') = 0, 'ALTER TABLE budgets ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE AFTER end_date', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Backfill start_date/end_date từ month/year nếu database cũ vẫn còn 2 cột này.
SET @ddl = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'budgets' AND COLUMN_NAME = 'month') > 0
    AND
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'budgets' AND COLUMN_NAME = 'year') > 0,
    'UPDATE budgets SET start_date = COALESCE(start_date, STR_TO_DATE(CONCAT(year, ''-'', LPAD(month, 2, ''0''), ''-01''), ''%Y-%m-%d'')), end_date = COALESCE(end_date, LAST_DAY(STR_TO_DATE(CONCAT(year, ''-'', LPAD(month, 2, ''0''), ''-01''), ''%Y-%m-%d''))) WHERE start_date IS NULL OR end_date IS NULL',
    'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Schema mới không dùng month/year nữa. Cho phép NULL để backend mới chỉ cần start_date/end_date.
SET @ddl = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'budgets' AND COLUMN_NAME = 'month') > 0,
    'ALTER TABLE budgets MODIFY COLUMN month INT NULL',
    'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'budgets' AND COLUMN_NAME = 'year') > 0,
    'ALTER TABLE budgets MODIFY COLUMN year INT NULL',
    'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Backfill category_id từ budget_categories cũ: lấy category đầu tiên nếu ngân sách cũ có danh mục.
SET @ddl = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'budget_categories') > 0,
    'UPDATE budgets b SET category_id = COALESCE(category_id, (SELECT MIN(bc.category_id) FROM budget_categories bc WHERE bc.budget_id = b.id)), scope = CASE WHEN COALESCE(category_id, (SELECT MIN(bc2.category_id) FROM budget_categories bc2 WHERE bc2.budget_id = b.id)) IS NULL THEN ''ALL_CATEGORIES'' ELSE ''CATEGORY'' END',
    'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE budgets
SET start_date = COALESCE(start_date, CURRENT_DATE),
    end_date = COALESCE(end_date, CURRENT_DATE),
    wallet_scope = CASE WHEN wallet_id IS NULL THEN 'ALL_WALLETS' ELSE 'ONE_WALLET' END,
    scope = CASE WHEN category_id IS NULL THEN 'ALL_CATEGORIES' ELSE 'CATEGORY' END,
    repeat_type = COALESCE(repeat_type, 'NONE'),
    period_type = COALESCE(period_type, 'MONTH');

ALTER TABLE budgets MODIFY COLUMN start_date DATE NOT NULL;
ALTER TABLE budgets MODIFY COLUMN end_date DATE NOT NULL;

SET @ddl = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'budgets' AND COLUMN_NAME = 'category_key') = 0, 'ALTER TABLE budgets ADD COLUMN category_key BIGINT GENERATED ALWAYS AS (COALESCE(category_id, -1)) STORED', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'budgets' AND COLUMN_NAME = 'wallet_key') = 0, 'ALTER TABLE budgets ADD COLUMN wallet_key BIGINT GENERATED ALWAYS AS (COALESCE(wallet_id, -1)) STORED', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'budgets' AND INDEX_NAME = 'uk_budget_identity') = 0, 'CREATE UNIQUE INDEX uk_budget_identity ON budgets(user_id, category_key, wallet_key, start_date, end_date, active)', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'budgets' AND INDEX_NAME = 'idx_budgets_user_active') = 0, 'CREATE INDEX idx_budgets_user_active ON budgets(user_id, active)', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'budgets' AND INDEX_NAME = 'idx_budgets_period') = 0, 'CREATE INDEX idx_budgets_period ON budgets(user_id, start_date, end_date)', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'transactions' AND INDEX_NAME = 'idx_transactions_budget_calc') = 0, 'CREATE INDEX idx_transactions_budget_calc ON transactions(user_id, type, date, category_id, wallet_id)', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'wallets' AND INDEX_NAME = 'idx_wallets_user_active') = 0, 'CREATE INDEX idx_wallets_user_active ON wallets(user_id, is_archived, is_active)', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 6. Bảng BUDGET_CATEGORIES (legacy)
-- Bảng này giữ lại để tương thích dữ liệu/app cũ từng cho một ngân sách chứa nhiều danh mục.
-- Backend mới nên dùng budgets.category_id + budgets.scope theo logic Money Lover:
--   scope = CATEGORY + category_id != NULL
--   scope = ALL_CATEGORIES + category_id = NULL
CREATE TABLE IF NOT EXISTS budget_categories (
    budget_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (budget_id, category_id),
    FOREIGN KEY (budget_id) REFERENCES budgets(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

/*
Ghi chú workflow logic ngân sách:
1. Tạo ngân sách: 
   - Insert vào bảng `budgets`.
   - Nếu user chọn "tất cả danh mục": scope = 'ALL_CATEGORIES', category_id = NULL.
   - Nếu user chọn một danh mục chi tiêu: scope = 'CATEGORY', category_id = selected category.
   - Nếu user chọn "tất cả ví": wallet_scope = 'ALL_WALLETS', wallet_id = NULL.
   - Nếu user chọn một ví: wallet_scope = 'ONE_WALLET', wallet_id = selected wallet.

2. Query danh mục đúng theo user:
   SELECT *
   FROM categories
   WHERE is_deleted = FALSE
     AND (user_id IS NULL OR user_id = :current_user_id)
   ORDER BY sort_order ASC, name ASC;

3. Transaction mapping:
   - transactions.user_id = current_user_id
   - transactions.category_id = selected_category.id
   - Không dùng category name dạng text để xác định danh mục.

4. Tính toán ngân sách (Spent Amount):
   SELECT
       b.*,
       COALESCE(SUM(t.amount), 0) AS spent_amount,
       (b.amount - COALESCE(SUM(t.amount), 0)) AS remaining,
       CASE WHEN b.amount > 0 THEN (COALESCE(SUM(t.amount), 0) / b.amount * 100) ELSE 0 END AS percent_used
   FROM budgets b
   LEFT JOIN transactions t ON t.user_id = b.user_id
       AND t.type = 'EXPENSE'
       AND t.date BETWEEN b.start_date AND b.end_date
       AND (b.scope = 'ALL_CATEGORIES' OR t.category_id = b.category_id)
       AND (b.wallet_scope = 'ALL_WALLETS' OR t.wallet_id = b.wallet_id)
   WHERE b.user_id = :current_user_id
     AND b.active = TRUE
   GROUP BY b.id;

5. Cập nhật giao dịch: Không cần trigger hay cập nhật trực tiếp vào budgets. 
   Mọi thứ được tính toán real-time từ bảng transactions.

6. Xoá category:
   - Default category: không cho edit/delete.
   - Custom category đã có transaction: không xoá cứng; dùng soft delete bằng is_deleted = TRUE.
   - Custom category chưa dùng: có thể xoá cứng nếu cần.

7. Xoá ngân sách: nên soft delete bằng active = FALSE để giữ lịch sử. Có thể hard delete nếu app không cần audit.
*/

