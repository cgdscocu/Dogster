package com.dogster.sittingpost;

public record NearbySittingPostResultDto(
        Long postId,
        Long ownerId,
        Long petId,
        String description,
        double latitude,
        double longitude,
        SittingPostStatus status,
        Long assignedSitterId,
        double distanceKm
) {

    public static NearbySittingPostResultDto from(SittingPost post, double distanceKm) {
        Long assignedSitterId = post.getAssignedSitter() == null
                ? null
                : post.getAssignedSitter().getId();

        return new NearbySittingPostResultDto(
                post.getId(),
                post.getOwner().getId(),
                post.getPet().getId(),
                post.getDescription(),
                post.getLatitude(),
                post.getLongitude(),
                post.getStatus(),
                assignedSitterId,
                distanceKm
        );
    }
}
