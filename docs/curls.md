# cURL examples

## 1) Signup

```bash
curl -X POST http://localhost:9000/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username":"alice01",
    "password":"Pass1234",
    "fullName":"Alice Kumar",
    "address":"Pune, Maharashtra",
    "email":"alice01@example.com",
    "accountType":"ZERO_BALANCE"
  }'
```

## 2) Login

```bash
curl -X POST http://localhost:9000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username":"alice01",
    "password":"Pass1234"
  }'
```

Save token:

```bash
TOKEN=""
```

## 3) Create account (only if user does not have one)

```bash
curl -X POST http://localhost:9000/api/accounts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"accountType":"MIN_BALANCE"}'
```

## 4) Get my account and balance

```bash
curl -X GET http://localhost:9000/api/accounts/me \
  -H "Authorization: Bearer $TOKEN"
```

```bash
curl -X GET http://localhost:9000/api/accounts/balance \
  -H "Authorization: Bearer $TOKEN"
```

## 5) Transfer

```bash
curl -X POST http://localhost:9000/api/transactions/transfer \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "toAccountNumber":"123456789012",
    "amount":250.00,
    "remark":"rent split"
  }'
```

## 6) Withdraw

```bash
curl -X POST http://localhost:9000/api/transactions/withdraw \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount":150.00,
    "remark":"atm"
  }'
```

## 7) Transaction history and mini statement

```bash
curl -X GET "http://localhost:9000/api/transactions/history?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"
```

```bash
curl -X GET http://localhost:9000/api/transactions/mini-statement \
  -H "Authorization: Bearer $TOKEN"
```

## 8) Create FD and list FDs

```bash
curl -X POST http://localhost:9000/api/fds \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount":1000.00,
    "tenure":"SIX_MONTHS"
  }'
```

```bash
curl -X GET http://localhost:9000/api/fds \
  -H "Authorization: Bearer $TOKEN"
```

## 9) Close account

```bash
curl -X PATCH http://localhost:9000/api/accounts/close \
  -H "Authorization: Bearer $TOKEN"
```

## 10) Admin endpoints

Login as an admin user and store admin token:

```bash
ADMIN_TOKEN="<paste_admin_token_here>"
```

List users:

```bash
curl -X GET http://localhost:9000/api/admin/users \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

Update role:

```bash
curl -X PATCH http://localhost:9000/api/admin/users/2/role \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"role":"ADMIN"}'
```

