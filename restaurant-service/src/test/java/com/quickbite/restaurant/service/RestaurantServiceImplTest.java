package com.quickbite.restaurant.service;

import com.quickbite.restaurant.dto.*;
import com.quickbite.restaurant.entity.Restaurant;
import com.quickbite.restaurant.exception.CustomException;
import com.quickbite.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceImplTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private RestaurantServiceImpl restaurantService;

    private RestaurantRequest restaurantRequest;
    private Restaurant mockRestaurant;

    @BeforeEach
    void setUp() {
        restaurantRequest = new RestaurantRequest();
        restaurantRequest.setOwnerId(1L);
        restaurantRequest.setName("Pizza Hub");
        restaurantRequest.setAddress("123 Street");
        restaurantRequest.setLatitude(12.34);
        restaurantRequest.setLongitude(56.78);

        mockRestaurant = new Restaurant();
        mockRestaurant.setRestaurantId(1L);
        mockRestaurant.setOwnerId(1L);
        mockRestaurant.setName("Pizza Hub");
        mockRestaurant.setIsApproved(true);
        mockRestaurant.setIsOpen(true);
    }

    @Test
    void registerRestaurant_Success_ReturnsResponse() {
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(mockRestaurant);

        RestaurantResponse response = restaurantService.registerRestaurant(restaurantRequest);

        assertNotNull(response);
        assertEquals("Pizza Hub", response.getName());
        verify(restaurantRepository).save(any(Restaurant.class));
    }

    @Test
    void updateRestaurant_Success_ReturnsResponse() {
        RestaurantUpdateRequest updateRequest = new RestaurantUpdateRequest();
        updateRequest.setName("Updated Hub");
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(mockRestaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(mockRestaurant);

        RestaurantResponse response = restaurantService.updateRestaurant(1L, updateRequest);

        assertNotNull(response);
        assertEquals("Updated Hub", mockRestaurant.getName());
    }

    @Test
    void updateRestaurant_NotFound_ThrowsCustomException() {
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> restaurantService.updateRestaurant(1L, new RestaurantUpdateRequest()));
    }

    @Test
    void getRestaurantById_Success_ReturnsResponse() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(mockRestaurant));

        RestaurantResponse response = restaurantService.getRestaurantById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getRestaurantId());
    }

    @Test
    void getRestaurantById_NotFound_ThrowsCustomException() {
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> restaurantService.getRestaurantById(1L));
    }

    @Test
    void getRestaurantsByOwner_Success_ReturnsList() {
        when(restaurantRepository.findByOwnerId(1L)).thenReturn(Collections.singletonList(mockRestaurant));

        List<RestaurantResponse> results = restaurantService.getRestaurantsByOwner(1L);

        assertEquals(1, results.size());
    }

    @Test
    void getAllApprovedRestaurants_Success_ReturnsList() {
        when(restaurantRepository.findByIsApprovedTrue()).thenReturn(Collections.singletonList(mockRestaurant));

        List<RestaurantResponse> results = restaurantService.getAllApprovedRestaurants();

        assertEquals(1, results.size());
    }

    @Test
    void getPendingRestaurants_Success_ReturnsList() {
        when(restaurantRepository.findByIsApprovedFalse()).thenReturn(Collections.singletonList(mockRestaurant));

        List<RestaurantResponse> results = restaurantService.getPendingRestaurants();

        assertEquals(1, results.size());
    }

    @Test
    void getNearbyRestaurants_Success_ReturnsList() {
        NearbyRequest nearbyRequest = new NearbyRequest();
        nearbyRequest.setLatitude(12.0);
        nearbyRequest.setLongitude(56.0);
        when(restaurantRepository.findNearbyRestaurants(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(Collections.singletonList(mockRestaurant));

        List<RestaurantResponse> results = restaurantService.getNearbyRestaurants(nearbyRequest);

        assertFalse(results.isEmpty());
    }

    @Test
    void searchByCuisine_Success_ReturnsList() {
        when(restaurantRepository.findByCuisineContainingIgnoreCase(anyString()))
                .thenReturn(Collections.singletonList(mockRestaurant));

        List<RestaurantResponse> results = restaurantService.searchByCuisine("Italian");

        assertFalse(results.isEmpty());
    }

    @Test
    void approveRestaurant_Success_UpdatesStatus() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(mockRestaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(mockRestaurant);

        RestaurantResponse response = restaurantService.approveRestaurant(1L, true);

        assertTrue(response.getIsApproved());
    }

    @Test
    void toggleOpenStatus_Success_UpdatesStatus() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(mockRestaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(mockRestaurant);

        RestaurantResponse response = restaurantService.toggleOpenStatus(1L, false);

        assertFalse(response.getIsOpen());
    }

    @Test
    void updateRating_Success_UpdatesRating() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(mockRestaurant));

        restaurantService.updateRating(1L, 4.5);

        assertEquals(4.5, mockRestaurant.getAvgRating());
        verify(restaurantRepository).save(mockRestaurant);
    }
}
