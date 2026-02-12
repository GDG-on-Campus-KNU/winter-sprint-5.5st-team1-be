-- Stores 테이블
CREATE TABLE Stores (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        description TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Users 테이블
CREATE TABLE Users (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       name VARCHAR(100) NOT NULL,
                       phone VARCHAR(20),
                       address TEXT,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Product 테이블
CREATE TABLE Products (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          store_id INT NOT NULL,
                          name VARCHAR(255) NOT NULL,
                          description TEXT,
                          price DECIMAL(10, 2) NOT NULL,
                          stock INT NOT NULL DEFAULT 0,
                          product_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          FOREIGN KEY (store_id) REFERENCES Stores(id) ON DELETE RESTRICT,
                          INDEX idx_store_id (store_id),
                          INDEX idx_status (product_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- CartItem 테이블
CREATE TABLE CartItems (
                           product_id INT NOT NULL,
                           user_id INT NOT NULL,
                           quantity INT NOT NULL DEFAULT 1,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           PRIMARY KEY (user_id, product_id),
                           FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE,
                           FOREIGN KEY (product_id) REFERENCES Products(id) ON DELETE CASCADE,
                           INDEX idx_user_id (user_id),
                           INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Coupon 테이블
CREATE TABLE Coupons (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         min_order_price DECIMAL(10, 2) NOT NULL DEFAULT 0,
                         coupon_type ENUM('PERCENTAGE', 'FIXED') NOT NULL,
                         discount_value DECIMAL(10, 2) NOT NULL,
                         description TEXT,
                         coupon_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
                         valid_days INT NOT NULL DEFAULT 30,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         INDEX idx_status (coupon_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- UserCoupon 테이블
CREATE TABLE UserCoupons (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             coupon_id INT NOT NULL,
                             user_id INT NOT NULL,
                             issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             used_at TIMESTAMP NULL,
                             expired_at TIMESTAMP NOT NULL,
                             FOREIGN KEY (coupon_id) REFERENCES Coupons(id) ON DELETE RESTRICT,
                             FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE RESTRICT,
                             UNIQUE KEY unique_user_coupon (user_id, coupon_id),
                             INDEX idx_user_expired (user_id, expired_at),
                             INDEX idx_used_at (used_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Order 테이블
-- delivery_address(주소), delivery_detail_address(상세주소), recipient_name(받는 분),
-- recipient_phone(전화번호), delivery_message(배송 메시지) 필드로 분리하여 관리합니다.
CREATE TABLE Orders (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        user_id INT NOT NULL,
                        user_coupon_id INT NULL,
                        total_product_price DECIMAL(10, 2) NOT NULL,
                        discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
                        delivery_fee DECIMAL(10, 2) NOT NULL DEFAULT 0,
                        final_price DECIMAL(10, 2) NOT NULL,
    -- 배송지 정보 (와이어프레임 기준으로 필드 분리)
                        recipient_name VARCHAR(100) NOT NULL,                -- 받는 분
                        recipient_phone VARCHAR(20) NOT NULL,                -- 전화번호
                        delivery_address TEXT NOT NULL,                      -- 주소
                        delivery_detail_address VARCHAR(255),                -- 상세주소 (선택)
                        delivery_message VARCHAR(500),                       -- 배송 메시지 (선택)
                        order_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE RESTRICT,
                        FOREIGN KEY (user_coupon_id) REFERENCES UserCoupons(id) ON DELETE RESTRICT,
                        INDEX idx_user_created (user_id, created_at DESC),
                        INDEX idx_status (order_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- OrderItem 테이블
CREATE TABLE OrderItems (
                            product_id INT NOT NULL,
                            order_id INT NOT NULL,
                            quantity INT NOT NULL,
                            unit_price DECIMAL(10, 2) NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            PRIMARY KEY (order_id, product_id),
                            FOREIGN KEY (product_id) REFERENCES Products(id) ON DELETE RESTRICT,
                            FOREIGN KEY (order_id) REFERENCES Orders(id) ON DELETE CASCADE,
                            INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
