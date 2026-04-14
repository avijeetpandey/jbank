# jbank

`jbank` is a Spring Boot banking API implementing authentication, account management, transactions, fixed deposits, Redis caching, resilience, role-based access control, and strict Flyway-managed schema validation.

## Tech stack

- Java 17
- Spring Boot 4
- Spring Security + JWT
- Spring Data JPA (PostgreSQL)
- Spring Data Redis (`RedisTemplate`)
- Resilience4j (retry + circuit breaker)
- Flyway migrations

## Implemented features

- User signup and login with request validation
- One account per user, auto-created during signup
- Account types:
  - `ZERO_BALANCE`
  - `MIN_BALANCE` (minimum balance `10000.00`)
- Fund transfer and withdrawal with flat transaction fee `5.00`
- Account closure with business checks
- Fixed deposit creation (`SIX_MONTHS`, `ONE_YEAR`) at 7% annualized return
- Paginated transaction history
- Mini statement with latest 5 transactions
- JWT-based authentication for business APIs
- Role support:
  - `USER`
  - `ADMIN`
- Endpoint-level authorization using URL rules + method security (`@PreAuthorize`)

## Role-based authorization

- Public endpoints:
  - `POST /api/auth/signup`
  - `POST /api/auth/login`
- Authenticated user/admin endpoints:
  - `/api/accounts/**`
  - `/api/transactions/**`
  - `/api/fds/**`
- Admin-only endpoints:
  - `GET /api/admin/users`
  - `PATCH /api/admin/users/{userId}/role`

## Database migrations (Flyway)

Schema is managed through:

- `src/main/resources/db/migration/V1__init_schema.sql`

Key points:

- `spring.jpa.hibernate.ddl-auto=validate` to enforce entity/schema compatibility
- Flyway is the source of truth for schema
- Strict checks are added in SQL using:
  - `UNIQUE` constraints
  - `FOREIGN KEY` constraints
  - `CHECK` constraints for enums and non-negative amounts

> If you already have old tables created by Hibernate `update`, drop/recreate the DB once before first Flyway run or baseline your DB manually.

## Configuration

Defined in `src/main/resources/application.properties`.

Environment variables (optional overrides):

- `DB_URL` (default `jdbc:postgresql://localhost:5432/jbank`)
- `DB_USERNAME` (default `postgres`)
- `DB_PASSWORD` (default `postgres`)
- `REDIS_HOST` (default `localhost`)
- `REDIS_PORT` (default `6379`)
- `JWT_SECRET` (must be at least 32 characters)
- `JWT_EXPIRATION_MS` (default `86400000`)

## Local run

### 1) Start dependencies in Docker

Use `docker-compose.yml` for PostgreSQL and Redis only.

```bash
docker compose up -d
```

### 2) Run Spring Boot app in IntelliJ

Run `com.avijeet.jbank.JbankApplication` from IntelliJ.

### 3) Optional build/test via CLI

```bash
./mvnw --no-transfer-progress test
```

## Make first admin user

By default, signup assigns role `USER`. Promote one user to `ADMIN` using SQL:

```sql
UPDATE app_users SET role = 'ADMIN' WHERE username = 'your_username';
```

Then login again to receive JWT with admin authority applied at runtime.

## Redis balance cache

- Key pattern: `balance:{accountNumber}`
- TTL: 8 hours
- Redis calls are wrapped with:
  - retry: `redisRetry`
  - circuit breaker: `redisCircuit`

## API examples

See `docs/curls.md` for end-to-end cURL commands.

## Project structure

- `src/main/java/com/avijeet/jbank/controllers` - REST APIs
- `src/main/java/com/avijeet/jbank/services` - business logic
- `src/main/java/com/avijeet/jbank/entities` - JPA entities
- `src/main/java/com/avijeet/jbank/repositories` - data access
- `src/main/java/com/avijeet/jbank/security` - JWT and user details
- `src/main/java/com/avijeet/jbank/config` - security/redis config
- `src/main/resources/db/migration` - Flyway SQL scripts

