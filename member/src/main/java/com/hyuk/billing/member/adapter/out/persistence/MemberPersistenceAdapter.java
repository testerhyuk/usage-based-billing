package com.hyuk.billing.member.adapter.out.persistence;

import com.hyuk.billing.member.application.port.in.MemberDetails;
import com.hyuk.billing.member.application.port.out.MemberRepositoryPort;
import com.hyuk.billing.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberPersistenceAdapter implements MemberRepositoryPort {
    private final SpringDataMemberRepository springDataMemberRepository;
    private final MemberMapper memberMapper;
    private final BillingSettingsMapper billingSettingsMapper;

    @Override
    public Optional<Member> findByGoogleId(String googleId) {
        return springDataMemberRepository.findByGoogleId(googleId)
                .map(memberMapper::toDomain);
    }

    @Override
    public Optional<MemberDetails> findDetailsById(String memberId) {
        return springDataMemberRepository.findDetailsById(memberId)
                .map(result -> new MemberDetails(
                        memberMapper.toDomain(result.member()),
                        result.billingSettings() == null
                            ? null
                            : billingSettingsMapper.toDomain(result.billingSettings())
                ));
    }

    @Override
    public Member save(Member member) {
        MemberJpaEntity entity = memberMapper.toEntity(member);
        MemberJpaEntity savedEntity = springDataMemberRepository.save(entity);

        return memberMapper.toDomain(savedEntity);
    }
}
