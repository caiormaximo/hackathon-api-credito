package br.com.hackathon.apicredito.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO para a resposta completa do endpoint de telemetria.
 */
public record TelemetriaResponseDTO(
        LocalDate dataReferencia, // [cite: 187]
        List<EndpointTelemetriaDTO> listaEndpoints // [cite: 188]
) {}