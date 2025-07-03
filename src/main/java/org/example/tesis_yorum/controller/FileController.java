package org.example.tesis_yorum.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.tesis_yorum.entity.FileAttachment;
import org.example.tesis_yorum.service.FileAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {

    private final FileAttachmentService fileAttachmentService;

    @Autowired
    public FileController(FileAttachmentService fileAttachmentService) {
        this.fileAttachmentService = fileAttachmentService;
    }

    /**
     * Download/view file by stored filename
     * GET /api/files/{filename}
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename,
                                                 HttpServletRequest request) {

        // Load file as Resource
        Resource resource = fileAttachmentService.loadFileAsResource(filename);

        // Get file attachment metadata
        FileAttachment attachment = fileAttachmentService.getFileAttachmentByStoredFilename(filename);

        // Try to determine file's content type
        String contentType = attachment.getContentType();
        if (contentType == null) {
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                contentType = "application/octet-stream";
            }
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + attachment.getOriginalFilename() + "\"")
                .body(resource);
    }

    /**
     * Force download file (with attachment disposition)
     * GET /api/files/{filename}/download
     */
    @GetMapping("/{filename:.+}/download")
    public ResponseEntity<Resource> forceDownloadFile(@PathVariable String filename) {

        // Load file as Resource
        Resource resource = fileAttachmentService.loadFileAsResource(filename);

        // Get file attachment metadata
        FileAttachment attachment = fileAttachmentService.getFileAttachmentByStoredFilename(filename);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + attachment.getOriginalFilename() + "\"")
                .body(resource);
    }

    /**
     * Get file attachment metadata by ID
     * GET /api/files/metadata/{id}
     */
    @GetMapping("/metadata/{id}")
    public ResponseEntity<FileAttachment> getFileMetadata(@PathVariable Long id) {
        FileAttachment attachment = fileAttachmentService.getFileAttachmentById(id);
        return ResponseEntity.ok(attachment);
    }

    /**
     * Get file attachments by review ID
     * GET /api/files/review/{reviewId}
     */
    @GetMapping("/review/{reviewId}")
    public ResponseEntity<List<FileAttachment>> getFilesByReview(@PathVariable Long reviewId) {
        List<FileAttachment> attachments = fileAttachmentService.getAttachmentsByReview(reviewId);
        return ResponseEntity.ok(attachments);
    }

    /**
     * Get file attachments by user ID
     * GET /api/files/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FileAttachment>> getFilesByUser(@PathVariable Long userId) {
        List<FileAttachment> attachments = fileAttachmentService.getAttachmentsByUser(userId);
        return ResponseEntity.ok(attachments);
    }

    /**
     * Get file attachments by facility ID
     * GET /api/files/facility/{facilityId}
     */
    @GetMapping("/facility/{facilityId}")
    public ResponseEntity<List<FileAttachment>> getFilesByFacility(@PathVariable Long facilityId) {
        List<FileAttachment> attachments = fileAttachmentService.getAttachmentsByFacility(facilityId);
        return ResponseEntity.ok(attachments);
    }

    /**
     * Get all image attachments for approved reviews
     * GET /api/files/images/approved
     */
    @GetMapping("/images/approved")
    public ResponseEntity<List<FileAttachment>> getApprovedImages() {
        List<FileAttachment> attachments = fileAttachmentService.getAttachmentsForApprovedReviews();

        // Filter only images
        List<FileAttachment> imageAttachments = attachments.stream()
                .filter(FileAttachment::isImage)
                .toList();

        return ResponseEntity.ok(imageAttachments);
    }

    /**
     * Search files by original filename
     * GET /api/files/search?q={pattern}
     */
    @GetMapping("/search")
    public ResponseEntity<List<FileAttachment>> searchFiles(@RequestParam String q) {
        List<FileAttachment> attachments = fileAttachmentService.searchByOriginalFilename(q);
        return ResponseEntity.ok(attachments);
    }

    /**
     * Check if file exists in storage
     * GET /api/files/{filename}/exists
     */
    @GetMapping("/{filename:.+}/exists")
    public ResponseEntity<Boolean> checkFileExists(@PathVariable String filename) {
        boolean exists = fileAttachmentService.fileExistsInStorage(filename);
        return ResponseEntity.ok(exists);
    }

    /**
     * Validate file integrity (database record vs physical file)
     * GET /api/files/metadata/{id}/validate
     */
    @GetMapping("/metadata/{id}/validate")
    public ResponseEntity<FileValidationResult> validateFile(@PathVariable Long id) {
        boolean isValid = fileAttachmentService.validateFileIntegrity(id);
        FileAttachment attachment = fileAttachmentService.getFileAttachmentById(id);

        FileValidationResult result = new FileValidationResult(
                id,
                attachment.getStoredFilename(),
                attachment.getOriginalFilename(),
                isValid,
                isValid ? "File is valid" : "File not found in storage"
        );

        return ResponseEntity.ok(result);
    }

    /**
     * Get file info without downloading
     * GET /api/files/{filename}/info
     */
    @GetMapping("/{filename:.+}/info")
    public ResponseEntity<FileInfo> getFileInfo(@PathVariable String filename) {
        FileAttachment attachment = fileAttachmentService.getFileAttachmentByStoredFilename(filename);

        FileInfo info = new FileInfo(
                attachment.getId(),
                attachment.getOriginalFilename(),
                attachment.getStoredFilename(),
                attachment.getContentType(),
                attachment.getFileSize(),
                attachment.getFileSizeFormatted(),
                attachment.isImage(),
                attachment.getCreatedAt()
        );

        return ResponseEntity.ok(info);
    }

    /**
     * Delete file attachment (user can delete their own, admin can delete any)
     * DELETE /api/files/metadata/{id}
     */
    @DeleteMapping("/metadata/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id, @RequestParam Long userId) {
        FileAttachment attachment = fileAttachmentService.getFileAttachmentById(id);

        // Check if user owns the file (through review) or is admin
        // This would need proper authentication/authorization in a real app
        // For now, we'll allow deletion if the user ID matches the review's user ID
        if (!attachment.getReview().getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        fileAttachmentService.deleteFileAttachment(id);
        return ResponseEntity.noContent().build();
    }

    // Response DTOs
    public static class FileValidationResult {
        private final Long fileId;
        private final String storedFilename;
        private final String originalFilename;
        private final boolean isValid;
        private final String message;

        public FileValidationResult(Long fileId, String storedFilename, String originalFilename,
                                    boolean isValid, String message) {
            this.fileId = fileId;
            this.storedFilename = storedFilename;
            this.originalFilename = originalFilename;
            this.isValid = isValid;
            this.message = message;
        }

        // Getters
        public Long getFileId() { return fileId; }
        public String getStoredFilename() { return storedFilename; }
        public String getOriginalFilename() { return originalFilename; }
        public boolean isValid() { return isValid; }
        public String getMessage() { return message; }
    }

    public static class FileInfo {
        private final Long id;
        private final String originalFilename;
        private final String storedFilename;
        private final String contentType;
        private final Long fileSize;
        private final String fileSizeFormatted;
        private final boolean isImage;
        private final java.time.LocalDateTime uploadedAt;

        public FileInfo(Long id, String originalFilename, String storedFilename, String contentType,
                        Long fileSize, String fileSizeFormatted, boolean isImage,
                        java.time.LocalDateTime uploadedAt) {
            this.id = id;
            this.originalFilename = originalFilename;
            this.storedFilename = storedFilename;
            this.contentType = contentType;
            this.fileSize = fileSize;
            this.fileSizeFormatted = fileSizeFormatted;
            this.isImage = isImage;
            this.uploadedAt = uploadedAt;
        }

        // Getters
        public Long getId() { return id; }
        public String getOriginalFilename() { return originalFilename; }
        public String getStoredFilename() { return storedFilename; }
        public String getContentType() { return contentType; }
        public Long getFileSize() { return fileSize; }
        public String getFileSizeFormatted() { return fileSizeFormatted; }
        public boolean isImage() { return isImage; }
        public java.time.LocalDateTime getUploadedAt() { return uploadedAt; }
    }
}
