package com.dogster.pet;

import com.dogster.common.BusinessException;
import com.dogster.user.UserAccount;
import com.dogster.user.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PetService {

    private final PetRepository petRepository;
    private final UserAccountRepository userAccountRepository;
    private final PetPhotoStorage petPhotoStorage;

    public PetService(
            PetRepository petRepository,
            UserAccountRepository userAccountRepository,
            PetPhotoStorage petPhotoStorage
    ) {
        this.petRepository = petRepository;
        this.userAccountRepository = userAccountRepository;
        this.petPhotoStorage = petPhotoStorage;
    }

    @Transactional
    public CreatePetResultDto createPet(Long ownerId, String name, PetType type, MultipartFile photo) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("Pet name is required", HttpStatus.BAD_REQUEST);
        }

        UserAccount owner = userAccountRepository.findById(ownerId)
                .orElseThrow(() -> new BusinessException("Owner not found", HttpStatus.NOT_FOUND));

        String photoPath = petPhotoStorage.store(photo);
        Pet pet = petRepository.save(new Pet(
                owner,
                name.trim(),
                type,
                photoPath
        ));

        return new CreatePetResultDto(
                pet.getId(),
                owner.getId(),
                pet.getName(),
                pet.getType(),
                pet.getPhotoPath()
        );
    }
}
