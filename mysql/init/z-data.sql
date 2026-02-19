-- 초기 Mock 데이터
-- 주의: MySQL 공식 이미지는 파일명을 알파벳 순으로 실행하므로
-- 테이블 생성 스키마(schema.sql) 이후에 실행되도록 z- 접두어를 사용했습니다.
SET NAMES utf8mb4;
use team1;

-- 사용자 3명 (목업, 평문 비밀번호. 회원가입 사용자만 BCrypt)
INSERT INTO users (id, email, password, name, phone, address, role)
VALUES
    (1, 'dev1@gdg.com', 'dev123', 'Dev1', '010-1111-1111', '서울시 강남구', 'USER'),
    (2, 'dev2@gdg.com', 'dev123', 'Dev2', '010-2222-2222', '서울시 마포구', 'USER'),
    (3, 'dev3@gdg.com', 'dev123', 'Dev3', '010-3333-3333', '서울시 서초구', 'USER');

-- 상품 12개
INSERT INTO products (id, name, description, price, stock, product_status)
VALUES
    (1, 'A4 노트',           '80매 라인 노트',                                   4500, 100, 'ACTIVE'),
    (2, '볼펜 12개입',        '검정 볼펜 12개 세트',                             5000, 80,  'ACTIVE'),
    (3, '형광펜 세트',        '6색 형광펜 세트',                                  5500, 60,  'ACTIVE'),
    (4, '스티커 팩',          '다양한 디자인의 스티커 50매',                      5500, 50,  'ACTIVE'),
    (5, '파일철',             'A4 50매용 바인더',                                 6500, 30,  'ACTIVE'),
    (6, '클립 세트',          '다양한 크기의 클립 100개입',                       6000, 40,  'ACTIVE'),
    (7, '토트백',             '심플한 캔버스 토트백',                             15000, 40, 'ACTIVE'),
    (8, '지갑',               '슬림 카드지갑',                                    17000, 35, 'ACTIVE'),
    (9, '휴대폰 케이스',      '실리콘 휴대폰 케이스',                            18000, 30, 'ACTIVE'),
    (10, '이어폰 파우치',     '이어폰 보관용 파우치',                            19000, 25, 'ACTIVE'),
    (11, '마스크 5매입',      '일회용 마스크 5매',                               1500,  200,'ACTIVE'),
    (12, '손소독제 50ml',     '휴대용 손소독제 50ml',                            2000,  150,'ACTIVE');

-- 쿠폰 2개 (정률, 정액)
INSERT INTO coupons (id, name, min_order_price, coupon_type, discount_value, description, coupon_status, valid_days)
VALUES
    (1, '10% 할인 쿠폰', 10000, 'PERCENTAGE', 10.00, '주문 금액의 10% 할인', 'ACTIVE', 30),
    (2, '2,000원 할인 쿠폰', 8000, 'FIXED', 2000.00, '주문 금액에서 2,000원 할인', 'ACTIVE', 30);

-- 사용자 보유 쿠폰
INSERT INTO user_coupons (id, coupon_id, user_id, issued_at, used_at, expired_at)
VALUES
    (1, 1, 1, NOW() - INTERVAL 5 DAY,  NULL,                      NOW() + INTERVAL 25 DAY),
    (2, 2, 1, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 1 DAY,   NOW() + INTERVAL 20 DAY),
    (3, 2, 2, NOW() - INTERVAL 3 DAY,  NULL,                      NOW() + INTERVAL 27 DAY);

-- 장바구니 예시 데이터
INSERT INTO cart_items (product_id, user_id, quantity, created_at, updated_at)
VALUES
    (1, 1, 2, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
    (5, 1, 1, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
    (8, 2, 1, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY),
    (11,2, 2, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY);

-- 주문 및 주문 아이템 예시
-- recipient_name, recipient_phone, delivery_address, delivery_detail_address, delivery_message 컬럼 반영
INSERT INTO orders (id, user_id, user_coupon_id, total_product_price, discount_amount, delivery_fee, final_price,
                    recipient_name, recipient_phone, delivery_address, delivery_detail_address, delivery_message,
                    order_status, cancel_reason, created_at, updated_at) -- cancel_reason 추가
VALUES
    (1, 1, 2, 15500.00, 2000.00, 0, 13500.00, 'Dev1', '010-1111-1111', '주소', '상세주소', '메시지', 'PENDING', NULL, NOW(), NOW()),
    (2, 2, NULL, 22000.00, 0, 3000.00, 25000.00, 'Dev2', '010-2222-2222', '주소', NULL, NULL, 'DELIVERED', NULL, NOW(), NOW());

INSERT INTO order_items (product_id, order_id, quantity, unit_price, created_at)
VALUES
    -- 주문 1: Dev1 (문구점)
    (1, 1, 2, 4500, NOW() - INTERVAL 1 DAY),
    (5, 1, 1, 6500, NOW() - INTERVAL 1 DAY),
    -- 주문 2: Dev2 (잡화점)
    (8, 2, 1, 17000, NOW() - INTERVAL 2 DAY),
    (11,2, 2, 1500,  NOW() - INTERVAL 2 DAY),
    (12,2, 1, 2000,  NOW() - INTERVAL 2 DAY);
