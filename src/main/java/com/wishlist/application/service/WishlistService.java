package com.wishlist.application.service;

import com.wishlist.application.dto.AddProductResponse;
import com.wishlist.application.dto.ProductExistsResponse;
import com.wishlist.application.dto.ProductResponse;
import com.wishlist.application.dto.WishlistResponse;
import com.wishlist.application.exception.CustomerNotFoundException;
import com.wishlist.application.exception.InvalidCustomerIdException;
import com.wishlist.application.exception.InvalidProductIdException;
import com.wishlist.application.exception.ProductAlreadyExistsException;
import com.wishlist.application.exception.ProductNotFoundException;
import com.wishlist.application.exception.WishlistLimitExceededException;
import com.wishlist.domain.entity.Wishlist;
import com.wishlist.domain.entity.WishlistProduct;
import com.wishlist.domain.repository.WishlistRepository;
import com.wishlist.domain.usecase.WishlistUseCase;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WishlistService implements WishlistUseCase {
    private final WishlistRepository wishlistRepository;

    public WishlistService(WishlistRepository wishlistRepository) {
        this.wishlistRepository = wishlistRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public WishlistResponse getWishlist(String customerId) {
        validateCustomerId(customerId);
        final Wishlist wishlist = wishlistRepository
            .findByCustomerId(customerId)
            .orElse(new Wishlist(customerId));
        final List<ProductResponse> products = wishlist
            .getProducts()
            .stream()
            .map(this::mapToProductResponse)
            .collect(Collectors.toList());
        return new WishlistResponse(
            customerId,
            products,
            wishlist.getProductCount(),
            20
        );
    }

    @Override
    public AddProductResponse addProduct(
        String customerId,
        String productId
    ) {
        validateCustomerId(customerId);
        validateProductId(productId);
        Wishlist wishlist = wishlistRepository
            .findByCustomerId(customerId)
            .orElse(new Wishlist(customerId));
        if (wishlist.cannotAddProduct()) {
            throw new WishlistLimitExceededException("Wishlist cannot exceed 20 products");
        }
        if (wishlist.hasProduct(productId)) {
            throw new ProductAlreadyExistsException("Product already exists in wishlist");
        }
        wishlist.addProduct(productId);
        wishlistRepository.save(wishlist);
        return new AddProductResponse(
            "Product added to wishlist successfully",
            customerId,
            productId,
            LocalDateTime.now()
        );
    }

    @Override
    public void removeProduct(
        String customerId,
        String productId
    ) {
        validateCustomerId(customerId);
        validateProductId(productId);
        Wishlist wishlist = wishlistRepository
            .findByCustomerId(customerId)
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        boolean removed = wishlist.removeProduct(productId);
        if (!removed) {
            throw new ProductNotFoundException("Product not found in wishlist");
        }
        wishlistRepository.save(wishlist);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductExistsResponse checkProductExists(
        String customerId,
        String productId
    ) {
        validateCustomerId(customerId);
        validateProductId(productId);
        Wishlist wishlist = wishlistRepository
            .findByCustomerId(customerId)
            .orElse(new Wishlist(customerId));
        WishlistProduct product = wishlist
            .getProducts()
            .stream()
            .filter(p -> p.productId().equals(productId))
            .findFirst()
            .orElse(null);
        if (product == null) {
            throw new ProductNotFoundException("Product not found in wishlist");
        }
        return new ProductExistsResponse(
            customerId,
            productId,
            true,
            product.addedAt()
        );
    }

    @Override
    public void clearWishlist(String customerId) {
        validateCustomerId(customerId);
        if (!wishlistRepository.existsByCustomerId(customerId)) {
            throw new CustomerNotFoundException("Customer not found");
        }
        wishlistRepository.deleteByCustomerId(customerId);
    }

    private void validateCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new InvalidCustomerIdException("Customer ID cannot be null or empty");
        }
    }

    private void validateProductId(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new InvalidProductIdException("Product ID cannot be null or empty");
        }
    }

    private ProductResponse mapToProductResponse(WishlistProduct product) {
        return new ProductResponse(product.productId(), product.addedAt());
    }
}
