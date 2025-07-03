package org.example.tesis_yorum.service;

import org.example.tesis_yorum.entity.FileAttachment;
import org.example.tesis_yorum.entity.Review;
import org.example.tesis_yorum.entity.ReviewStatus;
import org.example.tesis_yorum.exceptions.ResourceNotFoundException;
import org.example.tesis_yorum.repository.FileAttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class FileAttachmentService {

    private final FileAttachmentRepository fileAttachmentRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public FileAttachmentService(FileAttachmentRepository fileAttachmentRepository,
                                 FileStorageService fileStorageService) {
        this.fileAttachmentRepository = fileAttachmentRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Create file attachment for a review
     */
    public FileAttachment createFileAttachment(Review review, MultipartFile file) {
        if (review == null) {
            throw new IllegalArgumentException("Review cannot be null");
        }

        // Store the file and get the stored filename
        String storedFilename = fileStorageService.storeFile(file);

        // Create file attachment entity
        FileAttachment attachment = new FileAttachment(
                file.getOriginalFilename(),
                storedFilename,
                fileStorageService.getFileStorageLocation().resolve(storedFilename).toString(),
                file.getContentType(),
                file.getSize(),
                review
        );

        return fileAttachmentRepository.save(attachment);
    }

    /**
     * Get file attachment by ID
     */
    @Transactional(readOnly = true)
    public FileAttachment getFileAttachmentById(Long id) {
        return fileAttachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File attachment not found with id: " + id));
    }

    /**
     * Get file attachment by stored filename
     */
    @Transactional(readOnly = true)
    public FileAttachment getFileAttachmentByStoredFilename(String storedFilename) {
        return fileAttachmentRepository.findByStoredFilename(storedFilename)
                .orElseThrow(() -> new ResourceNotFoundException("File attachment not found with filename: " + storedFilename));
    }

    /**
     * Get attachments by review ID
     */
    @Transactional(readOnly = true)
    public List<FileAttachment> getAttachmentsByReview(Long reviewId) {
        return fileAttachmentRepository.findByReviewId(reviewId);
    }

    /**
     * Get attachments by user ID (through reviews)
     */
    @Transactional(readOnly = true)
    public List<FileAttachment> getAttachmentsByUser(Long userId) {
        return fileAttachmentRepository.findByUserId(userId);
    }

    /**
     * Get attachments by facility ID (through reviews)
     */
    @Transactional(readOnly = true)
    public List<FileAttachment> getAttachmentsByFacility(Long facilityId) {
        return fileAttachmentRepository.findByFacilityId(facilityId);
    }

    /**
     * Get attachments for approved reviews only
     */
    @Transactional(readOnly = true)
    public List<FileAttachment> getAttachmentsForApprovedReviews() {
        return fileAttachmentRepository.findAttachmentsForApprovedReviews();
    }

    /**
     * Get attachments by review status
     */
    @Transactional(readOnly = true)
    public List<FileAttachment> getAttachmentsByReviewStatus(ReviewStatus status) {
        return fileAttachmentRepository.findByReviewStatus(status);
    }

    /**
     * Get image attachments only
     */
    @Transactional(readOnly = true)
    public List<FileAttachment> getImageAttachments() {
        return fileAttachmentRepository.findImageAttachments();
    }

    /**
     * Get large files (over specified size)
     */
    @Transactional(readOnly = true)
    public List<FileAttachment> getLargeFiles(Long sizeThreshold) {
        return fileAttachmentRepository.findLargeFiles(sizeThreshold);
    }

    /**
     * Get attachments within date range
     */
    @Transactional(readOnly = true)
    public List<FileAttachment> getAttachmentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return fileAttachmentRepository.findByCreatedAtBetween(startDate, endDate);
    }

    /**
     * Get recent uploads (within last N days)
     */
    @Transactional(readOnly = true)
    public List<FileAttachment> getRecentUploads(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        return fileAttachmentRepository.findRecentUploads(cutoffDate);
    }

    /**
     * Load file as Resource for download/display
     */
    @Transactional(readOnly = true)
    public Resource loadFileAsResource(String storedFilename) {
        // Verify the file attachment exists in database
        getFileAttachmentByStoredFilename(storedFilename);

        // Load the actual file
        return fileStorageService.loadFileAsResource(storedFilename);
    }

    /**
     * Delete file attachment
     */
    public void deleteFileAttachment(Long id) {
        FileAttachment attachment = getFileAttachmentById(id);

        // Delete the physical file
        fileStorageService.deleteFile(attachment.getStoredFilename());

        // Delete the database record
        fileAttachmentRepository.delete(attachment);
    }

    /**
     * Delete attachments by review (when review is deleted)
     */
    public void deleteAttachmentsByReview(Long reviewId) {
        List<FileAttachment> attachments = getAttachmentsByReview(reviewId);
        for (FileAttachment attachment : attachments) {
            deleteFileAttachment(attachment.getId());
        }
    }

    /**
     * Count attachments by review
     */
    @Transactional(readOnly = true)
    public long countAttachmentsByReview(Long reviewId) {
        return fileAttachmentRepository.countByReviewId(reviewId);
    }

    /**
     * Get total file size by review
     */
    @Transactional(readOnly = true)
    public Long getTotalFileSizeByReview(Long reviewId) {
        Long totalSize = fileAttachmentRepository.getTotalFileSizeByReview(reviewId);
        return totalSize != null ? totalSize : 0L;
    }

    /**
     * Get file statistics
     */
    @Transactional(readOnly = true)
    public FileStatistics getFileStatistics() {
        Object[] stats = fileAttachmentRepository.getFileStatistics();
        long fileCount = stats[0] != null ? ((Number) stats[0]).longValue() : 0;
        long totalSize = stats[1] != null ? ((Number) stats[1]).longValue() : 0;
        double averageSize = stats[2] != null ? ((Number) stats[2]).doubleValue() : 0.0;

        return new FileStatistics(fileCount, totalSize, averageSize);
    }

    /**
     * Get attachment count by content type
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getAttachmentCountByContentType() {
        List<Object[]> results = fileAttachmentRepository.countAttachmentsByContentType();
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> ((Number) result[1]).longValue()
                ));
    }

    /**
     * Get total storage used
     */
    @Transactional(readOnly = true)
    public Long getTotalStorageUsed() {
        Long totalSize = fileAttachmentRepository.getTotalStorageUsed();
        return totalSize != null ? totalSize : 0L;
    }

    /**
     * Get formatted total storage used
     */
    @Transactional(readOnly = true)
    public String getFormattedTotalStorageUsed() {
        return fileStorageService.formatFileSize(getTotalStorageUsed());
    }

    /**
     * Find orphaned attachments
     */
    @Transactional(readOnly = true)
    public List<FileAttachment> findOrphanedAttachments() {
        return fileAttachmentRepository.findOrphanedAttachments();
    }

    /**
     * Clean up orphaned attachments
     */
    public int cleanupOrphanedAttachments() {
        List<FileAttachment> orphaned = findOrphanedAttachments();
        for (FileAttachment attachment : orphaned) {
            deleteFileAttachment(attachment.getId());
        }
        return orphaned.size();
    }

    /**
     * Search attachments by original filename
     */
    @Transactional(readOnly = true)
    public List<FileAttachment> searchByOriginalFilename(String pattern) {
        return fileAttachmentRepository.findByOriginalFilenameContaining(pattern);
    }

    /**
     * Check if file exists in storage
     */
    @Transactional(readOnly = true)
    public boolean fileExistsInStorage(String storedFilename) {
        return fileStorageService.fileExists(storedFilename);
    }

    /**
     * Validate file attachment integrity (database record vs physical file)
     */
    @Transactional(readOnly = true)
    public boolean validateFileIntegrity(Long attachmentId) {
        FileAttachment attachment = getFileAttachmentById(attachmentId);
        return fileStorageService.fileExists(attachment.getStoredFilename());
    }

    /**
     * Get file content type information
     */
    @Transactional(readOnly = true)
    public String getFileContentType(Long attachmentId) {
        FileAttachment attachment = getFileAttachmentById(attachmentId);
        return attachment.getContentType();
    }

    /**
     * File statistics helper class
     */
    public static class FileStatistics {
        private final long fileCount;
        private final long totalSize;
        private final double averageSize;

        public FileStatistics(long fileCount, long totalSize, double averageSize) {
            this.fileCount = fileCount;
            this.totalSize = totalSize;
            this.averageSize = averageSize;
        }

        public long getFileCount() { return fileCount; }
        public long getTotalSize() { return totalSize; }
        public double getAverageSize() { return averageSize; }

        public String getFormattedTotalSize() {
            return formatBytes(totalSize);
        }

        public String getFormattedAverageSize() {
            return formatBytes((long) averageSize);
        }

        private String formatBytes(long bytes) {
            if (bytes < 1024) return bytes + " B";
            if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
            if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}