package br.com.hackathon.apicredito.repository;

import br.com.hackathon.apicredito.model.Simulacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SimulacaoRepository extends JpaRepository<Simulacao, UUID> {
    List<Simulacao> findByDataSimulacaoBetween(LocalDateTime start, LocalDateTime end);
}