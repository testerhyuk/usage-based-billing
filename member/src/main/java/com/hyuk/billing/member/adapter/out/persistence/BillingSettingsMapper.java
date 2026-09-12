package com.hyuk.billing.member.adapter.out.persistence;

import com.hyuk.billing.member.domain.Bank;
import com.hyuk.billing.member.domain.BankAccount;
import com.hyuk.billing.member.domain.BillingSettings;
import com.hyuk.billing.member.domain.WithdrawalDay;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.Objects;

@Component
public class BillingSettingsMapper {
    public BillingSettings toDomain(BillingSettingsJpaEntity entity) {
        Objects.requireNonNull(entity);

        BankAccount bankAccount = new BankAccount(
                Bank.valueOf(entity.getBank()),
                entity.getAccountHolderName(),
                entity.getAccountNumber()
        );

        BankAccount pendingBankAccount = entity.getPendingBank() == null
                ? null
                : new BankAccount(
                    Bank.valueOf(entity.getPendingBank()),
                    entity.getPendingAccountHolderName(),
                    entity.getPendingAccountNumber()
                );

        WithdrawalDay withdrawalDay = entity.getWithdrawalDay() == null
                ? null
                : WithdrawalDay.valueOf(entity.getWithdrawalDay());

        WithdrawalDay pendingWithdrawalDay = entity.getPendingWithdrawalDay() == null
                ? null
                : WithdrawalDay.valueOf(entity.getPendingWithdrawalDay());

        YearMonth effectiveMonth = entity.getEffectiveMonth() == null
                ? null
                : YearMonth.from(entity.getEffectiveMonth());

        return new BillingSettings(
                entity.getMemberId(),
                bankAccount,
                withdrawalDay,
                pendingBankAccount,
                pendingWithdrawalDay,
                effectiveMonth,
                entity.isMonthPerLimit()
        );
    }

    public BillingSettingsJpaEntity toEntity(BillingSettings billingSettings) {
        Objects.requireNonNull(billingSettings);

        BankAccount bankAccount = billingSettings.getBankAccount();
        BankAccount pendingBankAccount = billingSettings.getPendingBankAccount();
        WithdrawalDay pendingWithdrawalDay = billingSettings.getPendingWithdrawalDay();
        YearMonth effectiveMonth = billingSettings.getEffectiveMonth();

        return new BillingSettingsJpaEntity(
                billingSettings.getMemberId(),
                bankAccount.bank().name(),
                bankAccount.accountHolderName(),
                bankAccount.accountNumber(),
                billingSettings.getWithdrawalDay().name(),
                pendingBankAccount == null ? null : pendingBankAccount.bank().name(),
                pendingBankAccount == null ? null : pendingBankAccount.accountHolderName(),
                pendingBankAccount == null ? null : pendingBankAccount.accountNumber(),
                pendingWithdrawalDay == null ? null : pendingWithdrawalDay.name(),
                effectiveMonth == null ? null : effectiveMonth.atDay(1),
                billingSettings.isMonthPerLimit()
        );
    }
}
