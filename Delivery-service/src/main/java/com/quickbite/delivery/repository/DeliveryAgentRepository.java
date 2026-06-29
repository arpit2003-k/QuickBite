package com.quickbite.delivery.repository;

import com.quickbite.delivery.entity.DeliveryAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DeliveryAgentRepository extends JpaRepository<DeliveryAgent, Long> {
    Optional<DeliveryAgent> findByUserId(Long userId);
    List<DeliveryAgent> findByIsAvailableTrue();

    @Query("SELECT a FROM DeliveryAgent a WHERE a.isAvailable = true " +
           "AND (6371 * acos(cos(radians(:lat)) * cos(radians(a.currentLatitude)) * " +
           "cos(radians(a.currentLongitude) - radians(:lng)) + sin(radians(:lat)) * " +
           "sin(radians(a.currentLatitude)))) <= :radius " +
           "ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(a.currentLatitude)) * " +
           "cos(radians(a.currentLongitude) - radians(:lng)) + sin(radians(:lat)) * " +
           "sin(radians(a.currentLatitude)))) ASC, a.totalDeliveries ASC, a.agentId ASC")
    List<DeliveryAgent> findNearbyAvailableAgents(@Param("lat") double lat,
                                                  @Param("lng") double lng,
                                                  @Param("radius") double radius);
}
