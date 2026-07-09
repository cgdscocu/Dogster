package com.dogster.messaging;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SendSittingPostMessageDto(
        @NotNull
        Long senderId,

        @NotBlank
        @Size(max = 1000)
        String content
) {
}
