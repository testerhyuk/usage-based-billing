package com.hyuk.billing.member.adapter.out.persistence;

import com.hyuk.billing.member.domain.Bank;
import com.hyuk.billing.member.domain.BankAccount;
import com.hyuk.billing.member.domain.Member;
import com.hyuk.billing.member.domain.WithdrawalDay;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Objects;

@Component
public class MemberMapper {
    public Member toDomain(MemberJpaEntity memberJpaEntity) {
        Objects.requireNonNull(memberJpaEntity);

        return new Member(
                memberJpaEntity.getMemberId(),
                memberJpaEntity.getGoogleId(),
                memberJpaEntity.getEmail(),
                memberJpaEntity.getRegisteredAt(),
                memberJpaEntity.getRole()
        );
    }

    public MemberJpaEntity toEntity(Member member) {
        Objects.requireNonNull(member);

        return new MemberJpaEntity(
                member.getMemberId(),
                member.getGoogleId(),
                member.getEmail(),
                member.getRegisteredAt(),
                member.getRole()
        );
    }
}
