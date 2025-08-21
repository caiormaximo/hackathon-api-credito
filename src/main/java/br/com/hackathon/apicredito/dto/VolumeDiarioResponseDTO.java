package br.com.hackathon.apicredito.dto;

import java.time.LocalDate;
import java.util.List;

//DTO para a resposta completa do endpoint de volume diario de simulacoes

public record VolumeDiarioResponseDTO(
        LocalDate dataReferencia,
        List<VolumeProdutoDTO> simulacoes
) {}