package com.tharun.currency_alert_service;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ProtectedEndpointTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RateController(), new AlertController()).build();
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
