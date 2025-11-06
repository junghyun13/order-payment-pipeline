package com.example.orderpay.common.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Kafka 또는 Redpanda로 이벤트 발행 (자동 토픽명)
     */
    public void publish(Object event) {
        try {
            String topic = event.getClass().getSimpleName(); // ex) OrderCreatedEvent
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, payload);
            log.info("📤 Published event to {}: {}", topic, payload);
            System.out.println("✅ Event published to " + topic + ": " + event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish event", e);
        }
    }

    /**
     * Kafka 또는 Redpanda로 이벤트 발행 (토픽명 지정)
     */
    public void publish(String topic, Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, payload);
            log.info("📤 Published event to {}: {}", topic, payload);
            System.out.println("✅ Event published to " + topic + ": " + event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish event", e);
        }
    }
}
