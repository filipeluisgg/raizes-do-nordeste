package com.raizesdonordeste.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AjustarEstoqueRequest(
	@NotNull(message = "Quantidade é obrigatória")
	@Min(value = 0, message = "Quantidade não pode ser negativa")
	Integer quantidade,

	String motivo
) {}
