package com.hyuk.billing.member.adapter.in.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class SessionAuthenticationManager {
    private final SecurityContextRepository securityContextRepository;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;

    public void promoteToMember(
            String memberId,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws AccessDeniedException {
        Objects.requireNonNull(memberId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof OAuth2AuthenticationToken oAuth2Authentication)
                || !oAuth2Authentication.isAuthenticated()
                || !(oAuth2Authentication.getPrincipal() instanceof MemberPrincipal principal)
                || !principal.isRegistrationPending()
        ) {
            throw new AccessDeniedException("구글 인증을 완료한 회원가입 대기 사용자만 처리할 수 있습니다");
        }

        MemberPrincipal memberPrincipal = new MemberPrincipal(
                principal,
                memberId,
                MemberRole.ROLE_MEMBER
        );

        OAuth2AuthenticationToken memberAuthentication = new OAuth2AuthenticationToken(
                memberPrincipal,
                memberPrincipal.getAuthorities(),
                oAuth2Authentication.getAuthorizedClientRegistrationId()
        );

        memberAuthentication.setDetails(oAuth2Authentication.getDetails());

        sessionAuthenticationStrategy.onAuthentication(memberAuthentication, request, response);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(memberAuthentication);

        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, request, response);
    }
}
