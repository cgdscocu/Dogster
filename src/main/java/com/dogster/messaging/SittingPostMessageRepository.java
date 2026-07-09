package com.dogster.messaging;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SittingPostMessageRepository extends JpaRepository<SittingPostMessage, Long> {

    List<SittingPostMessage> findBySittingPostIdOrderByIdAsc(Long sittingPostId);
}
