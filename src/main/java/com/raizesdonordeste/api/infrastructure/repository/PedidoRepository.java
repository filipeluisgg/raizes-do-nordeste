package com.raizesdonordeste.api.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raizesdonordeste.api.domain.entity.Pedido;
import com.raizesdonordeste.api.domain.enums.CanalPedido;
import com.raizesdonordeste.api.domain.enums.StatusPedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

	List<Pedido> findByClienteId(Long clienteId);

	List<Pedido> findByUnidadeId(Long unidadeId);

	List<Pedido> findByCanalPedido(CanalPedido canalPedido);

	List<Pedido> findByStatus(StatusPedido status);

	List<Pedido> findByUnidadeIdAndCanalPedido(Long unidadeId, CanalPedido canalPedido);

	Optional<Pedido> findByIdAndClienteId(Long id, Long clienteId);
}
