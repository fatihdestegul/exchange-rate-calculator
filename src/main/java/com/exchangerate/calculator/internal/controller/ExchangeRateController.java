package com.exchangerate.calculator.internal.controller;

import com.exchangerate.calculator.internal.domain.ExchangeRate;
import com.exchangerate.calculator.internal.domain.RequestCount;
import com.exchangerate.calculator.internal.exception.ExchangeRateNotFoundException;
import com.exchangerate.calculator.internal.repository.ExchangeRateRepository;
import com.exchangerate.calculator.internal.repository.RequestCountRepository;
import com.exchangerate.calculator.internal.service.ExchangeRateService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/exchange")
public class ExchangeRateController {

    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateService exchangeRateService;
    private final RequestCountRepository requestCountRepository;

    private static final String BASE_CURRENCY = "EUR";
    private static final BigDecimal SPREAD_PERCENTAGE_OTHER = new BigDecimal(2.75);
    private static final Map<String, BigDecimal> SPREAD_PERCENTAGE_MAP = Map.of(
            "JPY", new BigDecimal(3.25),
            "HKD", new BigDecimal(3.25),
            "KRW", new BigDecimal(3.25),
            "MYR", new BigDecimal(4.5),
            "INR", new BigDecimal(4.5),
            "MXN", new BigDecimal(4.5),
            "RUB", new BigDecimal(6),
            "CNY", new BigDecimal(6),
            "ZAR", new BigDecimal(6));

    public ExchangeRateController(ExchangeRateRepository exchangeRateRepository,
            ExchangeRateService exchangeRateService, RequestCountRepository requestCountRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.exchangeRateService = exchangeRateService;
        this.requestCountRepository = requestCountRepository;
    }

    @GetMapping
    @Operation(summary = "Get Exchange Rate", description = "Gets desired exchange rate from-to")
    public ResponseEntity<ExchangeRate> getExchangeRate(@RequestParam String from, @RequestParam String to,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            if (BASE_CURRENCY.equals(from)) {
                return new ResponseEntity<>(writeRequestCount(findExchangeRate(from, to, date)), HttpStatus.OK);
            } else if (BASE_CURRENCY.equals(to)) {
                ExchangeRate exchangeRate = findExchangeRate(to, from, date);
                String temp = exchangeRate.getFromCurrency();
                exchangeRate.setFromCurrency(exchangeRate.getToCurrency());
                exchangeRate.setToCurrency(temp);
                exchangeRate.setExchangeRate(new BigDecimal(1).divide(exchangeRate.getExchangeRate()));
                return new ResponseEntity<>(writeRequestCount(exchangeRate), HttpStatus.OK);
            } else {
                ExchangeRate fromExchangeRate = findExchangeRate(BASE_CURRENCY, from, date);
                ExchangeRate toExchangeRate = findExchangeRate(BASE_CURRENCY, to, date);
                BigDecimal fromSpreadPercentage = SPREAD_PERCENTAGE_MAP.getOrDefault(fromExchangeRate.getToCurrency(),
                        SPREAD_PERCENTAGE_OTHER);
                BigDecimal toSpreadPercentage = SPREAD_PERCENTAGE_MAP.getOrDefault(toExchangeRate.getToCurrency(),
                        SPREAD_PERCENTAGE_OTHER);
                BigDecimal maxSpreadPercentage = fromSpreadPercentage.max(toSpreadPercentage);

                ExchangeRate exchangeRate = new ExchangeRate();
                exchangeRate.setFromCurrency(fromExchangeRate.getToCurrency());
                exchangeRate.setToCurrency(toExchangeRate.getToCurrency());
                exchangeRate.setExchangeRate(toExchangeRate.getExchangeRate()
                        .divide(fromExchangeRate.getExchangeRate(), 5, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100.0).subtract(maxSpreadPercentage).divide(new BigDecimal(100))));
                return new ResponseEntity<>(writeRequestCount(exchangeRate), HttpStatus.OK);
            }

        } catch (ExchangeRateNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping
    @Operation(summary = "Fetch rates from fixer.io", description = "Gets exchange rates of base currency when called and persist in database")
    public ResponseEntity<Iterable<ExchangeRate>> fetchExchangeRates() {
        return new ResponseEntity(exchangeRateService.fetchExchangeRates(), HttpStatus.OK);
    }

    private ExchangeRate findExchangeRate(String from, String to, LocalDate date) {
        if (date != null) {
            Optional<ExchangeRate> exchangeRate = exchangeRateRepository
                    .findFirstByFromCurrencyAndToCurrencyAndExchangeDate(from, to, date);
            if (exchangeRate.isEmpty()) {
                throw new ExchangeRateNotFoundException();
            }
            return exchangeRate.get();
        }

        Optional<ExchangeRate> lastExchangeRate = exchangeRateRepository.findFirstByOrderByExchangeDateDesc();
        if (lastExchangeRate.isEmpty()) {
            throw new ExchangeRateNotFoundException();
        }

        Optional<ExchangeRate> exchangeRate = exchangeRateRepository
                .findFirstByFromCurrencyAndToCurrencyAndExchangeDate(from, to,
                        lastExchangeRate.get().getExchangeDate());
        if (exchangeRate.isEmpty()) {
            throw new ExchangeRateNotFoundException();
        }
        return exchangeRate.get();
    }

    private ExchangeRate writeRequestCount(ExchangeRate exchangeRate) {

        Optional<RequestCount> fromExchangeRate = requestCountRepository.findFirstByCurrencyAndRequestDate(exchangeRate.getFromCurrency(), exchangeRate.getExchangeDate());
        if (fromExchangeRate.isEmpty()) {
          requestCountRepository.save(new RequestCount(exchangeRate.getFromCurrency(),1L,exchangeRate.getExchangeDate()));
        } else {
          fromExchangeRate.get().setRequestCount(fromExchangeRate.get().getRequestCount() + 1);
          requestCountRepository.save(fromExchangeRate.get());
        }

        Optional<RequestCount> toExchangeRate = requestCountRepository.findFirstByCurrencyAndRequestDate(exchangeRate.getToCurrency(), exchangeRate.getExchangeDate());
        if (toExchangeRate.isEmpty()) {
          requestCountRepository.save(new RequestCount(exchangeRate.getToCurrency(),1L,exchangeRate.getExchangeDate()));
        } else {
          toExchangeRate.get().setRequestCount(toExchangeRate.get().getRequestCount() + 1);
          requestCountRepository.save(toExchangeRate.get());
        }
            

        return exchangeRate;
    }


}