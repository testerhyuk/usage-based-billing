package com.hyuk.billing.member.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataBillingSettingsRepository extends JpaRepository<BillingSettingsJpaEntity, String> {
}
