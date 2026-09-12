package com.hyuk.billing.member.application.service;

import com.hyuk.billing.member.application.port.in.*;
import com.hyuk.billing.member.application.port.out.BillingSettingsRepositoryPort;
import com.hyuk.billing.member.application.port.out.IssueInitialApiKeyPort;
import com.hyuk.billing.member.application.port.out.IssuedApiKey;
import com.hyuk.billing.member.application.port.out.MemberRepositoryPort;
import com.hyuk.billing.member.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService implements MemberUseCase {
    private final MemberRepositoryPort memberRepositoryPort;
    private final IssueInitialApiKeyPort apiKeyIssuer;
    private final BillingSettingsRepositoryPort billingSettingsRepositoryPort;

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
                Instant.now(),
                Role.ROLE_MEMBER
        );

        memberRepositoryPort.save(member);

        BankAccount bankAccount = new BankAccount(
                registerMemberCommand.bankAccount().bank(),
                registerMemberCommand.bankAccount().accountHolderName(),
                registerMemberCommand.bankAccount().accountNumber()
        );

        BillingSettings billingSettings = new BillingSettings(
                memberId,
                bankAccount,
                registerMemberCommand.withdrawalDay(),
                null,
                null,
                null,
                false
        );

        billingSettingsRepositoryPort.save(billingSettings);

        IssuedApiKey apiKey = apiKeyIssuer.issueInitialKey(memberId);

        return new RegistrationResult(memberId, apiKey);
    }

    @Override
    public MemberDetails getMember(String memberId) {
        MemberDetails memberDetails = memberRepositoryPort
                .findDetailsById(memberId)
                .orElseThrow(() -> new RuntimeException(
                        "해당 회원 정보가 없습니다 : " + memberId
                ));

        if (memberDetails.billingSettings() == null) {
            throw new IllegalStateException(
                    "계좌 및 출금 설정이 없습니다 : " + memberId
            );
        }

        return memberDetails;
    }

    @Override
    @Transactional
    public void changeBillingSettings(String memberId, BankAccount bankAccount, WithdrawalDay withdrawalDay) {
        LocalDate currentDate = LocalDate.now();

        BillingSettings billingSettings = billingSettingsRepositoryPort.findById(memberId)
                .orElseThrow(() -> new IllegalStateException("계좌 및 출금 설정이 없습니다 : " + memberId));

        billingSettings.changeSettings(bankAccount, withdrawalDay, currentDate);

        billingSettingsRepositoryPort.save(billingSettings);
    }

    @Override
    @Transactional
    public void resetMonthPerLimit(String memberId) {
        MemberDetails memberDetails = getMember(memberId);

        memberDetails.billingSettings().resetMonthPerLimit();

        billingSettingsRepositoryPort.save(memberDetails.billingSettings());
    }
}
