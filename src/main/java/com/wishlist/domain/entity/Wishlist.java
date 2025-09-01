package com.wishlist.domain.entity;

import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "wishlists")
public class Wishlist {
    @Id
    private String id;
    @Indexed(unique = true)
    private String customerId;
    @Size(max = 20, message = "Wishlist cannot exceed 20 products")
    private List<WishlistProduct> products = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Wishlist(String customerId) {
        this.customerId = customerId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public List<WishlistProduct> getProducts() {
        return products;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean hasProduct(String productId) {
        return products
            .stream()
            .anyMatch(product -> product.productId().equals(productId));
    }

    public boolean cannotAddProduct() {
        return products.size() >= 20;
    }

    public void addProduct(String productId) {
        if (cannotAddProduct()) {
            throw new IllegalStateException("Wishlist cannot exceed 20 products");
        }
        if (hasProduct(productId)) {
            throw new IllegalArgumentException("Product already exists in wishlist");
        }
        products.add(new WishlistProduct(productId));
        this.updatedAt = LocalDateTime.now();
    }

    public boolean removeProduct(String productId) {
        boolean removed = products.removeIf(product -> product.productId().equals(productId));
        if (removed) {
            this.updatedAt = LocalDateTime.now();
        }
        return removed;
    }

    public int getProductCount() {
        return products.size();
    }

    public String getCustomerId() {
        return customerId;
    }
}
