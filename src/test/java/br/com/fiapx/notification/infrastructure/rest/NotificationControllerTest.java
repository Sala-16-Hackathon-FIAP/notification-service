package br.com.fiapx.notification.infrastructure.rest;

import br.com.fiapx.notification.application.port.output.NotificationRepositoryPort;
import br.com.fiapx.notification.domain.model.Notification;
import br.com.fiapx.notification.domain.model.NotificationType;
import br.com.fiapx.notification.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@Import(SecurityConfig.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationRepositoryPort repository;

    @MockBean
    private br.com.fiapx.notification.infrastructure.security.JwtAuthFilter jwtAuthFilter;

    private UUID userId;

    @BeforeEach
    void setUp() throws Exception {
        userId = UUID.randomUUID();
        doAnswer(invocation -> {
            var chain = invocation.getArgument(2, jakarta.servlet.FilterChain.class);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    private UsernamePasswordAuthenticationToken userAuth() {
        return new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void getMyNotifications_shouldReturn200_withNotificationList() throws Exception {
        UUID uploadId = UUID.randomUUID();
        List<Notification> notifications = List.of(
                new Notification(UUID.randomUUID(), userId, uploadId, "user@example.com",
                        "subject", "body", NotificationType.PROCESSING_COMPLETED,
                        true, LocalDateTime.now())
        );
        when(repository.findByUserId(userId)).thenReturn(notifications);

        mockMvc.perform(get("/api/v1/notifications").with(authentication(userAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getMyNotifications_shouldReturn200_withEmptyList() throws Exception {
        when(repository.findByUserId(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/notifications").with(authentication(userAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getMyNotifications_shouldReturn401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isForbidden());
    }
}
