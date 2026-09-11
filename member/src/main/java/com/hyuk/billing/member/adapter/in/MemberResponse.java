package com.hyuk.billing.member.adapter.in;

import com.hyuk.billing.member.domain.BankAccount;
import com.hyuk.billing.member.domain.Role;
import com.hyuk.billing.member.domain.WithdrawalDay;

import java.time.Instant;
import java.time.YearMonth;

public record MemberResponse(
        String memberId,
        String email,
        BankAccount bankAccount,
        WithdrawalDay withdrawalDay,
        WithdrawalDay pendingWithdrawalDay,
        YearMonth withdrawalDayEffectiveMonth,
        Instant registeredAt,
        Role role
) {
}
