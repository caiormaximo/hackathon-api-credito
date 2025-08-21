package br.com.hackathon.apicredito.service;

import br.com.hackathon.apicredito.dto.VolumeDiarioDTO;
import br.com.hackathon.apicredito.model.Produto;
import br.com.hackathon.apicredito.model.Simulacao;
import br.com.hackathon.apicredito.repository.SimulacaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock
    private SimulacaoRepository simulacaoRepository;

    @InjectMocks
    private RelatorioService relatorioService;

    @Test
    @DisplayName("Deve gerar volume diário corretamente com múltiplas simulações")
    void gerarVolumeDiario_deveRetornarAgregado_quandoExistemSimulacoes() {
        // Arrange
        LocalDate hoje = LocalDate.now();
        Simulacao s1_prod1 = criarSimulacaoMock(1, "Produto A", "1000.00");
        Simulacao s2_prod1 = criarSimulacaoMock(1, "Produto A", "1500.00");
        Simulacao s3_prod2 = criarSimulacaoMock(2, "Produto B", "5000.00");

        when(simulacaoRepository.findByDataSimulacaoBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(s1_prod1, s2_prod1, s3_prod2));

        // Act
        VolumeDiarioDTO resultado = relatorioService.gerarVolumeDiario(hoje);

        // Assert
        assertNotNull(resultado);
        assertEquals(hoje, resultado.dataReferencia());
        assertEquals(2, resultado.simulacoes().size());

        var dtoProd1 = resultado.simulacoes().stream().filter(s -> s.codigoProduto() == 1).findFirst().orElseThrow();
        assertEquals(1, dtoProd1.codigoProduto());
        assertEquals("Produto A", dtoProd1.descricaoProduto());
        assertEquals(2, dtoProd1.quantidadeSimulacoes());
        assertEquals(0, new BigDecimal("2500.00").compareTo(dtoProd1.valorTotalDesejado()));

        var dtoProd2 = resultado.simulacoes().stream().filter(s -> s.codigoProduto() == 2).findFirst().orElseThrow();
        assertEquals(2, dtoProd2.codigoProduto());
        assertEquals("Produto B", dtoProd2.descricaoProduto());
        assertEquals(1, dtoProd2.quantidadeSimulacoes());
        assertEquals(0, new BigDecimal("5000.00").compareTo(dtoProd2.valorTotalDesejado()));
    }

    private Simulacao criarSimulacaoMock(Integer codProduto, String descProduto, String valor) {
        Produto produtoMock = new Produto();
        produtoMock.setCodigo(codProduto);
        produtoMock.setNome(descProduto);

        Simulacao s = new Simulacao();
        s.setProduto(produtoMock);
        s.setValorDesejado(new BigDecimal(valor));
        s.setDataSimulacao(LocalDateTime.now());
        return s;
    }
}