package com.dogster.sittingpost;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SittingPostRepository extends JpaRepository<SittingPost, Long> {

    List<SittingPost> findByStatus(SittingPostStatus status);
}
