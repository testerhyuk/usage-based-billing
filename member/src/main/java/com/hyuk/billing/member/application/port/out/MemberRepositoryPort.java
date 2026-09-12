package com.hyuk.billing.member.application.port.out;

import com.hyuk.billing.member.application.port.in.MemberDetails;
import com.hyuk.billing.member.domain.Member;

import java.util.Optional;

public interface MemberRepositoryPort {
    Optional<Member> findByGoogleId(String googleId);
    Optional<MemberDetails> findDetailsById(String memberId);
    Member save(Member member);
}