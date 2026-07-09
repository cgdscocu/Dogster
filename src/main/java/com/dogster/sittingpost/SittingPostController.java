package com.dogster.sittingpost;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sitting-posts")
@Validated
public class SittingPostController {

    private final SittingPostService sittingPostService;

    public SittingPostController(SittingPostService sittingPostService) {
        this.sittingPostService = sittingPostService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SittingPostResultDto createPost(@Valid @RequestBody CreateSittingPostDto request) {
        return sittingPostService.createPost(request);
    }

    @GetMapping
    public List<SittingPostResultDto> listPosts() {
        return sittingPostService.listPosts();
    }

    @GetMapping("/nearby")
    public List<NearbySittingPostResultDto> listNearbyPosts(
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") double latitude,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") double longitude,
            @RequestParam @Positive double radiusKm
    ) {
        return sittingPostService.listNearbyPosts(latitude, longitude, radiusKm);
    }

    @PostMapping("/{postId}/assign")
    public SittingPostResultDto assignPost(
            @PathVariable Long postId,
            @Valid @RequestBody AssignSittingPostDto request
    ) {
        return sittingPostService.assignPost(postId, request);
    }

    @PostMapping("/{postId}/close")
    public SittingPostResultDto closePost(
            @PathVariable Long postId,
            @Valid @RequestBody CloseSittingPostDto request
    ) {
        return sittingPostService.closePost(postId, request);
    }

    @GetMapping("/{postId}")
    public SittingPostResultDto getPost(@PathVariable Long postId) {
        return sittingPostService.getPost(postId);
    }
}
