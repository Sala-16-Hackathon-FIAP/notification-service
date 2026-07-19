package br.com.fiapx.notification.application.port.output;

import br.com.fiapx.notification.domain.model.Notification;

import java.util.List;
import java.util.UUID;

public interface NotificationRepositoryPort {
    Notification save(Notification notification);
    List<Notification> findByUserId(UUID userId);
}
