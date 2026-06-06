package com.raizesdonordeste.api.domain.enums;

import java.util.Map;
import java.util.Set;

public enum StatusPedido {
	AGUARDANDO_PAGAMENTO,
	RECEBIDO,
	EM_PREPARACAO,
	PRONTO,
	ENTREGUE,
	CANCELADO;

	private static final Map<StatusPedido, Set<StatusPedido>> TRANSICOES_VALIDAS = Map.of(
		AGUARDANDO_PAGAMENTO, Set.of(RECEBIDO, CANCELADO),
		RECEBIDO, Set.of(EM_PREPARACAO, CANCELADO),
		EM_PREPARACAO, Set.of(PRONTO),
		PRONTO, Set.of(ENTREGUE),
		ENTREGUE, Set.of(),
		CANCELADO, Set.of()
	);

	public boolean podeTransitarPara(StatusPedido novoStatus) {
		return TRANSICOES_VALIDAS.getOrDefault(this, Set.of()).contains(novoStatus);
	}
}
