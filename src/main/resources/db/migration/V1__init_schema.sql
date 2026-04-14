CREATE TABLE app_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    address VARCHAR(300) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_app_users_role CHECK (role IN ('USER', 'ADMIN'))
);

CREATE TABLE bank_accounts (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(24) NOT NULL UNIQUE,
    account_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    balance NUMERIC(19,2) NOT NULL,
    minimum_balance NUMERIC(19,2) NOT NULL,
    opened_at TIMESTAMP NOT NULL,
    closed_at TIMESTAMP NULL,
    user_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_bank_accounts_user FOREIGN KEY (user_id) REFERENCES app_users (id),
    CONSTRAINT chk_bank_accounts_account_type CHECK (account_type IN ('ZERO_BALANCE', 'MIN_BALANCE')),
    CONSTRAINT chk_bank_accounts_status CHECK (status IN ('ACTIVE', 'CLOSED')),
    CONSTRAINT chk_bank_accounts_balance_non_negative CHECK (balance >= 0),
    CONSTRAINT chk_bank_accounts_minimum_balance_non_negative CHECK (minimum_balance >= 0)
);

CREATE TABLE account_transactions (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    reference_id VARCHAR(40) NOT NULL UNIQUE,
    transaction_type VARCHAR(30) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    fee NUMERIC(19,2) NOT NULL,
    balance_after NUMERIC(19,2) NOT NULL,
    counterparty_account_number VARCHAR(24) NULL,
    remark VARCHAR(300) NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_account_transactions_account FOREIGN KEY (account_id) REFERENCES bank_accounts (id),
    CONSTRAINT chk_account_transactions_type CHECK (transaction_type IN ('ACCOUNT_OPEN', 'TRANSFER', 'FD_CREATION', 'WITHDRAWAL', 'ACCOUNT_CLOSE')),
    CONSTRAINT chk_account_transactions_amount_positive CHECK (amount >= 0),
    CONSTRAINT chk_account_transactions_fee_non_negative CHECK (fee >= 0),
    CONSTRAINT chk_account_transactions_balance_after_non_negative CHECK (balance_after >= 0)
);

CREATE TABLE fixed_deposits (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    principal_amount NUMERIC(19,2) NOT NULL,
    tenure VARCHAR(20) NOT NULL,
    interest_rate NUMERIC(5,4) NOT NULL,
    maturity_amount NUMERIC(19,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    maturity_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_fixed_deposits_account FOREIGN KEY (account_id) REFERENCES bank_accounts (id),
    CONSTRAINT chk_fixed_deposits_tenure CHECK (tenure IN ('SIX_MONTHS', 'ONE_YEAR')),
    CONSTRAINT chk_fixed_deposits_status CHECK (status IN ('ACTIVE', 'CLOSED', 'MATURED')),
    CONSTRAINT chk_fixed_deposits_principal_positive CHECK (principal_amount > 0),
    CONSTRAINT chk_fixed_deposits_maturity_ge_principal CHECK (maturity_amount >= principal_amount)
);

CREATE INDEX idx_account_transactions_account_created_at
    ON account_transactions (account_id, created_at DESC);

CREATE INDEX idx_fixed_deposits_account_created_at
    ON fixed_deposits (account_id, created_at DESC);

