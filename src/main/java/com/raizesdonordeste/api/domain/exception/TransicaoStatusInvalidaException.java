package com.raizesdonordeste.api.domain.exception;

import com.raizesdonordeste.api.domain.enums.StatusPedido;

public class TransicaoStatusInvalidaException extends NegocioException {

	public TransicaoStatusInvalidaException(StatusPedido atual, StatusPedido desejado) {
		super(
			"TransicaoStatusInvalidaError",
			"Não é possível transitar do status '" + atual + "' para '" + desejado + "'.",
			"Consulte as transições de status válidas para o estado atual do pedido.",
			409
		);
	}
}
