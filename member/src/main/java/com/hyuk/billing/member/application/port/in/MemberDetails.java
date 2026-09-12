package com.hyuk.billing.member.application.port.in;

import com.hyuk.billing.member.domain.BillingSettings;
import com.hyuk.billing.member.domain.Member;

public record MemberDetails(
        Member member,
        BillingSettings billingSettings
) {
}
