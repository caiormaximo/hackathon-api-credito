package br.com.hackathon.apicredito.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SimulacaoItemDTO(
        UUID idSimulacao, //usando UUID como no nosso modelo para persistir (no desafio cita um long)
        BigDecimal valorDesejado,
        Integer prazo,
        BigDecimal valorTotalParcelas
) {}