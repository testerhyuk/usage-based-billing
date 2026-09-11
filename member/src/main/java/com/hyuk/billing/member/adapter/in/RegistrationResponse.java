package com.hyuk.billing.member.adapter.in;

public record RegistrationResponse(
        String memberId,
        String rawApiKey
) {
}
