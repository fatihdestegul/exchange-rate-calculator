package com.exchangerate.calculator.internal.repository;

import com.exchangerate.calculator.internal.domain.ExchangeRate;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends CrudRepository<ExchangeRate, Long> {

    void deleteByExchangeDate(LocalDate exchangeRate);

    Optional<ExchangeRate> findFirstByFromCurrencyAndToCurrencyAndExchangeDate(String fromCurrency, String toCurrency, LocalDate exchangeDate);

    Optional<ExchangeRate> findFirstByOrderByExchangeDateDesc();
}
