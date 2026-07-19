package br.com.fiapx.notification.infrastructure.messaging;

import br.com.fiapx.notification.application.port.input.NotificationUseCase;
import br.com.fiapx.notification.domain.model.Notification;
import br.com.fiapx.notification.domain.model.NotificationType;
import com.autoflow.rabbit_topic_lib.core.TopicConsumer;
import com.autoflow.rabbit_topic_lib.model.TopicBinding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventConsumerTest {

    @Mock private TopicConsumer topicConsumer;
    @Mock private NotificationUseCase notificationUseCase;

    private NotificationEventConsumer consumer;

    private UUID userId;
    private UUID uploadId;
    private UUID jobId;

    @BeforeEach
    void setUp() {
        consumer = new NotificationEventConsumer(topicConsumer, notificationUseCase);
        userId = UUID.randomUUID();
        uploadId = UUID.randomUUID();
        jobId = UUID.randomUUID();
    }

    @Test
    void registerConsumers_shouldRegisterTwoConsumers() {
        consumer.registerConsumers();
        verify(topicConsumer, times(2)).consume(any(TopicBinding.class), any(), any());
    }

    @Test
    void handleProcessingCompleted_shouldCallNotificationUseCase_withCompletedType() {
        Notification notification = new Notification(UUID.randomUUID(), userId, uploadId,
                "user@example.com", "sub", "body", NotificationType.PROCESSING_COMPLETED,
                true, LocalDateTime.now());
        when(notificationUseCase.sendNotification(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(notification);

        NotificationEventConsumer.ProcessingJobEvent event = new NotificationEventConsumer.ProcessingJobEvent(
                jobId, uploadId, userId, "user@example.com", "video.mp4",
                "results/video.zip", "COMPLETED", null, LocalDateTime.now());

        consumer.handleProcessingCompleted(event);

        verify(notificationUseCase).sendNotification(userId, uploadId, "user@example.com",
                "video.mp4", NotificationType.PROCESSING_COMPLETED, "results/video.zip", null);
    }

    @Test
    void handleProcessingFailed_shouldCallNotificationUseCase_withFailedType() {
        Notification notification = new Notification(UUID.randomUUID(), userId, uploadId,
                "user@example.com", "sub", "body", NotificationType.PROCESSING_FAILED,
                true, LocalDateTime.now());
        when(notificationUseCase.sendNotification(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(notification);

        NotificationEventConsumer.ProcessingJobEvent event = new NotificationEventConsumer.ProcessingJobEvent(
                jobId, uploadId, userId, "user@example.com", "video.mp4",
                null, "FAILED", "FFmpeg error", LocalDateTime.now());

        consumer.handleProcessingFailed(event);

        verify(notificationUseCase).sendNotification(userId, uploadId, "user@example.com",
                "video.mp4", NotificationType.PROCESSING_FAILED, null, "FFmpeg error");
    }

    @Test
    void handleProcessingCompleted_shouldUseSyntheticEmail_whenUserEmailIsNull() {
        NotificationEventConsumer.ProcessingJobEvent event = new NotificationEventConsumer.ProcessingJobEvent(
                jobId, uploadId, userId, null, "video.mp4",
                "results/video.zip", "COMPLETED", null, LocalDateTime.now());

        consumer.handleProcessingCompleted(event);

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationUseCase).sendNotification(eq(userId), eq(uploadId),
                emailCaptor.capture(), anyString(), eq(NotificationType.PROCESSING_COMPLETED), any(), any());
        assertThat(emailCaptor.getValue()).contains(userId.toString());
    }

    @Test
    void handleProcessingFailed_shouldUseSyntheticEmail_whenUserEmailIsBlank() {
        NotificationEventConsumer.ProcessingJobEvent event = new NotificationEventConsumer.ProcessingJobEvent(
                jobId, uploadId, userId, "  ", "video.mp4",
                null, "FAILED", "error", LocalDateTime.now());

        consumer.handleProcessingFailed(event);

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationUseCase).sendNotification(eq(userId), eq(uploadId),
                emailCaptor.capture(), anyString(), eq(NotificationType.PROCESSING_FAILED), any(), any());
        assertThat(emailCaptor.getValue()).contains(userId.toString());
    }
}
