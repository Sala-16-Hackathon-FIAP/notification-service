package br.com.fiapx.notification.infrastructure.persistence;

import br.com.fiapx.notification.domain.model.Notification;
import br.com.fiapx.notification.domain.model.NotificationType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationEntityTest {

    @Test
    void fromDomainAndToDomain_shouldRoundTrip() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID uploadId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Notification original = new Notification(id, userId, uploadId, "user@test.com",
                "Subject", "Body text", NotificationType.PROCESSING_COMPLETED, true, now);

        NotificationEntity entity = NotificationEntity.fromDomain(original);
        Notification result = entity.toDomain();

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.uploadId()).isEqualTo(uploadId);
        assertThat(result.email()).isEqualTo("user@test.com");
        assertThat(result.subject()).isEqualTo("Subject");
        assertThat(result.message()).isEqualTo("Body text");
        assertThat(result.type()).isEqualTo(NotificationType.PROCESSING_COMPLETED);
        assertThat(result.sent()).isTrue();
        assertThat(result.createdAt()).isEqualTo(now);
    }
}
