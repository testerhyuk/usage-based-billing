package com.hyuk.billing.member.adapter.out.persistence;

import com.hyuk.billing.member.domain.Bank;
import com.hyuk.billing.member.domain.BankAccount;
import com.hyuk.billing.member.domain.BillingSettings;
import com.hyuk.billing.member.domain.WithdrawalDay;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BillingSettingsMapperTest {
    private static final String MEMBER_ID = "member-1";
    private static final BankAccount BANK_ACCOUNT = new BankAccount(Bank.KB, "현재 예금주", "123-456");
    private static final BankAccount PENDING_BANK_ACCOUNT = new BankAccount(Bank.HANA, "예약 예금주", "789-012");
    private static final WithdrawalDay WITHDRAWAL_DAY = WithdrawalDay.DAY_5;
    private static final WithdrawalDay PENDING_WITHDRAWAL_DAY = WithdrawalDay.DAY_25;
    private static final YearMonth EFFECTIVE_MONTH = YearMonth.of(2026, 10);

    private final BillingSettingsMapper mapper = new BillingSettingsMapper();

    // toEntity() 테스트

    @ParameterizedTest(name = "{0}, monthPerLimit={4}")
    @MethodSource("reservationCases")
    void 예약_조합과_월_변경_제한에_관계없이_모든_필드를_엔티티로_변환한다(
            String reservationType,
            BankAccount pendingBankAccount,
            WithdrawalDay pendingWithdrawalDay,
            YearMonth effectiveMonth,
            boolean monthPerLimit
    ) {
        // given
        BillingSettings settings = new BillingSettings(
                MEMBER_ID, BANK_ACCOUNT, WITHDRAWAL_DAY,
                pendingBankAccount, pendingWithdrawalDay, effectiveMonth, monthPerLimit
        );

        // when
        BillingSettingsJpaEntity entity = mapper.toEntity(settings);

        // then
        assertThat(entity.getMemberId()).isEqualTo(MEMBER_ID);
        assertThat(entity.getBank()).isEqualTo("KB");
        assertThat(entity.getAccountHolderName()).isEqualTo("현재 예금주");
        assertThat(entity.getAccountNumber()).isEqualTo("123-456");
        assertThat(entity.getWithdrawalDay()).isEqualTo("DAY_5");
        assertThat(entity.getPendingBank()).isEqualTo(pendingBankAccount == null ? null : "HANA");
        assertThat(entity.getPendingAccountHolderName()).isEqualTo(pendingBankAccount == null ? null : "예약 예금주");
        assertThat(entity.getPendingAccountNumber()).isEqualTo(pendingBankAccount == null ? null : "789-012");
        assertThat(entity.getPendingWithdrawalDay()).isEqualTo(pendingWithdrawalDay == null ? null : "DAY_25");
        assertThat(entity.getEffectiveMonth()).isEqualTo(effectiveMonth == null ? null : LocalDate.of(2026, 10, 1));
        assertThat(entity.isMonthPerLimit()).isEqualTo(monthPerLimit);
    }

    @Test
    void 도메인이_null이면_엔티티_변환을_거부한다() {
        // given
        BillingSettings settings = null;

        // when & then
        assertThatThrownBy(() -> mapper.toEntity(settings)).isInstanceOf(NullPointerException.class);
    }

    // toDomain() 테스트

    @ParameterizedTest(name = "{0}, monthPerLimit={4}")
    @MethodSource("reservationCases")
    void 예약_조합과_월_변경_제한에_관계없이_모든_필드를_도메인으로_변환한다(
            String reservationType,
            BankAccount pendingBankAccount,
            WithdrawalDay pendingWithdrawalDay,
            YearMonth effectiveMonth,
            boolean monthPerLimit
    ) {
        // given
        BillingSettingsJpaEntity entity = new BillingSettingsJpaEntity(
                MEMBER_ID, "KB", "현재 예금주", "123-456", "DAY_5",
                pendingBankAccount == null ? null : "HANA",
                pendingBankAccount == null ? null : "예약 예금주",
                pendingBankAccount == null ? null : "789-012",
                pendingWithdrawalDay == null ? null : "DAY_25",
                effectiveMonth == null ? null : LocalDate.of(2026, 10, 1),
                monthPerLimit
        );

        // when
        BillingSettings settings = mapper.toDomain(entity);

        // then
        assertThat(settings.getMemberId()).isEqualTo(MEMBER_ID);
        assertThat(settings.getBankAccount()).isEqualTo(BANK_ACCOUNT);
        assertThat(settings.getWithdrawalDay()).isEqualTo(WITHDRAWAL_DAY);
        assertThat(settings.getPendingBankAccount()).isEqualTo(pendingBankAccount);
        assertThat(settings.getPendingWithdrawalDay()).isEqualTo(pendingWithdrawalDay);
        assertThat(settings.getEffectiveMonth()).isEqualTo(effectiveMonth);
        assertThat(settings.isMonthPerLimit()).isEqualTo(monthPerLimit);
    }

    @Test
    void 엔티티가_null이면_도메인_변환을_거부한다() {
        // given
        BillingSettingsJpaEntity entity = null;

        // when & then
        assertThatThrownBy(() -> mapper.toDomain(entity)).isInstanceOf(NullPointerException.class);
    }

    // 양방향 변환 테스트

    @ParameterizedTest
    @EnumSource(Bank.class)
    void 모든_은행은_현재와_예약_계좌에서_문자열로_저장되고_enum으로_복원된다(Bank bank) {
        // given
        BankAccount currentAccount = new BankAccount(bank, "현재 예금주", "123-456");
        BankAccount pendingAccount = new BankAccount(bank, "예약 예금주", "789-012");
        BillingSettings original = new BillingSettings(
                MEMBER_ID, currentAccount, WITHDRAWAL_DAY,
                pendingAccount, PENDING_WITHDRAWAL_DAY, EFFECTIVE_MONTH, true
        );

        // when
        BillingSettingsJpaEntity entity = mapper.toEntity(original);
        BillingSettings restored = mapper.toDomain(entity);

        // then
        assertThat(entity.getBank()).isEqualTo(bank.name());
        assertThat(entity.getPendingBank()).isEqualTo(bank.name());
        assertThat(restored.getBankAccount()).isEqualTo(currentAccount);
        assertThat(restored.getPendingBankAccount()).isEqualTo(pendingAccount);
    }

    @ParameterizedTest
    @EnumSource(WithdrawalDay.class)
    void 모든_출금일은_현재와_예약_설정에서_문자열로_저장되고_enum으로_복원된다(WithdrawalDay withdrawalDay) {
        // given
        BillingSettings original = new BillingSettings(
                MEMBER_ID, BANK_ACCOUNT, withdrawalDay,
                PENDING_BANK_ACCOUNT, withdrawalDay, EFFECTIVE_MONTH, true
        );

        // when
        BillingSettingsJpaEntity entity = mapper.toEntity(original);
        BillingSettings restored = mapper.toDomain(entity);

        // then
        assertThat(entity.getWithdrawalDay()).isEqualTo(withdrawalDay.name());
        assertThat(entity.getPendingWithdrawalDay()).isEqualTo(withdrawalDay.name());
        assertThat(restored.getWithdrawalDay()).isEqualTo(withdrawalDay);
        assertThat(restored.getPendingWithdrawalDay()).isEqualTo(withdrawalDay);
    }

    @ParameterizedTest
    @MethodSource("effectiveMonthCases")
    void 연말_연초와_윤년에도_적용월은_해당_월_1일로_저장되고_원래_월로_복원된다(
            YearMonth effectiveMonth, LocalDate expectedDate
    ) {
        // given
        BillingSettings original = new BillingSettings(
                MEMBER_ID, BANK_ACCOUNT, WITHDRAWAL_DAY,
                PENDING_BANK_ACCOUNT, PENDING_WITHDRAWAL_DAY, effectiveMonth, true
        );

        // when
        BillingSettingsJpaEntity entity = mapper.toEntity(original);
        BillingSettings restored = mapper.toDomain(entity);

        // then
        assertThat(entity.getEffectiveMonth()).isEqualTo(expectedDate);
        assertThat(restored.getEffectiveMonth()).isEqualTo(effectiveMonth);
    }

    private static Stream<Arguments> reservationCases() {
        return Stream.of(false, true).flatMap(monthPerLimit -> Stream.of(
                Arguments.of("예약 없음", null, null, null, monthPerLimit),
                Arguments.of("계좌만 예약", PENDING_BANK_ACCOUNT, null, EFFECTIVE_MONTH, monthPerLimit),
                Arguments.of("출금일만 예약", null, PENDING_WITHDRAWAL_DAY, EFFECTIVE_MONTH, monthPerLimit),
                Arguments.of("계좌와 출금일 모두 예약", PENDING_BANK_ACCOUNT, PENDING_WITHDRAWAL_DAY, EFFECTIVE_MONTH, monthPerLimit)
        ));
    }

    private static Stream<Arguments> effectiveMonthCases() {
        return Stream.of(
                Arguments.of(YearMonth.of(2026, 12), LocalDate.of(2026, 12, 1)),
                Arguments.of(YearMonth.of(2027, 1), LocalDate.of(2027, 1, 1)),
                Arguments.of(YearMonth.of(2027, 2), LocalDate.of(2027, 2, 1)),
                Arguments.of(YearMonth.of(2028, 2), LocalDate.of(2028, 2, 1))
        );
    }
}
