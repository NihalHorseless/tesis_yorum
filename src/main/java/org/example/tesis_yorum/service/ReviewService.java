package org.example.tesis_yorum.service;

import org.example.tesis_yorum.entity.*;
import org.example.tesis_yorum.exceptions.ResourceNotFoundException;
import org.example.tesis_yorum.exceptions.UnauthorizedException;
import org.example.tesis_yorum.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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


    public Review createReview(Long userId, Long facilityId, String content, Integer rating, List<MultipartFile> files) {
        User user = userService.getUserById(userId);
        Facility facility = facilityService.getFacilityById(facilityId);


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


    public Review createReview(Long userId, Long facilityId, String content, Integer rating) {
        return createReview(userId, facilityId, content, rating, null);
    }


    @Transactional(readOnly = true)
    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
    }


    @Transactional(readOnly = true)
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }


    @Transactional(readOnly = true)
    public List<Review> getReviewsByStatus(ReviewStatus status) {
        return reviewRepository.findByStatus(status);
    }


    @Transactional(readOnly = true)
    public List<Review> getPendingReviews() {
        return reviewRepository.findByStatusOrderByCreatedAtAsc(ReviewStatus.PENDING);
    }


    @Transactional(readOnly = true)
    public List<Review> getApprovedReviewsByFacility(Long facilityId) {
        return reviewRepository.findByFacilityIdAndStatus(facilityId, ReviewStatus.APPROVED);
    }


    @Transactional(readOnly = true)
    public List<Review> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    public Review approveReview(Long reviewId, Long adminId) {
        validateAdminPermission(adminId);
        Review review = getReviewById(reviewId);

        if (review.getStatus() != ReviewStatus.PENDING) {
            throw new IllegalStateException("Only pending reviews can be approved");
        }

        review.approve(adminId);
        return reviewRepository.save(review);
    }

    public Review rejectReview(Long reviewId, Long adminId, String adminNotes) {
        validateAdminPermission(adminId);
        Review review = getReviewById(reviewId);

        if (review.getStatus() != ReviewStatus.PENDING) {
            throw new IllegalStateException("Only pending reviews can be rejected");
        }

        review.reject(adminId, adminNotes);
        return reviewRepository.save(review);
    }

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

    @Transactional(readOnly = true)
    public Double calculateAverageRating(Long facilityId) {
        Double average = reviewRepository.calculateAverageRatingByFacilityId(facilityId);
        return average != null ? average : 0.0;
    }

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


    private void validateAdminPermission(Long userId) {
        if (!userService.isAdmin(userId)) {
            throw new UnauthorizedException("Admin privileges required for this operation");
        }
    }

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
