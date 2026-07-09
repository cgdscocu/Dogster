package com.dogster.user;

public record VerifyEmailResultDto(
        Long userId,
        String email,
        boolean verified
) {
}
