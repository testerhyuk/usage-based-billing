package com.hyuk.billing.member.adapter.out.persistence;

import com.hyuk.billing.member.application.port.out.MemberRepositoryPort;
import com.hyuk.billing.member.domain.Member;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class MemberPersistenceAdapter implements MemberRepositoryPort {
    private final SpringDataMemberRepository springDataMemberRepository;
    private final MemberMapper memberMapper;

    @Override
    public Optional<Member> findByGoogleId(String googleId) {
        return springDataMemberRepository.findByGoogleId(googleId)
                .map(memberMapper::toDomain);
    }

    @Override
    public Optional<Member> findById(String memberId) {
        return springDataMemberRepository.findById(memberId)
                .map(memberMapper::toDomain);
    }

    @Override
    public Member save(Member member) {
        MemberJpaEntity entity = memberMapper.toEntity(member);
        MemberJpaEntity savedEntity = springDataMemberRepository.save(entity);

        return memberMapper.toDomain(savedEntity);
    }
}
