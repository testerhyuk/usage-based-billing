-- 1. 청구·출금 설정 테이블 생성
CREATE TABLE billing_settings (
    member_id VARCHAR(255) PRIMARY KEY,

    bank VARCHAR(200) NOT NULL,
    account_holder_name VARCHAR(100) NOT NULL,
    account_number VARCHAR(100) NOT NULL,
    withdrawal_day VARCHAR(10) NOT NULL,

    pending_bank VARCHAR(200) NULL,
    pending_account_holder_name VARCHAR(100) NULL,
    pending_account_number VARCHAR(100) NULL,
    pending_withdrawal_day VARCHAR(10) NULL,
    effective_month DATE NULL,
    month_per_limit BIT(1) NOT NULL DEFAULT b'0',

    CONSTRAINT fk_billing_settings_member
        FOREIGN KEY (member_id) REFERENCES member (member_id)
) ENGINE = InnoDB;

-- 2. 이관한 컬럼을 member 테이블에서 제거
ALTER TABLE member
    DROP COLUMN bank,
    DROP COLUMN account_holder_name,
    DROP COLUMN account_number,
    DROP COLUMN withdrawal_day,
    DROP COLUMN pending_withdrawal_day,
    DROP COLUMN withdrawal_day_effective_month;