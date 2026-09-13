package com.hyuk.billing.member.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MemberTest {
    String memberId = "member-1";
    String googleId = "google-1";
    String email = "test@gmail.com";
    Instant registeredAt = LocalDate.of(2026, 9, 12)
            .atStartOfDay(ZoneId.of("Asia/Seoul"))
            .toInstant();
    Role role = Role.ROLE_MEMBER;

    @Test
    void 회원_생성_시_파라미터가_전달한_값으로_생성된다() {
        Member member = new Member(
                memberId,
                googleId,
                email,
                registeredAt,
                role
        );

        assertThat(member.getMemberId()).isEqualTo(memberId);
        assertThat(member.getGoogleId()).isEqualTo(googleId);
        assertThat(member.getEmail()).isEqualTo(email);
        assertThat(member.getRegisteredAt()).isEqualTo(registeredAt);
        assertThat(member.getRole()).isEqualTo(role);
    }

    @Test
    void 회원_생성_시_null이_하나라도_있다면_생성이_거부된다() {
        // memberId만 null
        assertThrows(NullPointerException.class, () -> new Member(
                null,
                googleId,
                email,
                registeredAt,
                role
        ));

        // googleId만 null
        assertThrows(NullPointerException.class, () -> new Member(
                memberId,
                null,
                email,
                registeredAt,
                role
        ));

        // email만 null
        assertThrows(NullPointerException.class, () -> new Member(
                memberId,
                googleId,
                null,
                registeredAt,
                role
        ));

        // registeredAt만 null
        assertThrows(NullPointerException.class, () -> new Member(
                memberId,
                googleId,
                email,
                null,
                role
        ));

        // role만 null
        assertThrows(NullPointerException.class, () -> new Member(
                memberId,
                googleId,
                email,
                registeredAt,
                null
        ));

        // 전부 null
        assertThrows(NullPointerException.class, () -> new Member(
                null,
                null,
                null,
                null,
                null
        ));
    }
}