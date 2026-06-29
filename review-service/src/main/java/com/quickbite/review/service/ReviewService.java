package com.quickbite.review.service;

import com.quickbite.review.dto.ReviewRequest;
import com.quickbite.review.dto.ReviewResponse;

import java.util.List;

public interface ReviewService {
    ReviewResponse addReview(ReviewRequest request);
    ReviewResponse getReviewByOrder(Long orderId);
    List<ReviewResponse> getReviewsByRestaurant(Long restaurantId);
    List<ReviewResponse> getReviewsByDeliveryAgent(Long agentId);
    List<ReviewResponse> getReviewsByCustomer(Long customerId);
    List<ReviewResponse> listAllReviews();
    void deleteReview(Long reviewId);   // admin only
}
