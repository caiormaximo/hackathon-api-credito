package br.com.hackathon.apicredito.controller;

import br.com.hackathon.apicredito.dto.*;
import br.com.hackathon.apicredito.dto.VolumeDiarioDTO;
import br.com.hackathon.apicredito.service.RelatorioService;
import br.com.hackathon.apicredito.service.SimulacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/simulacoes")
@RequiredArgsConstructor
public class SimulacaoController {

    private final SimulacaoService simulacaoService;
    private final RelatorioService relatorioService;

    @PostMapping
    public ResponseEntity<SimulacaoResponseDTO> simular(@Valid @RequestBody SimulacaoRequestDTO requestDTO) {
        SimulacaoResponseDTO response = simulacaoService.criarSimulacao(requestDTO);
        return ResponseEntity.created(URI.create("/simulacoes/" + response.idSimulacao())).body(response);
    }

    @GetMapping
    public ResponseEntity<ListaSimulacoesResponseDTO> listarSimulacoes(
            // Altera os nomes dos parâmetros para o padrão e inicia a contagem em 0
            @RequestParam(name = "page", defaultValue = "0") int pagina,
            @RequestParam(name = "size", defaultValue = "10") int qtdRegistrosPagina) {

        ListaSimulacoesResponseDTO response = simulacaoService.listarSimulacoes(pagina, qtdRegistrosPagina);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/volume-diario")
    public ResponseEntity<VolumeDiarioDTO> volumeSimuladoPorProdutoPorDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {

        VolumeDiarioDTO response = relatorioService.gerarVolumeDiario(data);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/telemetria")
    public ResponseEntity<TelemetriaResponseDTO> obterTelemetria(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {

        TelemetriaResponseDTO response = simulacaoService.obterDadosTelemetria(data);
        return ResponseEntity.ok(response);
    }
}