package com.exchangerate.calculator.internal.repository;

import com.exchangerate.calculator.internal.domain.RequestCount;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface RequestCountRepository extends CrudRepository<RequestCount, Long> {

    Optional<RequestCount> findFirstByCurrencyAndRequestDate(String currency, LocalDate requestDate);
}
