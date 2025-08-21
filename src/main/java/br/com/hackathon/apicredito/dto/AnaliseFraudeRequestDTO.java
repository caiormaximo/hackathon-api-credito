package br.com.hackathon.apicredito.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

//DTO com os dados enviados para a analise de fraude
public record AnaliseFraudeRequestDTO(
        UUID idSimulacao,
        BigDecimal valorDesejado,
        int prazo,
        LocalDateTime horaDaRequisicao
        //caso houvesse infos reais, adicionariamos as infos do cliente, ip, etc.
) {}