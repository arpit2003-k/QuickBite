package com.quickbite.review.service;

import com.quickbite.review.client.DeliveryClient;
import com.quickbite.review.client.RestaurantClient;
import com.quickbite.review.dto.ReviewRequest;
import com.quickbite.review.dto.ReviewResponse;
import com.quickbite.review.entity.Review;
import com.quickbite.review.exception.CustomException;
import com.quickbite.review.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private RestaurantClient restaurantClient;
    @Mock
    private DeliveryClient deliveryClient;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private ReviewRequest reviewRequest;
    private Review mockReview;

    @BeforeEach
    void setUp() {
        reviewRequest = new ReviewRequest();
        reviewRequest.setOrderId(100L);
        reviewRequest.setRestaurantId(10L);
        reviewRequest.setDeliveryAgentId(5L);
        reviewRequest.setFoodRating(5);
        reviewRequest.setDeliveryRating(4);
        reviewRequest.setComment("Great!");

        mockReview = new Review();
        mockReview.setReviewId(1L);
        mockReview.setOrderId(100L);
        mockReview.setRestaurantId(10L);
        mockReview.setDeliveryAgentId(5L);
        mockReview.setFoodRating(5);
        mockReview.setDeliveryRating(4);
    }

    @Test
    void addReview_Success_ReturnsResponse() {
        when(reviewRepository.findByOrderId(100L)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenReturn(mockReview);
        when(reviewRepository.getAverageFoodRating(10L)).thenReturn(4.5);
        when(reviewRepository.getAverageDeliveryRating(5L)).thenReturn(4.0);

        ReviewResponse response = reviewService.addReview(reviewRequest);

        assertNotNull(response);
        verify(restaurantClient).updateRestaurantRating(eq(10L), anyDouble());
        verify(deliveryClient).updateDeliveryAgentRating(eq(5L), anyDouble());
    }

    @Test
    void addReview_AlreadyExists_ThrowsCustomException() {
        when(reviewRepository.findByOrderId(100L)).thenReturn(Optional.of(mockReview));

        assertThrows(CustomException.class, () -> reviewService.addReview(reviewRequest));
    }

    @Test
    void getReviewByOrder_Success_ReturnsResponse() {
        when(reviewRepository.findByOrderId(100L)).thenReturn(Optional.of(mockReview));

        ReviewResponse response = reviewService.getReviewByOrder(100L);

        assertNotNull(response);
        assertEquals(1L, response.getReviewId());
    }

    @Test
    void getReviewByOrder_NotFound_ThrowsCustomException() {
        when(reviewRepository.findByOrderId(anyLong())).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> reviewService.getReviewByOrder(100L));
    }

    @Test
    void getReviewsByRestaurant_Success_ReturnsList() {
        when(reviewRepository.findByRestaurantId(10L)).thenReturn(Collections.singletonList(mockReview));

        var results = reviewService.getReviewsByRestaurant(10L);

        assertEquals(1, results.size());
    }

    @Test
    void deleteReview_Success_CallsDelete() {
        when(reviewRepository.existsById(1L)).thenReturn(true);

        reviewService.deleteReview(1L);

        verify(reviewRepository).deleteById(1L);
    }

    @Test
    void deleteReview_NotFound_ThrowsCustomException() {
        when(reviewRepository.existsById(1L)).thenReturn(false);

        assertThrows(CustomException.class, () -> reviewService.deleteReview(1L));
    }

    @Test
    void getReviewsByDeliveryAgent_Success_ReturnsList() {
        when(reviewRepository.findByDeliveryAgentId(5L)).thenReturn(Collections.singletonList(mockReview));

        var results = reviewService.getReviewsByDeliveryAgent(5L);

        assertEquals(1, results.size());
    }

    @Test
    void getReviewsByCustomer_Success_ReturnsList() {
        when(reviewRepository.findByCustomerId(1L)).thenReturn(Collections.singletonList(mockReview));

        var results = reviewService.getReviewsByCustomer(1L);

        assertEquals(1, results.size());
    }

    @Test
    void listAllReviews_Success_ReturnsList() {
        when(reviewRepository.findAll()).thenReturn(Collections.singletonList(mockReview));

        var results = reviewService.listAllReviews();

        assertEquals(1, results.size());
    }
}
