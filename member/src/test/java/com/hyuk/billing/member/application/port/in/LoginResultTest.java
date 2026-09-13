package com.hyuk.billing.member.application.port.in;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginResultTest {

    // isRegistrationPending() 테스트

    @Test
    void 회원_ID가_null이면_회원가입_대기_상태를_반환한다() {
        // given
        LoginResult loginResult = new LoginResult(null, false);

        // when
        boolean registrationPending = loginResult.isRegistrationPending();

        // then
        assertThat(registrationPending).isTrue();
    }

    @Test
    void 회원_ID가_있으면_회원가입_대기_상태가_아니다() {
        // given
        LoginResult loginResult = new LoginResult("member-1", false);

        // when
        boolean registrationPending = loginResult.isRegistrationPending();

        // then
        assertThat(registrationPending).isFalse();
    }
}
