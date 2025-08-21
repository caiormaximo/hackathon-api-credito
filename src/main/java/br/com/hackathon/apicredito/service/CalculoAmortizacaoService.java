package br.com.hackathon.apicredito.service;

import br.com.hackathon.apicredito.dto.ParcelaDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class CalculoAmortizacaoService {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    public List<ParcelaDTO> calcularSac(BigDecimal valorEmprestimo, int prazo, BigDecimal taxaJurosMensal) {
        List<ParcelaDTO> parcelas = new ArrayList<>();
        BigDecimal saldoDevedor = valorEmprestimo;
        BigDecimal amortizacao = valorEmprestimo.divide(BigDecimal.valueOf(prazo), SCALE, ROUNDING_MODE);

        for (int i = 1; i <= prazo; i++) {
            BigDecimal juros = saldoDevedor.multiply(taxaJurosMensal).setScale(SCALE, ROUNDING_MODE);
            BigDecimal prestacao = amortizacao.add(juros);
            saldoDevedor = saldoDevedor.subtract(amortizacao);

            parcelas.add(new ParcelaDTO(i, amortizacao, juros, prestacao));
        }
        return parcelas;
    }

    public List<ParcelaDTO> calcularPrice(BigDecimal valorEmprestimo, int prazo, BigDecimal taxaJurosMensal) {
        List<ParcelaDTO> parcelas = new ArrayList<>();
        BigDecimal taxa = taxaJurosMensal.add(BigDecimal.ONE);
        BigDecimal prestacao = valorEmprestimo
                .multiply(taxa.pow(prazo))
                .multiply(taxaJurosMensal)
                .divide(taxa.pow(prazo).subtract(BigDecimal.ONE), SCALE, ROUNDING_MODE);

        BigDecimal saldoDevedor = valorEmprestimo;
        for (int i = 1; i <= prazo; i++) {
            BigDecimal juros = saldoDevedor.multiply(taxaJurosMensal).setScale(SCALE, ROUNDING_MODE);
            BigDecimal amortizacao = prestacao.subtract(juros);
            saldoDevedor = saldoDevedor.subtract(amortizacao);

            parcelas.add(new ParcelaDTO(i, amortizacao, juros, prestacao));
        }
        return parcelas;
    }
}