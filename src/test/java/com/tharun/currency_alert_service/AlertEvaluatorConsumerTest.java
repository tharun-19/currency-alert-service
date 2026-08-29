package com.tharun.currency_alert_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

class AlertEvaluatorConsumerTest {

    @Test
    void publishesTriggeredAlertWhenRateBreachesThreshold() {
        AlertRuleRepository repository = mock(AlertRuleRepository.class);
        KafkaTemplate<String, AlertTriggeredEvent> kafkaTemplate = mock(KafkaTemplate.class);

        UUID userId = UUID.randomUUID();
        UUID ruleId = UUID.randomUUID();
        AlertRule rule = new AlertRule(ruleId, "USD/INR", userId, AlertRule.Direction.ABOVE, new BigDecimal("90.00"), "ACTIVE");

        when(repository.findByCurrencyPairAndStatus("USD/INR", "ACTIVE")).thenReturn(List.of(rule));
        when(repository.findById(ruleId)).thenReturn(java.util.Optional.of(rule));

        AlertEvaluatorConsumer consumer = new AlertEvaluatorConsumer(repository, kafkaTemplate);
        consumer.handleRateFetchedEvent(new RateFetchedEvent("USD/INR", new BigDecimal("95.50"), Instant.now().toString()));

        assertEquals("TRIGGERED", rule.getStatus());
        verify(kafkaTemplate).send(eq("alert-triggered"), eq(ruleId.toString()), any(AlertTriggeredEvent.class));
    }
}
