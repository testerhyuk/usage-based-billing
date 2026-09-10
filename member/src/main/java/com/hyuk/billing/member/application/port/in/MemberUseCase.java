package com.hyuk.billing.member.application.port.in;

import com.hyuk.billing.member.domain.BankAccount;
import com.hyuk.billing.member.domain.Member;
import com.hyuk.billing.member.domain.WithdrawalDay;

public interface MemberUseCase {
    LoginResult resolveLogin(GoogleIdentity googleIdentity);
    RegistrationResult register(RegisterMemberCommand registerMemberCommand);
    Member getMember(String memberId);
    void changeBankAccount(String memberId, BankAccount bankAccount);
    void changeWithdrawalDay(String memberId, WithdrawalDay withdrawalDay);
}
