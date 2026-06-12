package com.healthwallet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;



@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> body = new HashMap<>();
        body.put("status", 400);
        body.put("erro", "Dados inválidos");
        body.put("campos", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleEmailConflict(EmailAlreadyExistsException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 409);
        body.put("erro", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(AnamnesisAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleAnamnesisConflict(AnamnesisAlreadyExistsException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 409);
        body.put("erro", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePatientNotFound(PatientNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 404);
        body.put("erro", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(AnamnesisNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleAnamnesisNotFound(AnamnesisNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 404);
        body.put("erro", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(AppointmentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleAppointmentNotFound(AppointmentNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 404);
        body.put("erro", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(VaccineNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleVaccineNotFound(VaccineNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 404);
        body.put("erro", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleReportNotFound(ReportNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 404);
        body.put("erro", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(VaccineAttachmentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleVaccineAttachmentNotFound(VaccineAttachmentNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 404);
        body.put("erro", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(InvalidAttachmentException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidAttachment(InvalidAttachmentException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 400);
        body.put("erro", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 400);
        body.put("erro", "Arquivo excede o tamanho máximo permitido");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(DoctorNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleDoctorNotFound(DoctorNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 404);
        body.put("erro", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(InvalidDoctorRoleException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidDoctorRole(InvalidDoctorRoleException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 400);
        body.put("erro", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(SharedAccessNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleSharedAccessNotFound(SharedAccessNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 404);
        body.put("erro", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(DoctorAccessAlreadyGrantedException.class)
    public ResponseEntity<Map<String, Object>> handleDoctorAccessAlreadyGranted(DoctorAccessAlreadyGrantedException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 409);
        body.put("erro", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(UnauthorizedDoctorAccessException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedDoctorAccess(UnauthorizedDoctorAccessException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 403);
        body.put("erro", ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(CannotRenewRevokedAccessException.class)
    public ResponseEntity<Map<String, Object>> handleCannotRenewRevokedAccess(CannotRenewRevokedAccessException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 400);
        body.put("erro", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(InvalidShareTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidShareToken(InvalidShareTokenException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 403);
        body.put("erro", ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }
}
