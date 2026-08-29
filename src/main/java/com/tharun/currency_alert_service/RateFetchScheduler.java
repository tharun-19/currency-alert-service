package com.tharun.currency_alert_service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RateFetchScheduler {

    private static final Logger log = LoggerFactory.getLogger(RateFetchScheduler.class);
    private static final String TOPIC = "rate-fetched";

    private final AlertRuleRepository alertRuleRepository;
    private final RateFetchService rateFetchService;
    private final KafkaTemplate<String, RateFetchedEvent> kafkaTemplate;

    public RateFetchScheduler(AlertRuleRepository alertRuleRepository,
                             RateFetchService rateFetchService,
                             KafkaTemplate<String, RateFetchedEvent> kafkaTemplate) {
        this.alertRuleRepository = alertRuleRepository;
        this.rateFetchService = rateFetchService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedRate = 60000)
    public void publishActiveRates() {
        if (alertRuleRepository == null) {
            log.warn("AlertRuleRepository is not configured; skipping scheduled publication");
            return;
        }

        Set<String> currencyPairs = new LinkedHashSet<>();
        for (AlertRule rule : alertRuleRepository.findByStatus("ACTIVE")) {
            String pair = rule.getCurrencyPair().toUpperCase();
            currencyPairs.add(pair);
        }

        if (currencyPairs.isEmpty()) {
            log.info("No active alert rules found; skipping rate publication");
            return;
        }

        for (String currencyPair : currencyPairs) {
            String[] split = currencyPair.split("/");
            String baseCurrency = split[0];
            String targetCurrency = split[1];
            BigDecimal rate = rateFetchService.getRate(baseCurrency, targetCurrency);

            String timestamp = Instant.now().toString();
            RateFetchedEvent event = new RateFetchedEvent(currencyPair, rate, timestamp);
            kafkaTemplate.send(TOPIC, currencyPair, event);
            log.info("Published rate event to topic={} currencyPair={} rate={} at={}", TOPIC, currencyPair, rate, timestamp);
        }
    }
}
