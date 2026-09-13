package com.hyuk.billing.member.adapter.out.persistence;

import com.hyuk.billing.member.domain.Member;
import com.hyuk.billing.member.domain.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberMapperTest {
    private final MemberMapper memberMapper = new MemberMapper();

    // toEntity() 테스트
    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ROLE_MEMBER", "ROLE_ADMIN"})
    void 회원의_모든_필드와_과거_가입일을_엔티티에_그대로_매핑한다(Role role) {
        // given
        String memberId = "member-domain-1";
        String googleId = "google-domain-1";
        String email = "domain@example.com";
        Instant registeredAt = Instant.parse("2020-03-15T12:34:56.123456789Z");
        Member member = new Member(memberId, googleId, email, registeredAt, role);

        // when
        MemberJpaEntity entity = memberMapper.toEntity(member);

        // then
        assertThat(entity.getMemberId()).isEqualTo(memberId);
        assertThat(entity.getGoogleId()).isEqualTo(googleId);
        assertThat(entity.getEmail()).isEqualTo(email);
        assertThat(entity.getRegisteredAt()).isEqualTo(registeredAt);
        assertThat(entity.getRole()).isEqualTo(role);
    }

    @Test
    void 회원이_null이면_엔티티_변환_시_NullPointerException이_발생한다() {
        // given
        Member member = null;

        // when & then
        assertThatThrownBy(() -> memberMapper.toEntity(member))
                .isInstanceOf(NullPointerException.class);
    }

    // toDomain() 테스트
    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ROLE_MEMBER", "ROLE_ADMIN"})
    void 엔티티의_모든_필드와_과거_가입일을_회원에_그대로_매핑한다(Role role) {
        // given
        String memberId = "member-entity-2";
        String googleId = "google-entity-2";
        String email = "entity@example.com";
        Instant registeredAt = Instant.parse("2019-07-08T09:10:11.987654321Z");
        MemberJpaEntity entity = new MemberJpaEntity(memberId, googleId, email, registeredAt, role);

        // when
        Member member = memberMapper.toDomain(entity);

        // then
        assertThat(member.getMemberId()).isEqualTo(memberId);
        assertThat(member.getGoogleId()).isEqualTo(googleId);
        assertThat(member.getEmail()).isEqualTo(email);
        assertThat(member.getRegisteredAt()).isEqualTo(registeredAt);
        assertThat(member.getRole()).isEqualTo(role);
    }

    @Test
    void 엔티티가_null이면_회원_변환_시_NullPointerException이_발생한다() {
        // given
        MemberJpaEntity entity = null;

        // when & then
        assertThatThrownBy(() -> memberMapper.toDomain(entity))
                .isInstanceOf(NullPointerException.class);
    }
}
