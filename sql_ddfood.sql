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
    icon VARCHAR(50) DEFAULT 'category', -- Material icon key, ví dụ: home_repair_service
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
ALTER TABLE categories MODIFY COLUMN icon VARCHAR(50) DEFAULT 'category';

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
CREATE TABLE IF NOT EXISTS budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL, -- Tên ngân sách (vd: Ăn uống & Sinh hoạt)
    amount DECIMAL(18,2) NOT NULL, -- Tổng số tiền giới hạn cho ngân sách này
    month INT NOT NULL, -- Tháng áp dụng (1-12)
    year INT NOT NULL, -- Năm áp dụng (vd: 2024)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 6. Bảng BUDGET_CATEGORIES (Bảng trung gian: Một ngân sách chứa nhiều danh mục)
CREATE TABLE IF NOT EXISTS budget_categories (
    budget_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (budget_id, category_id),
    FOREIGN KEY (budget_id) REFERENCES budgets(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

/*
Ghi chú workflow logic:
1. Tạo ngân sách: 
   - Insert vào bảng `budgets` lấy generated id.
   - Insert các dòng vào `budget_categories` với `budget_id` vừa tạo và các `category_id` được chọn.

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

4. Tính toán ngân sách (Spent Amount), có hỗ trợ chọn category cha:
   SELECT 
       b.*,
       COALESCE(SUM(t.amount), 0) AS spent_amount,
       (b.amount - COALESCE(SUM(t.amount), 0)) AS remaining,
       (COALESCE(SUM(t.amount), 0) / b.amount * 100) AS percent_used
   FROM budgets b
   JOIN budget_categories bc ON b.id = bc.budget_id
   LEFT JOIN categories c ON c.id = bc.category_id OR c.parent_id = bc.category_id
   LEFT JOIN transactions t ON t.category_id = c.id
       AND t.user_id = b.user_id
       AND t.type = 'EXPENSE'
       AND MONTH(t.date) = b.month
       AND YEAR(t.date) = b.year
   GROUP BY b.id;

5. Cập nhật giao dịch: Không cần trigger hay cập nhật trực tiếp vào budgets. 
   Mọi thứ được tính toán real-time từ bảng transactions.

6. Xoá category:
   - Default category: không cho edit/delete.
   - Custom category đã có transaction: không xoá cứng; dùng soft delete bằng is_deleted = TRUE.
   - Custom category chưa dùng: có thể xoá cứng nếu cần.

7. Xoá ngân sách: ON DELETE CASCADE sẽ tự động xoá bản ghi trong budget_categories.
*/

