package com.hyuk.billing.member.application.port.in;

import com.hyuk.billing.member.domain.BankAccount;
import com.hyuk.billing.member.domain.BillingSettings;
import com.hyuk.billing.member.domain.Member;
import com.hyuk.billing.member.domain.WithdrawalDay;

public interface MemberUseCase {
    LoginResult resolveLogin(GoogleIdentity googleIdentity);
    RegistrationResult register(RegisterMemberCommand registerMemberCommand);
    MemberDetails getMember(String memberId);
    void resetMonthPerLimit(String memberId);
    void changeBillingSettings(String memberId, BankAccount bankAccount, WithdrawalDay withdrawalDay);
}
