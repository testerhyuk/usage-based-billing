package com.hyuk.billing.member.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "billing_settings")
@AllArgsConstructor
public class BillingSettingsJpaEntity {
    @Id
    private String memberId;
    private String bank;
    private String accountHolderName;
    private String accountNumber;
    private String withdrawalDay;
    private String pendingBank;
    private String pendingAccountHolderName;
    private String pendingAccountNumber;
    private String pendingWithdrawalDay;
    private LocalDate effectiveMonth;
    private boolean monthPerLimit;
}
