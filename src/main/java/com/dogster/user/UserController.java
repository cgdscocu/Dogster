package com.dogster.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRegistrationService userRegistrationService;

    public UserController(UserRegistrationService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterUserResultDto register(@Valid @RequestBody RegisterUserDto request) {
        return userRegistrationService.register(request);
    }

    @PostMapping("/verify-email")
    public VerifyEmailResultDto verifyEmail(@Valid @RequestBody VerifyEmailDto request) {
        return userRegistrationService.verifyEmail(request);
    }
}
