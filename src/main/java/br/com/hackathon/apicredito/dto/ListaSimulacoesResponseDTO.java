package br.com.hackathon.apicredito.dto;

import java.util.List;

//DTO para a resposta paginada do endpoint de listagem de simulacoes

public record ListaSimulacoesResponseDTO(
        Integer pagina, // [cite: 159]
        Long qtdRegistros, // [cite: 160]
        Integer qtdRegistrosPagina, // [cite: 161]
        List<SimulacaoItemDTO> registros // [cite: 162]
) {}