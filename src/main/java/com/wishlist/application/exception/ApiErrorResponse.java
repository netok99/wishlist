package com.wishlist.application.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class ApiErrorResponse {
    private String code;
    private String message;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime timestamp;
    private String path;

    public static ApiErrorResponseBuilder builder() {
        return new ApiErrorResponseBuilder();
    }

    public ApiErrorResponse(
        String code,
        String message,
        LocalDateTime timestamp,
        String path
    ) {
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
        this.path = path;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public static class ApiErrorResponseBuilder {
        private String code;
        private String message;
        private LocalDateTime timestamp;
        private String path;

        public ApiErrorResponseBuilder code(String code) {
            this.code = code;
            return this;
        }

        public ApiErrorResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ApiErrorResponseBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ApiErrorResponseBuilder path(String path) {
            this.path = path;
            return this;
        }

        public ApiErrorResponse build() {
            return new ApiErrorResponse(code, message, timestamp, path);
        }
    }
}
