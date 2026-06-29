package com.quickbite.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotNull private Long orderId;
    @NotNull private Long customerId;
    @NotNull private Long restaurantId;
    private Long deliveryAgentId;
    @Min(1) @Max(5) private Integer foodRating;
    @Min(1) @Max(5) private Integer deliveryRating;
    private String comment;
}
