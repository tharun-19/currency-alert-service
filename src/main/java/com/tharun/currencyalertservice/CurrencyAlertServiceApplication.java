package com.tharun.currencyalertservice;

import com.tharun.currencyalertservice.properties.ExchangeRateProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(ExchangeRateProperties.class)
@EnableScheduling
public class CurrencyAlertServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CurrencyAlertServiceApplication.class, args);
    }
}