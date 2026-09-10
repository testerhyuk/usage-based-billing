package com.hyuk.billing.member.application.port.out;

public interface IssueInitialApiKeyPort {
    IssuedApiKey issueInitialKey(String memberId);
}
