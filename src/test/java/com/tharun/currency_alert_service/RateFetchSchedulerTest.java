package com.tharun.currency_alert_service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

class RateFetchSchedulerTest {

    @Test
    void publishesRateForEachActiveCurrencyPair() {
        AlertRuleRepository alertRuleRepository = mock(AlertRuleRepository.class);
        RateFetchService rateFetchService = mock(RateFetchService.class);
        KafkaTemplate<String, RateFetchedEvent> kafkaTemplate = mock(KafkaTemplate.class);
        UUID userId = UUID.randomUUID();
        when(alertRuleRepository.findByStatus("ACTIVE")).thenReturn(List.of(
                new AlertRule("USD/INR", userId, AlertRule.Direction.ABOVE, new BigDecimal("80.00"), "ACTIVE"),
                new AlertRule("USD/INR", userId, AlertRule.Direction.ABOVE, new BigDecimal("80.00"), "ACTIVE")
        ));
        when(rateFetchService.getRate("USD", "INR")).thenReturn(new BigDecimal("83.50"));
        when(kafkaTemplate.send(anyString(), anyString(), any(RateFetchedEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        RateFetchScheduler scheduler = new RateFetchScheduler(alertRuleRepository, rateFetchService, kafkaTemplate);

        scheduler.publishActiveRates();

        verify(kafkaTemplate).send(eq("rate-fetched"), eq("USD/INR"), any(RateFetchedEvent.class));
    }
}
