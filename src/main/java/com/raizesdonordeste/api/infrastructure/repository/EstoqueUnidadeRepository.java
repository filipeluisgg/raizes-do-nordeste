package com.raizesdonordeste.api.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raizesdonordeste.api.domain.entity.EstoqueUnidade;

public interface EstoqueUnidadeRepository extends JpaRepository<EstoqueUnidade, Long> {

	Optional<EstoqueUnidade> findByUnidadeIdAndProdutoId(Long unidadeId, Long produtoId);

	List<EstoqueUnidade> findByUnidadeIdAndQuantidadeGreaterThan(Long unidadeId, int minQuantidade);
}
