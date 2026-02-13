-- 초기 Mock 데이터
-- 주의: MySQL 공식 이미지는 파일명을 알파벳 순으로 실행하므로
-- 테이블 생성 스키마(schema.sql) 이후에 실행되도록 z- 접두어를 사용했습니다.
SET NAMES utf8mb4;
use team1;
-- 매장 2개
INSERT INTO stores (id, name, description)
VALUES
    (1, '강남 카페', '스페셜티 커피와 디저트를 판매하는 카페'),
    (2, '홍대 피자샵', '수제 피자와 사이드를 판매하는 매장');

-- 사용자 2명
INSERT INTO users (id, email, password, name, phone, address)
VALUES
    (1, 'alice@example.com', 'password123', 'Alice', '010-1111-2222', '서울특별시 강남구 테헤란로 123'),
    (2, 'bob@example.com',   'password123', 'Bob',   '010-3333-4444', '서울특별시 마포구 와우산로 45');

-- 상품 10개 이상 (강남 카페 6개, 홍대 피자샵 6개)
INSERT INTO products (id, store_id, name, description, price, stock, product_status)
VALUES
    (1, 1, '아메리카노',        '진한 에스프레소와 깔끔한 맛의 커피',                 4500, 100, 'ACTIVE'),
    (2, 1, '카페라테',          '우유가 들어간 부드러운 커피',                       5000, 80,  'ACTIVE'),
    (3, 1, '바닐라라테',        '바닐라 시럽이 들어간 달콤한 라테',                  5500, 60,  'ACTIVE'),
    (4, 1, '콜드브루',          '12시간 이상 추출한 콜드브루 커피',                  5500, 50,  'ACTIVE'),
    (5, 1, '치즈케이크',        '진한 치즈 풍미의 디저트 케이크',                    6500, 30,  'ACTIVE'),
    (6, 1, '초코브라우니',      '진한 초콜릿 맛의 브라우니',                        6000, 40,  'ACTIVE'),

    (7,  2, '마르게리타 피자',  '토마토 소스와 모짜렐라 치즈의 기본 피자',           15000, 40, 'ACTIVE'),
    (8,  2, '페퍼로니 피자',    '페퍼로니 토핑이 듬뿍 올라간 피자',                 17000, 35, 'ACTIVE'),
    (9,  2, '콤비네이션 피자',  '다양한 토핑이 올려진 피자',                         18000, 30, 'ACTIVE'),
    (10, 2, '고르곤졸라 피자',  '꿀과 함께 먹는 고르곤졸라 치즈 피자',              19000, 25, 'ACTIVE'),
    (11, 2, '갈릭 디핑 소스',   '피자와 함께 먹는 마늘 디핑 소스',                  1500,  200,'ACTIVE'),
    (12, 2, '콜라 500ml',       '탄산음료 콜라 500ml',                              2000,  150,'ACTIVE');

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
    (1, 1, 2, 15500.00, 2000.00, 0, 13500.00, 'Alice', '010-1111-2222', '주소', '상세주소', '메시지', 'PENDING', NULL, NOW(), NOW()),
    (2, 2, NULL, 22000.00, 0, 3000.00, 25000.00, 'Bob', '010-3333-4444', '주소', NULL, NULL, 'DELIVERED', NULL, NOW(), NOW());

INSERT INTO order_items (product_id, order_id, quantity, unit_price, created_at)
VALUES
    -- 주문 1: Alice (카페)
    (1, 1, 2, 4500, NOW() - INTERVAL 1 DAY),
    (5, 1, 1, 6500, NOW() - INTERVAL 1 DAY),
    -- 주문 2: Bob (피자샵)
    (8, 2, 1, 17000, NOW() - INTERVAL 2 DAY),
    (11,2, 2, 1500,  NOW() - INTERVAL 2 DAY),
    (12,2, 1, 2000,  NOW() - INTERVAL 2 DAY);

