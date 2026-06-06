package com.raizesdonordeste.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AtualizarConsentimentoRequest(
	@NotNull(message = "Consentimento é obrigatório")
	Boolean consentimento,

	@NotBlank(message = "Versão do termo é obrigatória")
	String versaoTermo
) {}
