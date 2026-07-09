package com.dogster.sittingpost;

import jakarta.validation.constraints.NotNull;

public record AssignSittingPostDto(
        @NotNull
        Long sitterId
) {
}
