package org.example.tesis_yorum.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * HANDLE RESOURCE NOT FOUND (404 errors)
     *
     * @ExceptionHandler(ResourceNotFoundException.class) - This annotation tells Spring:
     * "Whenever a ResourceNotFoundException is thrown anywhere in the app, call this method"
     *
     * Example scenarios:
     * - User tries to get review with ID 999 that doesn't exist
     * - User tries to access facility ID 123 that was deleted
     * - User searches for username "john123" that doesn't exist
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {

        // Create a structured error response instead of showing raw exception
        ErrorResponse errorResponse = new ErrorResponse(
                404,                           // HTTP status code
                "Resource Not Found",          // Error category
                ex.getMessage(),               // Specific error message
                request.getDescription(false), // Request path that caused error
                LocalDateTime.now()            // When the error occurred
        );

        // Return HTTP 404 with our custom error structure
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * HANDLE UNAUTHORIZED ACCESS (403 errors)
     *
     * Example scenarios:
     * - Regular user tries to approve/reject reviews (admin only)
     * - User tries to edit someone else's review
     * - User tries to delete facility they don't own
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            UnauthorizedException ex, WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                403,
                "Access Denied",
                ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * HANDLE FILE STORAGE PROBLEMS (500 errors)
     *
     * Example scenarios:
     * - Disk is full, can't save uploaded file
     * - File upload directory doesn't exist
     * - Permission issues writing to file system
     * - File corruption during upload
     */
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponse> handleFileStorageException(
            FileStorageException ex, WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                500,
                "File Storage Error",
                ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * HANDLE INVALID FILES (400 errors)
     *
     * Example scenarios:
     * - User uploads .txt file instead of image
     * - User uploads 15MB file (over 10MB limit)
     * - User uploads corrupted image
     * - File has invalid extension (.exe, .pdf, etc.)
     */
    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFileException(
            InvalidFileException ex, WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                400,
                "Invalid File",
                ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * HANDLE ILLEGAL ARGUMENTS (400 errors)
     *
     * Example scenarios:
     * - Trying to create user with username that already exists
     * - Trying to create user with email that already exists
     * - Passing null values where they're not allowed
     * - Invalid data format (like negative rating)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                400,
                "Invalid Request",
                ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * HANDLE ILLEGAL STATE (409 errors)
     *
     * Example scenarios:
     * - Trying to approve a review that's already approved
     * - Trying to edit a review that's already been approved
     * - Trying to delete a facility that has active reviews
     * - Business rule violations
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex, WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                409,                    // 409 = Conflict
                "Operation Not Allowed",
                ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * HANDLE VALIDATION ERRORS FROM @Valid (400 errors)
     *
     * This handles when Spring's @Valid annotation finds problems with request data.
     *
     * Example scenarios:
     * - User submits review with rating = 6 (max is 5)
     * - User submits review with empty content
     * - User submits email without @ symbol
     * - Required fields are missing
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        // Extract all field-specific validation errors
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        // Create response that shows exactly which fields failed validation
        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
                400,
                "Validation Failed",
                "Request validation failed",
                request.getDescription(false),
                LocalDateTime.now(),
                fieldErrors  // This shows: {"rating": "must be between 1 and 5", "content": "must not be blank"}
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * HANDLE CONSTRAINT VIOLATIONS (400 errors)
     *
     * This handles validation errors from method parameters
     * (like @Min, @Max annotations on controller parameters)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();

        for (ConstraintViolation<?> violation : violations) {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            fieldErrors.put(fieldName, errorMessage);
        }

        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
                400,
                "Validation Failed",
                "Request validation failed",
                request.getDescription(false),
                LocalDateTime.now(),
                fieldErrors
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * HANDLE FILE TOO LARGE (400 errors)
     *
     * This is triggered when Spring detects file upload exceeds the configured limit
     * BEFORE our custom validation even runs.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxSizeException(
            MaxUploadSizeExceededException ex, WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                400,
                "File Too Large",
                "File size exceeds the maximum allowed limit of 10MB",
                request.getDescription(false),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * HANDLE UNEXPECTED RUNTIME ERRORS (500 errors)
     *
     * This catches any RuntimeException that we didn't specifically handle above.
     * It's a safety net for unexpected programming errors.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                500,
                "Internal Server Error",
                "An unexpected error occurred: " + ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * HANDLE ANY OTHER EXCEPTION (500 errors)
     *
     * This is the ultimate fallback - catches ANYTHING that wasn't caught above.
     * Prevents the application from crashing and showing stack traces to users.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                500,
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request.getDescription(false),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * STANDARD ERROR RESPONSE CLASS
     *
     * This creates a consistent error response format for all errors.
     * Instead of random error formats, every error will look like this:
     *
     * {
     *   "status": 404,
     *   "error": "Resource Not Found",
     *   "message": "User not found with id: 123",
     *   "path": "uri=/api/users/123",
     *   "timestamp": "2025-01-15T10:30:00"
     * }
     */
    public static class ErrorResponse {
        private final int status;        // HTTP status code (404, 500, etc.)
        private final String error;      // Error category/type
        private final String message;    // Specific error message
        private final String path;       // What URL caused the error
        private final LocalDateTime timestamp; // When error occurred

        public ErrorResponse(int status, String error, String message, String path, LocalDateTime timestamp) {
            this.status = status;
            this.error = error;
            this.message = message;
            this.path = path;
            this.timestamp = timestamp;
        }

        // Getters (required for JSON conversion)
        public int getStatus() { return status; }
        public String getError() { return error; }
        public String getMessage() { return message; }
        public String getPath() { return path; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    /**
     * VALIDATION ERROR RESPONSE CLASS
     *
     * Extends ErrorResponse to include field-specific validation errors.
     * Used when multiple fields fail validation at once.
     *
     * Example response:
     * {
     *   "status": 400,
     *   "error": "Validation Failed",
     *   "message": "Request validation failed",
     *   "path": "uri=/api/reviews",
     *   "timestamp": "2025-01-15T10:30:00",
     *   "fieldErrors": {
     *     "rating": "must be between 1 and 5",
     *     "content": "must not be blank"
     *   }
     * }
     */
    public static class ValidationErrorResponse extends ErrorResponse {
        private final Map<String, String> fieldErrors;

        public ValidationErrorResponse(int status, String error, String message, String path,
                                       LocalDateTime timestamp, Map<String, String> fieldErrors) {
            super(status, error, message, path, timestamp);
            this.fieldErrors = fieldErrors;
        }

        public Map<String, String> getFieldErrors() { return fieldErrors; }
    }
}
