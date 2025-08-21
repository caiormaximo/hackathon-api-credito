package br.com.hackathon.apicredito.repository;

import br.com.hackathon.apicredito.model.Simulacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime; // Importe LocalDateTime
import java.util.List;
import java.util.UUID;

@Repository
public interface SimulacaoRepository extends JpaRepository<Simulacao, UUID> {

    //paginacao
    Page<Simulacao> findAll(Pageable pageable);

    List<Simulacao> findByDataSimulacaoBetween(LocalDateTime inicio, LocalDateTime fim);

}