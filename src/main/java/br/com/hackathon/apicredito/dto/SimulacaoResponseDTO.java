package br.com.hackathon.apicredito.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SimulacaoResponseDTO(
        UUID idSimulacao,
        Integer codigoProduto,
        String descricaoProduto,
        BigDecimal taxaJuros,
        List<ResultadoSimulacaoDTO> resultadoSimulacao
) {}