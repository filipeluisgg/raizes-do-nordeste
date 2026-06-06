package com.raizesdonordeste.api.domain.exception;

public class EstoqueInsuficienteException extends NegocioException {

	public EstoqueInsuficienteException(String produto, int disponiveis, int solicitados) {
		super(
			"EstoqueInsuficienteError",
			"Produto '" + produto + "' possui apenas " + disponiveis + " unidades em estoque, mas foram solicitadas " + solicitados + ".",
			"Reduza a quantidade solicitada ou escolha outro produto.",
			409
		);
	}
}
