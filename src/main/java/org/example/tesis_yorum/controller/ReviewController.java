package org.example.tesis_yorum.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.example.tesis_yorum.entity.Review;
import org.example.tesis_yorum.entity.ReviewStatus;
import org.example.tesis_yorum.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
@Tag(name = "Yorumlar", description = "Yorum ve dosya yükleme işlemleri")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Create a new review with optional file attachments
     * POST /api/reviews
     */
    @Operation(
            summary = "Dosya ile yorum oluştur",
            description = "Yeni bir yorum oluşturur ve isteğe bağlı olarak dosya ekler.")
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Review> createReview(
            @RequestParam Long userId,
            @RequestParam Long facilityId,
            @RequestParam String content,
            @RequestParam @Min(1) @Max(5) Integer rating,
            @RequestParam(required = false) List<MultipartFile> files) {

        Review review = reviewService.createReview(userId, facilityId, content, rating, files);
        return new ResponseEntity<>(review, HttpStatus.CREATED);
    }

    /**
     * Get all reviews
     * GET /api/reviews
     */
    @Operation(
            summary = "Bütün Onaylanmış Yorumları Göster",
            description = "Bütün Onaylanmış Yorumları Gösterir.")
    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {

        List<Review> reviews = reviewService.getReviewsByStatus(ReviewStatus.APPROVED);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Get review by ID
     * GET /api/reviews/{id}
     */
    @Operation(
            summary = "Belirli Onaylanmış bir Yorumu Göster",
            description = "Girilen ID'ye göre Onaylanmış yorum gösterir.")
    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        Review review = reviewService.getReviewById(id);
        return ResponseEntity.ok(review);
    }

    /**
     * Get reviews by facility
     * GET /api/reviews/facility/{facilityId}
     */
    @Operation(
            summary = "Tesisin Bütün Onaylanmış Yorumlarını Göster",
            description = "Girilen Tesis ID'ye göre Onaylanmış yorumları gösterir.")
    @GetMapping("/facility/{facilityId}")
    public ResponseEntity<List<Review>> getReviewsByFacility(@PathVariable Long facilityId) {

        List<Review> reviews = reviewService.getApprovedReviewsByFacility(facilityId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Get reviews by user
     * GET /api/reviews/user/{userId}
     */
    @Operation(
            summary = "Kullanıcının Bütün Yorumlarını Göster",
            description = "Girilen Kullanıcı ID'ye göre yorumları gösterir.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Review>> getReviewsByUser(@PathVariable Long userId) {

        List<Review> reviews = reviewService.getReviewsByUser(userId);
        return ResponseEntity.ok(reviews);
    }


    /**
     * Get review statistics for a facility
     * GET /api/reviews/facility/{facilityId}/statistics
     */
    @Operation(
            summary = "Girilen Tesisin Yorum İstatistiklerini Göster",
            description = "Girilen Tesis ID'ye göre yorum istatistiklerini gösterir.")
    @GetMapping("/facility/{facilityId}/statistics")
    public ResponseEntity<ReviewService.ReviewStatistics> getReviewStatistics(@PathVariable Long facilityId) {
        ReviewService.ReviewStatistics stats = reviewService.getReviewStatistics(facilityId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Update review (user can only update their own pending reviews)
     * PUT /api/reviews/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(
            @PathVariable Long id,
            @RequestParam Long userId,
            @Valid @RequestBody UpdateReviewRequest request) {

        Review updatedReview = reviewService.updateReview(id, userId, request.getContent(), request.getRating());
        return ResponseEntity.ok(updatedReview);
    }

    /**
     * Delete review
     * DELETE /api/reviews/{id}
     */
    @Operation(
            summary = "Yorum Sil",
            description = "Girilen Yorum ID'ye göre yorum sil.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id, @RequestParam Long userId) {
        reviewService.deleteReview(id, userId);
        return ResponseEntity.noContent().build();
    }

    public static class UpdateReviewRequest {
        private String content;
        @Min(1) @Max(5)
        private Integer rating;

        // Getters and setters
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }
    }
}
