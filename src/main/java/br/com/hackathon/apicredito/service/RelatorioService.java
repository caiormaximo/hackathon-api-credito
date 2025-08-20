package br.com.hackathon.apicredito.service;

import br.com.hackathon.apicredito.dto.SimulacaoProdutoDTO;
import br.com.hackathon.apicredito.dto.VolumeDiarioDTO;
import br.com.hackathon.apicredito.model.Simulacao;
import br.com.hackathon.apicredito.repository.SimulacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final SimulacaoRepository simulacaoRepository;

    public VolumeDiarioDTO gerarVolumeDiario(LocalDate data) {
        List<Simulacao> simulacoesDoDia = simulacaoRepository.findByDataSimulacaoBetween(
                data.atStartOfDay(),
                data.atTime(LocalTime.MAX)
        );

        Map<Integer, List<Simulacao>> simulacoesPorProduto = simulacoesDoDia.stream()
                .collect(Collectors.groupingBy(Simulacao::getCodigoProduto));

        List<SimulacaoProdutoDTO> dtos = simulacoesPorProduto.entrySet().stream()
                .map(entry -> {
                    Integer codigoProduto = entry.getKey();
                    List<Simulacao> simulacoes = entry.getValue();
                    String descProduto = simulacoes.get(0).getDescricaoProduto();

                    BigDecimal valorTotalDesejado = simulacoes.stream()
                            .map(Simulacao::getValorDesejado)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new SimulacaoProdutoDTO(codigoProduto, descProduto, valorTotalDesejado, simulacoes.size());
                })
                .collect(Collectors.toList());

        return new VolumeDiarioDTO(data, dtos);
    }
}