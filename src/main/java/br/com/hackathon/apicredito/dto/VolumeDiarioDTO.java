package br.com.hackathon.apicredito.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record VolumeDiarioDTO(
        LocalDate dataReferencia,
        List<SimulacaoProdutoDTO> simulacoes
) {}