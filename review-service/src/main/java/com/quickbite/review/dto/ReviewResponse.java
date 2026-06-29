package com.quickbite.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReviewResponse {
    private Long reviewId;
    private Long orderId;
    private Long restaurantId;
    private Long deliveryAgentId;
    private Integer foodRating;
    private Integer deliveryRating;
    private String comment;
    private LocalDateTime reviewDate;
}
