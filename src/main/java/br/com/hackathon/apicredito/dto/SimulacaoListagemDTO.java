package br.com.hackathon.apicredito.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SimulacaoListagemDTO(
        UUID idSimulacao,
        BigDecimal valorDesejado,
        int prazo,
        BigDecimal valorTotal
) {}