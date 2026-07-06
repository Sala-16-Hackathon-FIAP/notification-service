package br.com.fiapx.notification.application.service;

import br.com.fiapx.notification.application.port.output.EmailPort;
import br.com.fiapx.notification.application.port.output.NotificationRepositoryPort;
import br.com.fiapx.notification.domain.model.Notification;
import br.com.fiapx.notification.domain.model.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepositoryPort repository;
    @Mock private EmailPort emailPort;

    @InjectMocks
    private NotificationService notificationService;

    private UUID userId;
    private UUID uploadId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        uploadId = UUID.randomUUID();
    }

    @Test
    void sendNotification_shouldSendEmailAndSaveNotification_whenProcessingCompleted() {
        Notification saved = new Notification(UUID.randomUUID(), userId, uploadId,
                "user@example.com", "subject", "body", NotificationType.PROCESSING_COMPLETED,
                true, LocalDateTime.now());
        when(repository.save(any())).thenReturn(saved);

        Notification result = notificationService.sendNotification(userId, uploadId,
                "user@example.com", "video.mp4", NotificationType.PROCESSING_COMPLETED,
                "processed/video.zip", null);

        verify(emailPort).send(eq("user@example.com"), contains("video.mp4"), anyString());
        verify(repository).save(any());
        assertThat(result).isNotNull();
    }

    @Test
    void sendNotification_shouldSendEmailAndSaveNotification_whenProcessingFailed() {
        Notification saved = new Notification(UUID.randomUUID(), userId, uploadId,
                "user@example.com", "subject", "body", NotificationType.PROCESSING_FAILED,
                true, LocalDateTime.now());
        when(repository.save(any())).thenReturn(saved);

        Notification result = notificationService.sendNotification(userId, uploadId,
                "user@example.com", "video.mp4", NotificationType.PROCESSING_FAILED,
                null, "FFmpeg error");

        verify(emailPort).send(eq("user@example.com"), contains("failed"), anyString());
        verify(repository).save(any());
        assertThat(result).isNotNull();
    }

    @Test
    void sendNotification_completedSubjectContainsFilename() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        notificationService.sendNotification(userId, uploadId, "user@example.com",
                "myvideo.mp4", NotificationType.PROCESSING_COMPLETED, "key.zip", null);

        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailPort).send(anyString(), subjectCaptor.capture(), anyString());
        assertThat(subjectCaptor.getValue()).contains("myvideo.mp4");
    }

    @Test
    void sendNotification_failedSubjectIndicatesFailure() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        notificationService.sendNotification(userId, uploadId, "user@example.com",
                "myvideo.mp4", NotificationType.PROCESSING_FAILED, null, "some error");

        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailPort).send(anyString(), subjectCaptor.capture(), anyString());
        assertThat(subjectCaptor.getValue()).containsIgnoringCase("fail");
    }

    @Test
    void sendNotification_bodyContainsResultKey_whenCompleted() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        notificationService.sendNotification(userId, uploadId, "user@example.com",
                "video.mp4", NotificationType.PROCESSING_COMPLETED, "results/video.zip", null);

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailPort).send(anyString(), anyString(), bodyCaptor.capture());
        assertThat(bodyCaptor.getValue()).contains("results/video.zip");
    }

    @Test
    void sendNotification_bodyContainsErrorMessage_whenFailed() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        notificationService.sendNotification(userId, uploadId, "user@example.com",
                "video.mp4", NotificationType.PROCESSING_FAILED, null, "Disk full");

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailPort).send(anyString(), anyString(), bodyCaptor.capture());
        assertThat(bodyCaptor.getValue()).contains("Disk full");
    }

    @Test
    void sendNotification_bodyHandlesNullError_gracefully() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        notificationService.sendNotification(userId, uploadId, "user@example.com",
                "video.mp4", NotificationType.PROCESSING_FAILED, null, null);

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailPort).send(anyString(), anyString(), bodyCaptor.capture());
        assertThat(bodyCaptor.getValue()).contains("Unknown error");
    }

    @Test
    void sendNotification_notificationIsMarkedAsSent() {
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        when(repository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        notificationService.sendNotification(userId, uploadId, "user@example.com",
                "video.mp4", NotificationType.PROCESSING_COMPLETED, "key.zip", null);

        assertThat(captor.getValue().sent()).isTrue();
    }
}
