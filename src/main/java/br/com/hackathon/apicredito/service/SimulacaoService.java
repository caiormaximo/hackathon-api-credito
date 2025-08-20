package br.com.hackathon.apicredito.service;

import br.com.hackathon.apicredito.dto.*;
import br.com.hackathon.apicredito.exception.BusinessException;
import br.com.hackathon.apicredito.model.Produto;
import br.com.hackathon.apicredito.model.Simulacao;
import br.com.hackathon.apicredito.repository.ProdutoRepository;
import br.com.hackathon.apicredito.repository.SimulacaoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulacaoService {

    private final ProdutoRepository produtoRepository;
    private final SimulacaoRepository simulacaoRepository;
    private final EventHubService eventHubService;
    private final ObjectMapper objectMapper;

    @Transactional
    public SimulacaoResponseDTO criarSimulacao(SimulacaoRequestDTO requestDTO) {
        //produto aplicavel
        Produto produto = encontrarProdutoAplicavel(requestDTO.valorDesejado(), requestDTO.prazo());

        //persiste a simulacao primeiro, deixando o banco gerar o ID
        Simulacao simulacaoSalva = persistirSimulacao(requestDTO, produto);

        //calcula os sistemas de amortizacao
        List<ParcelaDTO> parcelasSac = calcularSac(requestDTO.valorDesejado(), requestDTO.prazo(), produto.getTaxaJuros());
        List<ParcelaDTO> parcelasPrice = calcularPrice(requestDTO.valorDesejado(), requestDTO.prazo(), produto.getTaxaJuros());

        List<ResultadoSimulacaoDTO> resultados = List.of(
                new ResultadoSimulacaoDTO("SAC", parcelasSac),
                new ResultadoSimulacaoDTO("PRICE", parcelasPrice)
        );

        //cria o dto de resposta usando o ID gerado pelo banco
        SimulacaoResponseDTO responseDTO = new SimulacaoResponseDTO(
                simulacaoSalva.getId(), //ID que foi gerado e salvo
                produto.getCodigo(),
                produto.getNome(),
                produto.getTaxaJuros(),
                resultados
        );

        //atualiza a simulacao no banco com o json completo da resposta
        atualizarSimulacaoComJson(simulacaoSalva, responseDTO);

        //envia o evento para o eventHub
        eventHubService.enviarSimulacao(responseDTO);

        return responseDTO;
    }

    private Simulacao persistirSimulacao(SimulacaoRequestDTO requestDTO, Produto produto) {
        Simulacao simulacao = new Simulacao();

        simulacao.setDataSimulacao(LocalDateTime.now());
        simulacao.setCodigoProduto(produto.getCodigo());
        simulacao.setDescricaoProduto(produto.getNome());
        simulacao.setValorDesejado(requestDTO.valorDesejado());
        simulacao.setPrazo(requestDTO.prazo());
        simulacao.setResultadoJson("{}"); // json temporario
        return simulacaoRepository.save(simulacao);
    }

    private void atualizarSimulacaoComJson(Simulacao simulacao, SimulacaoResponseDTO responseDTO) {
        try {
            simulacao.setResultadoJson(objectMapper.writeValueAsString(responseDTO));
            simulacaoRepository.save(simulacao); //save-update
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar o resultado da simulação para persistência.", e);
            throw new BusinessException("Erro interno ao salvar a simulação.");
        }
    }


    public Page<SimulacaoListagemDTO> listarTodas(Pageable pageable) {
        return simulacaoRepository.findAll(pageable).map(this::converterParaListagemDTO);
    }

    private SimulacaoListagemDTO converterParaListagemDTO(Simulacao simulacao) {
        try {
            //se o json estiver vazio ou for o temporario, evite o erro
            if (simulacao.getResultadoJson() == null || "{}".equals(simulacao.getResultadoJson())) {
                return new SimulacaoListagemDTO(
                        simulacao.getId(),
                        simulacao.getValorDesejado(),
                        simulacao.getPrazo(),
                        BigDecimal.ZERO
                );
            }

            SimulacaoResponseDTO responseDTO = objectMapper.readValue(simulacao.getResultadoJson(), SimulacaoResponseDTO.class);
            BigDecimal valorTotal = responseDTO.resultadoSimulacao().stream()
                    .filter(r -> "PRICE".equals(r.tipo()))
                    .findFirst()
                    .map(r -> r.parcelas().get(0).valorPrestacao().multiply(BigDecimal.valueOf(simulacao.getPrazo())))
                    .orElse(BigDecimal.ZERO);

            return new SimulacaoListagemDTO(
                    simulacao.getId(),
                    simulacao.getValorDesejado(),
                    simulacao.getPrazo(),
                    valorTotal.setScale(2, RoundingMode.HALF_UP)
            );
        } catch (JsonProcessingException e) {
            log.error("Erro ao desserializar JSON da simulação {}", simulacao.getId(), e);
            throw new BusinessException("Não foi possível processar a simulação armazenada.");
        }
    }

    private Produto encontrarProdutoAplicavel(BigDecimal valor, int prazo) {
        return produtoRepository.findAll().stream()
                .filter(p -> valor.compareTo(p.getValorMinimo()) >= 0)
                .filter(p -> p.getValorMaximo() == null || valor.compareTo(p.getValorMaximo()) <= 0)
                .filter(p -> prazo >= p.getMinimoMeses())
                .filter(p -> p.getMaximoMeses() == null || prazo <= p.getMaximoMeses())
                .findFirst()
                .orElseThrow(() -> new BusinessException("Nenhum produto de crédito disponível para os parâmetros informados."));
    }

    private List<ParcelaDTO> calcularSac(BigDecimal valorFinanciado, int prazo, BigDecimal taxaJuros) {
        List<ParcelaDTO> parcelas = new ArrayList<>();
        BigDecimal saldoDevedor = valorFinanciado;
        BigDecimal valorAmortizacao = valorFinanciado.divide(BigDecimal.valueOf(prazo), 2, RoundingMode.HALF_UP);

        for (int i = 1; i <= prazo; i++) {
            BigDecimal juros = saldoDevedor.multiply(taxaJuros).setScale(2, RoundingMode.HALF_UP);
            BigDecimal prestacao = valorAmortizacao.add(juros);
            saldoDevedor = saldoDevedor.subtract(valorAmortizacao);

            parcelas.add(new ParcelaDTO(i, valorAmortizacao, juros, prestacao));
        }
        return parcelas;
    }

    private List<ParcelaDTO> calcularPrice(BigDecimal valorFinanciado, int prazo, BigDecimal taxaJuros) {
        List<ParcelaDTO> parcelas = new ArrayList<>();
        BigDecimal i = taxaJuros;
        BigDecimal n = BigDecimal.valueOf(prazo);
        // pmt = pv * [i * (1 + i)^n] / [(1 + i)^n - 1]
        BigDecimal fator = (BigDecimal.ONE.add(i)).pow(prazo);
        BigDecimal pmt = valorFinanciado.multiply(i.multiply(fator)).divide(fator.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);

        BigDecimal saldoDevedor = valorFinanciado;
        for (int j = 1; j <= prazo; j++) {
            BigDecimal juros = saldoDevedor.multiply(i).setScale(2, RoundingMode.HALF_UP);
            BigDecimal amortizacao = pmt.subtract(juros);
            saldoDevedor = saldoDevedor.subtract(amortizacao);

            //garantir que a ultima amortizacao quite o saldo devedor
            if (j == prazo && saldoDevedor.abs().compareTo(BigDecimal.ZERO) > 0) {
                amortizacao = amortizacao.add(saldoDevedor);
            }

            parcelas.add(new ParcelaDTO(j, amortizacao, juros, pmt));
        }
        return parcelas;
    }
}