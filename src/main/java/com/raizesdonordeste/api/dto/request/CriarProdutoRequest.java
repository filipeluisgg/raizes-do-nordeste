package com.raizesdonordeste.api.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CriarProdutoRequest(
	@NotBlank(message = "Nome é obrigatório")
	String nome,

	String descricao,

	@NotNull(message = "Preço é obrigatório")
	@Positive(message = "Preço deve ser maior que zero")
	BigDecimal preco,

	@NotBlank(message = "Categoria é obrigatória")
	String categoria,

	@NotNull(message = "Informar se é sazonal é obrigatório")
	Boolean sazonal,

	LocalDate disponivelDe,

	LocalDate disponivelAte
) {}
