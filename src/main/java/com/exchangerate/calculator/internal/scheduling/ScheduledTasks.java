package com.exchangerate.calculator.internal.scheduling;

import com.exchangerate.calculator.internal.service.ExchangeRateService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private final ExchangeRateService exchangeRateService;


    public ScheduledTasks(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @Scheduled(cron = "0 5 0 * * *", zone = "GMT")
    public void fetchExchangeRates() {
        exchangeRateService.fetchExchangeRates();
    }
}