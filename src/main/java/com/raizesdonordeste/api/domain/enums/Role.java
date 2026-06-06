package com.raizesdonordeste.api.domain.enums;

import java.util.Set;

public enum Role {
	CLIENTE(Set.of(
		"create:pedido", "read:pedido", "read:cardapio",
		"read:fidelidade", "manage:consentimento"
	)),
	ATENDENTE(Set.of(
		"create:pedido", "read:pedido", "read:cardapio",
		"update:pedido:status"
	)),
	GERENTE(Set.of(
		"create:pedido", "read:pedido", "read:cardapio",
		"update:pedido:status", "cancel:pedido",
		"manage:estoque", "read:auditoria"
	)),
	ADMIN(Set.of(
		"read:auditoria", "manage:unidade", "manage:produto",
		"manage:usuario", "read:cardapio"
	));

	private final Set<String> features;

	Role(Set<String> features) {
		this.features = features;
	}

	public Set<String> getFeatures() {
		return features;
	}
}
