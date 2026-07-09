package com.dogster.pet;

public record CreatePetResultDto(
        Long petId,
        Long ownerId,
        String name,
        PetType type,
        String photoPath
) {
}
