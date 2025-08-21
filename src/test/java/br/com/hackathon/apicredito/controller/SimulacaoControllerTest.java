package br.com.hackathon.apicredito.controller;

import br.com.hackathon.apicredito.dto.SimulacaoRequestDTO;
import br.com.hackathon.apicredito.dto.SimulacaoResponseDTO;
import br.com.hackathon.apicredito.service.RelatorioService;
import br.com.hackathon.apicredito.service.SimulacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SimulacaoController.class)
class SimulacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SimulacaoService simulacaoService;

    @MockBean
    private RelatorioService relatorioService;

    @Test
    @DisplayName("POST /simulacoes - Deve retornar 201 Created com corpo válido")
    void simular_deveRetornar201Created_comCorpoValido() throws Exception {
        SimulacaoRequestDTO request = new SimulacaoRequestDTO(new BigDecimal("9000.00"), 20);
        UUID simulacaoId = UUID.randomUUID();
        SimulacaoResponseDTO responseDTO = new SimulacaoResponseDTO(
                simulacaoId, 1, "Produto 1", new BigDecimal("0.0179"), Collections.emptyList()
        );

        when(simulacaoService.criarSimulacao(any(SimulacaoRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/simulacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()) // Espera o status 201 Created
                .andExpect(jsonPath("$.idSimulacao").value(simulacaoId.toString()))
                .andExpect(jsonPath("$.codigoProduto").value(1));
    }

    @Test
    @DisplayName("POST /simulacoes - Deve retornar 400 Bad Request com corpo inválido")
    void simular_deveRetornar400BadRequest_comCorpoInvalido() throws Exception {
        SimulacaoRequestDTO request = new SimulacaoRequestDTO(null, 20);

        mockMvc.perform(post("/api/simulacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}