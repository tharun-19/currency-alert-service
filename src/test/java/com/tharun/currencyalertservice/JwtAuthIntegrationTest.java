package com.tharun.currencyalertservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.tharun.currencyalertservice.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JwtAuthIntegrationTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void loginWithValidCredentialsReturnsJwt() {
        assertThat(jwtUtil.isValidCredentials("test", "test123")).isTrue();

        String token = jwtUtil.generateToken("test");

        assertThat(token).isNotBlank();
        assertThat(jwtUtil.validateToken(token)).isTrue();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("test");
    }

    @Test
    void loginWithInvalidCredentialsIsRejected() {
        assertThat(jwtUtil.isValidCredentials("test", "wrong-password")).isFalse();
    }
}
