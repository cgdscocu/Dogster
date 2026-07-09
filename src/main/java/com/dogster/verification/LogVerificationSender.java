package com.dogster.verification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LogVerificationSender implements VerificationSender {

    private static final Logger log = LoggerFactory.getLogger(LogVerificationSender.class);

    @Override
    public void send(String email, String code) {
        log.info("Verification code for {} is {}", email, code);
    }
}

