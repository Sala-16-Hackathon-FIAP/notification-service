package br.com.fiapx.notification.infrastructure.messaging;

import br.com.fiapx.notification.application.port.input.NotificationUseCase;
import br.com.fiapx.notification.domain.model.NotificationType;
import com.autoflow.rabbit_topic_lib.core.TopicConsumer;
import com.autoflow.rabbit_topic_lib.model.TopicBinding;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    static final String EXCHANGE = "fiapx.events";
    static final String QUEUE_COMPLETED = "notification.video.processing.completed";
    static final String QUEUE_FAILED = "notification.video.processing.failed";

    private final TopicConsumer consumer;
    private final NotificationUseCase notificationUseCase;

    public NotificationEventConsumer(TopicConsumer consumer, NotificationUseCase notificationUseCase) {
        this.consumer = consumer;
        this.notificationUseCase = notificationUseCase;
    }

    @PostConstruct
    public void registerConsumers() {
        consumer.consume(new TopicBinding(EXCHANGE, QUEUE_COMPLETED, "video.processing.completed"),
                ProcessingJobEvent.class, this::handleProcessingCompleted);
        consumer.consume(new TopicBinding(EXCHANGE, QUEUE_FAILED, "video.processing.failed"),
                ProcessingJobEvent.class, this::handleProcessingFailed);
    }

    public void handleProcessingCompleted(ProcessingJobEvent event) {
        log.info("Notification: processing completed for uploadId={}, user={}", event.uploadId(), event.userId());
        notificationUseCase.sendNotification(event.userId(), event.uploadId(), resolveEmail(event),
                event.filename(), NotificationType.PROCESSING_COMPLETED, event.resultS3Key(), null);
    }

    public void handleProcessingFailed(ProcessingJobEvent event) {
        log.warn("Notification: processing failed for uploadId={}: {}", event.uploadId(), event.errorMessage());
        notificationUseCase.sendNotification(event.userId(), event.uploadId(), resolveEmail(event),
                event.filename(), NotificationType.PROCESSING_FAILED, null, event.errorMessage());
    }

    private String resolveEmail(ProcessingJobEvent event) {
        // userEmail is populated when the upload event propagates it; fall back to synthetic address for simulation
        return (event.userEmail() != null && !event.userEmail().isBlank())
                ? event.userEmail()
                : "user-" + event.userId() + "@fiapx.local";
    }

    public record ProcessingJobEvent(
            UUID jobId,
            UUID uploadId,
            UUID userId,
            String userEmail,
            String filename,
            String resultS3Key,
            String status,
            String errorMessage,
            LocalDateTime timestamp
    ) {}
}
