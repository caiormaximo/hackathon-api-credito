package br.com.hackathon.apicredito.repository;

import br.com.hackathon.apicredito.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {

    @Query("SELECT p FROM Produto p WHERE " +
            ":valorDesejado >= p.valorMinimo AND (:valorDesejado <= p.valorMaximo OR p.valorMaximo IS NULL) AND " +
            ":prazo >= p.minimoMeses AND (:prazo <= p.maximoMeses OR p.maximoMeses IS NULL)")
    Optional<Produto> findProdutoElegivel(@Param("valorDesejado") BigDecimal valorDesejado, @Param("prazo") int prazo);
}