package com.raizesdonordeste.api.infrastructure.gateway;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.raizesdonordeste.api.domain.enums.FormaPagamento;

/**
 * Simula o comportamento de um gateway de pagamento externo.
 * Ref: Seção 3.6 do relatório — justificativa: mock como @Service preserva
 * desacoplamento via interface sem overhead de microsserviço separado.
 */
@Service
public class PagamentoGatewayMock implements PagamentoGateway {

	private static final BigDecimal LIMITE_RECUSA = new BigDecimal("500.00");

	@Override
	public PagamentoGatewayResponse processar(BigDecimal valor, FormaPagamento formaPagamento) {
		simularLatencia();

		if (deveRecusar(valor)) {
			return new PagamentoGatewayResponse(
				false,
				null,
				"Saldo insuficiente no cartão."
			);
		}

		return new PagamentoGatewayResponse(
			true,
			UUID.randomUUID().toString(),
			null
		);
	}

	private boolean deveRecusar(BigDecimal valor) {
		return valor.compareTo(LIMITE_RECUSA) > 0;
	}

	private void simularLatencia() {
		try {
			Thread.sleep(150);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
