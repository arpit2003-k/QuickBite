package com.quickbite.restaurant.repository;

import com.quickbite.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    
    List<Restaurant> findByOwnerId(Long ownerId);
    
    List<Restaurant> findByIsApprovedTrue();
    List<Restaurant> findByIsApprovedFalse();
    List<Restaurant> findByIsApprovedTrueAndIsOpenTrue();
    
    List<Restaurant> findByCuisineContainingIgnoreCase(String cuisine);
    
    List<Restaurant> findByCityIgnoreCase(String city);
    
    Optional<Restaurant> findByRestaurantIdAndIsApprovedTrue(Long restaurantId);
    
    @Query("SELECT r FROM Restaurant r WHERE " +
           "r.isApproved = true AND " +
           "r.isOpen = true AND " +
           "(6371 * acos(cos(radians(:lat)) * cos(radians(r.latitude)) * " +
           "cos(radians(r.longitude) - radians(:lng)) + sin(radians(:lat)) * " +
           "sin(radians(r.latitude)))) <= :radius")
    List<Restaurant> findNearbyRestaurants(@Param("lat") double lat,
                                           @Param("lng") double lng,
                                           @Param("radius") double radius);
}
