package com.tharun.currency_alert_service;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRuleRepository extends JpaRepository<AlertRule, UUID> {
    List<AlertRule> findByStatus(String status);
    List<AlertRule> findByCurrencyPairAndStatus(String currencyPair, String status);
}
