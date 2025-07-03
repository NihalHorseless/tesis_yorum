package org.example.tesis_yorum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.example.tesis_yorum.entity.Review;
import org.example.tesis_yorum.entity.ReviewStatus;
import org.example.tesis_yorum.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
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
     * Create a simple review without files (JSON)
     * POST /api/reviews/simple
     */
    @PostMapping("/simple")
    public ResponseEntity<Review> createSimpleReview(@Valid @RequestBody CreateReviewRequest request) {
        Review review = reviewService.createReview(
                request.getUserId(),
                request.getFacilityId(),
                request.getContent(),
                request.getRating()
        );
        return new ResponseEntity<>(review, HttpStatus.CREATED);
    }

    /**
     * Get all reviews
     * GET /api/reviews
     */
    @GetMapping
    public ResponseEntity<Page<Review>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Review> reviews = reviewService.getReviewsByStatus(ReviewStatus.APPROVED, pageable);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Get review by ID
     * GET /api/reviews/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        Review review = reviewService.getReviewById(id);
        return ResponseEntity.ok(review);
    }

    /**
     * Get reviews by facility
     * GET /api/reviews/facility/{facilityId}
     */
    @GetMapping("/facility/{facilityId}")
    public ResponseEntity<Page<Review>> getReviewsByFacility(
            @PathVariable Long facilityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Review> reviews = reviewService.getApprovedReviewsByFacility(facilityId, pageable);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Get reviews by user
     * GET /api/reviews/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Review>> getReviewsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviews = reviewService.getReviewsByUser(userId, pageable);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Get latest approved reviews
     * GET /api/reviews/latest
     */
    @GetMapping("/latest")
    public ResponseEntity<Page<Review>> getLatestReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviews = reviewService.getLatestApprovedReviews(pageable);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Get top rated reviews for a facility
     * GET /api/reviews/facility/{facilityId}/top-rated
     */
    @GetMapping("/facility/{facilityId}/top-rated")
    public ResponseEntity<Page<Review>> getTopRatedReviews(
            @PathVariable Long facilityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviews = reviewService.getTopRatedReviewsByFacility(facilityId, pageable);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Search reviews by content
     * GET /api/reviews/search?q={keyword}
     */
    @GetMapping("/search")
    public ResponseEntity<List<Review>> searchReviews(@RequestParam String q) {
        List<Review> reviews = reviewService.searchReviewsByContent(q);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Get reviews by rating range
     * GET /api/reviews/rating?min={min}&max={max}
     */
    @GetMapping("/rating")
    public ResponseEntity<List<Review>> getReviewsByRating(
            @RequestParam @Min(1) @Max(5) Integer min,
            @RequestParam @Min(1) @Max(5) Integer max) {

        List<Review> reviews = reviewService.getReviewsByRatingRange(min, max);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Get reviews within date range
     * GET /api/reviews/date-range?start={start}&end={end}
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<Review>> getReviewsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        List<Review> reviews = reviewService.getReviewsByDateRange(start, end);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Get reviews with attachments
     * GET /api/reviews/with-attachments
     */
    @GetMapping("/with-attachments")
    public ResponseEntity<List<Review>> getReviewsWithAttachments() {
        List<Review> reviews = reviewService.getReviewsWithAttachments();
        return ResponseEntity.ok(reviews);
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
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id, @RequestParam Long userId) {
        reviewService.deleteReview(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get average rating for a facility
     * GET /api/reviews/facility/{facilityId}/average-rating
     */
    @GetMapping("/facility/{facilityId}/average-rating")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long facilityId) {
        Double averageRating = reviewService.calculateAverageRating(facilityId);
        return ResponseEntity.ok(averageRating);
    }

    /**
     * Get review statistics for a facility
     * GET /api/reviews/facility/{facilityId}/statistics
     */
    @GetMapping("/facility/{facilityId}/statistics")
    public ResponseEntity<ReviewService.ReviewStatistics> getReviewStatistics(@PathVariable Long facilityId) {
        ReviewService.ReviewStatistics stats = reviewService.getReviewStatistics(facilityId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Check if user can review facility
     * GET /api/reviews/can-review?userId={userId}&facilityId={facilityId}
     */
    @GetMapping("/can-review")
    public ResponseEntity<Boolean> canUserReviewFacility(
            @RequestParam Long userId,
            @RequestParam Long facilityId) {

        boolean canReview = reviewService.canUserReviewFacility(userId, facilityId);
        return ResponseEntity.ok(canReview);
    }

    /**
     * Get user's review for a specific facility
     * GET /api/reviews/user-review?userId={userId}&facilityId={facilityId}
     */
    @GetMapping("/user-review")
    public ResponseEntity<Review> getUserReviewForFacility(
            @RequestParam Long userId,
            @RequestParam Long facilityId) {

        Review review = reviewService.getUserReviewForFacility(userId, facilityId);
        return review != null ? ResponseEntity.ok(review) : ResponseEntity.notFound().build();
    }

    // Request DTOs
    public static class CreateReviewRequest {
        private Long userId;
        private Long facilityId;
        private String content;
        @Min(1) @Max(5)
        private Integer rating;

        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Long getFacilityId() { return facilityId; }
        public void setFacilityId(Long facilityId) { this.facilityId = facilityId; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }
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
