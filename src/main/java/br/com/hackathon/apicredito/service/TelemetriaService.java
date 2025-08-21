package br.com.hackathon.apicredito.service;

import br.com.hackathon.apicredito.dto.EndpointTelemetriaDTO;
import br.com.hackathon.apicredito.dto.TelemetriaResponseDTO;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TelemetriaService {

    private final MeterRegistry meterRegistry;

    public TelemetriaResponseDTO obterDadosDeTelemetria(LocalDate dataReferencia) {

        List<EndpointTelemetriaDTO> endpoints = List.of(
                getMetricsForEndpoint("Criar Simulação", "/api/simulacoes", "POST"),
                getMetricsForEndpoint("Listar Simulações", "/api/simulacoes", "GET"),
                getMetricsForEndpoint("Obter Volume Diário", "/api/simulacoes/volume-diario", "GET"),
                getMetricsForEndpoint("Obter Telemetria", "/api/telemetria", "GET")
        );

        return new TelemetriaResponseDTO(
                dataReferencia,
                endpoints
        );
    }


    private EndpointTelemetriaDTO getMetricsForEndpoint(String nomeApi, String uri, String httpMethod) {
        Timer timer = meterRegistry.find("http.server.requests")
                .tag("uri", uri)
                .tag("method", httpMethod)
                .timer();

        if (timer == null) {
            return new EndpointTelemetriaDTO(nomeApi, 0L, 0L, 0L, 0L, 1.0);
        }

        long count = timer.count();
        long totalTimeMs = (long) timer.totalTime(TimeUnit.MILLISECONDS);
        long maxTimeMs = (long) timer.max(TimeUnit.MILLISECONDS);
        long minTimeMs = 0L;
        long avgTimeMs = (count > 0) ? (totalTimeMs / count) : 0;
        double successPercentage = 1.0;

        return new EndpointTelemetriaDTO(
                nomeApi,
                count,
                avgTimeMs,
                minTimeMs,
                maxTimeMs,
                successPercentage
        );
    }
}