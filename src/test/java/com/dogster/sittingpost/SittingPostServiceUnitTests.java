package com.dogster.sittingpost;

import com.dogster.common.BusinessException;
import com.dogster.location.HaversineDistanceCalculator;
import com.dogster.pet.Pet;
import com.dogster.pet.PetRepository;
import com.dogster.pet.PetType;
import com.dogster.user.UserAccount;
import com.dogster.user.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SittingPostServiceUnitTests {

    private final SittingPostRepository sittingPostRepository = mock(SittingPostRepository.class);
    private final UserAccountRepository userAccountRepository = mock(UserAccountRepository.class);
    private final PetRepository petRepository = mock(PetRepository.class);
    private final HaversineDistanceCalculator distanceCalculator = mock(HaversineDistanceCalculator.class);
    private final SittingPostService service = new SittingPostService(
            sittingPostRepository,
            userAccountRepository,
            petRepository,
            distanceCalculator
    );

    @Test
    void assignPostRejectsOwnerAsSitter() {
        UserAccount owner = userWithId(1L);
        SittingPost post = assignedReadyPost(owner);

        when(sittingPostRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> service.assignPost(10L, new AssignSittingPostDto(1L)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Owner cannot assign own post");
    }

    @Test
    void assignPostChangesStatusAndAssignedSitter() {
        UserAccount owner = userWithId(1L);
        UserAccount sitter = userWithId(2L);
        SittingPost post = assignedReadyPost(owner);

        when(sittingPostRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userAccountRepository.findById(2L)).thenReturn(Optional.of(sitter));

        SittingPostResultDto result = service.assignPost(10L, new AssignSittingPostDto(2L));

        assertThat(result.status()).isEqualTo(SittingPostStatus.ASSIGNED);
        assertThat(result.assignedSitterId()).isEqualTo(2L);
        assertThat(post.getAssignedSitter()).isEqualTo(sitter);
    }

    private SittingPost assignedReadyPost(UserAccount owner) {
        SittingPost post = new SittingPost(
                owner,
                petWithId(owner),
                "Need a sitter",
                41.0100,
                28.9800
        );
        ReflectionTestUtils.setField(post, "id", 10L);
        return post;
    }

    private UserAccount userWithId(Long id) {
        UserAccount user = new UserAccount("Test User", "test-%d@example.com".formatted(id), "hash");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Pet petWithId(UserAccount owner) {
        Pet pet = new Pet(owner, "Luna", PetType.DOG, "pets/luna.png");
        ReflectionTestUtils.setField(pet, "id", 20L);
        return pet;
    }
}
