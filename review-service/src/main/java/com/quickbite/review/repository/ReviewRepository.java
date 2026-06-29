package com.quickbite.review.repository;

import com.quickbite.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByOrderId(Long orderId);
    List<Review> findByRestaurantId(Long restaurantId);
    List<Review> findByDeliveryAgentId(Long agentId);
    List<Review> findByCustomerId(Long customerId);

    @Query("SELECT AVG(r.foodRating) FROM Review r WHERE r.restaurantId = :restaurantId")
    Double getAverageFoodRating(@Param("restaurantId") Long restaurantId);

    @Query("SELECT AVG(r.deliveryRating) FROM Review r WHERE r.deliveryAgentId = :agentId")
    Double getAverageDeliveryRating(@Param("agentId") Long agentId);
}
