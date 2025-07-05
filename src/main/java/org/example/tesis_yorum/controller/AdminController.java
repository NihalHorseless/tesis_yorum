package org.example.tesis_yorum.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Adminler", description = "Admin işlemleri")
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
    @Operation(
            summary = "Onay Bekleyen Yorumları Göster",
            description = "Onay Bekleyen Yorumları Gösterir.")
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
    @Operation(
            summary = "Onay Bekleyen Yorumu Onayla",
            description = "Onay Bekleyen Yorumu girilen Yorum ID'sine göre onayla.")
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
    @Operation(
            summary = "Onay Bekleyen Yorumu Reddet",
            description = "Onay Bekleyen Yorumu girilen Yorum ID'sine göre reddet.")
    @PostMapping("/reviews/{reviewId}/reject")
    public ResponseEntity<Review> rejectReview(
            @PathVariable Long reviewId,
            @RequestParam Long adminId,
            @Valid @RequestBody RejectReviewRequest request) {

        Review rejectedReview = reviewService.rejectReview(reviewId, adminId, request.getAdminNotes());
        return ResponseEntity.ok(rejectedReview);
    }


    /**
     * Get all reviews (all statuses) with pagination
     * GET /api/admin/reviews/all
     */
    @Operation(
            summary = "Bütün Yorumları Göster",
            description = "Bütün Yorumları Onaysız veya Onaylı Farketmeden Gösterir.")
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
    @Operation(
            summary = "Herhangi bir Yorumu Sil",
            description = "Admin Yetkisiyle girilen Yorum ID'ye göre Yorum siler.")
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReviewAsAdmin(
            @PathVariable Long reviewId,
            @RequestParam Long adminId) {

        reviewService.deleteReview(reviewId, adminId);
        return ResponseEntity.noContent().build();
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
