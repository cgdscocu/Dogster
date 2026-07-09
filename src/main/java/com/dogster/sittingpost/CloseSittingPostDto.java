package com.dogster.sittingpost;

import jakarta.validation.constraints.NotNull;

public record CloseSittingPostDto(
        @NotNull
        Long ownerId
) {
}
