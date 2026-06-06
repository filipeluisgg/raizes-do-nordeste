package com.raizesdonordeste.api.infrastructure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.raizesdonordeste.api.domain.entity.FidelidadeConsentimento;

public interface FidelidadeConsentimentoRepository extends JpaRepository<FidelidadeConsentimento, Long> {

	List<FidelidadeConsentimento> findByClienteIdOrderByCriadoEmDesc(Long clienteId);

	@Query("SELECT fc FROM FidelidadeConsentimento fc WHERE fc.cliente.id = :clienteId ORDER BY fc.criadoEm DESC LIMIT 1")
	FidelidadeConsentimento findConsentimentoAtual(@Param("clienteId") Long clienteId);
}
