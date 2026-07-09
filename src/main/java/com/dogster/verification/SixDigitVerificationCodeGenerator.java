package com.dogster.verification;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class SixDigitVerificationCodeGenerator implements VerificationCodeGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate() {
        int code = secureRandom.nextInt(1_000_000);
        return "%06d".formatted(code);
    }
}

