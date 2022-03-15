package com.exchangerate.calculator.internal.service;


import com.exchangerate.calculator.internal.domain.ExchangeRate;
import com.exchangerate.calculator.internal.dto.FixerResponseDTO;
import com.exchangerate.calculator.internal.repository.ExchangeRateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExchangeRateService {

    private static final String FIXER_URI = "http://data.fixer.io/api/latest?access_key=2b217926d3e442385560b57adb25b338&format=1";

    private final ExchangeRateRepository exchangeRateRepository;

    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    @Transactional
    public Iterable<ExchangeRate> fetchExchangeRates() {
        FixerResponseDTO fixerResponseDTO = new RestTemplate().getForObject(FIXER_URI, FixerResponseDTO.class);
        List<ExchangeRate> exchangeRates = new ArrayList<>();
        fixerResponseDTO.getRates().entrySet().forEach(entry -> {
            exchangeRates.add(new ExchangeRate(entry.getValue(), fixerResponseDTO.getBase(), entry.getKey(), fixerResponseDTO.getDate()));
        });
        exchangeRateRepository.deleteByExchangeDate(fixerResponseDTO.getDate());
        return exchangeRateRepository.saveAll(exchangeRates);
    }
}