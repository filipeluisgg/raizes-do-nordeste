package com.raizesdonordeste.api.infrastructure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.raizesdonordeste.api.domain.entity.PontoFidelidade;

public interface PontoFidelidadeRepository extends JpaRepository<PontoFidelidade, Long> {

	List<PontoFidelidade> findByClienteIdOrderByCriadoEmDesc(Long clienteId);

	boolean existsByPedidoIdAndPontosLessThan(Long pedidoId, Integer pontos);

	@Query("SELECT COALESCE(SUM(pf.pontos), 0) FROM PontoFidelidade pf WHERE pf.cliente.id = :clienteId")
	int calcularSaldoPontos(@Param("clienteId") Long clienteId);

	boolean existsByPedidoId(Long pedidoId);
}
