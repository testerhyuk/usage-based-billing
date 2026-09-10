package com.hyuk.billing.member.domain;

public record BankAccount(
    Bank bank,
    String accountHolderName,
    String accountNumber
) {
}
