package com.exchangerate.calculator.internal;

import com.exchangerate.calculator.internal.domain.ExchangeRate;
import com.exchangerate.calculator.internal.repository.ExchangeRateRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
public class ExchangeRateRepositoryTest {

   @Autowired
   private TestEntityManager entityManager;

   @Autowired
   private ExchangeRateRepository exchangeRateRepository;

   @Test
   public void whenFindSingleRate() {

       LocalDate date = LocalDate.parse("2022-03-15");
       
       ExchangeRate exchangeRate = new ExchangeRate();
       exchangeRate.setExchangeDate(date);
       exchangeRate.setExchangeRate(new BigDecimal(16.226232));
       exchangeRate.setFromCurrency("EUR");
       exchangeRate.setToCurrency("TRY");

       entityManager.persist(exchangeRate);
       entityManager.flush();


       //when
       Optional<ExchangeRate> exchangeRates = exchangeRateRepository.findFirstByFromCurrencyAndToCurrencyAndExchangeDate("EUR", "TRY", date);

       //then
       assertThat(exchangeRates.get().getExchangeDate().isEqual(date));
       assertThat(exchangeRates.get().getExchangeRate().compareTo(new BigDecimal(16.226232)) == 0);
       assertThat(exchangeRates.get().getFromCurrency().equals("EUR"));
       assertThat(exchangeRates.get().getToCurrency().equals("TRY"));
   }

}