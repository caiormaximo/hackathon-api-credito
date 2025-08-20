package br.com.hackathon.apicredito.dto;

import java.util.List;

public record ResultadoSimulacaoDTO(
        String tipo,
        List<ParcelaDTO> parcelas
) {}