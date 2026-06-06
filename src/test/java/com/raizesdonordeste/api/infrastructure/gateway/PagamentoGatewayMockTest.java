package com.raizesdonordeste.api.infrastructure.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.raizesdonordeste.api.domain.enums.FormaPagamento;
import com.raizesdonordeste.api.infrastructure.gateway.PagamentoGateway.PagamentoGatewayResponse;

class PagamentoGatewayMockTest {

	private PagamentoGatewayMock gateway;

	@BeforeEach
	void setUp() {
		gateway = new PagamentoGatewayMock();
	}

	@Test
	void processar_deveAprovarPagamentoAbaixoDoLimite() {
		PagamentoGatewayResponse response = gateway.processar(
			new BigDecimal("100.00"), FormaPagamento.PIX
		);

		assertTrue(response.aprovado());
		assertNotNull(response.transacaoId());
		assertNull(response.motivoRecusa());
	}

	@Test
	void processar_deveAprovarPagamentoNoLimite() {
		PagamentoGatewayResponse response = gateway.processar(
			new BigDecimal("500.00"), FormaPagamento.CARTAO_CREDITO
		);

		assertTrue(response.aprovado());
	}

	@Test
	void processar_deveRecusarPagamentoAcimaDoLimite() {
		PagamentoGatewayResponse response = gateway.processar(
			new BigDecimal("500.01"), FormaPagamento.CARTAO_DEBITO
		);

		assertFalse(response.aprovado());
		assertNull(response.transacaoId());
		assertNotNull(response.motivoRecusa());
		assertEquals("Saldo insuficiente no cartão.", response.motivoRecusa());
	}

	@Test
	void processar_deveAceitarTodasAsFormasDePagamento() {
		for (FormaPagamento forma : FormaPagamento.values()) {
			PagamentoGatewayResponse response = gateway.processar(
				new BigDecimal("50.00"), forma
			);
			assertTrue(response.aprovado());
		}
	}
}
