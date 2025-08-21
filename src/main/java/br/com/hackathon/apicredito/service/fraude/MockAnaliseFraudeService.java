package br.com.hackathon.apicredito.service.fraude;

import br.com.hackathon.apicredito.dto.AnaliseFraudeRequestDTO;
import br.com.hackathon.apicredito.dto.AnaliseFraudeResponseDTO;
import br.com.hackathon.apicredito.dto.StatusFraude;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class MockAnaliseFraudeService implements AnaliseFraudeService {

    private static final BigDecimal VALOR_LIMITE_ANALISE = new BigDecimal("100000.00");

    @Override
    public AnaliseFraudeResponseDTO verificarFraude(AnaliseFraudeRequestDTO request) {
        log.info("[Detector de Fraude] Iniciando análise para simulação ID: {}", request.idSimulacao());

        //regra 1 - valores muito altos precisarão de analise manual
        if (request.valorDesejado().compareTo(VALOR_LIMITE_ANALISE) > 0) {
            log.warn("[Detector de Fraude] Simulação {} marcada para ANÁLISE MANUAL devido a valor elevado.", request.idSimulacao());
            return new AnaliseFraudeResponseDTO(
                    StatusFraude.REQUER_ANALISE_MANUAL,
                    "Valor da simulação excede o limite para aprovação automática.",
                    0.75 //pontuacao de fraude medio pra alta
            );
        }

        //regra 2: acoes de madrugada com prazo curto sao suspeitas
        int hora = request.horaDaRequisicao().getHour();
        if ((hora >= 0 && hora <= 5) && request.prazo() <= 12) {
            log.error("[Detector de Fraude] Simulação {} NEGADA por comportamento suspeito (horário e prazo).", request.idSimulacao());
            return new AnaliseFraudeResponseDTO(
                    StatusFraude.NEGADO,
                    "Comportamento da simulação inconsistente com padrões usuais.",
                    0.95 //pontuacao de fraude altissima
            );
        }

        //aprovado/padrao
        log.info("[Detector de Fraude] Simulação {} APROVADA na análise de fraude.", request.idSimulacao());
        return new AnaliseFraudeResponseDTO(
                StatusFraude.APROVADO,
                "Nenhum indicador de fraude detectado.",
                0.10 //pontuacao de fraude baixa
        );
    }
}