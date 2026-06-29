package com.quickbite.notification.repository;

import com.quickbite.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientIdOrderBySentAtDesc(Long recipientId);
    List<Notification> findByRecipientIdAndIsReadFalseOrderBySentAtDesc(Long recipientId);
    long countByRecipientIdAndIsReadFalse(Long recipientId);
    boolean existsByRecipientIdAndRelatedIdAndType(Long recipientId, Long relatedId, Notification.NotificationType type);
    
    @Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipientId = :recipientId")
    void markAllAsRead(@Param("recipientId") Long recipientId);
}