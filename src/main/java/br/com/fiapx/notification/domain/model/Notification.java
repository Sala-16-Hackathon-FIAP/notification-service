package br.com.fiapx.notification.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record Notification(
    UUID id,
    UUID userId,
    UUID uploadId,
    String email,
    String subject,
    String message,
    NotificationType type,
    boolean sent,
    LocalDateTime createdAt
) {
    public static Notification create(UUID userId, UUID uploadId, String email,
                                       String subject, String message, NotificationType type) {
        return new Notification(UUID.randomUUID(), userId, uploadId, email, subject, message, type,
                false, LocalDateTime.now());
    }

    public Notification markAsSent() {
        return new Notification(id, userId, uploadId, email, subject, message, type, true, createdAt);
    }
}
