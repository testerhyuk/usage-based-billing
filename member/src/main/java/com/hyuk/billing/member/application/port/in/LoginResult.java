package com.hyuk.billing.member.application.port.in;

public record LoginResult(String memberId) {
    public boolean isRegistrationPending() {
        return memberId == null;
    }
}
