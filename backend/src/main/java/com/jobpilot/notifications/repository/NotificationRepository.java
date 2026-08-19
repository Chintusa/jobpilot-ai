package com.jobpilot.notifications.repository;

import com.jobpilot.notifications.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(UUID userId);
    List<Notification> findByUserIdAndCategoryOrderByCreatedAtDesc(UUID userId, String category);
    long countByUserIdAndReadFalse(UUID userId);

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.dedupKey = :dedupKey AND n.createdAt > :since")
    List<Notification> findRecentByDedupKey(@Param("userId") UUID userId, @Param("dedupKey") String dedupKey, @Param("since") Instant since);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = :now WHERE n.user.id = :userId AND n.read = false")
    int markAllAsRead(@Param("userId") UUID userId, @Param("now") Instant now);
}
