package com.quickbite.review.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", uniqueConstraints = {
    @UniqueConstraint(columnNames = "orderId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @Column(nullable = false, unique = true)
    private Long orderId;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Long restaurantId;

    private Long deliveryAgentId;   // can be null if not assigned (should not happen)

    private Integer foodRating;      // 1-5
    private Integer deliveryRating;  // 1-5

    private String comment;

    private LocalDateTime reviewDate;

    private Boolean isVerified = false;   // could be used to filter fake reviews
}
