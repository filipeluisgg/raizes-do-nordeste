package com.raizesdonordeste.api.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.raizesdonordeste.api.domain.exception.EstoqueInsuficienteException;

class EstoqueUnidadeTest {

	private EstoqueUnidade estoque;

	@BeforeEach
	void setUp() {
		Unidade unidade = new Unidade("Recife Centro", "Rua do Sol, 100", "Recife", "PE", true);
		Produto produto = new Produto("Acarajé", "Acarajé tradicional baiano", new BigDecimal("15.90"), "Salgados");
		estoque = new EstoqueUnidade(unidade, produto, 10, 3);
	}

	@Test
	void baixar_deveDecrementarQuantidade() {
		estoque.baixar(4);
		assertEquals(6, estoque.getQuantidade());
	}

	@Test
	void baixar_devePermitirZerarEstoque() {
		estoque.baixar(10);
		assertEquals(0, estoque.getQuantidade());
	}

	@Test
	void baixar_deveLancarExcecaoQuandoInsuficiente() {
		EstoqueInsuficienteException ex = assertThrows(
			EstoqueInsuficienteException.class,
			() -> estoque.baixar(15)
		);
		assertEquals(409, ex.getStatusCode());
		assertTrue(ex.getMessage().contains("Acarajé"));
		assertTrue(ex.getMessage().contains("10"));
		assertTrue(ex.getMessage().contains("15"));
	}

	@Test
	void repor_deveIncrementarQuantidade() {
		estoque.repor(5);
		assertEquals(15, estoque.getQuantidade());
	}

	@Test
	void abaixoDoMinimo_deveRetornarTrueQuandoAbaixo() {
		estoque.baixar(8);
		assertTrue(estoque.abaixoDoMinimo());
	}

	@Test
	void abaixoDoMinimo_deveRetornarFalseQuandoAcima() {
		assertFalse(estoque.abaixoDoMinimo());
	}

	@Test
	void abaixoDoMinimo_deveRetornarFalseQuandoIgualAoMinimo() {
		estoque.baixar(7);
		assertFalse(estoque.abaixoDoMinimo());
	}
}
