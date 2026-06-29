package com.quickbite.restaurant.service;

import com.quickbite.restaurant.dto.*;
import java.util.List;

public interface RestaurantService {
    RestaurantResponse registerRestaurant(RestaurantRequest request);
    RestaurantResponse updateRestaurant(Long restaurantId, RestaurantUpdateRequest request);
    RestaurantResponse getRestaurantById(Long restaurantId);
    List<RestaurantResponse> getRestaurantsByOwner(Long ownerId);
    List<RestaurantResponse> getAllApprovedRestaurants();
    List<RestaurantResponse> getPendingRestaurants();
    List<RestaurantResponse> getNearbyRestaurants(NearbyRequest request);
    List<RestaurantResponse> searchByCuisine(String cuisine);
    RestaurantResponse approveRestaurant(Long restaurantId, boolean approved);
    RestaurantResponse toggleOpenStatus(Long restaurantId, boolean isOpen);
    void updateRating(Long restaurantId, Double newAvgRating);
}
