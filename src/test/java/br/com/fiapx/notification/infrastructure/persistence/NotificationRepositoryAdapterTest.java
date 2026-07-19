package br.com.fiapx.notification.infrastructure.persistence;

import br.com.fiapx.notification.domain.model.Notification;
import br.com.fiapx.notification.domain.model.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationRepositoryAdapterTest {

    @Mock
    private NotificationJpaRepository jpaRepository;

    private NotificationRepositoryAdapter adapter;
    private Notification sampleNotification;

    @BeforeEach
    void setUp() {
        adapter = new NotificationRepositoryAdapter(jpaRepository);
        sampleNotification = new Notification(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "user@test.com", "Subject", "Body", NotificationType.PROCESSING_COMPLETED, true,
                LocalDateTime.now());
    }

    @Test
    void save_shouldPersistAndReturnDomainObject() {
        NotificationEntity entity = NotificationEntity.fromDomain(sampleNotification);
        when(jpaRepository.save(any())).thenReturn(entity);

        Notification result = adapter.save(sampleNotification);

        assertThat(result.id()).isEqualTo(sampleNotification.id());
        verify(jpaRepository).save(any(NotificationEntity.class));
    }

    @Test
    void findByUserId_shouldReturnList() {
        NotificationEntity entity = NotificationEntity.fromDomain(sampleNotification);
        when(jpaRepository.findByUserId(sampleNotification.userId())).thenReturn(List.of(entity));

        List<Notification> result = adapter.findByUserId(sampleNotification.userId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(sampleNotification.id());
    }

    @Test
    void findByUserId_shouldReturnEmpty_whenNoNotifications() {
        when(jpaRepository.findByUserId(any())).thenReturn(List.of());

        List<Notification> result = adapter.findByUserId(UUID.randomUUID());

        assertThat(result).isEmpty();
    }
}
