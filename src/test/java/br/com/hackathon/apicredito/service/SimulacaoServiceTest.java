package br.com.hackathon.apicredito.service;

import br.com.hackathon.apicredito.dto.*;
import br.com.hackathon.apicredito.exception.ProdutoNaoEncontradoException;
import br.com.hackathon.apicredito.model.Produto;
import br.com.hackathon.apicredito.model.Simulacao;
import br.com.hackathon.apicredito.repository.ProdutoRepository;
import br.com.hackathon.apicredito.repository.SimulacaoRepository;
import br.com.hackathon.apicredito.service.fraude.AnaliseFraudeService; // <-- Importar novo serviço
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimulacaoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private SimulacaoRepository simulacaoRepository;
    @Mock
    private CalculoAmortizacaoService calculoAmortizacaoService;
    @Mock
    private EventHubService eventHubService;
    @Mock
    private TelemetriaService telemetriaService;

    @Mock // <-- NOVO MOCK ADICIONADO
    private AnaliseFraudeService analiseFraudeService;

    private SimulacaoService simulacaoService;

    @BeforeEach
    void setUp() {
        ObjectMapper realObjectMapper = new ObjectMapper();
        realObjectMapper.registerModule(new JavaTimeModule());

        simulacaoService = new SimulacaoService(
                produtoRepository,
                simulacaoRepository,
                calculoAmortizacaoService,
                eventHubService,
                telemetriaService,
                realObjectMapper,
                analiseFraudeService
        );
    }

    @Test
    @DisplayName("Deve criar simulação com sucesso quando um produto válido for encontrado")
    void criarSimulacao_deveRetornarSucesso_quandoProdutoValidoEncontrado() {
        SimulacaoRequestDTO request = new SimulacaoRequestDTO(new BigDecimal("5000.00"), 12);
        Produto produtoValido = criarProdutoMock();
        List<ParcelaDTO> parcelasMock = List.of(new ParcelaDTO(1, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));

        var respostaFraudeAprovada = new AnaliseFraudeResponseDTO(StatusFraude.APROVADO, "OK", 0.1);
        when(analiseFraudeService.verificarFraude(any(AnaliseFraudeRequestDTO.class))).thenReturn(respostaFraudeAprovada);

        when(produtoRepository.findProdutoElegivel(request.valorDesejado(), request.prazo())).thenReturn(Optional.of(produtoValido));
        when(calculoAmortizacaoService.calcularSac(any(), anyInt(), any())).thenReturn(parcelasMock);
        when(calculoAmortizacaoService.calcularPrice(any(), anyInt(), any())).thenReturn(parcelasMock);
        when(simulacaoRepository.save(any(Simulacao.class))).thenReturn(new Simulacao());
        doNothing().when(eventHubService).enviarSimulacao(anyString(), any(Runnable.class), any(Consumer.class));

        SimulacaoResponseDTO response = simulacaoService.criarSimulacao(request);

        assertNotNull(response);
        assertEquals(produtoValido.getCodigo(), response.codigoProduto());

        verify(analiseFraudeService, times(1)).verificarFraude(any(AnaliseFraudeRequestDTO.class));
        verify(eventHubService, times(1)).enviarSimulacao(anyString(), any(Runnable.class), any(Consumer.class));
    }

    @Test
    @DisplayName("Deve lançar ProdutoNaoEncontradoException quando nenhum produto for aplicável")
    void criarSimulacao_deveLancarExcecao_quandoNenhumProdutoEncontrado() {
        SimulacaoRequestDTO request = new SimulacaoRequestDTO(new BigDecimal("100.00"), 5);

        var respostaFraudeAprovada = new AnaliseFraudeResponseDTO(StatusFraude.APROVADO, "OK", 0.1);
        when(analiseFraudeService.verificarFraude(any(AnaliseFraudeRequestDTO.class))).thenReturn(respostaFraudeAprovada);

        when(produtoRepository.findProdutoElegivel(request.valorDesejado(), request.prazo())).thenReturn(Optional.empty());

        assertThrows(ProdutoNaoEncontradoException.class, () -> simulacaoService.criarSimulacao(request));

        verify(eventHubService, never()).enviarSimulacao(anyString(), any(Runnable.class), any(Consumer.class));
    }

    private Produto criarProdutoMock() {
        Produto produto = new Produto();
        produto.setCodigo(1);
        produto.setNome("Produto Mock");
        produto.setTaxaJuros(new BigDecimal("0.015"));
        return produto;
    }
}