package com.hyuk.billing.member.adapter.in.security;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.urls")
public record SecurityUrlProperties(
        @NotBlank String login,
        @NotBlank String googleLogin,
        @NotBlank String googleCallback,
        @NotBlank String registration,
        @NotBlank String memberMe,
        @NotBlank String billingSettings,
        @NotBlank String adminPattern,
        @NotBlank String registrationPage,
        @NotBlank String memberPage,
        @NotBlank String adminPage,
        @NotBlank String csrf
) {
}
