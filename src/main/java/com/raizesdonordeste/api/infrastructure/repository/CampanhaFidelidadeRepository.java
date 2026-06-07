package com.raizesdonordeste.api.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.raizesdonordeste.api.domain.entity.CampanhaFidelidade;

public interface CampanhaFidelidadeRepository extends JpaRepository<CampanhaFidelidade, Long> {

	@Query("SELECT c FROM CampanhaFidelidade c WHERE c.ativa = true AND " +
		   "c.inicio <= :dataHora AND c.fim >= :dataHora AND " +
		   "(c.unidade.id = :unidadeId OR c.unidade IS NULL)")
	List<CampanhaFidelidade> findCampanhasValidas(@Param("dataHora") LocalDateTime dataHora, 
												  @Param("unidadeId") Long unidadeId);
}
