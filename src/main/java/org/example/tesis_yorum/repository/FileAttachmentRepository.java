package org.example.tesis_yorum.repository;

import org.example.tesis_yorum.entity.FileAttachment;
import org.example.tesis_yorum.entity.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {

    /**
     * Find attachments by review ID
     */
    List<FileAttachment> findByReviewId(Long reviewId);

    /**
     * Find attachment by stored filename
     */
    Optional<FileAttachment> findByStoredFilename(String storedFilename);

    /**
     * Find attachments by content type
     */
    List<FileAttachment> findByContentType(String contentType);

    /**
     * Find image attachments (JPEG and PNG)
     */
    @Query("SELECT f FROM FileAttachment f WHERE f.contentType IN ('image/jpeg', 'image/jpg', 'image/png')")
    List<FileAttachment> findImageAttachments();

    /**
     * Find attachments by file size range
     */
    @Query("SELECT f FROM FileAttachment f WHERE f.fileSize >= :minSize AND f.fileSize <= :maxSize")
    List<FileAttachment> findByFileSizeBetween(@Param("minSize") Long minSize, @Param("maxSize") Long maxSize);

    /**
     * Find large files (over specific size)
     */
    @Query("SELECT f FROM FileAttachment f WHERE f.fileSize > :sizeThreshold")
    List<FileAttachment> findLargeFiles(@Param("sizeThreshold") Long sizeThreshold);

    /**
     * Find attachments uploaded within date range
     */
    @Query("SELECT f FROM FileAttachment f WHERE f.createdAt >= :startDate AND f.createdAt <= :endDate")
    List<FileAttachment> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find attachments by review status (through review)
     */
    @Query("SELECT f FROM FileAttachment f WHERE f.review.status = :status")
    List<FileAttachment> findByReviewStatus(@Param("status") ReviewStatus status);

    /**
     * Find attachments for approved reviews only
     */
    @Query("SELECT f FROM FileAttachment f WHERE f.review.status = 'APPROVED'")
    List<FileAttachment> findAttachmentsForApprovedReviews();

    /**
     * Find attachments by facility (through review)
     */
    @Query("SELECT f FROM FileAttachment f WHERE f.review.facility.id = :facilityId")
    List<FileAttachment> findByFacilityId(@Param("facilityId") Long facilityId);

    /**
     * Find attachments by user (through review)
     */
    @Query("SELECT f FROM FileAttachment f WHERE f.review.user.id = :userId")
    List<FileAttachment> findByUserId(@Param("userId") Long userId);

    /**
     * Count attachments by review
     */
    long countByReviewId(Long reviewId);

    /**
     * Count total file size by review
     */
    @Query("SELECT SUM(f.fileSize) FROM FileAttachment f WHERE f.review.id = :reviewId")
    Long getTotalFileSizeByReview(@Param("reviewId") Long reviewId);

    /**
     * Count attachments by content type
     */
    @Query("SELECT f.contentType, COUNT(f) FROM FileAttachment f GROUP BY f.contentType")
    List<Object[]> countAttachmentsByContentType();

    /**
     * Calculate total storage used
     */
    @Query("SELECT SUM(f.fileSize) FROM FileAttachment f")
    Long getTotalStorageUsed();

    /**
     * Find orphaned attachments (reviews that were deleted but files remain)
     * Note: This assumes soft delete or cascade delete issues
     */
    @Query("SELECT f FROM FileAttachment f WHERE f.review IS NULL")
    List<FileAttachment> findOrphanedAttachments();

    /**
     * Find recent uploads (within last N days)
     */
    @Query("SELECT f FROM FileAttachment f WHERE f.createdAt >= :cutoffDate ORDER BY f.createdAt DESC")
    List<FileAttachment> findRecentUploads(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find attachments by original filename pattern
     */
    @Query("SELECT f FROM FileAttachment f WHERE LOWER(f.originalFilename) LIKE LOWER(CONCAT('%', :pattern, '%'))")
    List<FileAttachment> findByOriginalFilenameContaining(@Param("pattern") String pattern);

    /**
     * Check if stored filename exists
     */
    boolean existsByStoredFilename(String storedFilename);

    /**
     * Get file statistics (count, total size, avg size)
     */
    @Query("SELECT COUNT(f), SUM(f.fileSize), AVG(f.fileSize) FROM FileAttachment f")
    Object[] getFileStatistics();
}
