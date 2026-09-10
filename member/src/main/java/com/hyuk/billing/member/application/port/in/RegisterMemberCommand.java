package com.hyuk.billing.member.application.port.in;

import com.hyuk.billing.member.domain.BankAccount;
import com.hyuk.billing.member.domain.WithdrawalDay;

public record RegisterMemberCommand(
        GoogleIdentity googleIdentity,
        BankAccount bankAccount,
        WithdrawalDay withdrawalDay
) {
}
