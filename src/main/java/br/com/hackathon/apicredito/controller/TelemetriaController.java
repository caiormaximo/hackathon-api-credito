package br.com.hackathon.apicredito.controller;

import br.com.hackathon.apicredito.dto.TelemetriaResponseDTO;
import br.com.hackathon.apicredito.service.TelemetriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/telemetria")
@RequiredArgsConstructor
public class TelemetriaController {

    private final TelemetriaService telemetriaService;

    @GetMapping
    public ResponseEntity<TelemetriaResponseDTO> getTelemetria() {
        return ResponseEntity.ok(telemetriaService.obterDadosDeTelemetria(LocalDate.now()));
    }
}