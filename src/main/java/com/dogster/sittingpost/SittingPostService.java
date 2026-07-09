package com.dogster.sittingpost;

import com.dogster.common.BusinessException;
import com.dogster.location.HaversineDistanceCalculator;
import com.dogster.pet.Pet;
import com.dogster.pet.PetRepository;
import com.dogster.user.UserAccount;
import com.dogster.user.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class SittingPostService {

    private final SittingPostRepository sittingPostRepository;
    private final UserAccountRepository userAccountRepository;
    private final PetRepository petRepository;
    private final HaversineDistanceCalculator distanceCalculator;

    public SittingPostService(
            SittingPostRepository sittingPostRepository,
            UserAccountRepository userAccountRepository,
            PetRepository petRepository,
            HaversineDistanceCalculator distanceCalculator
    ) {
        this.sittingPostRepository = sittingPostRepository;
        this.userAccountRepository = userAccountRepository;
        this.petRepository = petRepository;
        this.distanceCalculator = distanceCalculator;
    }

    @Transactional
    public SittingPostResultDto createPost(CreateSittingPostDto request) {
        UserAccount owner = userAccountRepository.findById(request.ownerId())
                .orElseThrow(() -> new BusinessException("Owner not found", HttpStatus.NOT_FOUND));

        Pet pet = petRepository.findById(request.petId())
                .orElseThrow(() -> new BusinessException("Pet not found", HttpStatus.NOT_FOUND));

        if (!pet.getOwner().getId().equals(owner.getId())) {
            throw new BusinessException("Pet does not belong to owner", HttpStatus.BAD_REQUEST);
        }

        SittingPost post = sittingPostRepository.save(new SittingPost(
                owner,
                pet,
                request.description().trim(),
                request.latitude(),
                request.longitude()
        ));

        return SittingPostResultDto.from(post);
    }

    @Transactional(readOnly = true)
    public List<SittingPostResultDto> listPosts() {
        return sittingPostRepository.findAll()
                .stream()
                .map(SittingPostResultDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NearbySittingPostResultDto> listNearbyPosts(double latitude, double longitude, double radiusKm) {
        return sittingPostRepository.findByStatus(SittingPostStatus.OPEN)
                .stream()
                .map(post -> NearbySittingPostResultDto.from(
                        post,
                        distanceCalculator.calculateKm(latitude, longitude, post.getLatitude(), post.getLongitude())
                ))
                .filter(post -> post.distanceKm() <= radiusKm)
                .sorted(Comparator.comparingDouble(NearbySittingPostResultDto::distanceKm))
                .toList();
    }

    @Transactional
    public SittingPostResultDto assignPost(Long postId, AssignSittingPostDto request) {
        SittingPost post = sittingPostRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("Sitting post not found", HttpStatus.NOT_FOUND));

        UserAccount sitter = userAccountRepository.findById(request.sitterId())
                .orElseThrow(() -> new BusinessException("Sitter not found", HttpStatus.NOT_FOUND));

        if (post.getStatus() != SittingPostStatus.OPEN) {
            throw new BusinessException("Sitting post is not open", HttpStatus.BAD_REQUEST);
        }

        if (post.getOwner().getId().equals(sitter.getId())) {
            throw new BusinessException("Owner cannot assign own post", HttpStatus.BAD_REQUEST);
        }

        post.assignTo(sitter);

        return SittingPostResultDto.from(post);
    }

    @Transactional
    public SittingPostResultDto closePost(Long postId, CloseSittingPostDto request) {
        SittingPost post = sittingPostRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("Sitting post not found", HttpStatus.NOT_FOUND));

        if (!post.getOwner().getId().equals(request.ownerId())) {
            throw new BusinessException("Only owner can close sitting post", HttpStatus.FORBIDDEN);
        }

        if (post.getStatus() != SittingPostStatus.ASSIGNED) {
            throw new BusinessException("Only assigned sitting post can be closed", HttpStatus.BAD_REQUEST);
        }

        post.close();

        return SittingPostResultDto.from(post);
    }

    @Transactional(readOnly = true)
    public SittingPostResultDto getPost(Long postId) {
        SittingPost post = sittingPostRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("Sitting post not found", HttpStatus.NOT_FOUND));

        return SittingPostResultDto.from(post);
    }

}
