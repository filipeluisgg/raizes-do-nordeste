package com.raizesdonordeste.api.dto.request;

import com.raizesdonordeste.api.domain.enums.FormaPagamento;

import jakarta.validation.constraints.NotNull;

public record ProcessarPagamentoRequest(
	@NotNull(message = "Forma de pagamento é obrigatória")
	FormaPagamento formaPagamento
) {}
