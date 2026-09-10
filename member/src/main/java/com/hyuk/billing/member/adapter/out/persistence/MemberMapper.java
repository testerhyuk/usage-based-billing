package com.hyuk.billing.member.adapter.out.persistence;

import com.hyuk.billing.member.domain.Bank;
import com.hyuk.billing.member.domain.BankAccount;
import com.hyuk.billing.member.domain.Member;
import com.hyuk.billing.member.domain.WithdrawalDay;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

public class MemberMapper {
    public Member toDomain(MemberJpaEntity memberJpaEntity) {
        Objects.requireNonNull(memberJpaEntity);

        BankAccount bankAccount = new BankAccount(
                Bank.valueOf(memberJpaEntity.getBank()),
                memberJpaEntity.getAccountHolderName(),
                memberJpaEntity.getAccountNumber()
        );

        WithdrawalDay withdrawalDay = WithdrawalDay.valueOf(memberJpaEntity.getWithdrawalDay());
        WithdrawalDay pendingWithdrawalDay = memberJpaEntity.getPendingWithdrawalDay() == null
                ? null
                : WithdrawalDay.valueOf(memberJpaEntity.getPendingWithdrawalDay());

        YearMonth withdrawalDayEffectiveMonth = memberJpaEntity.getWithdrawalDayEffectiveMonth() == null
                ? null
                : YearMonth.from(memberJpaEntity.getWithdrawalDayEffectiveMonth());

        return new Member(
                memberJpaEntity.getMemberId(),
                memberJpaEntity.getGoogleId(),
                memberJpaEntity.getEmail(),
                bankAccount,
                withdrawalDay,
                pendingWithdrawalDay,
                withdrawalDayEffectiveMonth
        );
    }

    public MemberJpaEntity toEntity(Member member) {
        Objects.requireNonNull(member);

        LocalDate withdrawalDayEffectiveMonth = member.getWithdrawalDayEffectiveMonth() == null
                ? null
                : member.getWithdrawalDayEffectiveMonth().atDay(1);

        String pendingWithdrawalDay = member.getPendingWithdrawalDay() == null
                ? null
                : member.getPendingWithdrawalDay().name();

        return new MemberJpaEntity(
                member.getMemberId(),
                member.getGoogleId(),
                member.getEmail(),
                String.valueOf(member.getBankAccount().bank()),
                String.valueOf(member.getBankAccount().accountHolderName()),
                String.valueOf(member.getBankAccount().accountNumber()),
                String.valueOf(member.getWithdrawalDay()),
                pendingWithdrawalDay,
                withdrawalDayEffectiveMonth
        );
    }
}
