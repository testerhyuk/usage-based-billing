package com.hyuk.billing.member.application.service;

import com.hyuk.billing.member.application.port.in.*;
import com.hyuk.billing.member.application.port.out.BillingSettingsRepositoryPort;
import com.hyuk.billing.member.application.port.out.IssueInitialApiKeyPort;
import com.hyuk.billing.member.application.port.out.IssuedApiKey;
import com.hyuk.billing.member.application.port.out.MemberRepositoryPort;
import com.hyuk.billing.member.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    @Mock
    private MemberRepositoryPort memberRepositoryPort;

    @Mock
    private IssueInitialApiKeyPort issueInitialApiKeyPort;

    @Mock
    private BillingSettingsRepositoryPort billingSettingsRepositoryPort;

    @InjectMocks
    private MemberService memberService;

    GoogleIdentity googleIdentity;
    GoogleIdentity adminGoogleIdentity;
    Member member;
    Member admin;
    BankAccount bankAccount;
    RegisterMemberCommand registerMemberCommand;
    IssuedApiKey issuedApiKey;
    BillingSettings billingSettings;

    @BeforeEach
    void setUp() {
        googleIdentity = new GoogleIdentity("google-1", "test@gmail.com");
        adminGoogleIdentity = new GoogleIdentity("admin-google-1", "test@gmail.com");
        member = new Member(
                    "member-1",
                    googleIdentity.googleId(),
                    googleIdentity.email(),
                    LocalDate.of(2026, 9, 1)
                            .atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant(),
                    Role.ROLE_MEMBER
                );
        admin = new Member(
                "admin-1",
                adminGoogleIdentity.googleId(),
                adminGoogleIdentity.email(),
                LocalDate.of(2026, 9, 1)
                        .atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant(),
                Role.ROLE_ADMIN
        );
        bankAccount = new BankAccount(
                Bank.KB,
                "hyuk",
                "123-456"
        );
        registerMemberCommand = new RegisterMemberCommand(
                googleIdentity,
                bankAccount,
                WithdrawalDay.DAY_5
        );
        issuedApiKey = new IssuedApiKey("api-key");
        billingSettings = new BillingSettings(
                "member-1",
                bankAccount,
                WithdrawalDay.DAY_5,
                null,
                null,
                null,
                false
        );
    }

    // resolveLogin 테스트

    @Test
    void 가입되지_않은_구글_ID이면_memberId는_null_admin은_false를_반환한다() {
        // given
        when(memberRepositoryPort.findByGoogleId(googleIdentity.googleId()))
                .thenReturn(Optional.empty());

        // when
        LoginResult loginResult = memberService.resolveLogin(googleIdentity);

        // then
        assertThat(loginResult.isRegistrationPending()).isTrue();
        assertThat(loginResult.admin()).isFalse();
    }

    @Test
    void 일반회원이면_저장된_회원_ID와_admin은_false를_반환한다() {
        // given
        when(memberRepositoryPort.findByGoogleId(googleIdentity.googleId()))
                .thenReturn(Optional.of(member));

        // when
        LoginResult loginResult = memberService.resolveLogin(googleIdentity);

        // then
        assertThat(loginResult.isRegistrationPending()).isFalse();
        assertThat(loginResult.memberId()).isEqualTo("member-1");
        assertThat(loginResult.admin()).isFalse();
    }

    @Test
    void 관리자라면_저장된_회원_ID와_admin은_true를_반환한다() {
        // given
        when(memberRepositoryPort.findByGoogleId(adminGoogleIdentity.googleId()))
                .thenReturn(Optional.of(admin));

        // when
        LoginResult loginResult = memberService.resolveLogin(adminGoogleIdentity);

        // then
        assertThat(loginResult.isRegistrationPending()).isFalse();
        assertThat(loginResult.admin()).isTrue();
        assertThat(loginResult.memberId()).isEqualTo("admin-1");
    }

    @Test
    void 로그인_판별만으로_회원이나_계좌_및_출금_설정을_저장하거나_API키를_발급하지_않는다() {
        // given
        when(memberRepositoryPort.findByGoogleId(googleIdentity.googleId()))
                .thenReturn(Optional.of(member));

        // when
        LoginResult loginResult = memberService.resolveLogin(googleIdentity);

        // then
        verify(memberRepositoryPort, never()).save(any());
        verify(billingSettingsRepositoryPort, never()).save(any());
        verify(issueInitialApiKeyPort, never()).issueInitialKey(any());
    }

    // register 테스트

    @Test
    void 회원_ID를_생성하고_요청한_구글_ID와_이메일로_회원_객체를_만든다() {
        // given
        when(issueInitialApiKeyPort.issueInitialKey(anyString()))
                .thenReturn(issuedApiKey);

        // when
        RegistrationResult registration = memberService.register(registerMemberCommand);

        // then
        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);

        verify(memberRepositoryPort, times(1)).save(captor.capture());

        Member savedMember = captor.getValue();

        verify(billingSettingsRepositoryPort, times(1)).save(any());
        verify(issueInitialApiKeyPort, times(1))
                .issueInitialKey(savedMember.getMemberId());

        assertThat(savedMember.getMemberId()).isNotBlank();
        assertThat(savedMember.getGoogleId()).isEqualTo(googleIdentity.googleId());
        assertThat(savedMember.getEmail()).isEqualTo(googleIdentity.email());

        assertThat(registration.memberId()).isEqualTo(savedMember.getMemberId());
        assertThat(registration.issuedApiKey()).isEqualTo(issuedApiKey);
    }

    @Test
    void 가입일이_회원가입_처리_시점으로_설정된다() {
        // given
        when(issueInitialApiKeyPort.issueInitialKey(anyString()))
                .thenReturn(issuedApiKey);

        // when
        Instant beforeRegistration = Instant.now();
        RegistrationResult registration = memberService.register(registerMemberCommand);
        Instant afterRegistration = Instant.now();

        // then
        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);

        verify(memberRepositoryPort, times(1)).save(captor.capture());

        Member savedMember = captor.getValue();

        assertThat(savedMember.getRegisteredAt())
                .isBetween(beforeRegistration, afterRegistration);
    }

    @Test
    void 신규_가입_역할은_항상_ROLE_MEMBER로_설정된다() {
        // given
        when(issueInitialApiKeyPort.issueInitialKey(anyString()))
                .thenReturn(issuedApiKey);

        // when
        RegistrationResult registrationResult = memberService.register(registerMemberCommand);

        // then
        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);

        verify(memberRepositoryPort, times(1)).save(captor.capture());

        Member savedMember = captor.getValue();

        assertThat(savedMember.getRole()).isEqualTo(Role.ROLE_MEMBER);
    }

    @Test
    void 회원과_같은_회원_ID로_계좌_및_출금_설정을_생성한다() {
        // given
        when(issueInitialApiKeyPort.issueInitialKey(anyString()))
                .thenReturn(issuedApiKey);

        // when
        RegistrationResult registrationResult = memberService.register(registerMemberCommand);

        // then
        ArgumentCaptor<BillingSettings> captorBillingSettings = ArgumentCaptor.forClass(BillingSettings.class);
        ArgumentCaptor<Member> captorMember = ArgumentCaptor.forClass(Member.class);

        verify(memberRepositoryPort, times(1))
                .save(captorMember.capture());

        verify(billingSettingsRepositoryPort, times(1))
                .save(captorBillingSettings.capture());

        Member savedMember = captorMember.getValue();
        BillingSettings savedBillingSettings = captorBillingSettings.getValue();

        assertThat(savedBillingSettings.getMemberId()).isEqualTo(savedMember.getMemberId());
    }

    @Test
    void 최초_계좌_및_출금_설정에는_요청한_계좌와_예정일이_들어간다() {
        // given
        when(issueInitialApiKeyPort.issueInitialKey(anyString()))
                .thenReturn(issuedApiKey);

        // when
        RegistrationResult registrationResult = memberService.register(registerMemberCommand);

        // then
        ArgumentCaptor<BillingSettings> captorBillingSettings = ArgumentCaptor.forClass(BillingSettings.class);

        verify(billingSettingsRepositoryPort, times(1))
                .save(captorBillingSettings.capture());

        BillingSettings savedBillingSettings = captorBillingSettings.getValue();

        assertThat(savedBillingSettings.getBankAccount())
                .isEqualTo(registerMemberCommand.bankAccount());
        assertThat(savedBillingSettings.getWithdrawalDay())
                .isEqualTo(registerMemberCommand.withdrawalDay());
    }

    @Test
    void 최초_예약값_및_적용_월은_null_그리고_monthPerLimit는_false다() {
        // given
        when(issueInitialApiKeyPort.issueInitialKey(anyString()))
                .thenReturn(issuedApiKey);

        // when
        RegistrationResult registrationResult = memberService.register(registerMemberCommand);

        // then
        ArgumentCaptor<BillingSettings> captorBillingSettings = ArgumentCaptor.forClass(BillingSettings.class);

        verify(billingSettingsRepositoryPort, times(1))
                .save(captorBillingSettings.capture());

        BillingSettings savedBillingSettings = captorBillingSettings.getValue();

        assertThat(savedBillingSettings.getPendingWithdrawalDay()).isNull();
        assertThat(savedBillingSettings.getPendingBankAccount()).isNull();
        assertThat(savedBillingSettings.isMonthPerLimit()).isFalse();
        assertThat(savedBillingSettings.getEffectiveMonth()).isNull();
    }

    @Test
    void 회원_저장_계좌_설정_저장_API_키_발급_순서로_호출된다() {
        // given
        when(issueInitialApiKeyPort.issueInitialKey(anyString()))
                .thenReturn(issuedApiKey);

        // when
        RegistrationResult registrationResult = memberService.register(registerMemberCommand);

        // then
        InOrder order = inOrder(
                memberRepositoryPort,
                billingSettingsRepositoryPort,
                issueInitialApiKeyPort
        );

        order.verify(memberRepositoryPort, times(1))
                .save(any(Member.class));

        order.verify(billingSettingsRepositoryPort, times(1))
                .save(any(BillingSettings.class));

        order.verify(issueInitialApiKeyPort, times(1))
                .issueInitialKey(registrationResult.memberId());
    }

    @Test
    void 가입_결과에_생성한_회원_ID와_API_키가_담긴다() {
        // given
        when(issueInitialApiKeyPort.issueInitialKey(anyString()))
                .thenReturn(issuedApiKey);

        // when
        RegistrationResult registrationResult = memberService.register(registerMemberCommand);

        // then
        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);

        verify(memberRepositoryPort, times(1)).save(captor.capture());

        Member savedMember = captor.getValue();

        assertThat(registrationResult.memberId())
                .isEqualTo(savedMember.getMemberId());
        assertThat(registrationResult.issuedApiKey()).isEqualTo(issuedApiKey);
    }

    @Test
    void 회원_저장에서_예외_발생_시_계좌_저장과_API_키_발급을_요청하지_않는다() {
        // given
        when(memberRepositoryPort.save(any(Member.class)))
                .thenThrow(new RuntimeException());

        // when & then
        assertThrows(RuntimeException.class, () -> memberService.register(registerMemberCommand));

        verify(billingSettingsRepositoryPort, never()).save(any());
        verify(issueInitialApiKeyPort, never()).issueInitialKey(any());
    }

    @Test
    void 계좌_저장_호출에서_예외가_발생하면_API_키_발급_요청을_하지_않는다() {
        // given
        when(billingSettingsRepositoryPort.save(any(BillingSettings.class)))
                .thenThrow(new RuntimeException());

        // when & then
        assertThrows(RuntimeException.class, () -> memberService.register(registerMemberCommand));

        verify(billingSettingsRepositoryPort, times(1)).save(any());
        verify(issueInitialApiKeyPort, never()).issueInitialKey(any());
    }

    @Test
    void API_키_발급에서_예외가_발생하면_가입_성공_결과를_반환하지_않는다() {
        // given
        when(issueInitialApiKeyPort.issueInitialKey(anyString()))
                .thenThrow(new RuntimeException());

        // when & then
        assertThrows(RuntimeException.class, () -> memberService.register(registerMemberCommand));
    }

    // getMember() 테스트

    @Test
    void 회원과_계좌_및_출금_설정이_존재하면_MemberDetails를_반환한다() {
        // given
        BillingSettings billingSettings = new BillingSettings(
                member.getMemberId(),
                bankAccount,
                WithdrawalDay.DAY_5,
                null,
                null,
                null,
                false
        );

        MemberDetails memberDetails = new MemberDetails(
                member,
                billingSettings
        );

        when(memberRepositoryPort.findDetailsById(member.getMemberId()))
                .thenReturn(Optional.of(memberDetails));

        // when
        MemberDetails result = memberService.getMember(member.getMemberId());

        // then
        assertThat(result.member()).isEqualTo(member);
        assertThat(result.billingSettings()).isEqualTo(billingSettings);
    }

    @Test
    void 존재하지_않는_회원_ID로_조회하면_예외가_발생한다() {
        // given
        String memberId = "member-1";

        when(memberRepositoryPort.findDetailsById(memberId))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(RuntimeException.class, () -> memberService.getMember(memberId));
    }

    @Test
    void 회원은_있지만_계좌_및_출금_설정이_없으면_예외가_발생한다() {
        // given
        MemberDetails memberDetails = new MemberDetails(
                member,
                null
        );

        when(memberRepositoryPort.findDetailsById(member.getMemberId()))
                .thenReturn(Optional.of(memberDetails));

        // when & then
        assertThrows(IllegalStateException.class, () ->
                memberService.getMember(member.getMemberId()));
    }

    // changeBillingSettings() 테스트

    @Test
    void 전달받은_회원_ID로_계좌_및_출금_설정을_조회한다() {
        // given
        String memberId = "member-1";
        LocalDate currentDate = LocalDate.of(2026, 9, 10);

        BillingSettings billingSettings = new BillingSettings(
                memberId,
                bankAccount,
                WithdrawalDay.DAY_5,
                null,
                null,
                null,
                false
        );

        when(billingSettingsRepositoryPort.findById(memberId))
                .thenReturn(Optional.of(billingSettings));

        try (MockedStatic<LocalDate> dateMock = mockCurrentDate(currentDate)) {
            // when
            memberService.changeBillingSettings(memberId, bankAccount, WithdrawalDay.DAY_5);
        }

        // then
        verify(billingSettingsRepositoryPort, times(1)).findById(memberId);
        ArgumentCaptor<BillingSettings> captor = ArgumentCaptor.forClass(BillingSettings.class);
        verify(billingSettingsRepositoryPort).save(captor.capture());

        BillingSettings savedBillingSettings = captor.getValue();

        assertThat(savedBillingSettings.getMemberId()).isEqualTo(memberId);
        assertThat(savedBillingSettings.getWithdrawalDay()).isEqualTo(WithdrawalDay.DAY_5);
        assertThat(savedBillingSettings.getBankAccount()).isEqualTo(bankAccount);
    }

    @Test
    void 계좌만_변경하면_새_계좌를_예약하고_변경된_설정을_저장한다() {
        // given
        BankAccount changedBankAccount = new BankAccount(Bank.WOORI, "hyuk", "789-012");
        LocalDate currentDate = LocalDate.of(2026, 9, 1);
        when(billingSettingsRepositoryPort.findById(member.getMemberId()))
                .thenReturn(Optional.of(billingSettings));

        try (MockedStatic<LocalDate> dateMock = mockCurrentDate(currentDate)) {
            // when
            memberService.changeBillingSettings(member.getMemberId(), changedBankAccount, null);
        }

        // then
        verify(billingSettingsRepositoryPort, times(1))
                .findById(member.getMemberId());
        assertThat(billingSettings.getPendingBankAccount())
                .isEqualTo(changedBankAccount);
        assertThat(billingSettings.getPendingWithdrawalDay()).isNull();
        assertThat(billingSettings.getBankAccount()).isEqualTo(bankAccount);
        assertThat(billingSettings.getWithdrawalDay()).isEqualTo(WithdrawalDay.DAY_5);
        assertThat(billingSettings.getEffectiveMonth()).isEqualTo(YearMonth.of(2026, 10));
        assertThat(billingSettings.isMonthPerLimit()).isTrue();
        verify(billingSettingsRepositoryPort, times(1)).save(billingSettings);
    }

    @Test
    void 출금_예정일만_변경하면_새_예정일을_예약하고_변경된_설정을_저장한다() {
        // given
        WithdrawalDay changedWithdrawalDay = WithdrawalDay.DAY_25;
        LocalDate currentDate = LocalDate.of(2026, 9, 25);
        when(billingSettingsRepositoryPort.findById(member.getMemberId()))
                .thenReturn(Optional.of(billingSettings));

        try (MockedStatic<LocalDate> dateMock = mockCurrentDate(currentDate)) {
            // when
            memberService.changeBillingSettings(member.getMemberId(), null, changedWithdrawalDay);
        }

        // then
        assertThat(billingSettings.getPendingBankAccount()).isNull();
        assertThat(billingSettings.getPendingWithdrawalDay())
                .isEqualTo(changedWithdrawalDay);
        assertThat(billingSettings.getBankAccount()).isEqualTo(bankAccount);
        assertThat(billingSettings.getWithdrawalDay()).isEqualTo(WithdrawalDay.DAY_5);
        assertThat(billingSettings.getEffectiveMonth()).isEqualTo(YearMonth.of(2026, 10));
        assertThat(billingSettings.isMonthPerLimit()).isTrue();
        verify(billingSettingsRepositoryPort, times(1)).save(billingSettings);
    }

    @Test
    void 계좌와_출금_예정일을_함께_변경하면_두_예약값을_저장한다() {
        // given
        BankAccount changedBankAccount = new BankAccount(Bank.WOORI, "hyuk", "789-012");
        WithdrawalDay changedWithdrawalDay = WithdrawalDay.DAY_25;
        LocalDate currentDate = LocalDate.of(2026, 9, 10);
        when(billingSettingsRepositoryPort.findById(member.getMemberId()))
                .thenReturn(Optional.of(billingSettings));

        try (MockedStatic<LocalDate> dateMock = mockCurrentDate(currentDate)) {
            // when
            memberService.changeBillingSettings(
                    member.getMemberId(), changedBankAccount, changedWithdrawalDay
            );
        }

        // then
        assertThat(billingSettings.getPendingBankAccount()).isEqualTo(changedBankAccount);
        assertThat(billingSettings.getPendingWithdrawalDay()).isEqualTo(changedWithdrawalDay);
        assertThat(billingSettings.getBankAccount()).isEqualTo(bankAccount);
        assertThat(billingSettings.getWithdrawalDay()).isEqualTo(WithdrawalDay.DAY_5);
        assertThat(billingSettings.getEffectiveMonth()).isEqualTo(YearMonth.of(2026, 10));
        assertThat(billingSettings.isMonthPerLimit()).isTrue();
        verify(billingSettingsRepositoryPort, times(1)).save(billingSettings);
    }

    @Test
    void 계좌_및_출금_설정이_없으면_변경을_거부하고_저장하지_않는다() {
        // given
        String memberId = member.getMemberId();
        when(billingSettingsRepositoryPort.findById(memberId))
                .thenReturn(Optional.empty());

        // when
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> memberService.changeBillingSettings(memberId, bankAccount, WithdrawalDay.DAY_25)
        );

        // then
        assertThat(exception).hasMessage("계좌 및 출금 설정이 없습니다 : " + memberId);
        verify(billingSettingsRepositoryPort, times(1)).findById(memberId);
        verify(billingSettingsRepositoryPort, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {26, 30, 31})
    void 변경_기회가_남아_있어도_26일부터는_변경을_거부하고_저장하지_않는다(int day) {
        // given
        LocalDate currentDate = LocalDate.of(2026, 10, day);
        when(billingSettingsRepositoryPort.findById(member.getMemberId()))
                .thenReturn(Optional.of(billingSettings));

        try (MockedStatic<LocalDate> dateMock = mockCurrentDate(currentDate)) {
            // when & then
            assertThrows(
                    IllegalArgumentException.class,
                    () -> memberService.changeBillingSettings(
                            member.getMemberId(), bankAccount, WithdrawalDay.DAY_25
                    )
            );
        }

        // then
        verify(billingSettingsRepositoryPort, never()).save(any());
        assertThat(billingSettings.getPendingBankAccount()).isNull();
        assertThat(billingSettings.getPendingWithdrawalDay()).isNull();
        assertThat(billingSettings.getEffectiveMonth()).isNull();
        assertThat(billingSettings.isMonthPerLimit()).isFalse();
    }

    @Test
    void 이미_이번_달에_변경했다면_추가_변경을_거부하고_저장하지_않는다() {
        // given
        BankAccount pendingBankAccount = new BankAccount(Bank.HANA, "hyuk", "456-789");
        YearMonth effectiveMonth = YearMonth.of(2026, 10);
        BillingSettings limitedSettings = new BillingSettings(
                member.getMemberId(), bankAccount, WithdrawalDay.DAY_5,
                pendingBankAccount, WithdrawalDay.DAY_15, effectiveMonth, true
        );
        LocalDate currentDate = LocalDate.of(2026, 9, 10);
        when(billingSettingsRepositoryPort.findById(member.getMemberId()))
                .thenReturn(Optional.of(limitedSettings));

        try (MockedStatic<LocalDate> dateMock = mockCurrentDate(currentDate)) {
            // when & then
            assertThrows(
                    IllegalStateException.class,
                    () -> memberService.changeBillingSettings(
                            member.getMemberId(), bankAccount, WithdrawalDay.DAY_25
                    )
            );
        }

        // then
        verify(billingSettingsRepositoryPort, never()).save(any());
        assertThat(limitedSettings.getPendingBankAccount()).isEqualTo(pendingBankAccount);
        assertThat(limitedSettings.getPendingWithdrawalDay()).isEqualTo(WithdrawalDay.DAY_15);
        assertThat(limitedSettings.getEffectiveMonth()).isEqualTo(effectiveMonth);
        assertThat(limitedSettings.isMonthPerLimit()).isTrue();
    }

    @Test
    void 변경할_계좌와_예정일이_모두_없으면_변경을_거부하고_저장하지_않는다() {
        // given
        LocalDate currentDate = LocalDate.of(2026, 9, 10);
        when(billingSettingsRepositoryPort.findById(member.getMemberId()))
                .thenReturn(Optional.of(billingSettings));

        try (MockedStatic<LocalDate> dateMock = mockCurrentDate(currentDate)) {
            // when & then
            assertThrows(
                    IllegalArgumentException.class,
                    () -> memberService.changeBillingSettings(member.getMemberId(), null, null)
            );
        }

        // then
        verify(billingSettingsRepositoryPort, never()).save(any());
        assertThat(billingSettings.getPendingBankAccount()).isNull();
        assertThat(billingSettings.getPendingWithdrawalDay()).isNull();
        assertThat(billingSettings.getEffectiveMonth()).isNull();
        assertThat(billingSettings.isMonthPerLimit()).isFalse();
    }

    @Test
    void 설정_변경_시_회원을_다시_저장하거나_API_키를_발급하지_않는다() {
        // given
        BankAccount changedBankAccount = new BankAccount(Bank.WOORI, "hyuk", "789-012");
        LocalDate currentDate = LocalDate.of(2026, 9, 10);
        when(billingSettingsRepositoryPort.findById(member.getMemberId()))
                .thenReturn(Optional.of(billingSettings));

        try (MockedStatic<LocalDate> dateMock = mockCurrentDate(currentDate)) {
            // when
            memberService.changeBillingSettings(
                    member.getMemberId(), changedBankAccount, WithdrawalDay.DAY_25
            );
        }

        // then
        verify(billingSettingsRepositoryPort, times(1)).save(billingSettings);
        verify(memberRepositoryPort, never()).save(any());
        verify(issueInitialApiKeyPort, never()).issueInitialKey(any());
    }

    @Test
    void 변경된_설정의_저장이_실패하면_예외를_전달한다() {
        // given
        BankAccount changedBankAccount = new BankAccount(Bank.WOORI, "hyuk", "789-012");
        LocalDate currentDate = LocalDate.of(2026, 9, 10);
        RuntimeException saveException = new RuntimeException("계좌 설정 저장 실패");
        when(billingSettingsRepositoryPort.findById(member.getMemberId()))
                .thenReturn(Optional.of(billingSettings));
        when(billingSettingsRepositoryPort.save(any(BillingSettings.class)))
                .thenThrow(saveException);

        try (MockedStatic<LocalDate> dateMock = mockCurrentDate(currentDate)) {
            // when
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> memberService.changeBillingSettings(
                            member.getMemberId(), changedBankAccount, WithdrawalDay.DAY_25
                    )
            );

            // then
            assertThat(exception).isSameAs(saveException);
        }

        verify(billingSettingsRepositoryPort, times(1)).save(billingSettings);
    }

    // resetMonthPerLimit() 테스트

    @Test
    void 월_변경_제한을_초기화하고_나머지_설정을_유지한_채_저장한다() {
        // given
        BankAccount pendingBankAccount = new BankAccount(Bank.HANA, "hyuk", "456-789");
        YearMonth effectiveMonth = YearMonth.of(2026, 10);
        BillingSettings limitedSettings = new BillingSettings(
                member.getMemberId(), bankAccount, WithdrawalDay.DAY_5,
                pendingBankAccount, WithdrawalDay.DAY_15, effectiveMonth, true
        );
        MemberDetails memberDetails = new MemberDetails(member, limitedSettings);
        when(memberRepositoryPort.findDetailsById(member.getMemberId()))
                .thenReturn(Optional.of(memberDetails));

        // when
        memberService.resetMonthPerLimit(member.getMemberId());

        // then
        assertThat(limitedSettings.isMonthPerLimit()).isFalse();
        assertThat(limitedSettings.getMemberId()).isEqualTo(member.getMemberId());
        assertThat(limitedSettings.getBankAccount()).isEqualTo(bankAccount);
        assertThat(limitedSettings.getWithdrawalDay()).isEqualTo(WithdrawalDay.DAY_5);
        assertThat(limitedSettings.getPendingBankAccount()).isEqualTo(pendingBankAccount);
        assertThat(limitedSettings.getPendingWithdrawalDay()).isEqualTo(WithdrawalDay.DAY_15);
        assertThat(limitedSettings.getEffectiveMonth()).isEqualTo(effectiveMonth);
        verify(memberRepositoryPort, times(1)).findDetailsById(member.getMemberId());
        verify(billingSettingsRepositoryPort, times(1)).save(limitedSettings);
    }

    @Test
    void 회원이_없으면_월_변경_제한_초기화를_거부하고_저장하지_않는다() {
        // given
        String memberId = member.getMemberId();
        when(memberRepositoryPort.findDetailsById(memberId))
                .thenReturn(Optional.empty());

        // when
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> memberService.resetMonthPerLimit(memberId)
        );

        // then
        assertThat(exception).hasMessage("해당 회원 정보가 없습니다 : " + memberId);
        verify(billingSettingsRepositoryPort, never()).save(any());
    }

    @Test
    void 계좌_및_출금_설정이_없으면_월_변경_제한_초기화를_거부하고_저장하지_않는다() {
        // given
        String memberId = member.getMemberId();
        MemberDetails memberDetails = new MemberDetails(member, null);
        when(memberRepositoryPort.findDetailsById(memberId))
                .thenReturn(Optional.of(memberDetails));

        // when
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> memberService.resetMonthPerLimit(memberId)
        );

        // then
        assertThat(exception).hasMessage("계좌 및 출금 설정이 없습니다 : " + memberId);
        verify(billingSettingsRepositoryPort, never()).save(any());
    }

    @Test
    void 초기화한_설정의_저장이_실패하면_예외를_전달한다() {
        // given
        BillingSettings limitedSettings = new BillingSettings(
                member.getMemberId(), bankAccount, WithdrawalDay.DAY_5,
                null, null, null, true
        );
        MemberDetails memberDetails = new MemberDetails(member, limitedSettings);
        RuntimeException saveException = new RuntimeException("초기화한 설정 저장 실패");
        when(memberRepositoryPort.findDetailsById(member.getMemberId()))
                .thenReturn(Optional.of(memberDetails));
        when(billingSettingsRepositoryPort.save(any(BillingSettings.class)))
                .thenThrow(saveException);

        // when
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> memberService.resetMonthPerLimit(member.getMemberId())
        );

        // then
        assertThat(exception).isSameAs(saveException);
        verify(billingSettingsRepositoryPort, times(1)).save(limitedSettings);
    }

    // mockCurrentDate() - 서비스에서 사용하는 LocalDate.now()를 테스트 동안만 고정한다.

    private MockedStatic<LocalDate> mockCurrentDate(LocalDate currentDate) {
        MockedStatic<LocalDate> dateMock = mockStatic(LocalDate.class, CALLS_REAL_METHODS);
        dateMock.when(LocalDate::now).thenReturn(currentDate);
        return dateMock;
    }
}
