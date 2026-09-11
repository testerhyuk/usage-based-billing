package com.hyuk.billing.member.domain;

import lombok.Getter;

import java.time.Instant;
import java.time.YearMonth;
import java.util.Objects;

@Getter
public class Member {
    private String memberId;
    private String googleId;
    private String email;
    private BankAccount bankAccount;
    private WithdrawalDay withdrawalDay;
    private WithdrawalDay pendingWithdrawalDay;
    private YearMonth withdrawalDayEffectiveMonth;
    private Instant registeredAt;
    private Role role;

    public Member(
            String memberId,
            String googleId,
            String email,
            BankAccount bankAccount,
            WithdrawalDay withdrawalDay,
            WithdrawalDay pendingWithdrawalDay,
            YearMonth withdrawalDayEffectiveMonth,
            Instant registeredAt,
            Role role
    ) {
        this.memberId = Objects.requireNonNull(memberId);
        this.googleId = Objects.requireNonNull(googleId);
        this.email = Objects.requireNonNull(email);
        this.bankAccount = Objects.requireNonNull(bankAccount);
        this.withdrawalDay = Objects.requireNonNull(withdrawalDay);
        this.pendingWithdrawalDay = pendingWithdrawalDay;
        this.withdrawalDayEffectiveMonth = withdrawalDayEffectiveMonth;
        this.registeredAt = registeredAt;
        this.role = Objects.requireNonNull(role);
    }

    public void changeBankAccount(BankAccount bankAccount) {
        this.bankAccount = Objects.requireNonNull(bankAccount);
    }

    public void changeWithdrawalDay(WithdrawalDay day, YearMonth currentMonth) {
        this.pendingWithdrawalDay = Objects.requireNonNull(day);
        this.withdrawalDayEffectiveMonth = Objects.requireNonNull(currentMonth.plusMonths(1));
    }
}
