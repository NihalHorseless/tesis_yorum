package org.example.tesis_yorum.repository;

import org.example.tesis_yorum.entity.Review;
import org.example.tesis_yorum.entity.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByStatus(ReviewStatus status);

    List<Review> findByStatusOrderByCreatedAtAsc(ReviewStatus status);

    List<Review> findByUserId(Long userId);

    List<Review> findByFacilityIdAndStatus(Long facilityId, ReviewStatus status);

    long countByFacilityIdAndStatus(Long facilityId, ReviewStatus status);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.facility.id = :facilityId AND r.status = 'APPROVED'")
    Double calculateAverageRatingByFacilityId(@Param("facilityId") Long facilityId);

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.facility.id = :facilityId AND r.status = 'APPROVED' GROUP BY r.rating ORDER BY r.rating")
    List<Object[]> getReviewStatisticsByFacility(@Param("facilityId") Long facilityId);

}
