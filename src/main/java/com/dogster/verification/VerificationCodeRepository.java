package com.dogster.verification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    Optional<VerificationCode> findTopByUser_IdAndCodeAndUsedFalseOrderByIdDesc(Long userId, String code);
}
