package com.hyuk.billing.member.application.service;

import com.hyuk.billing.member.application.port.in.*;
import com.hyuk.billing.member.application.port.out.IssueInitialApiKeyPort;
import com.hyuk.billing.member.application.port.out.IssuedApiKey;
import com.hyuk.billing.member.application.port.out.MemberRepositoryPort;
import com.hyuk.billing.member.domain.BankAccount;
import com.hyuk.billing.member.domain.Member;
import com.hyuk.billing.member.domain.Role;
import com.hyuk.billing.member.domain.WithdrawalDay;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService implements MemberUseCase {
    private final MemberRepositoryPort memberRepositoryPort;
    private final IssueInitialApiKeyPort apiKeyIssuer;

    @Override
    public LoginResult resolveLogin(GoogleIdentity googleIdentity) {
        Optional<Member> googleInfo = memberRepositoryPort.findByGoogleId(googleIdentity.googleId());

        return new LoginResult(
                googleInfo.map(Member::getMemberId).orElse(null),
                googleInfo.map(member -> member.getRole() == Role.ROLE_ADMIN).orElse(false)
        );
    }

    @Override
    @Transactional
    public RegistrationResult register(RegisterMemberCommand registerMemberCommand) {
        String memberId = UUID.randomUUID().toString();

        Member member = new Member(
                memberId,
                registerMemberCommand.googleIdentity().googleId(),
                registerMemberCommand.googleIdentity().email(),
                registerMemberCommand.bankAccount(),
                registerMemberCommand.withdrawalDay(),
                null,
                null,
                Instant.now(),
                Role.ROLE_MEMBER
        );

        memberRepositoryPort.save(member);

        IssuedApiKey apiKey = apiKeyIssuer.issueInitialKey(memberId);

        return new RegistrationResult(memberId, apiKey);
    }

    @Override
    public Member getMember(String memberId) {
        Optional<Member> member = memberRepositoryPort.findById(memberId);

        if (member.isEmpty()) throw new RuntimeException("해당 회원 정보가 없습니다 : " + memberId);

        return member.get();
    }

    @Override
    public void changeBankAccount(String memberId, BankAccount bankAccount) {
        Member member = getMember(memberId);

        member.changeBankAccount(bankAccount);

        memberRepositoryPort.save(member);
    }

    @Override
    public void changeWithdrawalDay(String memberId, WithdrawalDay withdrawalDay) {
        Member member = getMember(memberId);

        member.changeWithdrawalDay(withdrawalDay, YearMonth.now());

        memberRepositoryPort.save(member);
    }
}
