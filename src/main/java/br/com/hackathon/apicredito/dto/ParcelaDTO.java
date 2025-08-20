package br.com.hackathon.apicredito.dto;

import java.math.BigDecimal;

public record ParcelaDTO(
        int numero,
        BigDecimal valorAmortizacao,
        BigDecimal valorJuros,
        BigDecimal valorPrestacao
) {}