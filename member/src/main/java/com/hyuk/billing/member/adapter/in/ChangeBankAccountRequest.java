package com.hyuk.billing.member.adapter.in;

import com.hyuk.billing.member.domain.Bank;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChangeBankAccountRequest(
        @NotNull Bank bank,
        @NotBlank String accountHolderName,
        @NotBlank String accountNumber
) {
}
