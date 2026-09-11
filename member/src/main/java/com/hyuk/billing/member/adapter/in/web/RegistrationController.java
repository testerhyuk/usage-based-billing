package com.hyuk.billing.member.adapter.in.web;

import com.hyuk.billing.member.adapter.in.RegisterMemberRequest;
import com.hyuk.billing.member.adapter.in.RegistrationResponse;
import com.hyuk.billing.member.adapter.in.security.MemberPrincipal;
import com.hyuk.billing.member.adapter.in.security.SessionAuthenticationManager;
import com.hyuk.billing.member.application.port.in.GoogleIdentity;
import com.hyuk.billing.member.application.port.in.MemberUseCase;
import com.hyuk.billing.member.application.port.in.RegisterMemberCommand;
import com.hyuk.billing.member.application.port.in.RegistrationResult;
import com.hyuk.billing.member.domain.BankAccount;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app.urls.registration}")
@RequiredArgsConstructor
public class RegistrationController {
    private final MemberUseCase memberUseCase;
    private final SessionAuthenticationManager sessionAuthenticationManager;

    @PostMapping
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody RegisterMemberRequest memberRequest,
            @AuthenticationPrincipal MemberPrincipal memberPrincipal,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        BankAccount bankAccount = new BankAccount(
                memberRequest.bank(),
                memberRequest.accountHolderName(),
                memberRequest.accountNumber()
        );

        GoogleIdentity googleIdentity = new GoogleIdentity(
                memberPrincipal.getSubject(),
                memberPrincipal.getEmail()
        );

        RegisterMemberCommand command = new RegisterMemberCommand(
                googleIdentity,
                bankAccount,
                memberRequest.withdrawalDay()
        );

        RegistrationResult member = memberUseCase.register(command);

        sessionAuthenticationManager.promoteToMember(
                member.memberId(),
                request,
                response
        );

        RegistrationResponse httpResponse = new RegistrationResponse(
                member.memberId(),
                member.issuedApiKey().rawApiKey()
        );

        return ResponseEntity.ok(httpResponse);
    }
}
