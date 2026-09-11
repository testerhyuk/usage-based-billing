package com.hyuk.billing.member.adapter.in;

import com.hyuk.billing.member.domain.WithdrawalDay;
import jakarta.validation.constraints.NotNull;

public record ChangeWithdrawalDayRequest(@NotNull WithdrawalDay withdrawalDay) {
}
