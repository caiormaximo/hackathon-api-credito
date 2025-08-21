package br.com.hackathon.apicredito.service;

import br.com.hackathon.apicredito.dto.*;
import br.com.hackathon.apicredito.exception.ProdutoNaoEncontradoException;
import br.com.hackathon.apicredito.model.Produto;
import br.com.hackathon.apicredito.model.Simulacao;
import br.com.hackathon.apicredito.repository.ProdutoRepository;
import br.com.hackathon.apicredito.repository.SimulacaoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SimulacaoService {

    private final ProdutoRepository produtoRepository;
    private final SimulacaoRepository simulacaoRepository;
    private final CalculoAmortizacaoService calculoAmortizacaoService;
    private final EventHubService eventHubService;
    private final br.com.hackathon.apicredito.service.TelemetriaService telemetriaService;
    private final ObjectMapper objectMapper;

    @Transactional
    @SneakyThrows //simplificar o tratamento de excecoes do json
    public SimulacaoResponseDTO criarSimulacao(SimulacaoRequestDTO requestDTO) {
        //encontra o produto valido
        Produto produto = produtoRepository
                .findProdutoElegivel(requestDTO.valorDesejado(), requestDTO.prazo())
                .orElseThrow(() -> new ProdutoNaoEncontradoException("Nenhum produto encontrado para os parâmetros informados."));

        //realiza os calculos de amortizacao
        List<ParcelaDTO> parcelasSac = calculoAmortizacaoService.calcularSac(requestDTO.valorDesejado(), requestDTO.prazo(), produto.getTaxaJuros());
        List<ParcelaDTO> parcelasPrice = calculoAmortizacaoService.calcularPrice(requestDTO.valorDesejado(), requestDTO.prazo(), produto.getTaxaJuros());

        var resultadoSimulacao = List.of(
                new ResultadoSimulacaoDTO("SAC", parcelasSac),
                new ResultadoSimulacaoDTO("PRICE", parcelasPrice)
        );

        //gera o id da simulacao
        UUID simulacaoId = UUID.randomUUID();

        //monta o dto de resposta
        var responseDTO = new SimulacaoResponseDTO(
                simulacaoId,
                produto.getCodigo(),
                produto.getNome(),
                produto.getTaxaJuros(),
                resultadoSimulacao
        );

        //converte o dto de resposta para a string json
        String responseJson = objectMapper.writeValueAsString(responseDTO);

        //cria a entidade simulacao com os dados prontos
        Simulacao novaSimulacao = new Simulacao();
        novaSimulacao.setId(simulacaoId); //id gerado
        novaSimulacao.setProduto(produto);
        novaSimulacao.setValorDesejado(requestDTO.valorDesejado());
        novaSimulacao.setPrazo(requestDTO.prazo());
        novaSimulacao.setDataSimulacao(LocalDateTime.now());
        novaSimulacao.setResultadoJson(responseJson); //json gerado

        //salva a entidade no banco de dados uma vez apenas
        simulacaoRepository.save(novaSimulacao);

        //envia o evento para o EventHub
        eventHubService.enviarSimulacao(responseJson);

        //retorna o dto de resposta
        return responseDTO;
    }

    @Transactional(readOnly = true)
    public ListaSimulacoesResponseDTO listarSimulacoes(int pagina, int qtdRegistrosPagina) {
        Pageable pageable = PageRequest.of(pagina - 1, qtdRegistrosPagina);
        Page<Simulacao> simulacaoPage = simulacaoRepository.findAll(pageable);

        List<SimulacaoItemDTO> registros = simulacaoPage.getContent().stream()
                .map(this::mapToSimulacaoItemDTO)
                .collect(Collectors.toList());

        return new ListaSimulacoesResponseDTO(
                pagina,
                simulacaoPage.getTotalElements(),
                simulacaoPage.getNumberOfElements(),
                registros
        );
    }



    public TelemetriaResponseDTO obterDadosTelemetria(LocalDate data) {
        return telemetriaService.obterDadosDeTelemetria(data);
    }


    private SimulacaoItemDTO mapToSimulacaoItemDTO(Simulacao simulacao) {
        try {
            SimulacaoResponseDTO responseDTO = objectMapper.readValue(simulacao.getResultadoJson(), SimulacaoResponseDTO.class);
            BigDecimal valorTotal = responseDTO.resultadoSimulacao().stream()
                    .filter(r -> "PRICE".equalsIgnoreCase(r.tipo()))
                    .findFirst()
                    .map(r -> r.parcelas().stream()
                            .map(ParcelaDTO::valorPrestacao)
                            .reduce(BigDecimal.ZERO, BigDecimal::add))
                    .orElse(BigDecimal.ZERO);

            return new SimulacaoItemDTO(
                    simulacao.getId(),
                    simulacao.getValorDesejado(),
                    simulacao.getPrazo(),
                    valorTotal.setScale(2, RoundingMode.HALF_UP)
            );
        } catch (JsonProcessingException e) {
            //em caso de falha, retorna um valor padrao
            return new SimulacaoItemDTO(simulacao.getId(), simulacao.getValorDesejado(), simulacao.getPrazo(), BigDecimal.ZERO);
        }
    }

    private BigDecimal calcularPrimeiraParcelaPrice(BigDecimal valor, int prazo, BigDecimal taxa) {
        return calculoAmortizacaoService.calcularPrice(valor, prazo, taxa).get(0).valorPrestacao();
    }
}
