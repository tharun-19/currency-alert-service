package com.tharun.currency_alert_service;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RateHistoryRepository extends JpaRepository<RateHistory, Long> {
}
