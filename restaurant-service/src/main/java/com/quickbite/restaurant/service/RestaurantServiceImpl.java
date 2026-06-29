package com.quickbite.restaurant.service;

import com.quickbite.restaurant.dto.*;
import com.quickbite.restaurant.entity.Restaurant;
import com.quickbite.restaurant.exception.CustomException;
import com.quickbite.restaurant.repository.RestaurantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RestaurantServiceImpl implements RestaurantService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    private RestaurantResponse convertToResponse(Restaurant r) {
        return new RestaurantResponse(
            r.getRestaurantId(), r.getOwnerId(), r.getName(), r.getDescription(), r.getCuisine(),
            r.getAddress(), r.getCity(), r.getLatitude(), r.getLongitude(),
            r.getPhone(), r.getAvgRating(), r.getIsOpen(), r.getIsApproved(),
            r.getDeliveryRadius(), r.getMinOrderAmount(), r.getEstimatedDeliveryMin(),
            r.getImageUrl()
        );
    }

    @Override
    @Transactional
    public RestaurantResponse registerRestaurant(RestaurantRequest request) {
        log.info("Registering new restaurant: {} for ownerId: {}", request.getName(), request.getOwnerId());

        Restaurant restaurant = new Restaurant();
        restaurant.setOwnerId(request.getOwnerId());
        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setCuisine(request.getCuisine());
        restaurant.setAddress(request.getAddress());
        restaurant.setCity(request.getCity());
        restaurant.setLatitude(request.getLatitude());
        restaurant.setLongitude(request.getLongitude());
        restaurant.setPhone(request.getPhone());
        restaurant.setIsOpen(false);
        restaurant.setIsApproved(false);   // Needs admin approval
        restaurant.setDeliveryRadius(request.getDeliveryRadius());
        restaurant.setMinOrderAmount(request.getMinOrderAmount());
        restaurant.setEstimatedDeliveryMin(request.getEstimatedDeliveryMin());
        restaurant.setImageUrl(request.getImageUrl());

        Restaurant saved = restaurantRepository.save(restaurant);
        log.info("Restaurant registered with ID: {}, awaiting approval", saved.getRestaurantId());
        return convertToResponse(saved);
    }

    @Override
    @Transactional
    public RestaurantResponse updateRestaurant(Long restaurantId, RestaurantUpdateRequest request) {
        log.info("Updating restaurant: {}", restaurantId);
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new CustomException("Restaurant not found"));

        if (request.getName() != null) restaurant.setName(request.getName());
        if (request.getDescription() != null) restaurant.setDescription(request.getDescription());
        if (request.getCuisine() != null) restaurant.setCuisine(request.getCuisine());
        if (request.getAddress() != null) restaurant.setAddress(request.getAddress());
        if (request.getCity() != null) restaurant.setCity(request.getCity());
        if (request.getLatitude() != null) restaurant.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) restaurant.setLongitude(request.getLongitude());
        if (request.getPhone() != null) restaurant.setPhone(request.getPhone());
        if (request.getIsOpen() != null) restaurant.setIsOpen(request.getIsOpen());
        if (request.getDeliveryRadius() != null) restaurant.setDeliveryRadius(request.getDeliveryRadius());
        if (request.getMinOrderAmount() != null) restaurant.setMinOrderAmount(request.getMinOrderAmount());
        if (request.getEstimatedDeliveryMin() != null) restaurant.setEstimatedDeliveryMin(request.getEstimatedDeliveryMin());
        if (request.getImageUrl() != null) restaurant.setImageUrl(request.getImageUrl());

        Restaurant updated = restaurantRepository.save(restaurant);
        return convertToResponse(updated);
    }

    @Override
    public RestaurantResponse getRestaurantById(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new CustomException("Restaurant not found"));
        return convertToResponse(restaurant);
    }

    @Override
    public List<RestaurantResponse> getRestaurantsByOwner(Long ownerId) {
        return restaurantRepository.findByOwnerId(ownerId)
                .stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public List<RestaurantResponse> getAllApprovedRestaurants() {
        return restaurantRepository.findByIsApprovedTrue()
                .stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public List<RestaurantResponse> getPendingRestaurants() {
        return restaurantRepository.findByIsApprovedFalse()
                .stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public List<RestaurantResponse> getNearbyRestaurants(NearbyRequest request) {
        double radius = request.getRadius() != null ? request.getRadius() : 10.0;
        log.info("Finding nearby restaurants at ({}, {}) within {} km", request.getLatitude(), request.getLongitude(), radius);
        return restaurantRepository.findNearbyRestaurants(request.getLatitude(), request.getLongitude(), radius)
                .stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public List<RestaurantResponse> searchByCuisine(String cuisine) {
        return restaurantRepository.findByCuisineContainingIgnoreCase(cuisine)
                .stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RestaurantResponse approveRestaurant(Long restaurantId, boolean approved) {
        log.info("Approving restaurant: {} = {}", restaurantId, approved);
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new CustomException("Restaurant not found"));
        restaurant.setIsApproved(approved);
        Restaurant saved = restaurantRepository.save(restaurant);
        return convertToResponse(saved);
    }

    @Override
    @Transactional
    public RestaurantResponse toggleOpenStatus(Long restaurantId, boolean isOpen) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new CustomException("Restaurant not found"));
        restaurant.setIsOpen(isOpen);
        Restaurant saved = restaurantRepository.save(restaurant);
        return convertToResponse(saved);
    }

    @Override
    @Transactional
    public void updateRating(Long restaurantId, Double newAvgRating) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new CustomException("Restaurant not found"));
        restaurant.setAvgRating(newAvgRating);
        restaurantRepository.save(restaurant);
        log.info("Updated rating for restaurant {} to {}", restaurantId, newAvgRating);
    }
}
