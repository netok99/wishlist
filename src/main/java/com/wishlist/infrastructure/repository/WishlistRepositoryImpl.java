package com.wishlist.infrastructure.repository;

import com.wishlist.domain.entity.Wishlist;
import com.wishlist.domain.repository.WishlistRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class WishlistRepositoryImpl implements WishlistRepository {
    private final MongoTemplate mongoTemplate;
    private static final String COLLECTION_NAME = "wishlists";

    public WishlistRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Optional<Wishlist> findByCustomerId(String customerId) {
        final Query query = new Query(
            Criteria
                .where("customerId")
                .is(customerId)
        );
        final Wishlist wishlist = mongoTemplate.findOne(
            query,
            Wishlist.class,
            COLLECTION_NAME
        );
        return Optional.ofNullable(wishlist);
    }

    @Override
    public Wishlist save(Wishlist wishlist) {
        wishlist.setUpdatedAt(LocalDateTime.now());
        return mongoTemplate.save(wishlist, COLLECTION_NAME);
    }

    @Override
    public void deleteByCustomerId(String customerId) {
        final Query query = new Query(
            Criteria
                .where("customerId")
                .is(customerId)
        );
        mongoTemplate.remove(query, Wishlist.class, COLLECTION_NAME);
    }

    @Override
    public boolean existsByCustomerId(String customerId) {
        final Query query = new Query(
            Criteria
                .where("customerId")
                .is(customerId)
        );
        return mongoTemplate.exists(query, Wishlist.class, COLLECTION_NAME);
    }
}
