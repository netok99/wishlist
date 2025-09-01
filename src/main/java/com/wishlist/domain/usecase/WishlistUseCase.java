package com.wishlist.domain.usecase;

import com.wishlist.application.dto.AddProductResponse;
import com.wishlist.application.dto.ProductExistsResponse;
import com.wishlist.application.dto.WishlistResponse;

public interface WishlistUseCase {
    WishlistResponse getWishlist(String customerId);

    AddProductResponse addProduct(String customerId, String productId);

    void removeProduct(String customerId, String productId);

    ProductExistsResponse checkProductExists(String customerId, String productId);

    void clearWishlist(String customerId);
}
