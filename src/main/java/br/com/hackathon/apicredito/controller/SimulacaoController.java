package br.com.hackathon.apicredito.controller;

import br.com.hackathon.apicredito.dto.SimulacaoListagemDTO;
import br.com.hackathon.apicredito.dto.SimulacaoRequestDTO;
import br.com.hackathon.apicredito.dto.SimulacaoResponseDTO;
import br.com.hackathon.apicredito.dto.VolumeDiarioDTO;
import br.com.hackathon.apicredito.service.RelatorioService;
import br.com.hackathon.apicredito.service.SimulacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/simulacoes")
@RequiredArgsConstructor
public class SimulacaoController {

    private final SimulacaoService simulacaoService;
    private final RelatorioService relatorioService;

    @PostMapping
    public ResponseEntity<SimulacaoResponseDTO> simular(@RequestBody @Valid SimulacaoRequestDTO requestDTO) {
        SimulacaoResponseDTO response = simulacaoService.criarSimulacao(requestDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<SimulacaoListagemDTO>> listar(Pageable pageable) {
        return ResponseEntity.ok(simulacaoService.listarTodas(pageable));
    }

    @GetMapping("/relatorios/volume-diario")
    public ResponseEntity<VolumeDiarioDTO> obterVolumeDiario(@RequestParam("data") LocalDate data) {
        return ResponseEntity.ok(relatorioService.gerarVolumeDiario(data));
    }
}