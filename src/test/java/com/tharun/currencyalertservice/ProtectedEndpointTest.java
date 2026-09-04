package com.tharun.currencyalertservice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tharun.currencyalertservice.controller.AlertController;
import com.tharun.currencyalertservice.controller.RateController;
import com.tharun.currencyalertservice.properties.ExchangeRateProperties;
import com.tharun.currencyalertservice.repository.AlertRuleRepository;
import com.tharun.currencyalertservice.service.RateFetchService;
import java.math.BigDecimal;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ProtectedEndpointTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        RateFetchService rateFetchService = new RateFetchService(null, new ExchangeRateProperties(), null) {
            @Override
            public BigDecimal getRate(String baseCurrency, String targetCurrency) {
                return new BigDecimal("83.50");
            }

            @Override
            public RateFetchService.RateResult getRateDetails(String baseCurrency, String targetCurrency) {
                return new RateFetchService.RateResult(new BigDecimal("83.50"), "mock");
            }

            @Override
            public RateFetchService.RateResult getRateDetails(String baseCurrency, String targetCurrency, boolean fresh) {
                return new RateFetchService.RateResult(new BigDecimal("83.50"), "mock");
            }
        };

        AlertRuleRepository alertRuleRepository = Mockito.mock(AlertRuleRepository.class);
        Mockito.when(alertRuleRepository.findByStatus("ACTIVE")).thenReturn(Collections.emptyList());

        mockMvc = MockMvcBuilders.standaloneSetup(new RateController(rateFetchService), new AlertController(alertRuleRepository)).build();
    }

    @Test
    void ratesEndpointIsMapped() throws Exception {
        mockMvc.perform(get("/api/rates"))
                .andExpect(status().isOk());
    }

    @Test
    void alertsEndpointIsMapped() throws Exception {
        mockMvc.perform(get("/api/alerts"))
                .andExpect(status().isOk());
    }
}
