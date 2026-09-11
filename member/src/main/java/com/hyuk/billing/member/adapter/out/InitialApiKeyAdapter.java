package com.hyuk.billing.member.adapter.out;

import com.hyuk.billing.member.application.port.out.IssueInitialApiKeyPort;
import com.hyuk.billing.member.application.port.out.IssuedApiKey;
import org.springframework.stereotype.Component;

@Component
public class InitialApiKeyAdapter implements IssueInitialApiKeyPort {
    @Override
    public IssuedApiKey issueInitialKey(String memberId) {
        // TODO: API 키 발급 구현
        return new IssuedApiKey("API_KEY");
    }
}
