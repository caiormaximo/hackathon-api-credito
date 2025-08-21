package br.com.hackathon.apicredito.dto;

//DTO com a resposta da analise de fraude
public record AnaliseFraudeResponseDTO(
        StatusFraude status,
        String motivo,
        double pontuacao //pont de 0.0 a 1.0 (1.0 = altissima probabilidade de fraude)
) {}