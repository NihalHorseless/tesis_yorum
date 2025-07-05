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
     * Find approved reviews by facility ID with pagination
     */
    Page<Review> findByFacilityIdAndStatus(Long facilityId, ReviewStatus status, Pageable pageable);

    /**
     * Find reviews by user and facility
     */
    List<Review> findByUserIdAndFacilityId(Long userId, Long facilityId);

    /**
     * Count reviews by facility and status
     */
    long countByFacilityIdAndStatus(Long facilityId, ReviewStatus status);


    /**
     * Calculate average rating for a facility (approved reviews only)
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.facility.id = :facilityId AND r.status = 'APPROVED'")
    Double calculateAverageRatingByFacilityId(@Param("facilityId") Long facilityId);

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
     * Get review statistics by facility
     */
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.facility.id = :facilityId AND r.status = 'APPROVED' GROUP BY r.rating ORDER BY r.rating")
    List<Object[]> getReviewStatisticsByFacility(@Param("facilityId") Long facilityId);

}
