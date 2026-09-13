package com.hyuk.billing.member.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class BillingSettingsTest {
    String memberId = "member-1";
    BankAccount bankAccount = new BankAccount(Bank.KB, "hyuk", "123-456");
    WithdrawalDay withdrawalDay = WithdrawalDay.DAY_5;
    BankAccount pendingBankAccount = new BankAccount(Bank.HANA, "hyuk", "789-456");
    WithdrawalDay pendingWithdrawalDay = WithdrawalDay.DAY_15;
    YearMonth effectiveMonth = YearMonth.of(2026, 10);
    boolean monthPerLimit = false;

    @Test
    void 계좌_및_출금_예정일_생성_시_전달한_값으로_생성된다() {
        BillingSettings billingSettings = new BillingSettings(
                memberId,
                bankAccount,
                withdrawalDay,
                pendingBankAccount,
                pendingWithdrawalDay,
                effectiveMonth,
                true
        );

        assertThat(billingSettings.getMemberId()).isEqualTo(memberId);
        assertThat(billingSettings.getBankAccount()).isEqualTo(bankAccount);
        assertThat(billingSettings.getWithdrawalDay()).isEqualTo(withdrawalDay);
        assertThat(billingSettings.getPendingBankAccount()).isEqualTo(pendingBankAccount);
        assertThat(billingSettings.getPendingWithdrawalDay()).isEqualTo(pendingWithdrawalDay);
        assertThat(billingSettings.getEffectiveMonth()).isEqualTo(effectiveMonth);
        assertThat(billingSettings.isMonthPerLimit()).isTrue();
    }

    @Test
    void memberId_bankAccount_withdrawalDay가_각각_null이면_생성을_거부한다() {
        // memberId만 null
        assertThrows(NullPointerException.class, () -> new BillingSettings(
                null,
                bankAccount,
                withdrawalDay,
                pendingBankAccount,
                pendingWithdrawalDay,
                effectiveMonth,
                monthPerLimit
        ));

        // bankAccount만 null
        assertThrows(NullPointerException.class, () -> new BillingSettings(
                memberId,
                null,
                withdrawalDay,
                pendingBankAccount,
                pendingWithdrawalDay,
                effectiveMonth,
                monthPerLimit
        ));

        // withdrawalDay만 null
        assertThrows(NullPointerException.class, () -> new BillingSettings(
                memberId,
                bankAccount,
                null,
                pendingBankAccount,
                pendingWithdrawalDay,
                effectiveMonth,
                monthPerLimit
        ));
    }

    @Test
    void 변경_예약이_없다면_pendingXXX와_effectiveMonth가_null이어도_생성_가능() {
        assertDoesNotThrow(() -> new BillingSettings(
                memberId,
                bankAccount,
                withdrawalDay,
                null,
                null,
                null,
                monthPerLimit
        ));
    }

    // changeSettings() 테스트

    @Test
    void 계좌만_변경하면_새_계좌는_pendingBankAccount에_저장되고_pendingWithdrawalDay는_null() {
        BillingSettings billingSettings = createInitialBillingSettings();

        BankAccount changedBankAccount = new BankAccount(Bank.WOORI, "hyuk", "159-753");

        billingSettings.changeSettings(changedBankAccount, null, LocalDate.of(2026, 9, 10));

        assertThat(billingSettings.getPendingBankAccount()).isEqualTo(changedBankAccount);
        assertThat(billingSettings.getPendingWithdrawalDay()).isNull();
        assertThat(billingSettings.getWithdrawalDay()).isEqualTo(withdrawalDay);
    }

    @Test
    void 출금_예정일만_변경하면_예정일이_pendingWithdrawalDay에_저장되고_pendingBankAccount는_null() {
        BillingSettings billingSettings = createInitialBillingSettings();

        WithdrawalDay changedWithdrawalDay = WithdrawalDay.DAY_25;

        billingSettings.changeSettings(null, changedWithdrawalDay, LocalDate.of(2026, 9, 10));

        assertThat(billingSettings.getPendingWithdrawalDay()).isEqualTo(changedWithdrawalDay);
        assertThat(billingSettings.getPendingBankAccount()).isNull();
    }

    @Test
    void 출금_예정일과_계좌_모두_변경하면_계좌와_출금_예정일의_예약값이_모두_저장된다() {
        BillingSettings billingSettings = createInitialBillingSettings();

        BankAccount changedBankAccount = new BankAccount(Bank.WOORI, "hyuk", "159-753");
        WithdrawalDay changedWithdrawalDay = WithdrawalDay.DAY_25;

        billingSettings.changeSettings(changedBankAccount, changedWithdrawalDay, LocalDate.of(2026, 9, 10));

        assertThat(billingSettings.getPendingWithdrawalDay()).isEqualTo(changedWithdrawalDay);
        assertThat(billingSettings.getPendingBankAccount()).isEqualTo(changedBankAccount);
        assertThat(billingSettings.getWithdrawalDay()).isEqualTo(withdrawalDay);
        assertThat(billingSettings.getBankAccount()).isEqualTo(bankAccount);
        assertThat(billingSettings.getMemberId()).isEqualTo(memberId);
    }

    @Test
    void 변경할_계좌와_출금_예정일이_모두_null이면_거부된다() {
        BillingSettings billingSettings = createInitialBillingSettings();

        assertThrows(IllegalArgumentException.class, () ->
                billingSettings.changeSettings(
                        null,
                        null,
                        LocalDate.of(2026, 9, 10)));

        assertThat(billingSettings.getMemberId()).isEqualTo(memberId);
        assertThat(billingSettings.getBankAccount()).isEqualTo(bankAccount);
        assertThat(billingSettings.getWithdrawalDay()).isEqualTo(withdrawalDay);
        assertThat(billingSettings.getPendingBankAccount()).isNull();
        assertThat(billingSettings.getPendingWithdrawalDay()).isNull();
        assertThat(billingSettings.getEffectiveMonth()).isNull();
        assertThat(billingSettings.isMonthPerLimit()).isFalse();
    }

    @Test
    void 기준_날짜인_currentDate가_null이면_거부된다() {
        BillingSettings billingSettings = createInitialBillingSettings();

        BankAccount changedBankAccount = new BankAccount(Bank.WOORI, "hyuk", "159-753");
        WithdrawalDay changedWithdrawalDay = WithdrawalDay.DAY_25;

        assertThrows(NullPointerException.class, () -> billingSettings.changeSettings(
                changedBankAccount,
                changedWithdrawalDay,
                null
        ));

        assertThat(billingSettings.getMemberId()).isEqualTo(memberId);
        assertThat(billingSettings.getBankAccount()).isEqualTo(bankAccount);
        assertThat(billingSettings.getWithdrawalDay()).isEqualTo(withdrawalDay);
        assertThat(billingSettings.getPendingBankAccount()).isNull();
        assertThat(billingSettings.getPendingWithdrawalDay()).isNull();
        assertThat(billingSettings.getEffectiveMonth()).isNull();
        assertThat(billingSettings.isMonthPerLimit()).isFalse();
    }

    @Test
    void 변경_기회가_남아_있어도_26일부터_말일까지의_변경_신청은_거부된다() {
        BillingSettings billingSettings = createInitialBillingSettings();

        BankAccount changedBankAccount = new BankAccount(Bank.WOORI, "hyuk", "159-753");
        WithdrawalDay changedWithdrawalDay = WithdrawalDay.DAY_25;

        assertThrows(IllegalArgumentException.class, () -> billingSettings.changeSettings(
                changedBankAccount,
                changedWithdrawalDay,
                LocalDate.of(2026, 9, 26)
        ));

        assertThat(billingSettings.getMemberId()).isEqualTo(memberId);
        assertThat(billingSettings.getBankAccount()).isEqualTo(bankAccount);
        assertThat(billingSettings.getWithdrawalDay()).isEqualTo(withdrawalDay);
        assertThat(billingSettings.getPendingBankAccount()).isNull();
        assertThat(billingSettings.getPendingWithdrawalDay()).isNull();
        assertThat(billingSettings.getEffectiveMonth()).isNull();
        assertThat(billingSettings.isMonthPerLimit()).isFalse();

        assertThrows(IllegalArgumentException.class, () -> billingSettings.changeSettings(
                changedBankAccount,
                changedWithdrawalDay,
                LocalDate.of(2026, 9, 30)
        ));

        assertThat(billingSettings.getMemberId()).isEqualTo(memberId);
        assertThat(billingSettings.getBankAccount()).isEqualTo(bankAccount);
        assertThat(billingSettings.getWithdrawalDay()).isEqualTo(withdrawalDay);
        assertThat(billingSettings.getPendingBankAccount()).isNull();
        assertThat(billingSettings.getPendingWithdrawalDay()).isNull();
        assertThat(billingSettings.getEffectiveMonth()).isNull();
        assertThat(billingSettings.isMonthPerLimit()).isFalse();

        assertDoesNotThrow(() -> billingSettings.changeSettings(
                changedBankAccount,
                changedWithdrawalDay,
                LocalDate.of(2026, 9, 1)
        ));
    }

    @Test
    void 변경_적용_월은_전달한_날짜의_다음_달로_저장된다() {
        BillingSettings billingSettings = createInitialBillingSettings();

        BankAccount changedBankAccount = new BankAccount(Bank.WOORI, "hyuk", "159-753");
        WithdrawalDay changedWithdrawalDay = WithdrawalDay.DAY_25;

        billingSettings.changeSettings(
                changedBankAccount,
                changedWithdrawalDay,
                LocalDate.of(2026, 9, 25)
        );

        assertThat(billingSettings.getEffectiveMonth())
                .isEqualTo(YearMonth.of(2026, 10));
    }

    @Test
    void _12월에_변경하면_적용일은_다음_해_1월이_된다() {
        BillingSettings billingSettings = createInitialBillingSettings();

        BankAccount changedBankAccount = new BankAccount(Bank.WOORI, "hyuk", "159-753");
        WithdrawalDay changedWithdrawalDay = WithdrawalDay.DAY_25;

        billingSettings.changeSettings(
                changedBankAccount,
                changedWithdrawalDay,
                LocalDate.of(2026, 12, 25)
        );

        assertThat(billingSettings.getEffectiveMonth())
                .isEqualTo(YearMonth.of(2027, 1));
    }

    @Test
    void 변경_성공_시_monthPerLimit이_true로_설정된다() {
        BillingSettings billingSettings = createInitialBillingSettings();

        BankAccount changedBankAccount = new BankAccount(Bank.WOORI, "hyuk", "159-753");
        WithdrawalDay changedWithdrawalDay = WithdrawalDay.DAY_25;

        billingSettings.changeSettings(
                changedBankAccount,
                changedWithdrawalDay,
                LocalDate.of(2026, 9, 25)
        );

        assertThat(billingSettings.isMonthPerLimit()).isTrue();
    }

    @Test
    void monthPerLimit이_true라면_계좌_혹은_예정일_변경이_거부되며_기존_설정은_유지된다() {
        BillingSettings billingSettings = createInitialBillingSettingsMonthPerLimitTrue();

        BankAccount changedBankAccount = new BankAccount(Bank.WOORI, "hyuk", "159-753");
        WithdrawalDay changedWithdrawalDay = WithdrawalDay.DAY_25;

        // 계좌 변경 및 출금 예정일 전부 존재할 때
        assertThrows(IllegalStateException.class, () -> billingSettings.changeSettings(
                changedBankAccount,
                changedWithdrawalDay,
                LocalDate.of(2026, 9, 25)
        ));

        assertThat(billingSettings.getBankAccount()).isEqualTo(bankAccount);
        assertThat(billingSettings.getWithdrawalDay()).isEqualTo(withdrawalDay);
        assertThat(billingSettings.getPendingBankAccount()).isEqualTo(pendingBankAccount);
        assertThat(billingSettings.getPendingWithdrawalDay()).isEqualTo(pendingWithdrawalDay);
        assertThat(billingSettings.getEffectiveMonth()).isEqualTo(YearMonth.of(2026, 10));
        assertThat(billingSettings.getMemberId()).isEqualTo(memberId);
        assertThat(billingSettings.isMonthPerLimit()).isTrue();

        // 계좌 변경은 null일 때
        assertThrows(IllegalStateException.class, () -> billingSettings.changeSettings(
                null,
                changedWithdrawalDay,
                LocalDate.of(2026, 9, 25)
        ));

        assertThat(billingSettings.getBankAccount()).isEqualTo(bankAccount);
        assertThat(billingSettings.getWithdrawalDay()).isEqualTo(withdrawalDay);
        assertThat(billingSettings.getPendingBankAccount()).isEqualTo(pendingBankAccount);
        assertThat(billingSettings.getPendingWithdrawalDay()).isEqualTo(pendingWithdrawalDay);
        assertThat(billingSettings.getEffectiveMonth()).isEqualTo(YearMonth.of(2026, 10));
        assertThat(billingSettings.getMemberId()).isEqualTo(memberId);
        assertThat(billingSettings.isMonthPerLimit()).isTrue();

        // 출금 예정일은 null일 때
        assertThrows(IllegalStateException.class, () -> billingSettings.changeSettings(
                changedBankAccount,
                null,
                LocalDate.of(2026, 9, 25)
        ));

        assertThat(billingSettings.getBankAccount()).isEqualTo(bankAccount);
        assertThat(billingSettings.getWithdrawalDay()).isEqualTo(withdrawalDay);
        assertThat(billingSettings.getPendingBankAccount()).isEqualTo(pendingBankAccount);
        assertThat(billingSettings.getPendingWithdrawalDay()).isEqualTo(pendingWithdrawalDay);
        assertThat(billingSettings.getEffectiveMonth()).isEqualTo(YearMonth.of(2026, 10));
        assertThat(billingSettings.getMemberId()).isEqualTo(memberId);
        assertThat(billingSettings.isMonthPerLimit()).isTrue();
    }

    // resetMonthPerLimit() 테스트

    @Test
    void monthPerLimit가_true인_설정을_초기화하면_false가_된다() {
        BillingSettings billingSettings = createInitialBillingSettingsMonthPerLimitTrue();

        billingSettings.resetMonthPerLimit();

        assertThat(billingSettings.isMonthPerLimit()).isFalse();
        assertThat(billingSettings.getBankAccount()).isEqualTo(bankAccount);
        assertThat(billingSettings.getWithdrawalDay()).isEqualTo(withdrawalDay);
        assertThat(billingSettings.getPendingBankAccount()).isEqualTo(pendingBankAccount);
        assertThat(billingSettings.getPendingWithdrawalDay()).isEqualTo(pendingWithdrawalDay);
        assertThat(billingSettings.getEffectiveMonth()).isEqualTo(YearMonth.of(2026, 10));
        assertThat(billingSettings.getMemberId()).isEqualTo(memberId);
    }

    @Test
    void 이미_monthPerLimit이_false인_상태에서_초기화_해도_false가_유지된다() {
        BillingSettings billingSettings = createInitialBillingSettings();
        billingSettings.resetMonthPerLimit();
        assertThat(billingSettings.isMonthPerLimit()).isFalse();
    }

    private BillingSettings createInitialBillingSettings() {
        return new BillingSettings(
                memberId,
                bankAccount,
                withdrawalDay,
                null,
                null,
                null,
                false
        );
    }

    private BillingSettings createInitialBillingSettingsMonthPerLimitTrue() {
        return new BillingSettings(
                memberId,
                bankAccount,
                withdrawalDay,
                pendingBankAccount,
                pendingWithdrawalDay,
                effectiveMonth,
                true
        );
    }
}