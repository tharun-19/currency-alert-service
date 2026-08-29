package com.tharun.currency_alert_service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AlertEvaluatorConsumer {

    private static final Logger log = LoggerFactory.getLogger(AlertEvaluatorConsumer.class);
    private static final String ALERT_TRIGGERED_TOPIC = "alert-triggered";

    private final AlertRuleRepository alertRuleRepository;
    private final KafkaTemplate<String, AlertTriggeredEvent> kafkaTemplate;

    public AlertEvaluatorConsumer(AlertRuleRepository alertRuleRepository,
                                 KafkaTemplate<String, AlertTriggeredEvent> kafkaTemplate) {
        this.alertRuleRepository = alertRuleRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "rate-fetched", groupId = "alert-evaluator-group")
    @Transactional
    public void handleRateFetchedEvent(RateFetchedEvent event) {
        try {
            log.info("Received rate event from Kafka: {}", event);

            List<AlertRule> rules = alertRuleRepository.findByCurrencyPairAndStatus(event.currencyPair(), "ACTIVE");
            if (rules.isEmpty()) {
                log.info("No active alert rules for currency pair={}", event.currencyPair());
                return;
            }

            for (AlertRule rule : rules) {
                if (rule == null || rule.getId() == null) {
                    continue;
                }

                AlertRule freshRule = alertRuleRepository.findById(rule.getId()).orElse(null);
                if (freshRule == null || !"ACTIVE".equalsIgnoreCase(freshRule.getStatus())) {
                    log.info("Skipping stale or non-active rule {} because it is no longer ACTIVE", rule.getId());
                    continue;
                }

                boolean breached = switch (rule.getDirection()) {
                    case ABOVE -> event.rate().compareTo(rule.getThreshold()) > 0;
                    case BELOW -> event.rate().compareTo(rule.getThreshold()) < 0;
                };

                if (!breached) {
                    continue;
                }

                freshRule.setStatus("TRIGGERED");
                freshRule.setTriggeredAt(Instant.now());
                alertRuleRepository.save(freshRule);

                String timestamp = Instant.now().toString();
                AlertTriggeredEvent triggeredEvent = new AlertTriggeredEvent(
                        freshRule.getId(),
                        freshRule.getUserId(),
                        freshRule.getCurrencyPair(),
                        freshRule.getThreshold(),
                        event.rate(),
                        freshRule.getDirection().name(),
                        timestamp
                );

                kafkaTemplate.send(ALERT_TRIGGERED_TOPIC, freshRule.getId().toString(), triggeredEvent);
                log.info("Triggered alert for ruleId={} pair={} threshold={} actualRate={} direction={}",
                        freshRule.getId(), freshRule.getCurrencyPair(), freshRule.getThreshold(), event.rate(), freshRule.getDirection());
            }
        } catch (Exception e) {
            log.error("Failed to process rate-fetched event {}", event, e);
            throw e;
        }
    }
}
