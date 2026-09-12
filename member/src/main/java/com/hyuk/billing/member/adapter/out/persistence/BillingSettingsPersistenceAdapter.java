package com.hyuk.billing.member.adapter.out.persistence;

import com.hyuk.billing.member.application.port.out.BillingSettingsRepositoryPort;
import com.hyuk.billing.member.domain.BillingSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BillingSettingsPersistenceAdapter implements BillingSettingsRepositoryPort {
    private final SpringDataBillingSettingsRepository billingSettingsRepository;
    private final BillingSettingsMapper billingSettingsMapper;

    @Override
    public BillingSettings save(BillingSettings billingSettings) {
        BillingSettingsJpaEntity entity =  billingSettingsMapper.toEntity(billingSettings);
        BillingSettingsJpaEntity savedEntity = billingSettingsRepository.save(entity);

        return billingSettingsMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<BillingSettings> findById(String memberId) {
        return billingSettingsRepository.findById(memberId)
                .map(billingSettingsMapper::toDomain);
    }
}
