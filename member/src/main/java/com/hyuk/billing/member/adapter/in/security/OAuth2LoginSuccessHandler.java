package com.hyuk.billing.member.adapter.in.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final SecurityUrlProperties urls;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();

        if (principal.isRegistrationPending()) {
            response.sendRedirect(urls.registrationPage());
            return;
        }

        boolean admin = authentication.getAuthorities()
                .stream()
                .anyMatch(a ->
                        MemberRole.ROLE_ADMIN.name().equals(a.getAuthority())
                );

        if (admin) {
            response.sendRedirect(urls.adminPage());
            return;
        }

        response.sendRedirect(urls.memberPage());
    }
}
