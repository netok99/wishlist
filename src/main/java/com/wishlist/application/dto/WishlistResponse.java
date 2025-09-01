package com.wishlist.application.dto;

import java.util.List;

public record WishlistResponse(
    String customerId,
    List<ProductResponse> products,
    Integer totalItems,
    Integer maxItems
) {
}
