package com.hyuk.billing.member.adapter.in.web;

import com.hyuk.billing.member.adapter.in.ChangeBankAccountRequest;
import com.hyuk.billing.member.adapter.in.ChangeWithdrawalDayRequest;
import com.hyuk.billing.member.adapter.in.MemberResponse;
import com.hyuk.billing.member.adapter.in.security.MemberPrincipal;
import com.hyuk.billing.member.application.port.in.MemberUseCase;
import com.hyuk.billing.member.domain.BankAccount;
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
        Member member = memberUseCase.getMember(memberPrincipal.getMemberId());

        MemberResponse response = new MemberResponse(
                member.getMemberId(),
                member.getEmail(),
                member.getBankAccount(),
                member.getWithdrawalDay(),
                member.getPendingWithdrawalDay(),
                member.getWithdrawalDayEffectiveMonth(),
                member.getRegisteredAt(),
                member.getRole()
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("${app.urls.bank-account}")
    public ResponseEntity<Void> changeBankAccount(
            @Valid @RequestBody ChangeBankAccountRequest request,
            @AuthenticationPrincipal MemberPrincipal memberPrincipal
    ) {
        BankAccount bankAccount = new BankAccount(
                request.bank(),
                request.accountHolderName(),
                request.accountNumber()
        );

        memberUseCase.changeBankAccount(
                memberPrincipal.getMemberId(),
                bankAccount);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("${app.urls.withdrawal-day}")
    public ResponseEntity<Void> changeWithdrawalDay(
            @Valid @RequestBody ChangeWithdrawalDayRequest request,
            @AuthenticationPrincipal MemberPrincipal memberPrincipal
    ) {
        memberUseCase.changeWithdrawalDay(
                memberPrincipal.getMemberId(),
                request.withdrawalDay()
        );

        return ResponseEntity.ok().build();
    }
}
