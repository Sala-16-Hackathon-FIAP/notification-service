package br.com.fiapx.notification.infrastructure.persistence;

import br.com.fiapx.notification.domain.model.Notification;
import br.com.fiapx.notification.domain.model.NotificationType;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class NotificationEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "upload_id", nullable = false)
    private UUID uploadId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    private boolean sent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected NotificationEntity() {}

    public static NotificationEntity fromDomain(Notification n) {
        NotificationEntity e = new NotificationEntity();
        e.id = n.id();
        e.userId = n.userId();
        e.uploadId = n.uploadId();
        e.email = n.email();
        e.subject = n.subject();
        e.message = n.message();
        e.type = n.type();
        e.sent = n.sent();
        e.createdAt = n.createdAt();
        return e;
    }

    public Notification toDomain() {
        return new Notification(id, userId, uploadId, email, subject, message, type, sent, createdAt);
    }
}
