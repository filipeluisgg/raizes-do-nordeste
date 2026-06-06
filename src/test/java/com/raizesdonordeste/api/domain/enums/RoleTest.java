package com.raizesdonordeste.api.domain.enums;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RoleTest {

	@Test
	void cliente_devePossuirFeaturesDeConsumo() {
		var features = Role.CLIENTE.getFeatures();
		assertTrue(features.contains("create:pedido"));
		assertTrue(features.contains("read:pedido"));
		assertTrue(features.contains("read:cardapio"));
		assertTrue(features.contains("read:fidelidade"));
		assertTrue(features.contains("manage:consentimento"));
	}

	@Test
	void cliente_naoDevePossuirFeaturesAdministrativas() {
		var features = Role.CLIENTE.getFeatures();
		assertFalse(features.contains("manage:estoque"));
		assertFalse(features.contains("cancel:pedido"));
		assertFalse(features.contains("read:auditoria"));
		assertFalse(features.contains("manage:usuario"));
	}

	@Test
	void atendente_devePossuirFeaturesOperacionais() {
		var features = Role.ATENDENTE.getFeatures();
		assertTrue(features.contains("create:pedido"));
		assertTrue(features.contains("read:pedido"));
		assertTrue(features.contains("update:pedido:status"));
	}

	@Test
	void atendente_naoDevePossuirFeaturesDeCancelamento() {
		assertFalse(Role.ATENDENTE.getFeatures().contains("cancel:pedido"));
	}

	@Test
	void gerente_devePossuirFeaturesDeGerencia() {
		var features = Role.GERENTE.getFeatures();
		assertTrue(features.contains("cancel:pedido"));
		assertTrue(features.contains("manage:estoque"));
		assertTrue(features.contains("read:auditoria"));
		assertTrue(features.contains("update:pedido:status"));
	}

	@Test
	void admin_devePossuirFeaturesAdministrativas() {
		var features = Role.ADMIN.getFeatures();
		assertTrue(features.contains("manage:unidade"));
		assertTrue(features.contains("manage:produto"));
		assertTrue(features.contains("manage:usuario"));
		assertTrue(features.contains("read:auditoria"));
	}

	@Test
	void admin_naoDevePossuirFeaturesOperacionais() {
		var features = Role.ADMIN.getFeatures();
		assertFalse(features.contains("create:pedido"));
		assertFalse(features.contains("update:pedido:status"));
	}

	@Test
	void todosOsRoles_devemPossuirFeaturesNaoNulas() {
		for (Role role : Role.values()) {
			assertNotNull(role.getFeatures());
			assertFalse(role.getFeatures().isEmpty());
		}
	}
}
