package com.exchangerate.calculator.internal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class FixerResponseDTO {

    private boolean success;
    private String base;
    private LocalDate date;
    private Map<String, BigDecimal> rates;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Map<String, BigDecimal> getRates() {
        return rates;
    }

    public void setRates(Map<String, BigDecimal> rates) {
        this.rates = rates;
    }
}
