package br.com.fiapx.notification.infrastructure.rest;

import br.com.fiapx.notification.application.port.output.NotificationRepositoryPort;
import br.com.fiapx.notification.domain.model.Notification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationRepositoryPort repository;

    public NotificationController(NotificationRepositoryPort repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(repository.findByUserId(userId));
    }
}
