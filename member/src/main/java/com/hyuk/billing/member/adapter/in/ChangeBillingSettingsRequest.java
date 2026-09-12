package com.hyuk.billing.member.adapter.in;

import com.hyuk.billing.member.domain.WithdrawalDay;
import jakarta.validation.Valid;

public record ChangeBillingSettingsRequest(
        @Valid ChangeBankAccountRequest bankAccount,
        WithdrawalDay withdrawalDay
) {
}
