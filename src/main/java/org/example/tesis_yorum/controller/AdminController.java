package org.example.tesis_yorum.controller;

import jakarta.validation.Valid;
import org.example.tesis_yorum.entity.Review;
import org.example.tesis_yorum.entity.ReviewStatus;
import org.example.tesis_yorum.service.FileAttachmentService;
import org.example.tesis_yorum.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final ReviewService reviewService;
    private final FileAttachmentService fileAttachmentService;

    @Autowired
    public AdminController(ReviewService reviewService, FileAttachmentService fileAttachmentService) {
        this.reviewService = reviewService;
        this.fileAttachmentService = fileAttachmentService;
    }

    /**
     * Get all pending reviews for approval
     * GET /api/admin/reviews/pending
     */
    @GetMapping("/reviews/pending")
    public ResponseEntity<List<Review>> getPendingReviews() {
        List<Review> pendingReviews = reviewService.getPendingReviews();
        return ResponseEntity.ok(pendingReviews);
    }

    /**
     * Get reviews by status with pagination
     * GET /api/admin/reviews/status/{status}
     */
    @GetMapping("/reviews/status/{status}")
    public ResponseEntity<Page<Review>> getReviewsByStatus(
            @PathVariable ReviewStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Review> reviews = reviewService.getReviewsByStatus(status, pageable);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Approve a review
     * POST /api/admin/reviews/{reviewId}/approve
     */
    @PostMapping("/reviews/{reviewId}/approve")
    public ResponseEntity<Review> approveReview(
            @PathVariable Long reviewId,
            @RequestParam Long adminId) {

        Review approvedReview = reviewService.approveReview(reviewId, adminId);
        return ResponseEntity.ok(approvedReview);
    }

    /**
     * Reject a review
     * POST /api/admin/reviews/{reviewId}/reject
     */
    @PostMapping("/reviews/{reviewId}/reject")
    public ResponseEntity<Review> rejectReview(
            @PathVariable Long reviewId,
            @RequestParam Long adminId,
            @Valid @RequestBody RejectReviewRequest request) {

        Review rejectedReview = reviewService.rejectReview(reviewId, adminId, request.getAdminNotes());
        return ResponseEntity.ok(rejectedReview);
    }

    /**
     * Get review counts by status
     * GET /api/admin/reviews/stats/counts
     */
    @GetMapping("/reviews/stats/counts")
    public ResponseEntity<Map<ReviewStatus, Long>> getReviewCountsByStatus() {
        Map<ReviewStatus, Long> counts = reviewService.getReviewCountsByStatus();
        return ResponseEntity.ok(counts);
    }

    /**
     * Get recent pending reviews (within last N days)
     * GET /api/admin/reviews/recent-pending?days={days}
     */
    @GetMapping("/reviews/recent-pending")
    public ResponseEntity<List<Review>> getRecentPendingReviews(
            @RequestParam(defaultValue = "7") int days) {

        List<Review> recentPending = reviewService.getRecentPendingReviews(days);
        return ResponseEntity.ok(recentPending);
    }

    /**
     * Get all reviews (all statuses) with pagination
     * GET /api/admin/reviews/all
     */
    @GetMapping("/reviews/all")
    public ResponseEntity<Page<Review>> getAllReviewsForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // For admin, we can show all reviews regardless of status
        Page<Review> reviews = reviewService.getReviewsByStatus(ReviewStatus.PENDING, pageable);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Delete any review (admin privilege)
     * DELETE /api/admin/reviews/{reviewId}
     */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReviewAsAdmin(
            @PathVariable Long reviewId,
            @RequestParam Long adminId) {

        reviewService.deleteReview(reviewId, adminId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get file storage statistics
     * GET /api/admin/files/stats
     */
    @GetMapping("/files/stats")
    public ResponseEntity<FileAttachmentService.FileStatistics> getFileStatistics() {
        FileAttachmentService.FileStatistics stats = fileAttachmentService.getFileStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get file count by content type
     * GET /api/admin/files/stats/by-type
     */
    @GetMapping("/files/stats/by-type")
    public ResponseEntity<Map<String, Long>> getFileCountByType() {
        Map<String, Long> counts = fileAttachmentService.getAttachmentCountByContentType();
        return ResponseEntity.ok(counts);
    }

    /**
     * Get total storage used
     * GET /api/admin/files/storage-used
     */
    @GetMapping("/files/storage-used")
    public ResponseEntity<StorageInfo> getTotalStorageUsed() {
        Long totalBytes = fileAttachmentService.getTotalStorageUsed();
        String formatted = fileAttachmentService.getFormattedTotalStorageUsed();

        StorageInfo storageInfo = new StorageInfo(totalBytes, formatted);
        return ResponseEntity.ok(storageInfo);
    }

    /**
     * Get recent file uploads (within last N days)
     * GET /api/admin/files/recent?days={days}
     */
    @GetMapping("/files/recent")
    public ResponseEntity<List<org.example.tesis_yorum.entity.FileAttachment>> getRecentFileUploads(
            @RequestParam(defaultValue = "7") int days) {

        List<org.example.tesis_yorum.entity.FileAttachment> recentFiles = fileAttachmentService.getRecentUploads(days);
        return ResponseEntity.ok(recentFiles);
    }

    /**
     * Get large files (over specified size in MB)
     * GET /api/admin/files/large?sizeMB={sizeMB}
     */
    @GetMapping("/files/large")
    public ResponseEntity<List<org.example.tesis_yorum.entity.FileAttachment>> getLargeFiles(
            @RequestParam(defaultValue = "5") int sizeMB) {

        long sizeInBytes = sizeMB * 1024L * 1024L;
        List<org.example.tesis_yorum.entity.FileAttachment> largeFiles = fileAttachmentService.getLargeFiles(sizeInBytes);
        return ResponseEntity.ok(largeFiles);
    }

    /**
     * Find and clean up orphaned file attachments
     * POST /api/admin/files/cleanup-orphaned
     */
    @PostMapping("/files/cleanup-orphaned")
    public ResponseEntity<CleanupResult> cleanupOrphanedFiles() {
        int deletedCount = fileAttachmentService.cleanupOrphanedAttachments();
        CleanupResult result = new CleanupResult(deletedCount, "Orphaned files cleaned up successfully");
        return ResponseEntity.ok(result);
    }

    /**
     * Get system dashboard statistics
     * GET /api/admin/dashboard/stats
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        Map<ReviewStatus, Long> reviewCounts = reviewService.getReviewCountsByStatus();
        FileAttachmentService.FileStatistics fileStats = fileAttachmentService.getFileStatistics();

        DashboardStats stats = new DashboardStats(
                reviewCounts.get(ReviewStatus.PENDING),
                reviewCounts.get(ReviewStatus.APPROVED),
                reviewCounts.get(ReviewStatus.REJECTED),
                fileStats.getFileCount(),
                fileStats.getTotalSize(),
                fileStats.getFormattedTotalSize()
        );

        return ResponseEntity.ok(stats);
    }

    // Request/Response DTOs
    public static class RejectReviewRequest {
        private String adminNotes;

        public String getAdminNotes() { return adminNotes; }
        public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
    }

    public static class StorageInfo {
        private final Long totalBytes;
        private final String formatted;

        public StorageInfo(Long totalBytes, String formatted) {
            this.totalBytes = totalBytes;
            this.formatted = formatted;
        }

        public Long getTotalBytes() { return totalBytes; }
        public String getFormatted() { return formatted; }
    }

    public static class CleanupResult {
        private final int deletedCount;
        private final String message;

        public CleanupResult(int deletedCount, String message) {
            this.deletedCount = deletedCount;
            this.message = message;
        }

        public int getDeletedCount() { return deletedCount; }
        public String getMessage() { return message; }
    }

    public static class DashboardStats {
        private final Long pendingReviews;
        private final Long approvedReviews;
        private final Long rejectedReviews;
        private final Long totalFiles;
        private final Long totalStorageBytes;
        private final String totalStorageFormatted;

        public DashboardStats(Long pendingReviews, Long approvedReviews, Long rejectedReviews,
                              Long totalFiles, Long totalStorageBytes, String totalStorageFormatted) {
            this.pendingReviews = pendingReviews;
            this.approvedReviews = approvedReviews;
            this.rejectedReviews = rejectedReviews;
            this.totalFiles = totalFiles;
            this.totalStorageBytes = totalStorageBytes;
            this.totalStorageFormatted = totalStorageFormatted;
        }

        // Getters
        public Long getPendingReviews() { return pendingReviews; }
        public Long getApprovedReviews() { return approvedReviews; }
        public Long getRejectedReviews() { return rejectedReviews; }
        public Long getTotalFiles() { return totalFiles; }
        public Long getTotalStorageBytes() { return totalStorageBytes; }
        public String getTotalStorageFormatted() { return totalStorageFormatted; }

        public Long getTotalReviews() {
            return pendingReviews + approvedReviews + rejectedReviews;
        }
    }
}
