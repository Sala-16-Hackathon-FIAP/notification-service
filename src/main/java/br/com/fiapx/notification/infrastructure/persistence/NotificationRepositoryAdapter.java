package br.com.fiapx.notification.infrastructure.persistence;

import br.com.fiapx.notification.application.port.output.NotificationRepositoryPort;
import br.com.fiapx.notification.domain.model.Notification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class NotificationRepositoryAdapter implements NotificationRepositoryPort {

    private final NotificationJpaRepository jpa;

    public NotificationRepositoryAdapter(NotificationJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Notification save(Notification notification) {
        return jpa.save(NotificationEntity.fromDomain(notification)).toDomain();
    }

    @Override
    public List<Notification> findByUserId(UUID userId) {
        return jpa.findByUserId(userId).stream()
                .map(NotificationEntity::toDomain)
                .toList();
    }
}
