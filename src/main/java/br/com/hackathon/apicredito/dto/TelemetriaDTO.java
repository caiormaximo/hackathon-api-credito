package br.com.hackathon.apicredito.dto;

import java.time.LocalDate;
import java.util.List;

public record TelemetriaDTO(
        LocalDate dataReferencia,
        List<EndpointTelemetriaDTO> listaEndpoints,
        double percentualSucesso
) {}