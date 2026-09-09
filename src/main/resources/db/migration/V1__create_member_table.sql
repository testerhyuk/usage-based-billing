create table member (
    member_id VARCHAR(255) PRIMARY KEY,
    google_id VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_bin NOT NULL UNIQUE,
    email VARCHAR(254) NOT NULL,
    bank VARCHAR(200) NOT NULL,
    account_holder_name VARCHAR(100) NOT NULL,
    account_number VARCHAR(100) NOT NULL,
    withdrawal_day VARCHAR(10) NOT NULL,
    pending_withdrawal_day VARCHAR(10) NULL,
    withdrawal_day_effective_month DATE NULL
);