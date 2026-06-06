package com.raizesdonordeste.api.dto.request;

import com.raizesdonordeste.api.domain.enums.StatusPedido;

import jakarta.validation.constraints.NotNull;

public record AtualizarStatusRequest(
	@NotNull(message = "Novo status é obrigatório")
	StatusPedido novoStatus,

	String motivo
) {}
