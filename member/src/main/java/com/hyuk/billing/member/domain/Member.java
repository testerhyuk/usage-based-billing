package com.hyuk.billing.member.domain;

import lombok.Getter;

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

    public Member(
            String memberId,
            String googleId,
            String email,
            BankAccount bankAccount,
            WithdrawalDay withdrawalDay,
            WithdrawalDay pendingWithdrawalDay,
            YearMonth withdrawalDayEffectiveMonth
    ) {
        this.memberId = Objects.requireNonNull(memberId);
        this.googleId = Objects.requireNonNull(googleId);
        this.email = Objects.requireNonNull(email);
        this.bankAccount = Objects.requireNonNull(bankAccount);
        this.withdrawalDay = Objects.requireNonNull(withdrawalDay);
        this.pendingWithdrawalDay = Objects.requireNonNull(pendingWithdrawalDay);
        this.withdrawalDayEffectiveMonth = Objects.requireNonNull(withdrawalDayEffectiveMonth);
    }

    public void changeBankAccount(BankAccount bankAccount) {
        this.bankAccount = Objects.requireNonNull(bankAccount);
    }

    public void changeWithdrawalDay(WithdrawalDay day, YearMonth currentMonth) {
        this.withdrawalDay = Objects.requireNonNull(day);
        this.withdrawalDayEffectiveMonth = Objects.requireNonNull(currentMonth.plusMonths(1));
    }
}
