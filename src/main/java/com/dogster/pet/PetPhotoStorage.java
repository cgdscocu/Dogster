package com.dogster.pet;

import com.dogster.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
public class PetPhotoStorage {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final Path storageRoot;

    public PetPhotoStorage(@Value("${dogster.storage.root-dir}") String storageRoot) {
        this.storageRoot = Path.of(storageRoot);
    }

    public String store(MultipartFile photo) {
        if (photo == null || photo.isEmpty()) {
            throw new BusinessException("Pet photo is required", HttpStatus.BAD_REQUEST);
        }

        String extension = resolveExtension(photo.getOriginalFilename());
        String fileName = UUID.randomUUID() + "." + extension;
        Path petPhotoDir = storageRoot.resolve("pets");
        Path target = petPhotoDir.resolve(fileName).normalize().toAbsolutePath();

        try {
            Files.createDirectories(petPhotoDir);
            photo.transferTo(target);
        } catch (IOException exception) {
            throw new BusinessException("Pet photo could not be stored", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return "pets/" + fileName;
    }

    private String resolveExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BusinessException("Pet photo extension is required", HttpStatus.BAD_REQUEST);
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1)
                .toLowerCase(Locale.ROOT);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("Pet photo type is not supported", HttpStatus.BAD_REQUEST);
        }

        return extension;
    }
}
