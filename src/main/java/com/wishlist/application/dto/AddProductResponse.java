package com.wishlist.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record AddProductResponse(
    String message,
    String customerId,
    String productId,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    LocalDateTime addedAt
) {
}
