package br.com.hackathon.apicredito.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SimulacaoRequestDTO(
        @NotNull(message = "O valor desejado é obrigatório.")
        @Min(value = 1, message = "O valor desejado deve ser maior que zero.")
        BigDecimal valorDesejado,

        @NotNull(message = "O prazo é obrigatório.")
        @Min(value = 1, message = "O prazo deve ser de no mínimo 1 mês.")
        int prazo
) {}