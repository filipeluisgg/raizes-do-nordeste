package com.raizesdonordeste.api.infrastructure.gateway;

import java.math.BigDecimal;

import com.raizesdonordeste.api.domain.enums.FormaPagamento;

public interface PagamentoGateway {

	PagamentoGatewayResponse processar(BigDecimal valor, FormaPagamento formaPagamento);

	record PagamentoGatewayResponse(
		boolean aprovado,
		String transacaoId,
		String motivoRecusa
	) {}
}
