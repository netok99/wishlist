package com.wishlist.domain.repository;

import com.wishlist.domain.entity.Wishlist;
import java.util.Optional;

public interface WishlistRepository {
    Optional<Wishlist> findByCustomerId(String customerId);

    Wishlist save(Wishlist wishlist);

    void deleteByCustomerId(String customerId);

    boolean existsByCustomerId(String customerId);
}
