package com.wishlist.application.exception;

import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(WishlistLimitExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleWishlistLimitExceeded(
        WishlistLimitExceededException exception,
        WebRequest request
    ) {
        final ApiErrorResponse error = ApiErrorResponse
            .builder()
            .code("WISHLIST_LIMIT_EXCEEDED")
            .message(exception.getMessage())
            .timestamp(LocalDateTime.now())
            .path(getRequestPath(request))
            .build();
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(error);
    }

    @ExceptionHandler(ProductAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleProductAlreadyExists(
        ProductAlreadyExistsException exception,
        WebRequest request
    ) {
        final ApiErrorResponse error = ApiErrorResponse.builder()
            .code("PRODUCT_ALREADY_EXISTS")
            .message(exception.getMessage())
            .timestamp(LocalDateTime.now())
            .path(getRequestPath(request))
            .build();
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(error);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleProductNotFound(
        ProductNotFoundException exception,
        WebRequest request
    ) {
        final ApiErrorResponse error = ApiErrorResponse.builder()
            .code("PRODUCT_NOT_FOUND")
            .message(exception.getMessage())
            .timestamp(LocalDateTime.now())
            .path(getRequestPath(request))
            .build();
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(error);
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleCustomerNotFound(
        CustomerNotFoundException exception,
        WebRequest request
    ) {
        final ApiErrorResponse error = ApiErrorResponse.builder()
            .code("CUSTOMER_NOT_FOUND")
            .message(exception.getMessage())
            .timestamp(LocalDateTime.now())
            .path(getRequestPath(request))
            .build();
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(error);
    }

    @ExceptionHandler({InvalidCustomerIdException.class, InvalidProductIdException.class})
    public ResponseEntity<ApiErrorResponse> handleInvalidIds(
        RuntimeException exception,
        WebRequest request
    ) {
        final String errorCode = exception instanceof InvalidCustomerIdException ?
            "INVALID_CUSTOMER_ID" : "INVALID_PRODUCT_ID";
        final ApiErrorResponse error = ApiErrorResponse
            .builder()
            .code(errorCode)
            .message(exception.getMessage())
            .timestamp(LocalDateTime.now())
            .path(getRequestPath(request))
            .build();
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
        ConstraintViolationException exception,
        WebRequest request
    ) {
        final ApiErrorResponse error = ApiErrorResponse.builder()
            .code("VALIDATION_ERROR")
            .message("Invalid input parameters: " + exception.getMessage())
            .timestamp(LocalDateTime.now())
            .path(getRequestPath(request))
            .build();
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
        MethodArgumentNotValidException exception,
        WebRequest request
    ) {
        StringBuilder message = new StringBuilder("Validation failed: ");
        exception
            .getBindingResult()
            .getFieldErrors()
            .forEach(error ->
                message
                    .append(error.getField())
                    .append(" ")
                    .append(error.getDefaultMessage())
                    .append("; ")
            );
        final ApiErrorResponse error = ApiErrorResponse.builder()
            .code("VALIDATION_ERROR")
            .message(message.toString())
            .timestamp(LocalDateTime.now())
            .path(getRequestPath(request))
            .build();
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatch(
        MethodArgumentTypeMismatchException exception,
        WebRequest request
    ) {
        final ApiErrorResponse error = ApiErrorResponse.builder()
            .code("INVALID_PARAMETER_TYPE")
            .message(
                String
                    .format("Invalid parameter '%s': expected %s",
                        exception.getName(),
                        exception
                            .getRequiredType()
                            .getSimpleName()
                    )
            )
            .timestamp(LocalDateTime.now())
            .path(getRequestPath(request))
            .build();
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
        Exception exception,
        WebRequest request
    ) {
        final ApiErrorResponse error = ApiErrorResponse
            .builder()
            .code("INTERNAL_SERVER_ERROR")
            .message("An unexpected error occurred. Please try again later.")
            .timestamp(LocalDateTime.now())
            .path(getRequestPath(request))
            .build();
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(error);
    }

    private String getRequestPath(WebRequest request) {
        return request
            .getDescription(false)
            .replace("uri=", "");
    }
}
