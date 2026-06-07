package com.raizesdonordeste.api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raizesdonordeste.api.domain.entity.Pedido;
import com.raizesdonordeste.api.domain.enums.StatusPedido;
import com.raizesdonordeste.api.dto.RelatorioVendasDTO;
import com.raizesdonordeste.api.infrastructure.repository.PedidoRepository;

@Service
public class RelatorioService {

	private final PedidoRepository pedidoRepository;

	public RelatorioService(PedidoRepository pedidoRepository) {
		this.pedidoRepository = pedidoRepository;
	}

	@Transactional(readOnly = true)
	public RelatorioVendasDTO gerarRelatorioPorUnidade(Long unidadeId, LocalDateTime inicio, LocalDateTime fim) {
		List<Pedido> pedidos = pedidoRepository.findByUnidadeIdAndStatusAndCriadoEmBetween(
				unidadeId, StatusPedido.ENTREGUE, inicio, fim);

		BigDecimal valorTotal = BigDecimal.ZERO;
		String nomeUnidade = "";
		
		for (Pedido p : pedidos) {
			valorTotal = valorTotal.add(p.getValorTotal());
			if (nomeUnidade.isEmpty() && p.getUnidade() != null) {
				nomeUnidade = p.getUnidade().getNome();
			}
		}

		return new RelatorioVendasDTO(unidadeId, nomeUnidade, (long) pedidos.size(), valorTotal);
	}
}
