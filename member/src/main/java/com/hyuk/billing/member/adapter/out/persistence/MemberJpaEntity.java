package com.hyuk.billing.member.adapter.out.persistence;

import com.hyuk.billing.member.domain.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MemberJpaEntity {
    @Id
    private String memberId;
    private String googleId;
    private String email;
    private String bank;
    private String accountHolderName;
    private String accountNumber;
    private String withdrawalDay;
    private String pendingWithdrawalDay;
    private LocalDate withdrawalDayEffectiveMonth;
    private Instant registeredAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
