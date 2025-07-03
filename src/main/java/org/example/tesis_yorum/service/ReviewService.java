package org.example.tesis_yorum.service;

import org.example.tesis_yorum.entity.Facility;
import org.example.tesis_yorum.entity.FileAttachment;
import org.example.tesis_yorum.entity.Review;
import org.example.tesis_yorum.entity.User;
import org.example.tesis_yorum.entity.ReviewStatus;
import org.example.tesis_yorum.entity.UserRole;
import org.example.tesis_yorum.exceptions.ResourceNotFoundException;
import org.example.tesis_yorum.exceptions.UnauthorizedException;
import org.example.tesis_yorum.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final FacilityService facilityService;
    private final FileAttachmentService fileAttachmentService;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository,
                         UserService userService,
                         FacilityService facilityService,
                         FileAttachmentService fileAttachmentService) {
        this.reviewRepository = reviewRepository;
        this.userService = userService;
        this.facilityService = facilityService;
        this.fileAttachmentService = fileAttachmentService;
    }

    /**
     * Create a new review with optional file attachments
     */
    public Review createReview(Long userId, Long facilityId, String content, Integer rating, List<MultipartFile> files) {
        User user = userService.getUserById(userId);
        Facility facility = facilityService.getFacilityById(facilityId);

        if (!facility.getActive()) {
            throw new IllegalArgumentException("Cannot create review for inactive facility");
        }

        Review review = new Review(content, rating, user, facility);
        review = reviewRepository.save(review);

        // Handle file attachments if provided
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    fileAttachmentService.createFileAttachment(review, file);
                }
            }
        }

        return review;
    }

    /**
     * Create a simple review without files
     */
    public Review createReview(Long userId, Long facilityId, String content, Integer rating) {
        return createReview(userId, facilityId, content, rating, null);
    }

    /**
     * Get review by ID
     */
    @Transactional(readOnly = true)
    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
    }

    /**
     * Get all reviews
     */
    @Transactional(readOnly = true)
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    /**
     * Get reviews by status
     */
    @Transactional(readOnly = true)
    public List<Review> getReviewsByStatus(ReviewStatus status) {
        return reviewRepository.findByStatus(status);
    }

    /**
     * Get reviews by status with pagination
     */
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByStatus(ReviewStatus status, Pageable pageable) {
        return reviewRepository.findByStatus(status, pageable);
    }

    /**
     * Get pending reviews for admin approval (ordered by creation date)
     */
    @Transactional(readOnly = true)
    public List<Review> getPendingReviews() {
        return reviewRepository.findByStatusOrderByCreatedAtAsc(ReviewStatus.PENDING);
    }

    /**
     * Get approved reviews by facility
     */
    @Transactional(readOnly = true)
    public List<Review> getApprovedReviewsByFacility(Long facilityId) {
        return reviewRepository.findByFacilityIdAndStatus(facilityId, ReviewStatus.APPROVED);
    }

    /**
     * Get approved reviews by facility with pagination
     */
    @Transactional(readOnly = true)
    public Page<Review> getApprovedReviewsByFacility(Long facilityId, Pageable pageable) {
        return reviewRepository.findByFacilityIdAndStatus(facilityId, ReviewStatus.APPROVED, pageable);
    }

    /**
     * Get reviews by user
     */
    @Transactional(readOnly = true)
    public List<Review> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    /**
     * Get reviews by user with pagination
     */
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByUser(Long userId, Pageable pageable) {
        return reviewRepository.findByUserId(userId, pageable);
    }

    /**
     * Get latest approved reviews
     */
    @Transactional(readOnly = true)
    public Page<Review> getLatestApprovedReviews(Pageable pageable) {
        return reviewRepository.findLatestApprovedReviews(pageable);
    }

    /**
     * Get top rated reviews for a facility
     */
    @Transactional(readOnly = true)
    public Page<Review> getTopRatedReviewsByFacility(Long facilityId, Pageable pageable) {
        return reviewRepository.findTopRatedReviewsByFacility(facilityId, pageable);
    }

    /**
     * Search reviews by content
     */
    @Transactional(readOnly = true)
    public List<Review> searchReviewsByContent(String keyword) {
        return reviewRepository.searchByContent(keyword);
    }

    /**
     * Get reviews by rating range
     */
    @Transactional(readOnly = true)
    public List<Review> getReviewsByRatingRange(Integer minRating, Integer maxRating) {
        return reviewRepository.findByRatingBetween(minRating, maxRating);
    }

    /**
     * Get reviews within date range
     */
    @Transactional(readOnly = true)
    public List<Review> getReviewsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return reviewRepository.findByCreatedAtBetween(startDate, endDate);
    }

    /**
     * Get reviews with attachments
     */
    @Transactional(readOnly = true)
    public List<Review> getReviewsWithAttachments() {
        return reviewRepository.findReviewsWithAttachments();
    }

    /**
     * Approve review (admin only)
     */
    public Review approveReview(Long reviewId, Long adminId) {
        validateAdminPermission(adminId);
        Review review = getReviewById(reviewId);

        if (review.getStatus() != ReviewStatus.PENDING) {
            throw new IllegalStateException("Only pending reviews can be approved");
        }

        review.approve(adminId);
        return reviewRepository.save(review);
    }

    /**
     * Reject review (admin only)
     */
    public Review rejectReview(Long reviewId, Long adminId, String adminNotes) {
        validateAdminPermission(adminId);
        Review review = getReviewById(reviewId);

        if (review.getStatus() != ReviewStatus.PENDING) {
            throw new IllegalStateException("Only pending reviews can be rejected");
        }

        review.reject(adminId, adminNotes);
        return reviewRepository.save(review);
    }

    /**
     * Update review (user can only update their own pending reviews)
     */
    public Review updateReview(Long reviewId, Long userId, String content, Integer rating) {
        Review review = getReviewById(reviewId);

        // Check if user owns the review
        if (!review.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only update your own reviews");
        }

        // Only allow updates to pending reviews
        if (review.getStatus() != ReviewStatus.PENDING) {
            throw new IllegalStateException("Only pending reviews can be updated");
        }

        review.setContent(content);
        review.setRating(rating);
        return reviewRepository.save(review);
    }

    /**
     * Delete review (user can delete their own, admin can delete any)
     */
    public void deleteReview(Long reviewId, Long userId) {
        Review review = getReviewById(reviewId);
        User user = userService.getUserById(userId);

        // Check permission: user can delete their own, admin can delete any
        if (!review.getUser().getId().equals(userId) && user.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("You can only delete your own reviews");
        }

        // Delete associated file attachments first
        List<FileAttachment> attachments = fileAttachmentService.getAttachmentsByReview(reviewId);
        for (FileAttachment attachment : attachments) {
            fileAttachmentService.deleteFileAttachment(attachment.getId());
        }

        reviewRepository.delete(review);
    }

    /**
     * Calculate average rating for a facility
     */
    @Transactional(readOnly = true)
    public Double calculateAverageRating(Long facilityId) {
        Double average = reviewRepository.calculateAverageRatingByFacilityId(facilityId);
        return average != null ? average : 0.0;
    }

    /**
     * Get review statistics for a facility
     */
    @Transactional(readOnly = true)
    public ReviewStatistics getReviewStatistics(Long facilityId) {
        List<Object[]> stats = reviewRepository.getReviewStatisticsByFacility(facilityId);
        Map<Integer, Long> ratingCounts = stats.stream()
                .collect(Collectors.toMap(
                        result -> (Integer) result[0],
                        result -> ((Number) result[1]).longValue()
                ));

        long totalReviews = reviewRepository.countByFacilityIdAndStatus(facilityId, ReviewStatus.APPROVED);
        double averageRating = calculateAverageRating(facilityId);

        return new ReviewStatistics(totalReviews, averageRating, ratingCounts);
    }

    /**
     * Get review counts by status
     */
    @Transactional(readOnly = true)
    public Map<ReviewStatus, Long> getReviewCountsByStatus() {
        return Map.of(
                ReviewStatus.PENDING, reviewRepository.countByStatus(ReviewStatus.PENDING),
                ReviewStatus.APPROVED, reviewRepository.countByStatus(ReviewStatus.APPROVED),
                ReviewStatus.REJECTED, reviewRepository.countByStatus(ReviewStatus.REJECTED)
        );
    }

    /**
     * Get recent pending reviews (within last N days)
     */
    @Transactional(readOnly = true)
    public List<Review> getRecentPendingReviews(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        return reviewRepository.findRecentPendingReviews(cutoffDate);
    }

    /**
     * Check if user can review facility (prevent duplicate reviews)
     */
    @Transactional(readOnly = true)
    public boolean canUserReviewFacility(Long userId, Long facilityId) {
        List<Review> existingReviews = reviewRepository.findByUserIdAndFacilityId(userId, facilityId);
        // Allow only one review per user per facility
        return existingReviews.isEmpty();
    }

    /**
     * Get user's review for a specific facility
     */
    @Transactional(readOnly = true)
    public Review getUserReviewForFacility(Long userId, Long facilityId) {
        List<Review> reviews = reviewRepository.findByUserIdAndFacilityId(userId, facilityId);
        return reviews.isEmpty() ? null : reviews.get(0);
    }

    /**
     * Validate admin permission
     */
    private void validateAdminPermission(Long userId) {
        if (!userService.isAdmin(userId)) {
            throw new UnauthorizedException("Admin privileges required for this operation");
        }
    }

    /**
     * Review statistics helper class
     */
    public static class ReviewStatistics {
        private final long totalReviews;
        private final double averageRating;
        private final Map<Integer, Long> ratingCounts;

        public ReviewStatistics(long totalReviews, double averageRating, Map<Integer, Long> ratingCounts) {
            this.totalReviews = totalReviews;
            this.averageRating = averageRating;
            this.ratingCounts = ratingCounts;
        }

        public long getTotalReviews() {
            return totalReviews;
        }

        public double getAverageRating() {
            return averageRating;
        }

        public Map<Integer, Long> getRatingCounts() {
            return ratingCounts;
        }

        public long getRatingCount(int rating) {
            return ratingCounts.getOrDefault(rating, 0L);
        }
    }
}
