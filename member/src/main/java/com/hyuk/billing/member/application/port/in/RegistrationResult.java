package com.hyuk.billing.member.application.port.in;

import com.hyuk.billing.member.application.port.out.IssuedApiKey;

public record RegistrationResult(
        String memberId,
        IssuedApiKey issuedApiKey
) {
}
