package com.dogster.user;

public record RegisterUserResultDto(
        Long userId,
        String email,
        boolean verified
) {
}
