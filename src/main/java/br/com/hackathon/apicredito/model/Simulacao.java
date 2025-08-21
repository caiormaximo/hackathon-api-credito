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
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CO_PRODUTO", nullable = false)
    private Produto produto;

    @Column(name = "VR_DESEJADO", nullable = false, precision = 18, scale = 2)
    private BigDecimal valorDesejado;

    @Column(name = "QT_PRAZO", nullable = false)
    private Integer prazo;

    @Lob
    @Column(name = "DS_RESULTADO_JSON", nullable = false)
    private String resultadoJson;

    @Column(name = "DT_SIMULACAO", nullable = false)
    private LocalDateTime dataSimulacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "ST_ENVIO_EVENTHUB", nullable = false)
    private StatusEnvioEventHub statusEnvioEventHub;
}