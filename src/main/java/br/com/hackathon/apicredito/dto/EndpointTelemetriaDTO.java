package br.com.hackathon.apicredito.dto;

//DTO para as metricas de telemetria

public record EndpointTelemetriaDTO(
        String nomeApi,
        Long qtdRequisicoes,
        Long tempoMedio, //miliseg
        Long tempoMinimo,
        Long tempoMaximo,
        Double percentualSucesso //200
) {}