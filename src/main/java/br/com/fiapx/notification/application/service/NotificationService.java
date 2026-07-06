package br.com.fiapx.notification.application.service;

import br.com.fiapx.notification.application.port.input.NotificationUseCase;
import br.com.fiapx.notification.application.port.output.EmailPort;
import br.com.fiapx.notification.application.port.output.NotificationRepositoryPort;
import br.com.fiapx.notification.domain.model.Notification;
import br.com.fiapx.notification.domain.model.NotificationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class NotificationService implements NotificationUseCase {

    private final NotificationRepositoryPort repository;
    private final EmailPort emailPort;

    public NotificationService(NotificationRepositoryPort repository, EmailPort emailPort) {
        this.repository = repository;
        this.emailPort = emailPort;
    }

    @Override
    @Transactional
    public Notification sendNotification(UUID userId, UUID uploadId, String email,
                                          String filename, NotificationType type, String resultKey, String error) {
        String subject = buildSubject(type, filename);
        String body = buildBody(type, filename, resultKey, error);

        Notification notification = Notification.create(userId, uploadId, email, subject, body, type);

        emailPort.send(email, subject, body);
        notification = notification.markAsSent();

        return repository.save(notification);
    }

    private String buildSubject(NotificationType type, String filename) {
        return switch (type) {
            case PROCESSING_COMPLETED -> "Your video '" + filename + "' has been processed successfully!";
            case PROCESSING_FAILED -> "Video processing failed for '" + filename + "'";
        };
    }

    private String buildBody(NotificationType type, String filename, String resultKey, String error) {
        return switch (type) {
            case PROCESSING_COMPLETED ->
                    "Good news! Your video '" + filename + "' has been processed.\n" +
                    "The extracted frames are available at: " + resultKey + "\n\n" +
                    "FIAP-X Video Processing Platform";
            case PROCESSING_FAILED ->
                    "Unfortunately, processing of your video '" + filename + "' failed.\n" +
                    "Reason: " + (error != null ? error : "Unknown error") + "\n\n" +
                    "Please try uploading the video again. FIAP-X Video Processing Platform";
        };
    }
}
