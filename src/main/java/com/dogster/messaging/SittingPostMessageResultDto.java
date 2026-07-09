package com.dogster.messaging;

import java.time.Instant;

public record SittingPostMessageResultDto(
        Long messageId,
        Long postId,
        Long senderId,
        String content,
        Instant sentAt
) {

    public static SittingPostMessageResultDto from(SittingPostMessage message) {
        return new SittingPostMessageResultDto(
                message.getId(),
                message.getSittingPost().getId(),
                message.getSender().getId(),
                message.getContent(),
                message.getSentAt()
        );
    }
}
