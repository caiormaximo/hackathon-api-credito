package br.com.hackathon.apicredito.dto;

import java.math.BigDecimal;

public record SimulacaoProdutoDTO(
        Integer codigoProduto,
        String descricaoProduto,
        BigDecimal valorTotalDesejado,
        long quantidadeSimulacoes
) {}