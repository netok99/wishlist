package com.wishlist.presentation;

import com.wishlist.application.dto.AddProductResponse;
import com.wishlist.application.dto.ProductExistsResponse;
import com.wishlist.application.dto.WishlistResponse;
import com.wishlist.domain.usecase.WishlistUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/v1/customers/{customerId}/wishlist")
@Tag(name = "Wishlist", description = "Wishlist management endpoints")
public class WishlistController {
    private final WishlistUseCase wishlistUseCase;
    private static final String REGEX_PATTERN_COSTUMER_ID = "^[a-zA-Z0-9-_]{1,50}$";
    private static final String REGEX_PATTERN_PRODUCT_ID = "^[a-zA-Z0-9-_]{1,100}$";
    private static final String MESSAGE_VALIDATION_ID = "Invalid product ID format";

    public WishlistController(WishlistUseCase wishlistUseCase) {
        this.wishlistUseCase = wishlistUseCase;
    }

    @Operation(
        summary = "Get customer wishlist",
        description = "Retrieve all products in customer's wishlist"
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Wishlist retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid customer ID"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
        }
    )
    @GetMapping
    public ResponseEntity<WishlistResponse> getWishlist(
        @Parameter(description = "Customer unique identifier", required = true)
        @PathVariable
        @NotBlank
        @Pattern(regexp = REGEX_PATTERN_COSTUMER_ID, message = MESSAGE_VALIDATION_ID)
        String customerId
    ) {
        return ResponseEntity
            .ok()
            .body(wishlistUseCase.getWishlist(customerId));
    }

    @Operation(
        summary = "Add product to wishlist",
        description = "Add a product to customer's wishlist"
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201", description = "Product added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or wishlist limit exceeded"),
            @ApiResponse(responseCode = "409", description = "Product already exists in wishlist")
        }
    )
    @PostMapping("/products/{productId}")
    public ResponseEntity<AddProductResponse> addProduct(
        @Parameter(description = "Customer unique identifier", required = true)
        @PathVariable
        @NotBlank
        @Pattern(regexp = REGEX_PATTERN_COSTUMER_ID, message = MESSAGE_VALIDATION_ID)
        String customerId,
        @Parameter(description = "Product unique identifier", required = true)
        @PathVariable
        @NotBlank
        @Pattern(regexp = REGEX_PATTERN_PRODUCT_ID, message = MESSAGE_VALIDATION_ID)
        String productId
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(wishlistUseCase.addProduct(customerId, productId));
    }

    @Operation(
        summary = "Remove product from wishlist",
        description = "Remove a product from customer's wishlist"
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "204", description = "Product removed successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found in wishlist or customer not found")
        }
    )
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<Void> removeProduct(
        @Parameter(description = "Customer unique identifier", required = true)
        @PathVariable
        @NotBlank
        @Pattern(regexp = REGEX_PATTERN_COSTUMER_ID, message = MESSAGE_VALIDATION_ID)
        String customerId,
        @Parameter(description = "Product unique identifier", required = true)
        @PathVariable
        @NotBlank
        @Pattern(regexp = REGEX_PATTERN_PRODUCT_ID, message = MESSAGE_VALIDATION_ID)
        String productId
    ) {
        wishlistUseCase.removeProduct(customerId, productId);
        return ResponseEntity
            .noContent()
            .build();
    }

    @Operation(
        summary = "Check if product exists in wishlist",
        description = "Verify if a specific product exists in customer's wishlist"
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Product exists in wishlist"),
            @ApiResponse(responseCode = "404", description = "Product not found in wishlist")
        }
    )
    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductExistsResponse> checkProductExists(
        @Parameter(description = "Customer unique identifier", required = true)
        @PathVariable
        @NotBlank
        @Pattern(regexp = REGEX_PATTERN_COSTUMER_ID, message = MESSAGE_VALIDATION_ID)
        String customerId,
        @Parameter(description = "Product unique identifier", required = true)
        @PathVariable
        @NotBlank
        @Pattern(regexp = REGEX_PATTERN_PRODUCT_ID, message = MESSAGE_VALIDATION_ID)
        String productId
    ) {
        return ResponseEntity.ok(wishlistUseCase.checkProductExists(customerId, productId));
    }

    @Operation(
        summary = "Clear customer wishlist",
        description = "Remove all products from customer's wishlist"
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "204", description = "Wishlist cleared successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
        }
    )
    @DeleteMapping
    public ResponseEntity<Void> clearWishlist(
        @Parameter(description = "Customer unique identifier", required = true)
        @PathVariable
        @NotBlank @Pattern(regexp = REGEX_PATTERN_COSTUMER_ID, message = MESSAGE_VALIDATION_ID)
        String customerId
    ) {
        wishlistUseCase.clearWishlist(customerId);
        return ResponseEntity
            .noContent()
            .build();
    }
}
