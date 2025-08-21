package br.com.hackathon.apicredito.dto;

import java.math.BigDecimal;

//DTO para os dados somados de simulacao de um unico produto em um dia especifico.

public record VolumeProdutoDTO(
        Integer codigoProduto,
        String descricaoProduto,
        BigDecimal taxaMediaJuro,
        BigDecimal valorMedioPrestacao,
        BigDecimal valorTotalDesejado,
        BigDecimal valorTotalCredito
) {}