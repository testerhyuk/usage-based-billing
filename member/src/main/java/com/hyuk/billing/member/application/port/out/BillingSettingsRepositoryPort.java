package com.hyuk.billing.member.application.port.out;

import com.hyuk.billing.member.domain.BillingSettings;

import java.util.Optional;

public interface BillingSettingsRepositoryPort {
    BillingSettings save(BillingSettings billingSettings);
    Optional<BillingSettings> findById(String memberId);
}
