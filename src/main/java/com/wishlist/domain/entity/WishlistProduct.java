package com.wishlist.domain.entity;

import java.time.LocalDateTime;
import java.util.Objects;

public record WishlistProduct(
    String productId,
    LocalDateTime addedAt
) {
    public WishlistProduct(String productId) {
        this(productId, LocalDateTime.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WishlistProduct that = (WishlistProduct) o;
        return Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }
}
