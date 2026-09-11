package com.hyuk.billing.member.adapter.in;

public record CsrfResponse(
        String headerName,
        String token
) {
}
