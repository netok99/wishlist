package com.wishlist.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record ProductExistsResponse(
    String customerId,
    String productId,
    boolean exists,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    LocalDateTime addedAt
) {
}
