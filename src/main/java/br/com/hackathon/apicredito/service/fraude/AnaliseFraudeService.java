package br.com.hackathon.apicredito.service.fraude;

import br.com.hackathon.apicredito.dto.AnaliseFraudeRequestDTO;
import br.com.hackathon.apicredito.dto.AnaliseFraudeResponseDTO;

public interface AnaliseFraudeService {
    AnaliseFraudeResponseDTO verificarFraude(AnaliseFraudeRequestDTO request);
}