package com.raizesdonordeste.api.dto.request;

import java.util.List;

import com.raizesdonordeste.api.domain.enums.CanalPedido;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CriarPedidoRequest(
	@NotNull(message = "ID da unidade é obrigatório")
	Long unidadeId,

	@NotNull(message = "Canal do pedido é obrigatório")
	CanalPedido canalPedido,

	String observacao,

	@NotEmpty(message = "O pedido deve conter pelo menos um item")
	@Valid
	List<ItemRequest> itens
) {
	public record ItemRequest(
		@NotNull(message = "ID do produto é obrigatório")
		Long produtoId,

		@NotNull(message = "Quantidade é obrigatória")
		@jakarta.validation.constraints.Min(value = 1, message = "Quantidade mínima é 1")
		Integer quantidade
	) {}
}
