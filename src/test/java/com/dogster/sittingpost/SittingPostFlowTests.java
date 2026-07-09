package com.dogster.sittingpost;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.dogster.messaging.SittingPostMessageRepository;
import com.dogster.pet.Pet;
import com.dogster.pet.PetRepository;
import com.dogster.pet.PetType;
import com.dogster.user.UserAccount;
import com.dogster.user.UserAccountRepository;
import com.dogster.verification.VerificationCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SittingPostFlowTests {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final SittingPostMessageRepository messageRepository;
    private final SittingPostRepository sittingPostRepository;
    private final PetRepository petRepository;
    private final UserAccountRepository userAccountRepository;
    private final VerificationCodeRepository verificationCodeRepository;

    @Autowired
    SittingPostFlowTests(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            SittingPostMessageRepository messageRepository,
            SittingPostRepository sittingPostRepository,
            PetRepository petRepository,
            UserAccountRepository userAccountRepository,
            VerificationCodeRepository verificationCodeRepository
    ) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
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
    void createsSittingPost() throws Exception {
        UserAccount owner = createUser("owner@example.com");
        Pet pet = createPet(owner);

        mockMvc.perform(post("/api/sitting-posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerId": %d,
                                  "petId": %d,
                                  "description": "Need help for two days",
                                  "latitude": 41.0082,
                                  "longitude": 28.9784
                                }
                                """.formatted(owner.getId(), pet.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ownerId").value(owner.getId()))
                .andExpect(jsonPath("$.petId").value(pet.getId()))
                .andExpect(jsonPath("$.description").value("Need help for two days"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.assignedSitterId").doesNotExist());

        SittingPost post = sittingPostRepository.findAll().getFirst();
        assertThat(post.getStatus()).isEqualTo(SittingPostStatus.OPEN);
    }

    @Test
    void listsAndGetsSittingPost() throws Exception {
        UserAccount owner = createUser("owner@example.com");
        Pet pet = createPet(owner);
        SittingPost post = sittingPostRepository.save(new SittingPost(
                owner,
                pet,
                "Visit once in the evening",
                40.9900,
                29.0300
        ));

        mockMvc.perform(get("/api/sitting-posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].postId").value(post.getId()))
                .andExpect(jsonPath("$[0].status").value("OPEN"));

        mockMvc.perform(get("/api/sitting-posts/{postId}", post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(post.getId()))
                .andExpect(jsonPath("$.description").value("Visit once in the evening"));
    }

    @Test
    void listsNearbySittingPostsInsideRadius() throws Exception {
        UserAccount owner = createUser("owner@example.com");
        Pet pet = createPet(owner);
        SittingPost nearPost = sittingPostRepository.save(new SittingPost(
                owner,
                pet,
                "Near Istanbul post",
                41.0100,
                28.9800
        ));
        sittingPostRepository.save(new SittingPost(
                owner,
                pet,
                "Far Ankara post",
                39.9208,
                32.8541
        ));

        MvcResult result = mockMvc.perform(get("/api/sitting-posts/nearby")
                        .param("latitude", "41.0082")
                        .param("longitude", "28.9784")
                        .param("radiusKm", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].postId").value(nearPost.getId()))
                .andExpect(jsonPath("$[0].description").value("Near Istanbul post"))
                .andReturn();

        double distanceKm = objectMapper.readTree(result.getResponse().getContentAsString())
                .get(0)
                .get("distanceKm")
                .asDouble();
        assertThat(distanceKm).isBetween(0.20, 0.30);
    }

    @Test
    void assignsOpenSittingPostToSitter() throws Exception {
        UserAccount owner = createUser("owner@example.com");
        UserAccount sitter = createUser("sitter@example.com");
        Pet pet = createPet(owner);
        SittingPost post = sittingPostRepository.save(new SittingPost(
                owner,
                pet,
                "Need a sitter",
                41.0100,
                28.9800
        ));

        mockMvc.perform(post("/api/sitting-posts/{postId}/assign", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sitterId": %d
                                }
                                """.formatted(sitter.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(post.getId()))
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.assignedSitterId").value(sitter.getId()));

        SittingPost assignedPost = sittingPostRepository.findById(post.getId()).orElseThrow();
        assertThat(assignedPost.getStatus()).isEqualTo(SittingPostStatus.ASSIGNED);
        assertThat(assignedPost.getAssignedSitter().getId()).isEqualTo(sitter.getId());
    }

    @Test
    void rejectsAssigningOwnSittingPost() throws Exception {
        UserAccount owner = createUser("owner@example.com");
        Pet pet = createPet(owner);
        SittingPost post = sittingPostRepository.save(new SittingPost(
                owner,
                pet,
                "Owner should not assign own post",
                41.0100,
                28.9800
        ));

        mockMvc.perform(post("/api/sitting-posts/{postId}/assign", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sitterId": %d
                                }
                                """.formatted(owner.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Owner cannot assign own post"));
    }

    @Test
    void rejectsAssigningAlreadyAssignedSittingPost() throws Exception {
        UserAccount owner = createUser("owner@example.com");
        UserAccount firstSitter = createUser("first-sitter@example.com");
        UserAccount secondSitter = createUser("second-sitter@example.com");
        Pet pet = createPet(owner);
        SittingPost post = sittingPostRepository.save(new SittingPost(
                owner,
                pet,
                "Already assigned post",
                41.0100,
                28.9800
        ));
        post.assignTo(firstSitter);
        sittingPostRepository.save(post);

        mockMvc.perform(post("/api/sitting-posts/{postId}/assign", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sitterId": %d
                                }
                                """.formatted(secondSitter.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Sitting post is not open"));
    }

    @Test
    void closesAssignedSittingPostByOwner() throws Exception {
        UserAccount owner = createUser("owner@example.com");
        UserAccount sitter = createUser("sitter@example.com");
        Pet pet = createPet(owner);
        SittingPost post = sittingPostRepository.save(new SittingPost(
                owner,
                pet,
                "Assigned post to close",
                41.0100,
                28.9800
        ));
        post.assignTo(sitter);
        sittingPostRepository.save(post);

        mockMvc.perform(post("/api/sitting-posts/{postId}/close", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerId": %d
                                }
                                """.formatted(owner.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(post.getId()))
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.assignedSitterId").value(sitter.getId()));

        SittingPost closedPost = sittingPostRepository.findById(post.getId()).orElseThrow();
        assertThat(closedPost.getStatus()).isEqualTo(SittingPostStatus.CLOSED);
    }

    @Test
    void rejectsClosingPostByNonOwner() throws Exception {
        UserAccount owner = createUser("owner@example.com");
        UserAccount sitter = createUser("sitter@example.com");
        Pet pet = createPet(owner);
        SittingPost post = sittingPostRepository.save(new SittingPost(
                owner,
                pet,
                "Assigned post",
                41.0100,
                28.9800
        ));
        post.assignTo(sitter);
        sittingPostRepository.save(post);

        mockMvc.perform(post("/api/sitting-posts/{postId}/close", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerId": %d
                                }
                                """.formatted(sitter.getId())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Only owner can close sitting post"));
    }

    @Test
    void rejectsClosingOpenPost() throws Exception {
        UserAccount owner = createUser("owner@example.com");
        Pet pet = createPet(owner);
        SittingPost post = sittingPostRepository.save(new SittingPost(
                owner,
                pet,
                "Open post",
                41.0100,
                28.9800
        ));

        mockMvc.perform(post("/api/sitting-posts/{postId}/close", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerId": %d
                                }
                                """.formatted(owner.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only assigned sitting post can be closed"));
    }

    @Test
    void rejectsPostWhenPetDoesNotBelongToOwner() throws Exception {
        UserAccount owner = createUser("owner@example.com");
        UserAccount otherOwner = createUser("other@example.com");
        Pet otherPet = createPet(otherOwner);

        mockMvc.perform(post("/api/sitting-posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerId": %d,
                                  "petId": %d,
                                  "description": "This should not be allowed",
                                  "latitude": 41.0082,
                                  "longitude": 28.9784
                                }
                                """.formatted(owner.getId(), otherPet.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Pet does not belong to owner"));
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
