package br.com.hackathon.apicredito.config;

import br.com.hackathon.apicredito.model.Produto;
import br.com.hackathon.apicredito.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProdutoRepository produtoRepository;

    @Override
    public void run(String... args) throws Exception {
        if (produtoRepository.count() == 0) {
            Produto p1 = new Produto();
            p1.setCodigo(1);
            p1.setNome("Produto 1");
            p1.setTaxaJuros(new BigDecimal("0.017900000"));
            p1.setMinimoMeses(0);
            p1.setMaximoMeses(24);
            p1.setValorMinimo(new BigDecimal("200.00"));
            p1.setValorMaximo(new BigDecimal("10000.00"));

            Produto p2 = new Produto();
            p2.setCodigo(2);
            p2.setNome("Produto 2");
            p2.setTaxaJuros(new BigDecimal("0.017500000"));
            p2.setMinimoMeses(25);
            p2.setMaximoMeses(48);
            p2.setValorMinimo(new BigDecimal("10001.00"));
            p2.setValorMaximo(new BigDecimal("100000.00"));

            Produto p3 = new Produto();
            p3.setCodigo(3);
            p3.setNome("Produto 3");
            p3.setTaxaJuros(new BigDecimal("0.018200000"));
            p3.setMinimoMeses(49);
            p3.setMaximoMeses(96);
            p3.setValorMinimo(new BigDecimal("100000.01"));
            p3.setValorMaximo(new BigDecimal("1000000.00"));

            Produto p4 = new Produto();
            p4.setCodigo(4);
            p4.setNome("Produto 4");
            p4.setTaxaJuros(new BigDecimal("0.015100000"));
            p4.setMinimoMeses(96);
            p4.setMaximoMeses(null); //sem maximo de meses
            p4.setValorMinimo(new BigDecimal("1000000.01"));
            p4.setValorMaximo(null); //sem maximo de valor

            produtoRepository.saveAll(Arrays.asList(p1, p2, p3, p4));
        }
    }
}