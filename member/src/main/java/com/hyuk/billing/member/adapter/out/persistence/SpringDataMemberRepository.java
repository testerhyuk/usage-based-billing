package com.hyuk.billing.member.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataMemberRepository extends JpaRepository<MemberJpaEntity, String> {
    Optional<MemberJpaEntity> findByGoogleId(String googleId);
}
