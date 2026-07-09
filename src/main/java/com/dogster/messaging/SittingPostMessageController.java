package com.dogster.messaging;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sitting-posts/{postId}/messages")
@Validated
public class SittingPostMessageController {

    private final SittingPostMessageService messageService;

    public SittingPostMessageController(SittingPostMessageService messageService) {
        this.messageService = messageService;
    }

    @MessageMapping("/sitting-posts/{postId}/messages")
    @SendTo("/topic/sitting-posts/{postId}/messages")
    public SittingPostMessageResultDto sendMessage(
            @DestinationVariable Long postId,
            @Valid SendSittingPostMessageDto request
    ) {
        return messageService.sendMessage(postId, request);
    }

    @GetMapping
    public List<SittingPostMessageResultDto> listMessages(
            @PathVariable Long postId,
            @RequestParam @NotNull Long requesterId
    ) {
        return messageService.listMessages(postId, requesterId);
    }
}
