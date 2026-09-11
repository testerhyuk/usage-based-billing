package com.hyuk.billing.member.adapter.in.security;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MemberPrincipal implements OidcUser {
    private final OidcUser oidcUser;
    @Getter
    private final String memberId;
    private final MemberRole memberRole;

    public MemberPrincipal(OidcUser oidcUser, String memberId, MemberRole memberRole) {
        this.oidcUser = Objects.requireNonNull(oidcUser);
        this.memberId = memberId;
        this.memberRole = Objects.requireNonNull(memberRole);
    }

    public boolean isRegistrationPending() {
        return memberId == null;
    }

    @Override
    public Map<String, Object> getClaims() {
        return oidcUser.getClaims();
    }

    @Override
    public @Nullable OidcUserInfo getUserInfo() {
        return oidcUser.getUserInfo();
    }

    @Override
    public OidcIdToken getIdToken() {
        return oidcUser.getIdToken();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oidcUser.getAttributes();
    }

    @Override
    public String getName() {
        return oidcUser.getName();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(memberRole.name()));
    }
}
