package com.dogster.user;

import com.dogster.common.BusinessException;
import com.dogster.verification.VerificationCode;
import com.dogster.verification.VerificationCodeGenerator;
import com.dogster.verification.VerificationCodeRepository;
import com.dogster.verification.VerificationSender;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class UserRegistrationService {

    private static final long VERIFICATION_TTL_MINUTES = 10;

    private final UserAccountRepository userAccountRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final VerificationCodeGenerator verificationCodeGenerator;
    private final VerificationSender verificationSender;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public UserRegistrationService(
            UserAccountRepository userAccountRepository,
            VerificationCodeRepository verificationCodeRepository,
            VerificationCodeGenerator verificationCodeGenerator,
            VerificationSender verificationSender,
            PasswordEncoder passwordEncoder,
            Clock clock
    ) {
        this.userAccountRepository = userAccountRepository;
        this.verificationCodeRepository = verificationCodeRepository;
        this.verificationCodeGenerator = verificationCodeGenerator;
        this.verificationSender = verificationSender;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Transactional
    public RegisterUserResultDto register(RegisterUserDto request) {
        String email = normalizeEmail(request.email());

        if (userAccountRepository.existsByEmail(email)) {
            throw new BusinessException("Email is already registered", HttpStatus.CONFLICT);
        }

        String passwordHash = passwordEncoder.encode(request.password());
        UserAccount user = userAccountRepository.save(new UserAccount(
                request.fullName().trim(),
                email,
                passwordHash
        ));

        VerificationCode verificationCode = createVerificationCode(user);
        verificationSender.send(email, verificationCode.code());

        return new RegisterUserResultDto(
                user.getId(),
                user.getEmail(),
                user.isVerified()
        );
    }

    @Transactional
    public VerifyEmailResultDto verifyEmail(VerifyEmailDto request) {
        String email = normalizeEmail(request.email());
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        VerificationCode verificationCode = verificationCodeRepository
                .findTopByUser_IdAndCodeAndUsedFalseOrderByIdDesc(user.getId(), request.code())
                .orElseThrow(() -> new BusinessException("Verification code is invalid", HttpStatus.BAD_REQUEST));

        if (verificationCode.isExpired(Instant.now(clock))) {
            throw new BusinessException("Verification code is expired", HttpStatus.BAD_REQUEST);
        }

        verificationCode.markUsed();
        user.markVerified();

        return new VerifyEmailResultDto(
                user.getId(),
                user.getEmail(),
                user.isVerified()
        );
    }

    private VerificationCode createVerificationCode(UserAccount user) {
        VerificationCode verificationCode = new VerificationCode(
                user,
                verificationCodeGenerator.generate(),
                Instant.now(clock).plusSeconds(VERIFICATION_TTL_MINUTES * 60)
        );
        return verificationCodeRepository.save(verificationCode);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
