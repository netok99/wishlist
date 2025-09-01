package com.wishlist.application.service;

import com.wishlist.application.dto.AddProductResponse;
import com.wishlist.application.dto.ProductExistsResponse;
import com.wishlist.application.dto.WishlistResponse;
import com.wishlist.application.exception.CustomerNotFoundException;
import com.wishlist.application.exception.InvalidCustomerIdException;
import com.wishlist.application.exception.InvalidProductIdException;
import com.wishlist.application.exception.ProductAlreadyExistsException;
import com.wishlist.application.exception.ProductNotFoundException;
import com.wishlist.application.exception.WishlistLimitExceededException;
import com.wishlist.domain.entity.Wishlist;
import com.wishlist.domain.repository.WishlistRepository;
import java.util.Arrays;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("Wishlist Service - Customer Wishlist Management Use Cases")
public class WishlistServiceTest {
    @Mock
    private WishlistRepository wishlistRepository;
    @InjectMocks
    private WishlistService wishlistService;
    private final String VALID_CUSTOMER_ID = "customer123";
    private final String VALID_PRODUCT_ID = "product456";

    @Nested
    @DisplayName("Feature: Add product to wishlist")
    class AddProductToWishlistFeature {

        @Nested
        @DisplayName("Scenario: Add product to empty wishlist")
        class AddProductToEmptyWishlist {

            @Test
            @DisplayName("Given customer has empty wishlist, When I add product, Then product should be added successfully")
            void givenEmptyWishlist_whenAddProduct_thenProductShouldBeAddedSuccessfully() {
                given(wishlistRepository.findByCustomerId(VALID_CUSTOMER_ID))
                    .willReturn(Optional.empty());
                given(wishlistRepository.save(any(Wishlist.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

                final AddProductResponse response = wishlistService.addProduct(VALID_CUSTOMER_ID, VALID_PRODUCT_ID);

                assertThat(response).isNotNull();
                assertThat(response.customerId()).isEqualTo(VALID_CUSTOMER_ID);
                assertThat(response.productId()).isEqualTo(VALID_PRODUCT_ID);
                assertThat(response.message()).contains("successfully");
                assertThat(response.addedAt()).isNotNull();

                then(wishlistRepository)
                    .should()
                    .save(
                        argThat(wishlist ->
                            wishlist.getCustomerId().equals(VALID_CUSTOMER_ID) &&
                                wishlist.getProductCount() == 1 &&
                                wishlist.hasProduct(VALID_PRODUCT_ID)
                        )
                    );
            }
        }

        @Nested
        @DisplayName("Scenario: Add multiple products to wishlist")
        class AddMultipleProductsToWishlist {

            @Test
            @DisplayName("Given empty wishlist, When I add multiple products, Then all products should be present")
            void givenEmptyWishlist_whenAddMultipleProducts_thenAllProductsShouldBePresent() {
                given(wishlistRepository.findByCustomerId(VALID_CUSTOMER_ID))
                    .willReturn(Optional.empty())
                    .willReturn(Optional.of(createWishlistWithProducts("product001")))
                    .willReturn(Optional.of(createWishlistWithProducts("product001", "product002")));

                given(wishlistRepository.save(any(Wishlist.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

                wishlistService.addProduct(VALID_CUSTOMER_ID, "product001");
                wishlistService.addProduct(VALID_CUSTOMER_ID, "product002");
                wishlistService.addProduct(VALID_CUSTOMER_ID, "product003");

                then(wishlistRepository)
                    .should(times(3))
                    .save(any(Wishlist.class));
            }
        }

        @Nested
        @DisplayName("Scenario: Try to add duplicate product to wishlist")
        class TryToAddDuplicateProduct {

            @Test
            @DisplayName(
                "Given wishlist contains product, " +
                    "When I try to add same product again, Then should return product already exists error"
            )
            void givenWishlistContainsProduct_whenTryToAddSameProductAgain_thenShouldReturnProductAlreadyExistsError() {
                final Wishlist existingWishlist = createWishlistWithProducts(VALID_PRODUCT_ID);
                given(wishlistRepository.findByCustomerId(VALID_CUSTOMER_ID))
                    .willReturn(Optional.of(existingWishlist));

                assertThatThrownBy(() -> wishlistService.addProduct(VALID_CUSTOMER_ID, VALID_PRODUCT_ID))
                    .isInstanceOf(ProductAlreadyExistsException.class)
                    .hasMessageContaining("already exists in wishlist");

                then(wishlistRepository)
                    .should(never())
                    .save(any());
            }
        }

        @Nested
        @DisplayName("Scenario: Try to add product when wishlist is at limit")
        class TryToAddProductWhenWishlistAtLimit {

            @Test
            @DisplayName(
                "Given wishlist has 20 products, When I try to add product," +
                    " Then should return wishlist limit exceeded error"
            )
            void givenWishlistHas20Products_whenTryToAddProduct_thenShouldReturnWishlistLimitExceededError() {
                final Wishlist fullWishlist = createFullWishlist();
                given(wishlistRepository.findByCustomerId(VALID_CUSTOMER_ID))
                    .willReturn(Optional.of(fullWishlist));

                assertThatThrownBy(() -> wishlistService.addProduct(VALID_CUSTOMER_ID, "product999"))
                    .isInstanceOf(WishlistLimitExceededException.class)
                    .hasMessageContaining("cannot exceed 20 products");

                then(wishlistRepository)
                    .should(never())
                    .save(any());
            }
        }
    }

    @Nested
    @DisplayName("Feature: Remove product from wishlist")
    class RemoveProductFromWishlistFeature {

        @Nested
        @DisplayName("Scenario: Remove existing product from wishlist")
        class RemoveExistingProductFromWishlist {

            @Test
            @DisplayName(
                "Given wishlist contains products, When I remove existing product, " +
                    "Then product should be removed successfully"
            )
            void givenWishlistContainsProducts_whenRemoveExistingProduct_thenProductShouldBeRemovedSuccessfully() {
                final Wishlist existingWishlist = createWishlistWithProducts(
                    "product001",
                    "product002",
                    "product003"
                );
                given(wishlistRepository.findByCustomerId(VALID_CUSTOMER_ID))
                    .willReturn(Optional.of(existingWishlist));
                given(wishlistRepository.save(any(Wishlist.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

                assertThatNoException()
                    .isThrownBy(() -> wishlistService.removeProduct(VALID_CUSTOMER_ID, "product002"));

                then(wishlistRepository)
                    .should()
                    .save(argThat(wishlist ->
                        wishlist.getProductCount() == 2 &&
                            !wishlist.hasProduct("product002") &&
                            wishlist.hasProduct("product001") &&
                            wishlist.hasProduct("product003")
                    ));
            }
        }

        @Nested
        @DisplayName("Scenario: Try to remove product that doesn't exist in wishlist")
        class TryToRemoveNonExistentProduct {

            @Test
            @DisplayName("Given wishlist contains product, When I try to remove non-existent product, Then should return product not found error")
            void givenWishlistContainsProduct_whenTryToRemoveNonExistentProduct_thenShouldReturnProductNotFoundError() {
                final Wishlist existingWishlist = createWishlistWithProducts(VALID_PRODUCT_ID);
                given(wishlistRepository.findByCustomerId(VALID_CUSTOMER_ID))
                    .willReturn(Optional.of(existingWishlist));

                assertThatThrownBy(() -> wishlistService.removeProduct(VALID_CUSTOMER_ID, "product999"))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessageContaining("not found in wishlist");

                then(wishlistRepository)
                    .should(never())
                    .save(any());
            }
        }
    }

    @Nested
    @DisplayName("Feature: Query wishlist")
    class QueryWishlistFeature {

        @Nested
        @DisplayName("Scenario: Query wishlist with products")
        class QueryWishlistWithProducts {

            @Test
            @DisplayName(
                "Given wishlist contains products, When I query complete wishlist, " +
                    "Then should return wishlist with all products"
            )
            void givenWishlistContainsProducts_whenQueryCompleteWishlist_thenShouldReturnWishlistWithAllProducts() {
                final Wishlist existingWishlist = createWishlistWithProducts(
                    "product001",
                    "product002",
                    "product003"
                );
                given(wishlistRepository.findByCustomerId(VALID_CUSTOMER_ID))
                    .willReturn(Optional.of(existingWishlist));

                final WishlistResponse response = wishlistService.getWishlist(VALID_CUSTOMER_ID);

                assertThat(response).isNotNull();
                assertThat(response.customerId()).isEqualTo(VALID_CUSTOMER_ID);
                assertThat(response.totalItems()).isEqualTo(3);
                assertThat(response.products()).hasSize(3);
                assertThat(response.maxItems()).isEqualTo(20);
                assertThat(response.products())
                    .allMatch(product -> product.addedAt() != null)
                    .extracting("productId")
                    .containsExactlyInAnyOrder("product001", "product002", "product003");
            }
        }

        @Nested
        @DisplayName("Scenario: Query empty wishlist")
        class QueryEmptyWishlist {

            @Test
            @DisplayName(
                "Given customer has empty wishlist, When I query complete wishlist, Then should return empty wishlist"
            )
            void givenCustomerHasEmptyWishlist_whenQueryCompleteWishlist_thenShouldReturnEmptyWishlist() {
                given(wishlistRepository.findByCustomerId(VALID_CUSTOMER_ID))
                    .willReturn(Optional.empty());

                final WishlistResponse response = wishlistService.getWishlist(VALID_CUSTOMER_ID);

                assertThat(response).isNotNull();
                assertThat(response.customerId()).isEqualTo(VALID_CUSTOMER_ID);
                assertThat(response.totalItems()).isZero();
                assertThat(response.products()).isEmpty();
                assertThat(response.maxItems()).isEqualTo(20);
            }
        }
    }

    @Nested
    @DisplayName("Feature: Check if product exists in wishlist")
    class CheckProductExistsFeature {

        @Nested
        @DisplayName("Scenario: Check if product exists in wishlist")
        class CheckIfProductExists {

            @Test
            @DisplayName("Given wishlist contains product, When I check if product exists, Then should confirm product exists with timestamp")
            void givenWishlistContainsProduct_whenCheckIfProductExists_thenShouldConfirmProductExistsWithTimestamp() {
                final Wishlist existingWishlist = createWishlistWithProducts(VALID_PRODUCT_ID);
                given(wishlistRepository.findByCustomerId(VALID_CUSTOMER_ID))
                    .willReturn(Optional.of(existingWishlist));

                final ProductExistsResponse response = wishlistService.checkProductExists(
                    VALID_CUSTOMER_ID,
                    VALID_PRODUCT_ID
                );

                assertThat(response).isNotNull();
                assertThat(response.customerId()).isEqualTo(VALID_CUSTOMER_ID);
                assertThat(response.productId()).isEqualTo(VALID_PRODUCT_ID);
                assertThat(response.exists()).isTrue();
                assertThat(response.addedAt()).isNotNull();
            }
        }

        @Nested
        @DisplayName("Scenario: Check product that doesn't exist in wishlist")
        class CheckProductThatDoesntExist {

            @Test
            @DisplayName(
                "Given customer has empty wishlist, When I check if product exists, " +
                    "Then should return product not found error"
            )
            void givenCustomerHasEmptyWishlist_whenCheckIfProductExists_thenShouldReturnProductNotFoundError() {
                given(wishlistRepository.findByCustomerId(VALID_CUSTOMER_ID))
                    .willReturn(Optional.empty());

                assertThatThrownBy(() -> wishlistService.checkProductExists(VALID_CUSTOMER_ID, "product999"))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessageContaining("not found in wishlist");
            }
        }
    }

    @Nested
    @DisplayName("Feature: Clear wishlist")
    class ClearWishlistFeature {

        @Nested
        @DisplayName("Scenario: Clear wishlist with products")
        class ClearWishlistWithProducts {

            @Test
            @DisplayName(
                "Given customer has products in wishlist, When I clear wishlist completely, " +
                    "Then wishlist should become empty"
            )
            void givenCustomerHasProductsInWishlist_whenClearWishlistCompletely_thenWishlistShouldBecomeEmpty() {
                given(wishlistRepository.existsByCustomerId(VALID_CUSTOMER_ID))
                    .willReturn(true);

                assertThatNoException()
                    .isThrownBy(() -> wishlistService.clearWishlist(VALID_CUSTOMER_ID));

                then(wishlistRepository).should().deleteByCustomerId(VALID_CUSTOMER_ID);
            }

            @Test
            @DisplayName(
                "Given customer doesn't exist, When I clear wishlist, Then should return customer not found error"
            )
            void givenCustomerDoesntExist_whenClearWishlist_thenShouldReturnCustomerNotFoundError() {
                given(wishlistRepository.existsByCustomerId(VALID_CUSTOMER_ID))
                    .willReturn(false);

                assertThatThrownBy(() -> wishlistService.clearWishlist(VALID_CUSTOMER_ID))
                    .isInstanceOf(CustomerNotFoundException.class)
                    .hasMessageContaining("Customer not found");

                then(wishlistRepository)
                    .should(never())
                    .deleteByCustomerId(any());
            }
        }
    }

    @Nested
    @DisplayName("Feature: Validate invalid IDs")
    class ValidateInvalidIdsFeature {

        @Nested
        @DisplayName("Scenario Outline: Validate invalid customer IDs")
        class ValidateInvalidCustomerIds {

            @Test
            @DisplayName(
                "Given invalid customer ID is empty, When I try to add product, " +
                    "Then should return invalid customer ID error"
            )
            void givenInvalidCustomerIdIsEmpty_whenTryToAddProduct_thenShouldReturnInvalidCustomerIdError() {
                assertThatThrownBy(() -> wishlistService.addProduct("", "product123"))
                    .isInstanceOf(InvalidCustomerIdException.class)
                    .hasMessageContaining("cannot be null or empty");
            }

            @Test
            @DisplayName(
                "Given invalid customer ID is null, When I try to remove product, " +
                    "Then should return invalid customer ID error"
            )
            void givenInvalidCustomerIdIsNull_whenTryToRemoveProduct_thenShouldReturnInvalidCustomerIdError() {
                assertThatThrownBy(() ->
                    wishlistService.removeProduct(null, "product123"))
                    .isInstanceOf(InvalidCustomerIdException.class)
                    .hasMessageContaining("cannot be null or empty");
            }

            @Test
            @DisplayName(
                "Given invalid customer ID is whitespace, When I query wishlist, " +
                    "Then should return invalid customer ID error"
            )
            void givenInvalidCustomerIdIsWhitespace_whenQueryWishlist_thenShouldReturnInvalidCustomerIdError() {
                assertThatThrownBy(() -> wishlistService.getWishlist(" "))
                    .isInstanceOf(InvalidCustomerIdException.class)
                    .hasMessageContaining("cannot be null or empty");
            }
        }

        @Nested
        @DisplayName("Scenario Outline: Validate invalid product IDs")
        class ValidateInvalidProductIds {

            @Test
            @DisplayName(
                "Given invalid product ID is empty, When I try to add product, " +
                    "Then should return invalid product ID error"
            )
            void givenInvalidProductIdIsEmpty_whenTryToAddProduct_thenShouldReturnInvalidProductIdError() {
                assertThatThrownBy(() -> wishlistService.addProduct("customer123", ""))
                    .isInstanceOf(InvalidProductIdException.class)
                    .hasMessageContaining("cannot be null or empty");
            }

            @Test
            @DisplayName(
                "Given invalid product ID is null, When I try to remove product, " +
                    "Then should return invalid product ID error"
            )
            void givenInvalidProductIdIsNull_whenTryToRemoveProduct_thenShouldReturnInvalidProductIdError() {
                assertThatThrownBy(() -> wishlistService.removeProduct("customer123", null))
                    .isInstanceOf(InvalidProductIdException.class)
                    .hasMessageContaining("cannot be null or empty");
            }
        }
    }

    private Wishlist createWishlistWithProducts(String... productIds) {
        Wishlist wishlist = new Wishlist(VALID_CUSTOMER_ID);
        Arrays.stream(productIds).forEach(wishlist::addProduct);
        return wishlist;
    }

    private Wishlist createFullWishlist() {
        Wishlist wishlist = new Wishlist(VALID_CUSTOMER_ID);
        IntStream
            .rangeClosed(1, 20)
            .mapToObj(i -> "product" + String.format("%03d", i))
            .forEach(wishlist::addProduct);
        return wishlist;
    }
}
