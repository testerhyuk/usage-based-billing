package com.hyuk.billing.member.application.port.out;

import com.hyuk.billing.member.domain.Member;

import java.util.Optional;

public interface MemberRepositoryPort {
    Optional<Member> findByGoogleId(String googleId);
    Optional<Member> findById(String memberId);
    Member save(Member member);
}