package com.hyuk.billing.member.adapter.out.persistence;

import com.hyuk.billing.member.domain.Member;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class MemberPersistenceAdapter {
    private final SpringDataMemberRepository springDataMemberRepository;
    private final MemberMapper memberMapper;

    public Optional<Member> findByGoogleId(String googleId) {
        return springDataMemberRepository.findByGoogleId(googleId)
                .map(memberMapper::toDomain);
    }
}
