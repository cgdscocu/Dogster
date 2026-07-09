package com.dogster.messaging;

import com.dogster.common.BusinessException;
import com.dogster.pet.Pet;
import com.dogster.pet.PetRepository;
import com.dogster.pet.PetType;
import com.dogster.sittingpost.SittingPost;
import com.dogster.sittingpost.SittingPostRepository;
import com.dogster.user.UserAccount;
import com.dogster.user.UserAccountRepository;
import com.dogster.verification.VerificationCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SittingPostMessageFlowTests {

    private final MockMvc mockMvc;
    private final SittingPostMessageService messageService;
    private final SittingPostMessageRepository messageRepository;
    private final SittingPostRepository sittingPostRepository;
    private final PetRepository petRepository;
    private final UserAccountRepository userAccountRepository;
    private final VerificationCodeRepository verificationCodeRepository;

    @Autowired
    SittingPostMessageFlowTests(
            MockMvc mockMvc,
            SittingPostMessageService messageService,
            SittingPostMessageRepository messageRepository,
            SittingPostRepository sittingPostRepository,
            PetRepository petRepository,
            UserAccountRepository userAccountRepository,
            VerificationCodeRepository verificationCodeRepository
    ) {
        this.mockMvc = mockMvc;
        this.messageService = messageService;
        this.messageRepository = messageRepository;
        this.sittingPostRepository = sittingPostRepository;
        this.petRepository = petRepository;
        this.userAccountRepository = userAccountRepository;
        this.verificationCodeRepository = verificationCodeRepository;
    }

    @BeforeEach
    void cleanDatabase() {
        messageRepository.deleteAll();
        sittingPostRepository.deleteAll();
        petRepository.deleteAll();
        verificationCodeRepository.deleteAll();
        userAccountRepository.deleteAll();
    }

    @Test
    void matchedOwnerAndSitterCanExchangeMessagesAndListHistory() throws Exception {
        UserAccount owner = createUser("owner@example.com");
        UserAccount sitter = createUser("sitter@example.com");
        SittingPost post = createAssignedPost(owner, sitter);

        SittingPostMessageResultDto ownerMessage = messageService.sendMessage(
                post.getId(),
                new SendSittingPostMessageDto(owner.getId(), "Hi, Luna eats at 7.")
        );
        SittingPostMessageResultDto sitterMessage = messageService.sendMessage(
                post.getId(),
                new SendSittingPostMessageDto(sitter.getId(), "Got it, I will be there.")
        );

        assertThat(ownerMessage.messageId()).isNotNull();
        assertThat(sitterMessage.messageId()).isNotNull();
        assertThat(messageRepository.findAll()).hasSize(2);

        mockMvc.perform(get("/api/sitting-posts/{postId}/messages", post.getId())
                        .param("requesterId", owner.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].content").value("Hi, Luna eats at 7."))
                .andExpect(jsonPath("$[1].content").value("Got it, I will be there."));
    }

    @Test
    void rejectsMessageFromUserOutsideMatch() {
        UserAccount owner = createUser("owner@example.com");
        UserAccount sitter = createUser("sitter@example.com");
        UserAccount outsider = createUser("outsider@example.com");
        SittingPost post = createAssignedPost(owner, sitter);

        assertThatThrownBy(() -> messageService.sendMessage(
                post.getId(),
                new SendSittingPostMessageDto(outsider.getId(), "Can I join?")
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("User is not part of this sitting post");
    }

    @Test
    void rejectsMessagesBeforePostIsAssigned() {
        UserAccount owner = createUser("owner@example.com");
        Pet pet = createPet(owner);
        SittingPost post = sittingPostRepository.save(new SittingPost(
                owner,
                pet,
                "Open post",
                41.0100,
                28.9800
        ));

        assertThatThrownBy(() -> messageService.sendMessage(
                post.getId(),
                new SendSittingPostMessageDto(owner.getId(), "Anyone there?")
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Sitting post is not assigned");
    }

    private SittingPost createAssignedPost(UserAccount owner, UserAccount sitter) {
        Pet pet = createPet(owner);
        SittingPost post = new SittingPost(
                owner,
                pet,
                "Assigned post",
                41.0100,
                28.9800
        );
        post.assignTo(sitter);
        return sittingPostRepository.save(post);
    }

    private UserAccount createUser(String email) {
        return userAccountRepository.save(new UserAccount(
                "Test User",
                email,
                "hashed-password"
        ));
    }

    private Pet createPet(UserAccount owner) {
        return petRepository.save(new Pet(
                owner,
                "Luna",
                PetType.DOG,
                "pets/luna.png"
        ));
    }
}
