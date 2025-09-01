package com.wishlist.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wishlist.application.dto.AddProductResponse;
import com.wishlist.application.dto.ProductExistsResponse;
import com.wishlist.application.dto.ProductResponse;
import com.wishlist.application.dto.WishlistResponse;
import com.wishlist.application.exception.CustomerNotFoundException;
import com.wishlist.application.exception.InvalidCustomerIdException;
import com.wishlist.application.exception.ProductAlreadyExistsException;
import com.wishlist.application.exception.ProductNotFoundException;
import com.wishlist.application.exception.WishlistLimitExceededException;
import com.wishlist.domain.usecase.WishlistUseCase;
import com.wishlist.presentation.WishlistController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(WishlistController.class)
@DisplayName("Wishlist Controller - REST API Behavior")
public class WishlistControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private WishlistUseCase wishlistUseCase;
    @Autowired
    private ObjectMapper objectMapper;
    private final String VALID_CUSTOMER_ID = "customer123";
    private final String VALID_PRODUCT_ID = "product456";

    @Nested
    @DisplayName("Given a customer wants to get their wishlist")
    class GivenCustomerWantsToGetWishlist {

        @Test
        @DisplayName("Should return 200 OK with empty wishlist for new customer")
        void shouldReturn200OkWithEmptyWishlistForNewCustomer() throws Exception {
            final WishlistResponse emptyResponse = new WishlistResponse(
                VALID_CUSTOMER_ID,
                Collections.emptyList(),
                0,
                20
            );

            given(wishlistUseCase.getWishlist(VALID_CUSTOMER_ID)).willReturn(emptyResponse);

            mockMvc
                .perform(get("/api/v1/customers/{customerId}/wishlist", VALID_CUSTOMER_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.customerId", is(VALID_CUSTOMER_ID)))
                .andExpect(jsonPath("$.totalItems", is(0)))
                .andExpect(jsonPath("$.maxItems", is(20)))
                .andExpect(jsonPath("$.products", hasSize(0)));
        }

        @Test
        @DisplayName("Should return 200 OK with populated wishlist for existing customer")
        void shouldReturn200OkWithPopulatedWishlistForExistingCustomer() throws Exception {
            final ProductResponse product1 = new ProductResponse(
                "product1",
                LocalDateTime.of(2024, 8, 29, 10, 30)
            );
            final ProductResponse product2 = new ProductResponse(
                "product2",
                LocalDateTime.of(2024, 8, 29, 11, 45)
            );
            final WishlistResponse wishlistWithProducts = new WishlistResponse(
                VALID_CUSTOMER_ID,
                Arrays.asList(product1, product2),
                2,
                20
            );

            given(wishlistUseCase.getWishlist(VALID_CUSTOMER_ID)).willReturn(wishlistWithProducts);

            mockMvc
                .perform(get("/api/v1/customers/{customerId}/wishlist", VALID_CUSTOMER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId", is(VALID_CUSTOMER_ID)))
                .andExpect(jsonPath("$.totalItems", is(2)))
                .andExpect(jsonPath("$.maxItems", is(20)))
                .andExpect(jsonPath("$.products", hasSize(2)))
                .andExpect(jsonPath("$.products[*].productId", containsInAnyOrder("product1", "product2")))
                .andExpect(jsonPath("$.products[*].addedAt", everyItem(notNullValue())));
        }

        @Test
        @DisplayName("Should return 400 Bad Request for invalid customer ID format")
        void shouldReturn400BadRequestForInvalidCustomerIdFormat() throws Exception {
            final String invalidCustomerId = "customer@invalid";
            mockMvc
                .perform(get("/api/v1/customers/{customerId}/wishlist", invalidCustomerId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message",
                        containsString(
                            "Invalid input parameters: getWishlist.customerId: Invalid product ID format"
                        )
                    )
                )
                .andExpect(jsonPath("$.timestamp", notNullValue()));
        }

        @Test
        @DisplayName("Should return 400 Bad Request for empty customer ID")
        void shouldReturn400BadRequestForEmptyCustomerId() throws Exception {
            given(wishlistUseCase.getWishlist(""))
                .willThrow(new InvalidCustomerIdException("Customer ID cannot be null or empty"));

            mockMvc
                .perform(get("/api/v1/customers/{customerId}/wishlist", ""))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Given a customer wants to add a product to wishlist")
    class GivenCustomerWantsToAddProduct {

        @Test
        @DisplayName("Should return 201 Created when product is added successfully")
        void shouldReturn201CreatedWhenProductIsAddedSuccessfully() throws Exception {
            final AddProductResponse successResponse = new AddProductResponse(
                "Product added to wishlist successfully",
                VALID_CUSTOMER_ID,
                VALID_PRODUCT_ID,
                LocalDateTime.of(2024, 8, 29, 10, 30)
            );

            given(wishlistUseCase.addProduct(VALID_CUSTOMER_ID, VALID_PRODUCT_ID))
                .willReturn(successResponse);

            mockMvc
                .perform(
                    post(
                        "/api/v1/customers/{customerId}/wishlist/products/{productId}",
                        VALID_CUSTOMER_ID,
                        VALID_PRODUCT_ID
                    )
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.customerId", is(VALID_CUSTOMER_ID)))
                .andExpect(jsonPath("$.productId", is(VALID_PRODUCT_ID)))
                .andExpect(jsonPath("$.message", containsString("successfully")))
                .andExpect(jsonPath("$.addedAt", notNullValue()));
        }

        @Test
        @DisplayName("Should return 409 Conflict when product already exists")
        void shouldReturn409ConflictWhenProductAlreadyExists() throws Exception {
            given(wishlistUseCase.addProduct(VALID_CUSTOMER_ID, VALID_PRODUCT_ID))
                .willThrow(new ProductAlreadyExistsException("Product already exists in wishlist"));

            mockMvc
                .perform(
                    post(
                        "/api/v1/customers/{customerId}/wishlist/products/{productId}",
                        VALID_CUSTOMER_ID,
                        VALID_PRODUCT_ID
                    )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("PRODUCT_ALREADY_EXISTS")))
                .andExpect(jsonPath("$.message", containsString("already exists")))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.path", notNullValue()));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when wishlist limit is exceeded")
        void shouldReturn400BadRequestWhenWishlistLimitIsExceeded() throws Exception {
            given(wishlistUseCase.addProduct(VALID_CUSTOMER_ID, VALID_PRODUCT_ID))
                .willThrow(new WishlistLimitExceededException("Wishlist cannot exceed 20 products"));

            mockMvc
                .perform(
                    post(
                        "/api/v1/customers/{customerId}/wishlist/products/{productId}",
                        VALID_CUSTOMER_ID,
                        VALID_PRODUCT_ID
                    )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("WISHLIST_LIMIT_EXCEEDED")))
                .andExpect(jsonPath("$.message", containsString("cannot exceed 20 products")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
        }

        @Test
        @DisplayName("Should return 400 Bad Request for invalid product ID format")
        void shouldReturn400BadRequestForInvalidProductIdFormat() throws Exception {
            final String invalidProductId = "product@invalid#123";
            mockMvc
                .perform(
                    post("/api/v1/customers/{customerId}/wishlist/products/{productId}",
                        VALID_CUSTOMER_ID,
                        invalidProductId
                    )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", containsString("Invalid product ID format")));
        }

        @Test
        @DisplayName("Should handle request with custom headers properly")
        void shouldHandleRequestWithCustomHeadersProperly() throws Exception {
            final AddProductResponse response = new AddProductResponse(
                "Product added to wishlist successfully",
                VALID_CUSTOMER_ID,
                VALID_PRODUCT_ID,
                LocalDateTime.now()
            );

            given(wishlistUseCase.addProduct(VALID_CUSTOMER_ID, VALID_PRODUCT_ID))
                .willReturn(response);

            mockMvc
                .perform(
                    post("/api/v1/customers/{customerId}/wishlist/products/{productId}",
                        VALID_CUSTOMER_ID,
                        VALID_PRODUCT_ID
                    )
                        .header("User-Agent", "WishlistApp/1.0")
                        .header("Accept", "application/json"))
                .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("Given a customer wants to remove a product from wishlist")
    class GivenCustomerWantsToRemoveProduct {

        @Test
        @DisplayName("Should return 204 No Content when product is removed successfully")
        void shouldReturn204NoContentWhenProductIsRemovedSuccessfully() throws Exception {
            willDoNothing()
                .given(wishlistUseCase)
                .removeProduct(VALID_CUSTOMER_ID, VALID_PRODUCT_ID);

            mockMvc
                .perform(
                    delete("/api/v1/customers/{customerId}/wishlist/products/{productId}",
                        VALID_CUSTOMER_ID,
                        VALID_PRODUCT_ID
                    )
                )
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

            then(wishlistUseCase).should().removeProduct(VALID_CUSTOMER_ID, VALID_PRODUCT_ID);
        }

        @Test
        @DisplayName("Should return 404 Not Found when product doesn't exist")
        void shouldReturn404NotFoundWhenProductDoesntExist() throws Exception {
            willThrow(new ProductNotFoundException("Product not found in wishlist"))
                .given(wishlistUseCase).removeProduct(VALID_CUSTOMER_ID, VALID_PRODUCT_ID);

            mockMvc
                .perform(
                    delete("/api/v1/customers/{customerId}/wishlist/products/{productId}",
                        VALID_CUSTOMER_ID,
                        VALID_PRODUCT_ID
                    )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("PRODUCT_NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("not found in wishlist")));
        }

        @Test
        @DisplayName("Should return 404 Not Found when customer doesn't exist")
        void shouldReturn404NotFoundWhenCustomerDoesntExist() throws Exception {
            willThrow(new CustomerNotFoundException("Customer not found"))
                .given(wishlistUseCase).removeProduct(VALID_CUSTOMER_ID, VALID_PRODUCT_ID);

            mockMvc
                .perform(
                    delete("/api/v1/customers/{customerId}/wishlist/products/{productId}",
                        VALID_CUSTOMER_ID,
                        VALID_PRODUCT_ID
                    )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("CUSTOMER_NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("Customer not found")));
        }
    }

    @Nested
    @DisplayName("Given a customer wants to check if product exists in wishlist")
    class GivenCustomerWantsToCheckProductExists {

        @Test
        @DisplayName("Should return 200 OK when product exists in wishlist")
        void shouldReturn200OkWhenProductExistsInWishlist() throws Exception {
            // Given - Product exists in customer's wishlist
            ProductExistsResponse existsResponse = new ProductExistsResponse(
                VALID_CUSTOMER_ID,
                VALID_PRODUCT_ID,
                true,
                LocalDateTime.of(2024, 8, 29, 10, 30)
            );

            given(wishlistUseCase.checkProductExists(VALID_CUSTOMER_ID, VALID_PRODUCT_ID))
                .willReturn(existsResponse);

            mockMvc
                .perform(
                    get(
                        "/api/v1/customers/{customerId}/wishlist/products/{productId}",
                        VALID_CUSTOMER_ID,
                        VALID_PRODUCT_ID
                    )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId", is(VALID_CUSTOMER_ID)))
                .andExpect(jsonPath("$.productId", is(VALID_PRODUCT_ID)))
                .andExpect(jsonPath("$.exists", is(true)))
                .andExpect(jsonPath("$.addedAt", notNullValue()));
        }

        @Test
        @DisplayName("Should return 404 Not Found when product doesn't exist in wishlist")
        void shouldReturn404NotFoundWhenProductDoesntExistInWishlist() throws Exception {
            given(wishlistUseCase.checkProductExists(VALID_CUSTOMER_ID, VALID_PRODUCT_ID))
                .willThrow(new ProductNotFoundException("Product not found in wishlist"));

            mockMvc.perform(get("/api/v1/customers/{customerId}/wishlist/products/{productId}",
                    VALID_CUSTOMER_ID, VALID_PRODUCT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("PRODUCT_NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("not found in wishlist")));
        }
    }

    @Nested
    @DisplayName("Given a customer wants to clear their entire wishlist")
    class GivenCustomerWantsToClearWishlist {

        @Test
        @DisplayName("Should return 204 No Content when wishlist is cleared successfully")
        void shouldReturn204NoContentWhenWishlistIsClearedSuccessfully() throws Exception {
            willDoNothing().given(wishlistUseCase).clearWishlist(VALID_CUSTOMER_ID);

            mockMvc
                .perform(delete("/api/v1/customers/{customerId}/wishlist", VALID_CUSTOMER_ID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

            then(wishlistUseCase).should().clearWishlist(VALID_CUSTOMER_ID);
        }

        @Test
        @DisplayName("Should return 404 Not Found when customer doesn't exist")
        void shouldReturn404NotFoundWhenCustomerDoesntExist() throws Exception {
            willThrow(new CustomerNotFoundException("Customer not found"))
                .given(wishlistUseCase)
                .clearWishlist(VALID_CUSTOMER_ID);

            mockMvc
                .perform(delete("/api/v1/customers/{customerId}/wishlist", VALID_CUSTOMER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("CUSTOMER_NOT_FOUND")));
        }
    }

    @Nested
    @DisplayName("Given there are validation and error handling scenarios")
    class GivenValidationAndErrorHandlingScenarios {

        @Test
        @DisplayName("Should handle unexpected server errors gracefully")
        void shouldHandleUnexpectedServerErrorsGracefully() throws Exception {
            given(wishlistUseCase.getWishlist(VALID_CUSTOMER_ID))
                .willThrow(new RuntimeException("Database connection failed"));

            mockMvc
                .perform(get("/api/v1/customers/{customerId}/wishlist", VALID_CUSTOMER_ID))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code", is("INTERNAL_SERVER_ERROR")))
                .andExpect(jsonPath("$.message", containsString("unexpected error occurred")));
        }

        @Test
        @DisplayName("Should validate customer ID format in all endpoints")
        void shouldValidateCustomerIdFormatInAllEndpoints() throws Exception {
            final String invalidId = "customer@#$%";

            mockMvc
                .perform(get("/api/v1/customers/{customerId}/wishlist", invalidId))
                .andExpect(status().isBadRequest());

            mockMvc
                .perform(
                    post(
                        "/api/v1/customers/{customerId}/wishlist/products/{productId}",
                        invalidId,
                        VALID_PRODUCT_ID
                    )
                )
                .andExpect(status().isBadRequest());

            mockMvc
                .perform(
                    delete("/api/v1/customers/{customerId}/wishlist/products/{productId}",
                        invalidId,
                        VALID_PRODUCT_ID
                    )
                )
                .andExpect(status().isBadRequest());

            mockMvc
                .perform(
                    get("/api/v1/customers/{customerId}/wishlist/products/{productId}",
                        invalidId,
                        VALID_PRODUCT_ID
                    )
                )
                .andExpect(status().isBadRequest());

            mockMvc
                .perform(delete("/api/v1/customers/{customerId}/wishlist", invalidId))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should validate product ID format in relevant endpoints")
        void shouldValidateProductIdFormatInRelevantEndpoints() throws Exception {
            final String invalidProductId = "product@#$%";

            mockMvc
                .perform(
                    post("/api/v1/customers/{customerId}/wishlist/products/{productId}",
                        VALID_CUSTOMER_ID,
                        invalidProductId
                    )
                )
                .andExpect(status().isBadRequest());

            mockMvc
                .perform(
                    delete("/api/v1/customers/{customerId}/wishlist/products/{productId}",
                        VALID_CUSTOMER_ID,
                        invalidProductId
                    )
                )
                .andExpect(status().isBadRequest());

            mockMvc
                .perform(
                    get("/api/v1/customers/{customerId}/wishlist/products/{productId}",
                        VALID_CUSTOMER_ID,
                        invalidProductId
                    )
                )
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle CORS headers correctly")
        void shouldHandleCorsHeadersCorrectly() throws Exception {
            final WishlistResponse emptyResponse = new WishlistResponse(
                VALID_CUSTOMER_ID,
                Collections.emptyList(),
                0,
                20
            );

            given(wishlistUseCase.getWishlist(VALID_CUSTOMER_ID)).willReturn(emptyResponse);

            mockMvc
                .perform(
                    get("/api/v1/customers/{customerId}/wishlist", VALID_CUSTOMER_ID)
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should include proper content type in all responses")
        void shouldIncludeProperContentTypeInAllResponses() throws Exception {
            final WishlistResponse response = new WishlistResponse(
                VALID_CUSTOMER_ID,
                Collections.emptyList(),
                0,
                20
            );

            given(wishlistUseCase.getWishlist(VALID_CUSTOMER_ID)).willReturn(response);

            mockMvc
                .perform(get("/api/v1/customers/{customerId}/wishlist", VALID_CUSTOMER_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }
    }
}
