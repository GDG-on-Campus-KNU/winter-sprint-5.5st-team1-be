-- Stores 테이블
CREATE TABLE Stores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Users 테이블
-- 한 사용자는 여러 주문(Orders), 장바구니 항목(CartItems), 쿠폰 보유(UserCoupons)를 가질 수 있는 1:N 관계의 기준 엔티티입니다.
-- 사용자 삭제 시 주문/결제 이력은 유지해야 하므로 Orders.user_id 는 ON DELETE RESTRICT 를 사용하고,
-- 장바구니와 보유 쿠폰은 함께 정리되는 것이 자연스러워 CartItems.user_id, UserCoupons.user_id 는 ON DELETE CASCADE 를 사용합니다.
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
-- 한 상점(Stores)은 여러 상품(Products)을 가지는 1:N 관계입니다.
-- 상점이 삭제될 경우 기존 주문/상품 이력 보존을 위해 상품을 자동으로 지우지 않도록 ON DELETE RESTRICT 를 사용합니다.
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
    -- 특정 상점이 삭제되더라도 기존 상품/주문 이력은 보호해야 하므로 RESTRICT 로 상점 삭제를 막습니다.
    FOREIGN KEY (store_id) REFERENCES Stores(id) ON DELETE RESTRICT,
    INDEX idx_store_id (store_id),
    INDEX idx_status (product_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- CartItem 테이블
-- Users(1) - CartItems(N), Products(1) - CartItems(N) 관계를 표현하는 조인 테이블입니다.
-- 유저나 상품이 삭제되면 장바구니의 해당 레코드는 의미가 없어지므로 둘 다 ON DELETE CASCADE 로 함께 제거되도록 합니다.
CREATE TABLE CartItems (
    product_id INT NOT NULL,
    user_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, product_id),
    -- 유저 탈퇴 시 해당 유저의 장바구니는 더 이상 필요 없으므로 함께 삭제
    FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE,
    -- 상품 삭제 시 해당 상품을 가리키는 장바구니 항목도 무의미하므로 함께 삭제
    FOREIGN KEY (product_id) REFERENCES Products(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Coupon 테이블
-- 플랫폼 전체에서 사용 가능한 쿠폰 마스터 테이블입니다.
-- 실무에서는 쿠폰 사용/정산 이력을 남기기 위해 쿠폰 행을 직접 DELETE 하기보다는
-- coupon_status 로 비활성화(소프트 삭제)하는 방식을 기본 전제로 합니다.
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
-- Users(1) - UserCoupons(N), Coupons(1) - UserCoupons(N)의 조인 테이블로,
-- 특정 유저에게 어떤 쿠폰이 언제 발급/사용/만료되었는지 기록합니다.
-- 주문이 UserCoupons 를 참조하므로, 쿠폰 사용 이력을 보존하기 위해 쿠폰/유저쿠폰은 원칙적으로 DELETE 하지 않고
-- 상태(coupon_status, used_at, expired_at)로만 관리하는 것을 전제로 합니다.
CREATE TABLE UserCoupons (
    id INT AUTO_INCREMENT PRIMARY KEY,
    coupon_id INT NOT NULL,
    user_id INT NOT NULL,
    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    used_at TIMESTAMP NULL,
    expired_at TIMESTAMP NOT NULL,
    -- 쿠폰 마스터 삭제를 허용하지 않고(RESTRICT), 쿠폰은 coupon_status 로만 비활성화하여 사용 이력을 보존
    FOREIGN KEY (coupon_id) REFERENCES Coupons(id) ON DELETE RESTRICT,
    -- 유저 삭제 역시 제한(RESTRICT)하고, 유저 탈퇴 시에도 어떤 쿠폰을 썼는지 이력 조회가 가능하도록 설계하는 것을 권장
    FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE RESTRICT,
    UNIQUE KEY unique_user_coupon (user_id, coupon_id),
    INDEX idx_user_expired (user_id, expired_at),
    INDEX idx_used_at (used_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Order 테이블
-- Users(1) - Orders(N), UserCoupons(1) - Orders(N) 관계입니다.
-- 사용자 삭제 시 결제/주문 이력은 반드시 남겨야 하므로 user_id 는 ON DELETE RESTRICT 를 사용합니다.
-- 어떤 쿠폰을 사용했는지 이력을 항상 추적하기 위해, user_coupon_id 도 ON DELETE RESTRICT 로 두고
-- UserCoupons 역시 실제 DELETE 가 아닌 used_at/expired_at 기반 상태 관리만 한다는 전제를 둡니다.
CREATE TABLE Orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    user_coupon_id INT NULL,
    total_product_price DECIMAL(10, 2) NOT NULL,
    discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    delivery_fee DECIMAL(10, 2) NOT NULL DEFAULT 0,
    final_price DECIMAL(10, 2) NOT NULL,
    delivery_address TEXT NOT NULL,
    order_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- 유저 삭제 시 해당 유저의 주문 이력은 보존해야 하므로 유저 삭제를 제한
    FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE RESTRICT,
    -- 쿠폰 사용 이력을 잃지 않기 위해 UserCoupons 삭제를 제한(RESTRICT)하고, 주문-쿠폰 관계를 유지
    FOREIGN KEY (user_coupon_id) REFERENCES UserCoupons(id) ON DELETE RESTRICT,
    INDEX idx_user_created (user_id, created_at DESC),
    INDEX idx_status (order_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- OrderItem 테이블
-- Orders(1) - OrderItems(N), Products(1) - OrderItems(N)의 관계를 표현합니다.
-- 주문이 삭제되면 해당 주문의 상세 항목들도 더 이상 의미가 없으므로 order_id 는 ON DELETE CASCADE 를 사용합니다.
-- 반대로 상품이 삭제되더라도 과거에 어떤 상품이 얼마에 팔렸는지 주문 이력은 남겨야 하므로 product_id 는 ON DELETE RESTRICT 를 사용합니다.
CREATE TABLE OrderItems (
    product_id INT NOT NULL,
    order_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (order_id, product_id),
    -- 상품 삭제 시에도 주문 상세 이력은 보호해야 하므로 상품 삭제를 제한
    FOREIGN KEY (product_id) REFERENCES Products(id) ON DELETE RESTRICT,
    -- 주문 삭제 시, 해당 주문의 상세 항목은 함께 삭제되는 것이 자연스러움
    FOREIGN KEY (order_id) REFERENCES Orders(id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;