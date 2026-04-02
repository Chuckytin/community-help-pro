package com.communityhelp.app.common.exceptions;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global Exception Handler.
 * Intercepta las excepciones lanzadas por cualquier RestController y
 * devuelve errores estandarizados en formato {@link ApiErrorResponse}.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja errores de validación de DTOs (@Valid) devolviendo error 400.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrorsException(MethodArgumentNotValidException ex) {

        List<ApiErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new ApiErrorResponse.FieldError(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()))
                .collect(Collectors.toList());

        log.warn("Validation failed with {} errors: {}", fieldErrors.size(), fieldErrors);

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ErrorCode.VALIDATION_ERROR.name())
                .message("Validation failed")
                .errors(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Maneja IllegalArgumentException y devuelve error 400.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, ex.getMessage());
    }

    /**
     * Maneja errores de conversión de tipos en parámetros de consulta o path variables y devuelve error 400.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String expectedType = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName()
                : "unknown";
        String message = String.format("Parameter '%s' with value '%s' is invalid. Expected type: %s",
                ex.getName(), ex.getValue(), expectedType);
        log.warn("Type mismatch: {}", message);
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCode.TYPE_MISMATCH, message);
    }

    /**
     * Maneja errores cuando el JSON enviado está mal formado.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Malformed JSON request: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCode.MALFORMED_JSON,"Malformed JSON request");
    }

    /**
     * Maneja cualquier BadCredentialsException
     * y devuelve error 401 si las credenciales incluidas no son correctas.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse>handleBadCredentialException(BadCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_CREDENTIALS, "Invalid credentials");

    }

    /**
     * Maneja excepciones de autenticación (falta de token, token inválido)
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ErrorCode.AUTHENTICATION_REQUIRED, "Authentication required");
    }

    /**
     * Maneja errores de acceso denegado de usuarios, error 403
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        String username = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "anonymous";
        log.warn("Access denied for user {}: {}", username, ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, ErrorCode.ACCESS_DENIED, "You don't have permission to access this resource");
    }

    /**
     * Maneja errores de falta de permisos para el usuario, error 403
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        String username = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "anonymous";
        log.warn("Authorization denied for user {}: {}", username, ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, ErrorCode.ACCESS_DENIED, "You don't have permission to access this resource");
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailNotVerified(EmailNotVerifiedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ErrorCode.EMAIL_NOT_VERIFIED,
                "Email not verified. Please check your inbox.");
    }

    /**
     * Maneja cualquier EntityNotFoundException
     * y devuelve error 404 si la entidad no se encuentra.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse>handleEntityNotException(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, ex.getMessage());
    }

    /**
     * Maneja IllegalStateException y devuelve error 409.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ErrorCode.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException ex) {
        log.warn("Business error: {}", ex.getMessage());
        return buildResponse(
                HttpStatus.CONFLICT,
                ex.getErrorCode(),
                ex.getMessage()
        );
    }

    /**
     * Maneja cualquier error de DataIntegrityViolationException de base de datos.
     * Si por error se intentase duplicar alguna entidad única.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return buildResponse(HttpStatus.CONFLICT, ErrorCode.DATABASE_CONSTRAINT, "Database constraint violation");
    }

    /**
     * Maneja cualquier excepción no controlada y devuelve un error genérico 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR, "An unexpected error occurred");
    }

    /**
     * Helper para construir la respuesta de cada Exception.
     */
    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            ErrorCode code,
            String message
    ) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .status(status.value())
                .code(code.name())
                .message(message)
                .errors(Collections.emptyList())
                .build();
        return new ResponseEntity<>(errorResponse, status);
    }

}
