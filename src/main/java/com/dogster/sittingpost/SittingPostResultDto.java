package com.dogster.sittingpost;

public record SittingPostResultDto(
        Long postId,
        Long ownerId,
        Long petId,
        String description,
        double latitude,
        double longitude,
        SittingPostStatus status,
        Long assignedSitterId
) {

    public static SittingPostResultDto from(SittingPost post) {
        Long assignedSitterId = post.getAssignedSitter() == null
                ? null
                : post.getAssignedSitter().getId();

        return new SittingPostResultDto(
                post.getId(),
                post.getOwner().getId(),
                post.getPet().getId(),
                post.getDescription(),
                post.getLatitude(),
                post.getLongitude(),
                post.getStatus(),
                assignedSitterId
        );
    }
}
