package br.com.hackathon.apicredito.service;

import br.com.hackathon.apicredito.dto.ParcelaDTO;
import br.com.hackathon.apicredito.dto.SimulacaoRequestDTO;
import br.com.hackathon.apicredito.dto.SimulacaoResponseDTO;
import br.com.hackathon.apicredito.exception.ProdutoNaoEncontradoException;
import br.com.hackathon.apicredito.model.Produto;
import br.com.hackathon.apicredito.model.Simulacao;
import br.com.hackathon.apicredito.repository.ProdutoRepository;
import br.com.hackathon.apicredito.repository.SimulacaoRepository;
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

    //instancia do serviço q sera testado
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
                realObjectMapper
        );
    }

    @Test
    @DisplayName("Deve criar simulação com sucesso quando um produto válido for encontrado")
    void criarSimulacao_deveRetornarSucesso_quandoProdutoValidoEncontrado() {

        SimulacaoRequestDTO request = new SimulacaoRequestDTO(new BigDecimal("5000.00"), 12);
        Produto produtoValido = criarProdutoMock();
        List<ParcelaDTO> parcelasMock = List.of(new ParcelaDTO(1, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));

        when(produtoRepository.findProdutoElegivel(request.valorDesejado(), request.prazo())).thenReturn(Optional.of(produtoValido));
        when(calculoAmortizacaoService.calcularSac(any(), anyInt(), any())).thenReturn(parcelasMock);
        when(calculoAmortizacaoService.calcularPrice(any(), anyInt(), any())).thenReturn(parcelasMock);
        doNothing().when(eventHubService).enviarSimulacao(any(String.class));
        when(simulacaoRepository.save(any(Simulacao.class))).thenReturn(new Simulacao());

        SimulacaoResponseDTO response = simulacaoService.criarSimulacao(request);

        assertNotNull(response);
        assertEquals(produtoValido.getCodigo(), response.codigoProduto());
        assertEquals("Produto Mock", response.descricaoProduto());
        assertNotNull(response.idSimulacao());

        verify(produtoRepository, times(1)).findProdutoElegivel(any(), anyInt());
        verify(calculoAmortizacaoService, times(1)).calcularSac(any(), anyInt(), any());
        verify(calculoAmortizacaoService, times(1)).calcularPrice(any(), anyInt(), any());
        verify(simulacaoRepository, times(1)).save(any(Simulacao.class));
        verify(eventHubService, times(1)).enviarSimulacao(any(String.class));
    }

    @Test
    @DisplayName("Deve lançar ProdutoNaoEncontradoException quando nenhum produto for aplicável")
    void criarSimulacao_deveLancarExcecao_quandoNenhumProdutoEncontrado() {
        SimulacaoRequestDTO request = new SimulacaoRequestDTO(new BigDecimal("100.00"), 5);
        when(produtoRepository.findProdutoElegivel(request.valorDesejado(), request.prazo())).thenReturn(Optional.empty());

        assertThrows(ProdutoNaoEncontradoException.class, () -> simulacaoService.criarSimulacao(request));

        verify(simulacaoRepository, never()).save(any());
        verify(eventHubService, never()).enviarSimulacao(anyString());
    }

    private Produto criarProdutoMock() {
        Produto produto = new Produto();
        produto.setCodigo(1);
        produto.setNome("Produto Mock");
        produto.setTaxaJuros(new BigDecimal("0.015"));
        //outros sets se necessario para o teste
        return produto;
    }
}