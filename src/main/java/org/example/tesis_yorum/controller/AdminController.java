package org.example.tesis_yorum.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.tesis_yorum.entity.Review;
import org.example.tesis_yorum.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@Tag(name = "Adminler", description = "Admin işlemleri")
public class AdminController {

    private final ReviewService reviewService;

    @Autowired
    public AdminController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }


    @Operation(
            summary = "Onay Bekleyen Yorumları Göster",
            description = "Onay Bekleyen Yorumları Gösterir.")
    @GetMapping("/reviews/pending")
    public ResponseEntity<List<Review>> getPendingReviews() {
        List<Review> pendingReviews = reviewService.getPendingReviews();
        return ResponseEntity.ok(pendingReviews);
    }


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


    @Operation(
            summary = "Bütün Yorumları Göster",
            description = "Bütün Yorumları Onaysız veya Onaylı Farketmeden Gösterir.")
    @GetMapping("/reviews/all")
    public ResponseEntity<List<Review>> getAllReviewsForAdmin() {


        // For admin, we can show all reviews regardless of status
        List<Review> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

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


    // Request/Response DTOs
    public static class RejectReviewRequest {
        private String adminNotes;

        public String getAdminNotes() { return adminNotes; }
        public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
    }




}
