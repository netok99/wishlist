package com.wishlist.infrastructure.repository;

import com.wishlist.domain.entity.Wishlist;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@DataMongoTest
@Testcontainers
@Import(WishlistRepositoryImpl.class)
@DisplayName("Repositório MongoDB - Comportamentos")
public class WishlistRepositoryTest {
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0")
        .withExposedPorts(27017);
    @Autowired
    private WishlistRepositoryImpl wishlistRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    private final String VALID_CUSTOMER_ID = "customer123";
    private final String ANOTHER_CUSTOMER_ID = "customer456";
    private final String NON_EXISTENT_CUSTOMER_ID = "nonExistentCustomer";

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.data.mongodb.database", () -> "wishlist_test_db");
    }

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection("wishlists");
    }

    @Nested
    @DisplayName("Given I need to find a wishlist by customer ID")
    class GivenINeedToFindWishlistByCustomerId {

        @Nested
        @DisplayName("When customer has no wishlist")
        class WhenCustomerHasNoWishlist {

            @Test
            @DisplayName("Then should return empty Optional")
            void thenShouldReturnEmptyOptional() {
                final Optional<Wishlist> result = wishlistRepository.findByCustomerId(VALID_CUSTOMER_ID);

                assertThat(result).isEmpty();
            }

            @Test
            @DisplayName("Then should not throw any exception")
            void thenShouldNotThrowAnyException() {
                assertThatNoException()
                    .isThrownBy(() -> wishlistRepository.findByCustomerId(VALID_CUSTOMER_ID));
            }
        }

        @Nested
        @DisplayName("When customer has an existing wishlist")
        class WhenCustomerHasExistingWishlist {

            @Test
            @DisplayName("Then should return wishlist with correct customer ID")
            void thenShouldReturnWishlistWithCorrectCustomerId() {
                final Wishlist savedWishlist = givenCustomerHasWishlistWithProducts(
                    VALID_CUSTOMER_ID,
                    "product1",
                    "product2"
                );

                final Optional<Wishlist> result = wishlistRepository.findByCustomerId(VALID_CUSTOMER_ID);

                assertThat(result).isPresent();
                assertThat(result.get().getCustomerId()).isEqualTo(VALID_CUSTOMER_ID);
                assertThat(result.get().getId()).isEqualTo(savedWishlist.getId());
            }

            @Test
            @DisplayName("Then should return wishlist with all products intact")
            void thenShouldReturnWishlistWithAllProductsIntact() {
                givenCustomerHasWishlistWithProducts(VALID_CUSTOMER_ID, "product1", "product2", "product3");

                final Optional<Wishlist> result = wishlistRepository.findByCustomerId(VALID_CUSTOMER_ID);

                assertThat(result).isPresent();
                assertThat(result.get().getProducts()).hasSize(3);
                assertThat(result.get().hasProduct("product1")).isTrue();
                assertThat(result.get().hasProduct("product2")).isTrue();
                assertThat(result.get().hasProduct("product3")).isTrue();
            }

            @Test
            @DisplayName("Then should return wishlist with timestamps preserved")
            void thenShouldReturnWishlistWithTimestampsPreserved() {
                final Wishlist originalWishlist = givenCustomerHasWishlistWithProducts(
                    VALID_CUSTOMER_ID,
                    "product1"
                );
                final Optional<Wishlist> result = wishlistRepository.findByCustomerId(VALID_CUSTOMER_ID);
                final LocalDateTime expectedCreatedAt = originalWishlist.getCreatedAt().truncatedTo(ChronoUnit.MILLIS);
                final LocalDateTime expectedUpdatedAt = originalWishlist.getUpdatedAt().truncatedTo(ChronoUnit.MILLIS);
                final LocalDateTime actualCreatedAt = result.get().getCreatedAt().truncatedTo(ChronoUnit.MILLIS);
                final LocalDateTime actualUpdatedAt = result.get().getUpdatedAt().truncatedTo(ChronoUnit.MILLIS);

                assertThat(result).isPresent();
                assertThat(actualCreatedAt).isEqualTo(expectedCreatedAt);
                assertThat(actualUpdatedAt).isEqualTo(expectedUpdatedAt);
            }
        }

        @Nested
        @DisplayName("When searching for different customers")
        class WhenSearchingForDifferentCustomers {

            @Test
            @DisplayName("Then should return correct wishlist for each customer")
            void thenShouldReturnCorrectWishlistForEachCustomer() {
                givenCustomerHasWishlistWithProducts(VALID_CUSTOMER_ID, "product1", "product2");
                givenCustomerHasWishlistWithProducts(ANOTHER_CUSTOMER_ID, "product3", "product4");

                final Optional<Wishlist> result1 = wishlistRepository.findByCustomerId(VALID_CUSTOMER_ID);
                final Optional<Wishlist> result2 = wishlistRepository.findByCustomerId(ANOTHER_CUSTOMER_ID);

                assertThat(result1).isPresent();
                assertThat(result1.get().getCustomerId()).isEqualTo(VALID_CUSTOMER_ID);
                assertThat(result1.get().hasProduct("product1")).isTrue();
                assertThat(result1.get().hasProduct("product3")).isFalse();

                assertThat(result2).isPresent();
                assertThat(result2.get().getCustomerId()).isEqualTo(ANOTHER_CUSTOMER_ID);
                assertThat(result2.get().hasProduct("product3")).isTrue();
                assertThat(result2.get().hasProduct("product1")).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("Given I need to save a wishlist")
    class GivenINeedToSaveWishlist {

        @Nested
        @DisplayName("When saving a new wishlist")
        class WhenSavingNewWishlist {

            @Test
            @DisplayName("Then should persist wishlist and return it with generated ID")
            void thenShouldPersistWishlistAndReturnItWithGeneratedId() {
                Wishlist newWishlist = new Wishlist(VALID_CUSTOMER_ID);
                newWishlist.addProduct("product1");
                final Wishlist savedWishlist = wishlistRepository.save(newWishlist);

                assertThat(savedWishlist).isNotNull();
                assertThat(savedWishlist.getId()).isNotNull();
                assertThat(savedWishlist.getCustomerId()).isEqualTo(VALID_CUSTOMER_ID);
                assertThat(savedWishlist.hasProduct("product1")).isTrue();
            }

            @Test
            @DisplayName("Then should set updatedAt timestamp automatically")
            void thenShouldSetUpdatedAtTimestampAutomatically() {
                LocalDateTime beforeSave = LocalDateTime.now();
                Wishlist savedWishlist = wishlistRepository.save(new Wishlist(VALID_CUSTOMER_ID));

                assertThat(savedWishlist.getUpdatedAt()).isAfterOrEqualTo(beforeSave);
                assertThat(savedWishlist.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
            }

            @Test
            @DisplayName("Then should be retrievable by customer ID after saving")
            void thenShouldBeRetrievableByCustomerIdAfterSaving() {
                Wishlist newWishlist = new Wishlist(VALID_CUSTOMER_ID);
                newWishlist.addProduct("product1");
                wishlistRepository.save(newWishlist);

                Optional<Wishlist> retrievedWishlist = wishlistRepository.findByCustomerId(VALID_CUSTOMER_ID);
                assertThat(retrievedWishlist).isPresent();
                assertThat(retrievedWishlist.get().getCustomerId()).isEqualTo(VALID_CUSTOMER_ID);
                assertThat(retrievedWishlist.get().hasProduct("product1")).isTrue();
            }
        }

        @Nested
        @DisplayName("When updating an existing wishlist")
        class WhenUpdatingExistingWishlist {

            @Test
            @DisplayName("Then should update wishlist and preserve ID")
            void thenShouldUpdateWishlistAndPreserveId() {
                Wishlist existingWishlist = givenCustomerHasWishlistWithProducts(
                    VALID_CUSTOMER_ID,
                    "product1"
                );
                String originalId = existingWishlist.getId();
                existingWishlist.addProduct("product2");
                final Wishlist updatedWishlist = wishlistRepository.save(existingWishlist);

                assertThat(updatedWishlist.getId()).isEqualTo(originalId);
                assertThat(updatedWishlist.hasProduct("product1")).isTrue();
                assertThat(updatedWishlist.hasProduct("product2")).isTrue();
            }

            @Test
            @DisplayName("Then should update the updatedAt timestamp")
            void thenShouldUpdateTheUpdatedAtTimestamp() {
                final Wishlist existingWishlist = givenCustomerHasWishlistWithProducts(
                    VALID_CUSTOMER_ID,
                    "product1"
                );
                final LocalDateTime originalUpdatedAt = existingWishlist.getUpdatedAt();

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }

                existingWishlist.addProduct("product2");
                Wishlist updatedWishlist = wishlistRepository.save(existingWishlist);

                assertThat(updatedWishlist.getUpdatedAt()).isAfter(originalUpdatedAt);
            }
        }
    }

    @Nested
    @DisplayName("Given I need to delete a wishlist by customer ID")
    class GivenINeedToDeleteWishlistByCustomerId {

        @Nested
        @DisplayName("When customer has an existing wishlist")
        class WhenCustomerHasExistingWishlist {

            @Test
            @DisplayName("Then should remove wishlist from database")
            void thenShouldRemoveWishlistFromDatabase() {
                givenCustomerHasWishlistWithProducts(VALID_CUSTOMER_ID, "product1", "product2");

                assertThat(wishlistRepository.findByCustomerId(VALID_CUSTOMER_ID)).isPresent();

                wishlistRepository.deleteByCustomerId(VALID_CUSTOMER_ID);

                final Optional<Wishlist> result = wishlistRepository.findByCustomerId(VALID_CUSTOMER_ID);
                assertThat(result).isEmpty();
            }

            @Test
            @DisplayName("Then should not affect other customers' wishlists")
            void thenShouldNotAffectOtherCustomersWishlists() {
                givenCustomerHasWishlistWithProducts(VALID_CUSTOMER_ID, "product1");
                givenCustomerHasWishlistWithProducts(ANOTHER_CUSTOMER_ID, "product2");

                wishlistRepository.deleteByCustomerId(VALID_CUSTOMER_ID);

                assertThat(wishlistRepository.findByCustomerId(VALID_CUSTOMER_ID)).isEmpty();
                assertThat(wishlistRepository.findByCustomerId(ANOTHER_CUSTOMER_ID)).isPresent();
            }
        }

        @Nested
        @DisplayName("When customer has no wishlist")
        class WhenCustomerHasNoWishlist {

            @Test
            @DisplayName("Then should complete without error")
            void thenShouldCompleteWithoutError() {
                assertThatNoException()
                    .isThrownBy(() -> wishlistRepository.deleteByCustomerId(NON_EXISTENT_CUSTOMER_ID));
            }
        }
    }

    @Nested
    @DisplayName("Given I need to check if a wishlist exists by customer ID")
    class GivenINeedToCheckIfWishlistExistsByCustomerId {

        @Nested
        @DisplayName("When customer has an existing wishlist")
        class WhenCustomerHasExistingWishlist {

            @Test
            @DisplayName("Then should return true")
            void thenShouldReturnTrue() {
                givenCustomerHasWishlistWithProducts(VALID_CUSTOMER_ID, "product1");
                final boolean exists = wishlistRepository.existsByCustomerId(VALID_CUSTOMER_ID);

                assertThat(exists).isTrue();
            }

            @Test
            @DisplayName("Then should return true even for empty wishlist")
            void thenShouldReturnTrueEvenForEmptyWishlist() {
                wishlistRepository.save(new Wishlist(VALID_CUSTOMER_ID));
                final boolean exists = wishlistRepository.existsByCustomerId(VALID_CUSTOMER_ID);

                assertThat(exists).isTrue();
            }
        }

        @Nested
        @DisplayName("When customer has no wishlist")
        class WhenCustomerHasNoWishlist {

            @Test
            @DisplayName("Then should return false")
            void thenShouldReturnFalse() {
                final boolean exists = wishlistRepository.existsByCustomerId(NON_EXISTENT_CUSTOMER_ID);

                assertThat(exists).isFalse();
            }
        }

        @Nested
        @DisplayName("When checking existence for different customers")
        class WhenCheckingExistenceForDifferentCustomers {

            @Test
            @DisplayName("Then should return correct existence status for each customer")
            void thenShouldReturnCorrectExistenceStatusForEachCustomer() {
                givenCustomerHasWishlistWithProducts(VALID_CUSTOMER_ID, "product1");

                final boolean customer1Exists = wishlistRepository.existsByCustomerId(VALID_CUSTOMER_ID);
                final boolean customer2Exists = wishlistRepository.existsByCustomerId(ANOTHER_CUSTOMER_ID);

                assertThat(customer1Exists).isTrue();
                assertThat(customer2Exists).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("Given I need to handle edge cases and validation")
    class GivenINeedToHandleEdgeCasesAndValidation {

        @Nested
        @DisplayName("When dealing with null or empty customer IDs")
        class WhenDealingWithNullOrEmptyCustomerIds {

            @Test
            @DisplayName("Then findByCustomerId should handle null gracefully")
            void thenFindByCustomerIdShouldHandleNullGracefully() {
                assertThatNoException()
                    .isThrownBy(() -> {
                        Optional<Wishlist> result = wishlistRepository.findByCustomerId(null);
                        assertThat(result).isEmpty();
                    });
            }

            @Test
            @DisplayName("Then existsByCustomerId should handle empty string gracefully")
            void thenExistsByCustomerIdShouldHandleEmptyStringGracefully() {
                final boolean exists = wishlistRepository.existsByCustomerId("");

                assertThat(exists).isFalse();
            }
        }

        @Nested
        @DisplayName("When dealing with large wishlists")
        class WhenDealingWithLargeWishlists {

            @Test
            @DisplayName("Then should handle wishlist at maximum capacity")
            void thenShouldHandleWishlistAtMaximumCapacity() {
                Wishlist maxCapacityWishlist = new Wishlist(VALID_CUSTOMER_ID);
                IntStream.rangeClosed(1, 20)
                    .mapToObj(i -> "product" + String.format("%03d", i))
                    .forEach(maxCapacityWishlist::addProduct);
                wishlistRepository.save(maxCapacityWishlist);
                final Optional<Wishlist> retrievedWishlist = wishlistRepository.findByCustomerId(VALID_CUSTOMER_ID);

                assertThat(retrievedWishlist).isPresent();
                assertThat(retrievedWishlist.get().getProductCount()).isEqualTo(20);
                assertThat(retrievedWishlist.get().cannotAddProduct()).isTrue();
            }
        }
    }

    private Wishlist givenCustomerHasWishlistWithProducts(String customerId, String... productIds) {
        final Wishlist wishlist = new Wishlist(customerId);
        Stream.of(productIds).forEach(wishlist::addProduct);
        return wishlistRepository.save(wishlist);
    }
}
