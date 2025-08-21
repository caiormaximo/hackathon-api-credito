package br.com.hackathon.apicredito.service;

import br.com.hackathon.apicredito.dto.*;
import br.com.hackathon.apicredito.exception.BusinessException;
import br.com.hackathon.apicredito.exception.ProdutoNaoEncontradoException;
import br.com.hackathon.apicredito.model.Produto;
import br.com.hackathon.apicredito.model.Simulacao;
import br.com.hackathon.apicredito.model.StatusEnvioEventHub;
import br.com.hackathon.apicredito.repository.ProdutoRepository;
import br.com.hackathon.apicredito.repository.SimulacaoRepository;
import br.com.hackathon.apicredito.service.fraude.AnaliseFraudeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulacaoService{

    private final ProdutoRepository produtoRepository;
    private final SimulacaoRepository simulacaoRepository;
    private final CalculoAmortizacaoService calculoAmortizacaoService;
    private final EventHubService eventHubService;
    private final TelemetriaService telemetriaService;
    private final ObjectMapper objectMapper;
    private final AnaliseFraudeService analiseFraudeService;

    @Transactional
    @SneakyThrows
    public SimulacaoResponseDTO criarSimulacao(SimulacaoRequestDTO requestDTO) {
        UUID simulacaoId = UUID.randomUUID();
        LocalDateTime horaDaRequisicao = LocalDateTime.now();

        var requisicaoFraude = new AnaliseFraudeRequestDTO(simulacaoId, requestDTO.valorDesejado(), requestDTO.prazo(), horaDaRequisicao);
        AnaliseFraudeResponseDTO respostaFraude = analiseFraudeService.verificarFraude(requisicaoFraude);

        if (respostaFraude.status() == StatusFraude.NEGADO) {
            throw new BusinessException("A simulação foi bloqueada por suspeita de fraude. Motivo: " + respostaFraude.motivo());
        }

        Produto produto = produtoRepository
                .findProdutoElegivel(requestDTO.valorDesejado(), requestDTO.prazo())
                .orElseThrow(() -> new ProdutoNaoEncontradoException("Nenhum produto encontrado para os parâmetros informados."));

        List<ParcelaDTO> parcelasSac = calculoAmortizacaoService.calcularSac(requestDTO.valorDesejado(), requestDTO.prazo(), produto.getTaxaJuros());
        List<ParcelaDTO> parcelasPrice = calculoAmortizacaoService.calcularPrice(requestDTO.valorDesejado(), requestDTO.prazo(), produto.getTaxaJuros());
        var resultadoSimulacao = List.of(new ResultadoSimulacaoDTO("SAC", parcelasSac), new ResultadoSimulacaoDTO("PRICE", parcelasPrice));

        var responseDTO = new SimulacaoResponseDTO(simulacaoId, produto.getCodigo(), produto.getNome(), produto.getTaxaJuros(), resultadoSimulacao);
        String responseJson = objectMapper.writeValueAsString(responseDTO);

        Simulacao novaSimulacao = new Simulacao();
        novaSimulacao.setId(simulacaoId);
        novaSimulacao.setProduto(produto);
        novaSimulacao.setValorDesejado(requestDTO.valorDesejado());
        novaSimulacao.setPrazo(requestDTO.prazo());
        novaSimulacao.setDataSimulacao(horaDaRequisicao);
        novaSimulacao.setResultadoJson(responseJson);
        novaSimulacao.setStatusEnvioEventHub(StatusEnvioEventHub.AGUARDANDO_ENVIO);
        novaSimulacao.setStatusAnaliseFraude(respostaFraude.status());
        novaSimulacao.setPontuacaoFraude(respostaFraude.pontuacao());

        simulacaoRepository.save(novaSimulacao);

        eventHubService.enviarSimulacao(
                responseJson,
                () -> {
                    log.info("Callback de sucesso do EventHub para simulação ID: {}", simulacaoId);
                    atualizarStatusEnvio(simulacaoId, StatusEnvioEventHub.ENVIADO);
                },
                (error) -> {
                    log.error("Callback de erro do EventHub para simulação ID: {}. Causa: {}", simulacaoId, error.getMessage());
                    atualizarStatusEnvio(simulacaoId, StatusEnvioEventHub.FALHA_NO_ENVIO);
                }
        );

        return responseDTO;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void atualizarStatusEnvio(UUID simulacaoId, StatusEnvioEventHub status) {
        simulacaoRepository.findById(simulacaoId).ifPresent(simulacao -> {
            simulacao.setStatusEnvioEventHub(status);
            simulacaoRepository.save(simulacao);
            log.info("Status de envio da simulação {} atualizado para {}", simulacaoId, status);
        });
    }
    @Transactional(readOnly = true)
    public ListaSimulacoesResponseDTO listarSimulacoes(int pagina, int qtdRegistrosPagina) {
        Pageable pageable = PageRequest.of(pagina, qtdRegistrosPagina);
        Page<Simulacao> simulacaoPage = simulacaoRepository.findAll(pageable);
        List<SimulacaoItemDTO> registros = simulacaoPage.getContent().stream()
                .map(this::mapToSimulacaoItemDTO)
                .collect(Collectors.toList());
        return new ListaSimulacoesResponseDTO(simulacaoPage.getNumber(), simulacaoPage.getTotalElements(), simulacaoPage.getNumberOfElements(), registros);
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
            return new SimulacaoItemDTO(simulacao.getId(), simulacao.getValorDesejado(), simulacao.getPrazo(), valorTotal.setScale(2, RoundingMode.HALF_UP));
        } catch (JsonProcessingException e) {
            return new SimulacaoItemDTO(simulacao.getId(), simulacao.getValorDesejado(), simulacao.getPrazo(), BigDecimal.ZERO);
        }
    }
}
