package com.hyuk.billing.member.domain;

import lombok.Getter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

@Getter
public class BillingSettings {
    private String memberId;
    private BankAccount bankAccount;
    private WithdrawalDay withdrawalDay;
    private BankAccount pendingBankAccount;
    private WithdrawalDay pendingWithdrawalDay;
    private YearMonth effectiveMonth;
    private boolean monthPerLimit;

    public BillingSettings(
            String memberId,
            BankAccount bankAccount,
            WithdrawalDay withdrawalDay,
            BankAccount pendingBankAccount,
            WithdrawalDay pendingWithdrawalDay,
            YearMonth effectiveMonth,
            boolean monthPerLimit
    ) {
        this.memberId = Objects.requireNonNull(memberId);
        this.bankAccount = Objects.requireNonNull(bankAccount);
        this.withdrawalDay = Objects.requireNonNull(withdrawalDay);

        this.pendingBankAccount = pendingBankAccount;
        this.pendingWithdrawalDay = pendingWithdrawalDay;
        this.effectiveMonth = effectiveMonth;
        this.monthPerLimit = monthPerLimit;
    }

    public void resetMonthPerLimit() {
        this.monthPerLimit = false;
    }

    public void validateChangeDate(LocalDate currentDate) {
        Objects.requireNonNull(currentDate);

        if (currentDate.getDayOfMonth() > 25) {
            throw new IllegalArgumentException("변경 신청은 매월 1일부터 25일까지 가능합니다. 다음 달 1일부터 다시 변경할 수 있습니다");
        }
    }

    public void changeSettings(BankAccount changedBankAccount, WithdrawalDay changeWithdrawalDay, LocalDate currentDate) {
        validateChangeDate(currentDate);

        if (monthPerLimit) {
            throw new IllegalStateException("월 1회만 게좌 및 출금 예정일 변경이 가능합니다");
        }

        if (changedBankAccount == null && changeWithdrawalDay == null) {
            throw new IllegalArgumentException("변경할 계좌 또는 출금 예정일을 입력해야 합니다");
        }

        YearMonth nextMonth = YearMonth.from(currentDate).plusMonths(1);

        this.pendingBankAccount = changedBankAccount;
        this.pendingWithdrawalDay = changeWithdrawalDay;
        this.effectiveMonth = nextMonth;
        this.monthPerLimit = true;
    }
}
