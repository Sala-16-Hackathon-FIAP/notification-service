package br.com.fiapx.notification.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {

    @Test
    void create_shouldInitializeWithCorrectValues() {
        UUID userId = UUID.randomUUID();
        UUID uploadId = UUID.randomUUID();
        Notification n = Notification.create(userId, uploadId, "user@test.com",
                "Subject", "Body", NotificationType.PROCESSING_COMPLETED);

        assertThat(n.id()).isNotNull();
        assertThat(n.userId()).isEqualTo(userId);
        assertThat(n.uploadId()).isEqualTo(uploadId);
        assertThat(n.email()).isEqualTo("user@test.com");
        assertThat(n.subject()).isEqualTo("Subject");
        assertThat(n.message()).isEqualTo("Body");
        assertThat(n.type()).isEqualTo(NotificationType.PROCESSING_COMPLETED);
        assertThat(n.sent()).isFalse();
        assertThat(n.createdAt()).isNotNull();
    }

    @Test
    void markAsSent_shouldSetSentToTrue() {
        Notification n = Notification.create(UUID.randomUUID(), UUID.randomUUID(), "u@t.com",
                "s", "b", NotificationType.PROCESSING_FAILED);
        Notification sent = n.markAsSent();

        assertThat(sent.sent()).isTrue();
        assertThat(sent.id()).isEqualTo(n.id());
        assertThat(sent.email()).isEqualTo(n.email());
    }

    @Test
    void recordAccessors_shouldReturnCorrectValues() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID uploadId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Notification n = new Notification(id, userId, uploadId, "e@e.com", "subj", "msg",
                NotificationType.PROCESSING_COMPLETED, true, now);

        assertThat(n.id()).isEqualTo(id);
        assertThat(n.userId()).isEqualTo(userId);
        assertThat(n.uploadId()).isEqualTo(uploadId);
        assertThat(n.email()).isEqualTo("e@e.com");
        assertThat(n.subject()).isEqualTo("subj");
        assertThat(n.message()).isEqualTo("msg");
        assertThat(n.type()).isEqualTo(NotificationType.PROCESSING_COMPLETED);
        assertThat(n.sent()).isTrue();
        assertThat(n.createdAt()).isEqualTo(now);
    }
}
