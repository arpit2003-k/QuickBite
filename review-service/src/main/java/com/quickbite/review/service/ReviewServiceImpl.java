package com.quickbite.review.service;

import com.quickbite.review.client.DeliveryClient;
import com.quickbite.review.client.RestaurantClient;
import com.quickbite.review.dto.ReviewRequest;
import com.quickbite.review.dto.ReviewResponse;
import com.quickbite.review.entity.Review;
import com.quickbite.review.exception.CustomException;
import com.quickbite.review.repository.ReviewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private RestaurantClient restaurantClient;

    @Autowired
    private DeliveryClient deliveryClient;

    private ReviewResponse toResponse(Review r) {
        return new ReviewResponse(
            r.getReviewId(),
            r.getOrderId(),
            r.getRestaurantId(),
            r.getDeliveryAgentId(),
            r.getFoodRating(),
            r.getDeliveryRating(),
            r.getComment(),
            r.getReviewDate()
        );
    }

    @Override
    @Transactional
    public ReviewResponse addReview(ReviewRequest request) {
        log.info("Adding review for orderId: {}", request.getOrderId());

        // Check if review already exists
        if (reviewRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new CustomException("Review already submitted for this order");
        }

        Review review = new Review();
        review.setOrderId(request.getOrderId());
        review.setCustomerId(request.getCustomerId());
        review.setRestaurantId(request.getRestaurantId());
        review.setDeliveryAgentId(request.getDeliveryAgentId());
        review.setFoodRating(request.getFoodRating());
        review.setDeliveryRating(request.getDeliveryRating());
        review.setComment(request.getComment());
        review.setReviewDate(LocalDateTime.now());
        review.setIsVerified(true);   // auto-verify for demo

        Review saved = reviewRepository.save(review);

        // Update average rating in Restaurant Service
        Double newFoodAvg = reviewRepository.getAverageFoodRating(saved.getRestaurantId());
        if (newFoodAvg != null) {
            try {
                restaurantClient.updateRestaurantRating(saved.getRestaurantId(), newFoodAvg);
                log.info("Updated restaurant {} avg rating to {}", saved.getRestaurantId(), newFoodAvg);
            } catch (Exception e) {
                log.warn("Failed to update restaurant rating: {}", e.getMessage());
            }
        }

        // Update average rating in Delivery Service
        if (saved.getDeliveryAgentId() != null) {
            Double newDeliveryAvg = reviewRepository.getAverageDeliveryRating(saved.getDeliveryAgentId());
            if (newDeliveryAvg != null) {
                try {
                    deliveryClient.updateDeliveryAgentRating(saved.getDeliveryAgentId(), newDeliveryAvg);
                    log.info("Updated delivery agent {} avg rating to {}", saved.getDeliveryAgentId(), newDeliveryAvg);
                } catch (Exception e) {
                    log.warn("Failed to update agent rating: {}", e.getMessage());
                }
            }
        }

        return toResponse(saved);
    }

    @Override
    public ReviewResponse getReviewByOrder(Long orderId) {
        return toResponse(reviewRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException("Review not found for order")));
    }

    @Override
    public List<ReviewResponse> getReviewsByRestaurant(Long restaurantId) {
        return reviewRepository.findByRestaurantId(restaurantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponse> getReviewsByDeliveryAgent(Long agentId) {
        return reviewRepository.findByDeliveryAgentId(agentId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponse> getReviewsByCustomer(Long customerId) {
        return reviewRepository.findByCustomerId(customerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponse> listAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new CustomException("Review not found");
        }
        reviewRepository.deleteById(reviewId);
        log.info("Deleted reviewId: {}", reviewId);
    }
}
