package com.hyuk.billing.member.application.port.in;

public record GoogleIdentity(
        String googleId,
        String email
) {
}
