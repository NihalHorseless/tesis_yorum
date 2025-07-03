package org.example.tesis_yorum.repository;

import org.example.tesis_yorum.entity.Review;
import org.example.tesis_yorum.entity.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Find reviews by status
     */
    List<Review> findByStatus(ReviewStatus status);

    /**
     * Find reviews by status with pagination
     */
    Page<Review> findByStatus(ReviewStatus status, Pageable pageable);

    /**
     * Find pending reviews (for admin approval)
     */
    List<Review> findByStatusOrderByCreatedAtAsc(ReviewStatus status);

    /**
     * Find reviews by user ID
     */
    List<Review> findByUserId(Long userId);

    /**
     * Find reviews by user ID with pagination
     */
    Page<Review> findByUserId(Long userId, Pageable pageable);

    /**
     * Find reviews by facility ID
     */
    List<Review> findByFacilityId(Long facilityId);

    /**
     * Find approved reviews by facility ID
     */
    List<Review> findByFacilityIdAndStatus(Long facilityId, ReviewStatus status);

    /**
     * Find approved reviews by facility ID with pagination
     */
    Page<Review> findByFacilityIdAndStatus(Long facilityId, ReviewStatus status, Pageable pageable);

    /**
     * Find reviews by user and facility
     */
    List<Review> findByUserIdAndFacilityId(Long userId, Long facilityId);

    /**
     * Find reviews by rating
     */
    List<Review> findByRating(Integer rating);

    /**
     * Find reviews by rating range
     */
    @Query("SELECT r FROM Review r WHERE r.rating >= :minRating AND r.rating <= :maxRating AND r.status = 'APPROVED'")
    List<Review> findByRatingBetween(@Param("minRating") Integer minRating, @Param("maxRating") Integer maxRating);

    /**
     * Find reviews created within date range
     */
    @Query("SELECT r FROM Review r WHERE r.createdAt >= :startDate AND r.createdAt <= :endDate")
    List<Review> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find reviews approved by specific admin
     */
    List<Review> findByApprovedBy(Long adminId);

    /**
     * Count reviews by status
     */
    long countByStatus(ReviewStatus status);

    /**
     * Count reviews by facility and status
     */
    long countByFacilityIdAndStatus(Long facilityId, ReviewStatus status);

    /**
     * Count reviews by user
     */
    long countByUserId(Long userId);

    /**
     * Calculate average rating for a facility (approved reviews only)
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.facility.id = :facilityId AND r.status = 'APPROVED'")
    Double calculateAverageRatingByFacilityId(@Param("facilityId") Long facilityId);

    /**
     * Find latest approved reviews
     */
    @Query("SELECT r FROM Review r WHERE r.status = 'APPROVED' ORDER BY r.createdAt DESC")
    Page<Review> findLatestApprovedReviews(Pageable pageable);

    /**
     * Find reviews with attachments
     */
    @Query("SELECT r FROM Review r WHERE SIZE(r.attachments) > 0")
    List<Review> findReviewsWithAttachments();

    /**
     * Find reviews without attachments
     */
    @Query("SELECT r FROM Review r WHERE SIZE(r.attachments) = 0")
    List<Review> findReviewsWithoutAttachments();

    /**
     * Search reviews by content (case insensitive)
     */
    @Query("SELECT r FROM Review r WHERE LOWER(r.content) LIKE LOWER(CONCAT('%', :keyword, '%')) AND r.status = 'APPROVED'")
    List<Review> searchByContent(@Param("keyword") String keyword);

    /**
     * Find top rated reviews for a facility
     */
    @Query("SELECT r FROM Review r WHERE r.facility.id = :facilityId AND r.status = 'APPROVED' ORDER BY r.rating DESC, r.createdAt DESC")
    Page<Review> findTopRatedReviewsByFacility(@Param("facilityId") Long facilityId, Pageable pageable);

    /**
     * Get review statistics by facility
     */
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.facility.id = :facilityId AND r.status = 'APPROVED' GROUP BY r.rating ORDER BY r.rating")
    List<Object[]> getReviewStatisticsByFacility(@Param("facilityId") Long facilityId);

    /**
     * Find recent reviews pending approval (within last N days)
     */
    @Query("SELECT r FROM Review r WHERE r.status = 'PENDING' AND r.createdAt >= :cutoffDate ORDER BY r.createdAt ASC")
    List<Review> findRecentPendingReviews(@Param("cutoffDate") LocalDateTime cutoffDate);
}
