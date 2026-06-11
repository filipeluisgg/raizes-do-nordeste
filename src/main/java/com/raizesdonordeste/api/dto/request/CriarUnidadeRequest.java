package com.raizesdonordeste.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarUnidadeRequest(
	@NotBlank(message = "Nome é obrigatório")
	String nome,

	@NotBlank(message = "Endereço é obrigatório")
	String endereco,

	@NotBlank(message = "Cidade é obrigatória")
	String cidade,

	@NotBlank(message = "Estado é obrigatório")
	@Size(min = 2, max = 2, message = "Estado deve ter 2 caracteres")
	String estado,

	@NotNull(message = "Informar se possui cozinha completa é obrigatório")
	Boolean cozinhaCompleta
) {}
