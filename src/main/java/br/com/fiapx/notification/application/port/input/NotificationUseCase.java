package br.com.fiapx.notification.application.port.input;

import br.com.fiapx.notification.domain.model.Notification;
import br.com.fiapx.notification.domain.model.NotificationType;

import java.util.UUID;

public interface NotificationUseCase {
    Notification sendNotification(UUID userId, UUID uploadId, String email,
                                   String filename, NotificationType type, String resultKey, String error);
}
