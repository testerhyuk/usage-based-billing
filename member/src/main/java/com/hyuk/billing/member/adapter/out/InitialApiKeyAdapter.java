package com.hyuk.billing.member.adapter.out;

import com.hyuk.billing.member.application.port.out.IssueInitialApiKeyPort;
import com.hyuk.billing.member.application.port.out.IssuedApiKey;

public class InitialApiKeyAdapter implements IssueInitialApiKeyPort {
    @Override
    public IssuedApiKey issueInitialKey(String memberId) {
        // TODO: API 키 발급 구현
        throw new UnsupportedOperationException("API 키 발급 연동 미구현");
    }
}
