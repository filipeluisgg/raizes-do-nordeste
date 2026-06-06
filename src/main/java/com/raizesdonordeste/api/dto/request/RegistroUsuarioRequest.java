package com.raizesdonordeste.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroUsuarioRequest(
	@NotBlank(message = "Nome é obrigatório")
	@Size(min = 2, max = 150, message = "Nome deve ter entre 2 e 150 caracteres")
	String nome,

	@NotBlank(message = "E-mail é obrigatório")
	@Email(message = "E-mail deve ter formato válido")
	String email,

	@NotBlank(message = "Senha é obrigatória")
	@Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres")
	String senha,

	String telefone,

	boolean consentimentoFidelidade
) {}
