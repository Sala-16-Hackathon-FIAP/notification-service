package br.com.fiapx.notification.infrastructure.email;

import br.com.fiapx.notification.application.port.output.EmailPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SimulatedEmailAdapter implements EmailPort {

    private static final Logger log = LoggerFactory.getLogger(SimulatedEmailAdapter.class);

    @Override
    public void send(String to, String subject, String body) {
        log.info("=== [EMAIL SIMULATION] ===");
        log.info("To:      {}", to);
        log.info("Subject: {}", subject);
        log.info("Body:    {}", body);
        log.info("=========================");
    }
}
