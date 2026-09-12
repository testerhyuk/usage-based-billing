package com.hyuk.billing.member.adapter.out.persistence;

import com.hyuk.billing.member.application.port.in.MemberDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataMemberRepository extends JpaRepository<MemberJpaEntity, String> {
    Optional<MemberJpaEntity> findByGoogleId(String googleId);

    @Query("""
            SELECT new com.hyuk.billing.member.adapter.out.persistence.MemberDetailsJpaResult(m, b)
            FROM MemberJpaEntity m
            LEFT JOIN BillingSettingsJpaEntity b
                ON m.memberId = b.memberId
            WHERE m.memberId = :memberId
            """)
    Optional<MemberDetailsJpaResult> findDetailsById(
            @Param("memberId") String memberId
    );
}
