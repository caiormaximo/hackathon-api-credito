package br.com.hackathon.apicredito.dto;

public record EndpointTelemetriaDTO(
        String nomeApi,
        String metodo,
        String uri,
        long qtdRequisicoes,
        double tempoMedio, // em milissegundos
        double tempoMinimo,
        double tempoMaximo
) {}