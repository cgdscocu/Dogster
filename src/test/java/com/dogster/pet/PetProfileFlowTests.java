package com.dogster.pet;

import com.dogster.messaging.SittingPostMessageRepository;
import com.dogster.user.UserAccount;
import com.dogster.user.UserAccountRepository;
import com.dogster.verification.VerificationCodeRepository;
import com.dogster.sittingpost.SittingPostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PetProfileFlowTests {

    private final MockMvc mockMvc;
    private final SittingPostMessageRepository messageRepository;
    private final PetRepository petRepository;
    private final SittingPostRepository sittingPostRepository;
    private final UserAccountRepository userAccountRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final Path storageRoot;

    @Autowired
    PetProfileFlowTests(
            MockMvc mockMvc,
            SittingPostMessageRepository messageRepository,
            PetRepository petRepository,
            SittingPostRepository sittingPostRepository,
            UserAccountRepository userAccountRepository,
            VerificationCodeRepository verificationCodeRepository,
            @Value("${dogster.storage.root-dir}") String storageRoot
    ) {
        this.mockMvc = mockMvc;
        this.messageRepository = messageRepository;
        this.petRepository = petRepository;
        this.sittingPostRepository = sittingPostRepository;
        this.userAccountRepository = userAccountRepository;
        this.verificationCodeRepository = verificationCodeRepository;
        this.storageRoot = Path.of(storageRoot);
    }

    @BeforeEach
    void cleanDatabaseAndFiles() throws IOException {
        messageRepository.deleteAll();
        sittingPostRepository.deleteAll();
        petRepository.deleteAll();
        verificationCodeRepository.deleteAll();
        userAccountRepository.deleteAll();
        deleteDirectoryIfExists(storageRoot);
    }

    @Test
    void createsPetProfileWithRequiredPhoto() throws Exception {
        UserAccount owner = userAccountRepository.save(new UserAccount(
                "Pet Owner",
                "owner@example.com",
                "hashed-password"
        ));

        MockMultipartFile photo = new MockMultipartFile(
                "photo",
                "luna.png",
                "image/png",
                "fake-image-content".getBytes()
        );

        mockMvc.perform(multipart("/api/pets")
                        .file(photo)
                        .param("ownerId", owner.getId().toString())
                        .param("name", "Luna")
                        .param("type", "DOG"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ownerId").value(owner.getId()))
                .andExpect(jsonPath("$.name").value("Luna"))
                .andExpect(jsonPath("$.type").value("DOG"))
                .andExpect(jsonPath("$.photoPath").exists());

        Pet pet = petRepository.findAll().getFirst();
        assertThat(pet.getPhotoPath()).startsWith("pets/");
        assertThat(Files.exists(storageRoot.resolve(pet.getPhotoPath()))).isTrue();
    }

    @Test
    void rejectsPetProfileWithoutPhoto() throws Exception {
        UserAccount owner = userAccountRepository.save(new UserAccount(
                "Pet Owner",
                "owner@example.com",
                "hashed-password"
        ));

        MockMultipartFile emptyPhoto = new MockMultipartFile(
                "photo",
                "empty.png",
                "image/png",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/pets")
                        .file(emptyPhoto)
                        .param("ownerId", owner.getId().toString())
                        .param("name", "Luna")
                        .param("type", "DOG"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Pet photo is required"));

        assertThat(petRepository.findAll()).isEmpty();
    }

    @Test
    void rejectsUnknownOwner() throws Exception {
        MockMultipartFile photo = new MockMultipartFile(
                "photo",
                "luna.png",
                "image/png",
                "fake-image-content".getBytes()
        );

        mockMvc.perform(multipart("/api/pets")
                        .file(photo)
                        .param("ownerId", "999")
                        .param("name", "Luna")
                        .param("type", "DOG"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Owner not found"));
    }

    private void deleteDirectoryIfExists(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }

        try (var paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException exception) {
                            throw new IllegalStateException(exception);
                        }
                    });
        }
    }
}
