# GDG Sprint Team1 Backend

Spring Boot 기반 백엔드 API (Week 1: 메뉴/장바구니/주문, Week 2: JWT 인증·마이페이지).

## Week 2 변경 사항 (인증 & 사용자 경험 강화)

- **X-USER-ID 제거**: Week 1에서 사용하던 임시 헤더 `X-USER-ID`는 제거되었습니다. 모든 사용자 전용 API는 **JWT 기반 인증**을 사용합니다.
- **인증 API**: 회원가입(`POST /api/v1/auth/signup`), 로그인(`POST /api/v1/auth/login` → Access + Refresh Token 발급), 토큰 재발급(`POST /api/v1/auth/refresh`), 내 정보(`GET /api/v1/users/me`).
- **보호 API**: 장바구니(`/api/v1/cart`), 주문(`/api/v1/orders`), 마이페이지(`/api/v1/my/**`)는 `Authorization: Bearer <token>` 필수. 토큰 없이 접근 시 `401 UNAUTHORIZED` (AUTH_REQUIRED 등).
- **마이페이지**: `GET /api/v1/my/orders`, `GET /api/v1/my/orders/{order_id}`, `GET /api/v1/my/coupons` (필터: `status=AVAILABLE` | `USED`).

## 테스트 계정 (QA/프론트용)

| 이메일         | 비밀번호   |
|----------------|------------|
| dev1@gdg.com   | password1   |
| dev2@gdg.com   | password2   |
| dev3@gdg.com   | password3   |

z-data.sql 목업으로 들어가 있으며, 평문 비밀번호로 로그인할 수 있습니다. (회원가입으로 만든 계정만 BCrypt 사용)

로그인 시 **Access Token**(30분)과 **Refresh Token**(7일)이 함께 발급됩니다. Access Token을 `Authorization: Bearer <token>` 헤더에 넣어 보호된 API를 호출하고, 만료 시 `POST /api/v1/auth/refresh`에 Refresh Token을 보내 새 Access·Refresh Token을 받으면 됩니다.

## 환경 변수

`.env.sample`을 참고해 `.env`를 구성하세요.

- **DB**: `MYSQL_*` (호스트, 포트, DB명, 사용자, 비밀번호)
- **JWT**: `JWT_SECRET`, `JWT_ACCESS_EXPIRE_MINUTES` (기본 30), `JWT_REFRESH_EXPIRE_DAYS` (기본 7)

## API 문서

- **Swagger UI**: `http://localhost:8080/swagger-ui.html` (실행 후)
- 인증이 필요한 API는 Swagger 상단 **Authorize**에서 Bearer 토큰을 입력한 뒤 호출할 수 있습니다.

## 실행

- **Docker**: `docker-compose up -d` (MySQL 먼저 기동·헬스체크 후 앱이 연결되므로, 첫 기동 시 MySQL 준비까지 15~30초 정도 걸릴 수 있습니다.)
- **로컬**: MySQL이 먼저 떠 있어야 합니다. `./gradlew :team1:bootRun` 또는 IDE에서 `Team1Application` 실행. DB는 `localhost:3306`(또는 `.env`의 `MYSQL_HOST`/`MYSQL_PORT`)로 접속합니다.

## 기존 DB에 role 컬럼이 없는 경우

이미 Week 1 스키마로 DB를 만든 경우, `users` 테이블에 `role` 컬럼을 추가해야 합니다.

```sql
ALTER TABLE users ADD COLUMN role VARCHAR(50) NOT NULL DEFAULT 'USER' AFTER address;
ALTER TABLE users ADD INDEX idx_role (role);
```

이후 앱을 재기동하면 JWT·역할 기반 API가 정상 동작합니다.
