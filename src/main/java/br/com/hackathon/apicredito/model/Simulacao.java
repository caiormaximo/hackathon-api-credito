package br.com.hackathon.apicredito.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "SIMULACAO")
public class Simulacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "DT_SIMULACAO", nullable = false)
    private LocalDateTime dataSimulacao;

    @Column(name = "CD_PRODUTO", nullable = false)
    private Integer codigoProduto;

    @Column(name = "DS_PRODUTO", nullable = false)
    private String descricaoProduto;

    @Column(name = "VR_DESEJADO", nullable = false, precision = 18, scale = 2)
    private BigDecimal valorDesejado;

    @Column(name = "QT_PRAZO", nullable = false)
    private int prazo;

    @Lob //armazenar json completo
    @Column(name = "DS_RESULTADO_JSON", nullable = false)
    private String resultadoJson;
}