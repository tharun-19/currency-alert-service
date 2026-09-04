package com.tharun.currencyalertservice.repository;

import com.tharun.currencyalertservice.domain.AlertRule;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRuleRepository extends JpaRepository<AlertRule, UUID> {
    List<AlertRule> findByStatus(String status);
    List<AlertRule> findByCurrencyPairAndStatus(String currencyPair, String status);
}