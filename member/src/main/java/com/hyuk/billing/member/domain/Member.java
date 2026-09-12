package com.hyuk.billing.member.domain;

import lombok.Getter;

import java.time.Instant;
import java.time.YearMonth;
import java.util.Objects;

@Getter
public class Member {
    private String memberId;
    private String googleId;
    private String email;
    private Instant registeredAt;
    private Role role;

    public Member(
            String memberId,
            String googleId,
            String email,
            Instant registeredAt,
            Role role
    ) {
        this.memberId = Objects.requireNonNull(memberId);
        this.googleId = Objects.requireNonNull(googleId);
        this.email = Objects.requireNonNull(email);
        this.registeredAt = registeredAt;
        this.role = Objects.requireNonNull(role);
    }
}
