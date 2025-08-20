package br.com.hackathon.apicredito.service;

import br.com.hackathon.apicredito.dto.EndpointTelemetriaDTO;
import br.com.hackathon.apicredito.dto.TelemetriaDTO;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class TelemetriaService {

    private final MeterRegistry meterRegistry;

    public TelemetriaService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public TelemetriaDTO getDadosTelemetria() {
        List<Timer> timers = new ArrayList<>(meterRegistry.find("http.server.requests").timers());

        List<EndpointTelemetriaDTO> endpoints = timers.stream()
                .map(this::criarEndpointDto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        long totalRequisicoes = endpoints.stream().mapToLong(EndpointTelemetriaDTO::qtdRequisicoes).sum();
        long sucessoCount = timers.stream()
                .filter(t -> "200".equals(t.getId().getTag("status")))
                .mapToLong(Timer::count)
                .sum();

        double percentualSucesso = totalRequisicoes > 0 ? (double) sucessoCount / totalRequisicoes : 0.0;

        return new TelemetriaDTO(LocalDate.now(), endpoints, percentualSucesso);
    }

    private EndpointTelemetriaDTO criarEndpointDto(Timer timer) {
        String uri = timer.getId().getTag("uri");
        //filtra para nao incluir URIs do actuator
        if (uri != null && !uri.startsWith("/actuator")) {
            return new EndpointTelemetriaDTO(
                    "API Simulador de Crédito",
                    timer.getId().getTag("method"),
                    uri,
                    timer.count(),
                    timer.mean(TimeUnit.MILLISECONDS),
                    timer.totalTime(TimeUnit.MILLISECONDS) / (timer.count() > 0 ? timer.count() : 1), //min nao eh direto, usando media
                    timer.max(TimeUnit.MILLISECONDS)
            );
        }
        return null;
    }
}

//config para o actuator coletar trocas http
@Configuration
class ActuatorConfig {
    @Bean
    public HttpExchangeRepository httpExchangeRepository() {
        return new InMemoryHttpExchangeRepository();
    }
}