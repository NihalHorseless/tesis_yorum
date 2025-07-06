package org.example.tesis_yorum.service;

import org.example.tesis_yorum.entity.FileAttachment;
import org.example.tesis_yorum.entity.Review;
import org.example.tesis_yorum.exceptions.ResourceNotFoundException;
import org.example.tesis_yorum.repository.FileAttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
     * Get attachments by review ID
     */
    @Transactional(readOnly = true)
    public List<FileAttachment> getAttachmentsByReview(Long reviewId) {
        return fileAttachmentRepository.findByReviewId(reviewId);
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


}