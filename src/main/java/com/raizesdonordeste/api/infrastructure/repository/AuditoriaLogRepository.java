package com.raizesdonordeste.api.infrastructure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raizesdonordeste.api.domain.entity.AuditoriaLog;

public interface AuditoriaLogRepository extends JpaRepository<AuditoriaLog, Long> {

	List<AuditoriaLog> findByEntidadeAndEntidadeId(String entidade, Long entidadeId);

	List<AuditoriaLog> findByAcao(String acao);

	List<AuditoriaLog> findAllByOrderByCriadoEmDesc();
}
