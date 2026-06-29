package com.quickbite.review.controller;

import com.quickbite.review.dto.ReviewRequest;
import com.quickbite.review.dto.ReviewResponse;
import com.quickbite.review.exception.CustomException;
import com.quickbite.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS})
@Slf4j
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponse> addReview(@Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.addReview(request));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ReviewResponse> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(reviewService.getReviewByOrder(orderId));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<ReviewResponse>> getByRestaurant(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(reviewService.getReviewsByRestaurant(restaurantId));
    }

    @GetMapping("/delivery/{agentId}")
    public ResponseEntity<List<ReviewResponse>> getByDeliveryAgent(@PathVariable Long agentId) {
        return ResponseEntity.ok(reviewService.getReviewsByDeliveryAgent(agentId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<ReviewResponse>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(reviewService.getReviewsByCustomer(customerId));
    }

    @GetMapping("/admin/all")
    @Operation(summary = "Get all reviews for moderation (Admin only)")
    public ResponseEntity<List<ReviewResponse>> listAllReviews() {
        return ResponseEntity.ok(reviewService.listAllReviews());
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId,
                                             @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ADMIN".equals(role)) {
            throw new CustomException("Only admin can delete reviews");
        }
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}
