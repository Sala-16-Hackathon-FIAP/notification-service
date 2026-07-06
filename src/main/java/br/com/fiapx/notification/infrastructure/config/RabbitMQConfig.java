package br.com.fiapx.notification.infrastructure.config;

import com.autoflow.rabbit_topic_lib.core.TopicExchangeManager;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public RabbitMQConfig(TopicExchangeManager exchangeManager) {
        exchangeManager.declareExchange("fiapx.events");
    }
}
