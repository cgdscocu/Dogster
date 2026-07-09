package com.dogster.messaging;

import com.dogster.common.BusinessException;
import com.dogster.sittingpost.SittingPost;
import com.dogster.sittingpost.SittingPostRepository;
import com.dogster.sittingpost.SittingPostStatus;
import com.dogster.user.UserAccount;
import com.dogster.user.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class SittingPostMessageService {

    private final SittingPostMessageRepository messageRepository;
    private final SittingPostRepository sittingPostRepository;
    private final UserAccountRepository userAccountRepository;
    private final Clock clock;

    public SittingPostMessageService(
            SittingPostMessageRepository messageRepository,
            SittingPostRepository sittingPostRepository,
            UserAccountRepository userAccountRepository,
            Clock clock
    ) {
        this.messageRepository = messageRepository;
        this.sittingPostRepository = sittingPostRepository;
        this.userAccountRepository = userAccountRepository;
        this.clock = clock;
    }

    @Transactional
    public SittingPostMessageResultDto sendMessage(Long postId, SendSittingPostMessageDto request) {
        SittingPost post = findPost(postId);
        UserAccount sender = userAccountRepository.findById(request.senderId())
                .orElseThrow(() -> new BusinessException("Sender not found", HttpStatus.NOT_FOUND));

        validateCanMessage(post, sender.getId());

        SittingPostMessage message = messageRepository.save(new SittingPostMessage(
                post,
                sender,
                request.content().trim(),
                Instant.now(clock)
        ));

        return SittingPostMessageResultDto.from(message);
    }

    @Transactional(readOnly = true)
    public List<SittingPostMessageResultDto> listMessages(Long postId, Long requesterId) {
        SittingPost post = findPost(postId);
        validateCanMessage(post, requesterId);

        return messageRepository.findBySittingPostIdOrderByIdAsc(postId)
                .stream()
                .map(SittingPostMessageResultDto::from)
                .toList();
    }

    private SittingPost findPost(Long postId) {
        return sittingPostRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("Sitting post not found", HttpStatus.NOT_FOUND));
    }

    private void validateCanMessage(SittingPost post, Long userId) {
        if (post.getStatus() != SittingPostStatus.ASSIGNED || post.getAssignedSitter() == null) {
            throw new BusinessException("Sitting post is not assigned", HttpStatus.BAD_REQUEST);
        }

        boolean isOwner = post.getOwner().getId().equals(userId);
        boolean isAssignedSitter = post.getAssignedSitter().getId().equals(userId);

        if (!isOwner && !isAssignedSitter) {
            throw new BusinessException("User is not part of this sitting post", HttpStatus.FORBIDDEN);
        }
    }
}
