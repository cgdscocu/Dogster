package com.dogster.user;

import com.dogster.verification.VerificationCode;
import com.dogster.verification.VerificationCodeGenerator;
import com.dogster.verification.VerificationCodeRepository;
import com.dogster.verification.VerificationSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserRegistrationFlowTests {

    private static final String FIXED_CODE = "123456";

    private final MockMvc mockMvc;
    private final UserAccountRepository userAccountRepository;
    private final VerificationCodeRepository verificationCodeRepository;

    @Autowired
    UserRegistrationFlowTests(
            MockMvc mockMvc,
            UserAccountRepository userAccountRepository,
            VerificationCodeRepository verificationCodeRepository
    ) {
        this.mockMvc = mockMvc;
        this.userAccountRepository = userAccountRepository;
        this.verificationCodeRepository = verificationCodeRepository;
    }

    @BeforeEach
    void cleanDatabase() {
        verificationCodeRepository.deleteAll();
        userAccountRepository.deleteAll();
    }

    @Test
    void registersUserAndCreatesVerificationCode() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Cagda Dogan",
                                  "email": "CAGDA@example.com",
                                  "password": "supersecret"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("cagda@example.com"))
                .andExpect(jsonPath("$.verified").value(false));

        UserAccount user = userAccountRepository.findByEmail("cagda@example.com").orElseThrow();
        assertThat(user.getPasswordHash()).isNotEqualTo("supersecret");

        List<VerificationCode> codes = verificationCodeRepository.findAll();
        assertThat(codes).hasSize(1);
        assertThat(codes.getFirst().code()).isEqualTo(FIXED_CODE);
    }

    @Test
    void rejectsDuplicateEmail() throws Exception {
        register("duplicate@example.com");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Duplicate User",
                                  "email": "duplicate@example.com",
                                  "password": "supersecret"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email is already registered"));
    }

    @Test
    void verifiesEmailWithValidCode() throws Exception {
        register("verify@example.com");

        mockMvc.perform(post("/api/users/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "verify@example.com",
                                  "code": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true));

        UserAccount user = userAccountRepository.findByEmail("verify@example.com").orElseThrow();
        assertThat(user.isVerified()).isTrue();
    }

    @Test
    void rejectsInvalidVerificationCode() throws Exception {
        register("invalid-code@example.com");

        mockMvc.perform(post("/api/users/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "invalid-code@example.com",
                                  "code": "000000"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Verification code is invalid"));
    }

    private void register(String email) throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Test User",
                                  "email": "%s",
                                  "password": "supersecret"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated());
    }

    @TestConfiguration
    static class VerificationTestConfig {

        @Bean
        @Primary
        VerificationCodeGenerator fixedVerificationCodeGenerator() {
            return () -> FIXED_CODE;
        }

        @Bean
        @Primary
        VerificationSender noopVerificationSender() {
            return (email, code) -> {
            };
        }
    }
}
