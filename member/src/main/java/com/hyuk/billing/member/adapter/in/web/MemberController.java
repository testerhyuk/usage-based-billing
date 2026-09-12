package com.hyuk.billing.member.adapter.in.web;

import com.hyuk.billing.member.adapter.in.ChangeBankAccountRequest;
import com.hyuk.billing.member.adapter.in.ChangeBillingSettingsRequest;
import com.hyuk.billing.member.adapter.in.MemberResponse;
import com.hyuk.billing.member.adapter.in.security.MemberPrincipal;
import com.hyuk.billing.member.application.port.in.MemberDetails;
import com.hyuk.billing.member.application.port.in.MemberUseCase;
import com.hyuk.billing.member.domain.BankAccount;
import com.hyuk.billing.member.domain.BillingSettings;
import com.hyuk.billing.member.domain.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberUseCase memberUseCase;

    @GetMapping("${app.urls.member-me}")
    public ResponseEntity<MemberResponse> getMe(@AuthenticationPrincipal MemberPrincipal memberPrincipal) {
        MemberDetails memberDetails = memberUseCase.getMember(memberPrincipal.getMemberId());

        Member member = memberDetails.member();
        BillingSettings billingSettings = memberDetails.billingSettings();

        MemberResponse response = new MemberResponse(
                member.getMemberId(),
                member.getEmail(),
                billingSettings.getBankAccount(),
                billingSettings.getWithdrawalDay(),
                billingSettings.getPendingWithdrawalDay(),
                billingSettings.getEffectiveMonth(),
                member.getRegisteredAt(),
                member.getRole()
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("${app.urls.billing-settings}")
    public ResponseEntity<Void> changeBillingSettings(
            @Valid @RequestBody ChangeBillingSettingsRequest request,
            @AuthenticationPrincipal MemberPrincipal memberPrincipal
    ) {
        ChangeBankAccountRequest accountRequest = request.bankAccount();

        BankAccount bankAccount = accountRequest == null
                ? null
                : new BankAccount(
                accountRequest.bank(),
                accountRequest.accountHolderName(),
                accountRequest.accountNumber()
        );

        memberUseCase.changeBillingSettings(
                memberPrincipal.getMemberId(),
                bankAccount,
                request.withdrawalDay()
        );

        return ResponseEntity.ok().build();
    }
}
