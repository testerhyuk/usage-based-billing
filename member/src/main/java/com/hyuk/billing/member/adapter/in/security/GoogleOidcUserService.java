package com.hyuk.billing.member.adapter.in.security;

import com.hyuk.billing.member.application.port.in.GoogleIdentity;
import com.hyuk.billing.member.application.port.in.LoginResult;
import com.hyuk.billing.member.application.port.in.MemberUseCase;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoogleOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {
    private final MemberUseCase memberUseCase;
    private final OidcUserService delegate = new OidcUserService();

    @Override
    public @Nullable OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = delegate.loadUser(userRequest);

        GoogleIdentity googleIdentity = new GoogleIdentity(
                oidcUser.getSubject(),
                oidcUser.getEmail()
        );

        LoginResult loginResult = memberUseCase.resolveLogin(googleIdentity);

        MemberRole role;

        if (loginResult.isRegistrationPending()) {
            role = MemberRole.ROLE_REGISTRATION_PENDING;
        } else if (loginResult.admin()) {
            role = MemberRole.ROLE_ADMIN;
        } else {
            role = MemberRole.ROLE_MEMBER;
        }

        return new MemberPrincipal(
                oidcUser,
                loginResult.memberId(),
                role
        );
    }
}
