package com.tharun.currencyalertservice.repository;

import com.tharun.currencyalertservice.domain.RateHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RateHistoryRepository extends JpaRepository<RateHistory, Long> {
}