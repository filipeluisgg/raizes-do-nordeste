package com.raizesdonordeste.api.infrastructure.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raizesdonordeste.api.domain.entity.Pagamento;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

	Optional<Pagamento> findByPedidoId(Long pedidoId);

	boolean existsByPedidoId(Long pedidoId);
}
