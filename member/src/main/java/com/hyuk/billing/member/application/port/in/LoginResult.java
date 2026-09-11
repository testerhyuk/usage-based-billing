package com.hyuk.billing.member.application.port.in;

public record LoginResult(String memberId, boolean admin) {
    public boolean isRegistrationPending() {
        return memberId == null;
    }
}
