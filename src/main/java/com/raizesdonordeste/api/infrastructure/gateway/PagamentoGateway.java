package com.raizesdonordeste.api.infrastructure.gateway;

import com.raizesdonordeste.api.domain.entity.Pedido;
import com.raizesdonordeste.api.domain.enums.FormaPagamento;

public interface PagamentoGateway {

	PagamentoGatewayResponse processar(Pedido pedido, FormaPagamento formaPagamento);

	record PagamentoGatewayResponse(
		boolean aprovado,
		String transacaoId,
		String motivoRecusa
	) {}
}
