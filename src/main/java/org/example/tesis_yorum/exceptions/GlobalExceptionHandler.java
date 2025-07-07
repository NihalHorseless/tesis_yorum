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
import org.springframework.web.multipart.MultipartException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 404: Kaynak Bulunamadı Hatası
     *
     * Örnek Durumlar:
     * - Kullanıcı olmayan bir ID parametresi ile çağrıda bulunursa
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
     * 403: Erişim Hatası
     *
     * Örnek Durumlar:
     * - Normal Kullanıcı Yorum onaylamaya veya reddetmeye çalışırsa
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
     * 500: Depolama Hatası
     *
     * Örnek Durumlar:
     * - Disk dolu dosya upload edilemedi
     * - Upload edilecek dosyanın adresinin boş olması
     * - Dosya işlemi izinlerinin olmaması
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
     * 400: Hatalı Dosya
     *
     * Örnek Durumlar:
     * - Kullanıcı izin verilmeyen türde bir dosya yüklerse
     * - Kullanıcı belirlenen sınırın üstünde Dosya yüklerse
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
     * 400: Geçersiz Argüman Hatası
     *
     * Örnek Durumlar:
     * - Olan bir kullanıcı adıyla yeni bir kullanıcı yaratma
     * - Null değeri girilmemesi gereken bir alana null veri girme
     * - Yanlış veri tipinde parametre girmek
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
     * 409: İzin Verilmeyen Durumlar
     *
     * Örnek:
     * - Onaylanmış yorumu tekrar onaylamaya çalışmak
     * - Onaylanmış yorumu değiştirmeye çalışmak
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
     * 400: @Valid Anotasyonundan Gelen Doğrulama Hatası
     *
     * Örnek Durumlar:
     * - Kullanıcı verilen aralıkta parametreler girmemiştir (rating kısmına 6 girmek gibi)
     * - Kullanıcı boş yorum girdiğinde
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
     * 400: Parametre Doğrulama Hataları
     *
     * @Min, @Max gibi method anotasyonlarından gelen hataları yakalar
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
     * 400: Büyük Dosya Hatası
     *
     * Dosya yükleme limiti aşıldığında ilk bu hata yakalanır
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxSizeException(
            MaxUploadSizeExceededException ex, WebRequest request) {

        String message = "File size exceeds the maximum allowed limit of 10MB. ";

        // Extract more specific information from the exception if possible
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("Maximum upload size exceeded")) {
                message += "Please reduce file sizes or upload fewer files.";
            }
        }

        ErrorResponse errorResponse = new ErrorResponse(
                400,
                "File Too Large",
                message,
                request.getDescription(false),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * 500: Beklenmedik Runtime Hatası
     *
     * Uygulama çalışırken beklenmedik bir hata geldiğinde çalışır
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
     * 500: Diğer Tüm Hatalar
     *
     * Başka hiçbir yerde yakalanmayan hatalar en son ihtimal burada yakalanır
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
     * Yakalanan hatalar için uygun Yanıt Formatı oluşturur
     *
     * Örnek Yanıt:
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
     * Birden fazla hatalı alanlar için Yanıt Formatı oluşturur
     *
     * Örnek Yanıt:
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