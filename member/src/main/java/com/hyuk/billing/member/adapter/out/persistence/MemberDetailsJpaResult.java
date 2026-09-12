package com.hyuk.billing.member.adapter.out.persistence;

public record MemberDetailsJpaResult(
        MemberJpaEntity member,
        BillingSettingsJpaEntity billingSettings
) {
}
