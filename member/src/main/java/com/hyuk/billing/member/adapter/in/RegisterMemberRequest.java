package com.hyuk.billing.member.adapter.in;

import com.hyuk.billing.member.domain.Bank;
import com.hyuk.billing.member.domain.WithdrawalDay;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterMemberRequest(
        @NotNull Bank bank,
        @NotBlank String accountHolderName,
        @NotBlank String accountNumber,
        @NotNull WithdrawalDay withdrawalDay
) {
}
