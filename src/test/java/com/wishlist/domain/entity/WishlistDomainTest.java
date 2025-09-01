package com.wishlist.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Wishlist Domain Entity")
public class WishlistDomainTest {
    @Nested
    @DisplayName("When creating a new wishlist")
    class WhenCreatingNewWishlist {

        @Test
        @DisplayName("Should create empty wishlist for valid customer")
        void shouldCreateEmptyWishlistForValidCustomer() {
            // Given - Given I have a valid customer
            String customerId = "customer123";

            // When - When I create a new wishlist
            Wishlist wishlist = new Wishlist(customerId);

            // Then - Then should create empty wishlist correctly
            assertThat(wishlist.getCustomerId()).isEqualTo(customerId);
            assertThat(wishlist.getProducts()).isEmpty();
            assertThat(wishlist.getProductCount()).isZero();
            assertThat(wishlist.getCreatedAt()).isNotNull();
            assertThat(wishlist.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("When adding products to wishlist")
    class WhenAddingProductsToWishlist {

        @Test
        @DisplayName("Should add first product successfully")
        void shouldAddFirstProductSuccessfully() {
            // Given - Given an empty wishlist
            Wishlist wishlist = new Wishlist("customer123");
            String productId = "product456";

            // When - When I add a product
            wishlist.addProduct(productId);

            // Then - Then the product should be added
            assertThat(wishlist.hasProduct(productId)).isTrue();
            assertThat(wishlist.getProductCount()).isEqualTo(1);
            assertThat(wishlist.cannotAddProduct()).isFalse();
        }

        @Test
        @DisplayName("Should add multiple different products")
        void shouldAddMultipleDifferentProducts() {
            // Given - Given an empty wishlist
            Wishlist wishlist = new Wishlist("customer123");

            // When - When I add several different products
            wishlist.addProduct("product1");
            wishlist.addProduct("product2");
            wishlist.addProduct("product3");

            // Then - Then all should be added
            assertThat(wishlist.getProductCount()).isEqualTo(3);
            assertThat(wishlist.hasProduct("product1")).isTrue();
            assertThat(wishlist.hasProduct("product2")).isTrue();
            assertThat(wishlist.hasProduct("product3")).isTrue();
        }

        @Test
        @DisplayName("Should not add duplicate product")
        void shouldNotAddDuplicateProduct() {
            // Given - Given a wishlist with one product
            Wishlist wishlist = new Wishlist("customer123");
            String productId = "product456";
            wishlist.addProduct(productId);

            // When/Then - When trying to add the same product
            assertThatThrownBy(() -> wishlist.addProduct(productId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

            // Then - Then should continue with only one product
            assertThat(wishlist.getProductCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should not add product when reaching limit of 20")
        void shouldNotAddProductWhenReachingLimitOf20() {
            // Given - Given a wishlist with 20 products
            Wishlist wishlist = new Wishlist("customer123");
            for (int i = 1; i <= 20; i++) {
                wishlist.addProduct("product" + i);
            }

            // When/Then - When trying to add the 21st product
            assertThatThrownBy(() -> wishlist.addProduct("product21"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot exceed 20 products");

            // Then - Then should continue with 20 products
            assertThat(wishlist.getProductCount()).isEqualTo(20);
            assertThat(wishlist.cannotAddProduct()).isTrue();
        }
    }

    @Nested
    @DisplayName("When removing products from wishlist")
    class WhenRemovingProductsFromWishlist {

        @Test
        @DisplayName("Should remove existing product successfully")
        void shouldRemoveExistingProductSuccessfully() {
            // Given - Given a wishlist with products
            Wishlist wishlist = new Wishlist("customer123");
            wishlist.addProduct("product1");
            wishlist.addProduct("product2");
            wishlist.addProduct("product3");

            // When - When I remove a product
            boolean removed = wishlist.removeProduct("product2");

            // Then - Then the product should be removed
            assertThat(removed).isTrue();
            assertThat(wishlist.getProductCount()).isEqualTo(2);
            assertThat(wishlist.hasProduct("product2")).isFalse();
            assertThat(wishlist.hasProduct("product1")).isTrue();
            assertThat(wishlist.hasProduct("product3")).isTrue();
        }

        @Test
        @DisplayName("Should not remove non-existent product")
        void shouldNotRemoveNonExistentProduct() {
            // Given - Given a wishlist with products
            Wishlist wishlist = new Wishlist("customer123");
            wishlist.addProduct("product1");

            // When - When trying to remove non-existent product
            boolean removed = wishlist.removeProduct("nonExistentProduct");

            // Then - Then should not change the wishlist
            assertThat(removed).isFalse();
            assertThat(wishlist.getProductCount()).isEqualTo(1);
            assertThat(wishlist.hasProduct("product1")).isTrue();
        }

        @Test
        @DisplayName("Should allow adding again after removal")
        void shouldAllowAddingAgainAfterRemoval() {
            // Given - Given a wishlist at maximum limit
            Wishlist wishlist = new Wishlist("customer123");
            for (int i = 1; i <= 20; i++) {
                wishlist.addProduct("product" + i);
            }

            // When - When I remove one product and add another
            wishlist.removeProduct("product1");
            wishlist.addProduct("newProduct");

            // Then - Then should work normally
            assertThat(wishlist.getProductCount()).isEqualTo(20);
            assertThat(wishlist.hasProduct("product1")).isFalse();
            assertThat(wishlist.hasProduct("newProduct")).isTrue();
        }
    }

    @Nested
    @DisplayName("When checking products in wishlist")
    class WhenCheckingProductsInWishlist {

        @Test
        @DisplayName("Should confirm existence of present product")
        void shouldConfirmExistenceOfPresentProduct() {
            // Given - Given a wishlist with products
            Wishlist wishlist = new Wishlist("customer123");
            wishlist.addProduct("product456");

            // When/Then - When checking if product exists
            assertThat(wishlist.hasProduct("product456")).isTrue();
        }

        @Test
        @DisplayName("Should deny existence of absent product")
        void shouldDenyExistenceOfAbsentProduct() {
            // Given - Given a wishlist with products
            Wishlist wishlist = new Wishlist("customer123");
            wishlist.addProduct("product456");

            // When/Then - When checking non-existent product
            assertThat(wishlist.hasProduct("nonExistentProduct")).isFalse();
        }

        @Test
        @DisplayName("Should return correct product count")
        void shouldReturnCorrectProductCount() {
            // Given - Given a wishlist
            Wishlist wishlist = new Wishlist("customer123");

            // When/Then - As products are added
            assertThat(wishlist.getProductCount()).isZero();

            wishlist.addProduct("product1");
            assertThat(wishlist.getProductCount()).isEqualTo(1);

            wishlist.addProduct("product2");
            assertThat(wishlist.getProductCount()).isEqualTo(2);

            wishlist.removeProduct("product1");
            assertThat(wishlist.getProductCount()).isEqualTo(1);
        }
    }
}
