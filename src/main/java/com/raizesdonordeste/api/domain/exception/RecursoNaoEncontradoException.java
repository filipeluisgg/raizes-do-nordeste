package com.raizesdonordeste.api.domain.exception;

public class RecursoNaoEncontradoException extends NegocioException {

	public RecursoNaoEncontradoException(String recurso, Long id) {
		super(
			"NotFoundError",
			recurso + " com ID " + id + " não foi encontrado.",
			"Verifique se o ID informado está correto e tente novamente.",
			404
		);
	}
}
