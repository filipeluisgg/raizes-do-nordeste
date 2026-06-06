package com.raizesdonordeste.api.domain.enums;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StatusPedidoTest {

	@Test
	void aguardandoPagamento_devePermitirTransicaoParaRecebido() {
		assertTrue(StatusPedido.AGUARDANDO_PAGAMENTO.podeTransitarPara(StatusPedido.RECEBIDO));
	}

	@Test
	void aguardandoPagamento_devePermitirTransicaoParaCancelado() {
		assertTrue(StatusPedido.AGUARDANDO_PAGAMENTO.podeTransitarPara(StatusPedido.CANCELADO));
	}

	@Test
	void recebido_devePermitirTransicaoParaEmPreparacao() {
		assertTrue(StatusPedido.RECEBIDO.podeTransitarPara(StatusPedido.EM_PREPARACAO));
	}

	@Test
	void recebido_devePermitirTransicaoParaCancelado() {
		assertTrue(StatusPedido.RECEBIDO.podeTransitarPara(StatusPedido.CANCELADO));
	}

	@Test
	void emPreparacao_devePermitirTransicaoParaPronto() {
		assertTrue(StatusPedido.EM_PREPARACAO.podeTransitarPara(StatusPedido.PRONTO));
	}

	@Test
	void pronto_devePermitirTransicaoParaEntregue() {
		assertTrue(StatusPedido.PRONTO.podeTransitarPara(StatusPedido.ENTREGUE));
	}

	@Test
	void aguardandoPagamento_naoDevePermitirTransicaoParaEmPreparacao() {
		assertFalse(StatusPedido.AGUARDANDO_PAGAMENTO.podeTransitarPara(StatusPedido.EM_PREPARACAO));
	}

	@Test
	void emPreparacao_naoDevePermitirCancelamento() {
		assertFalse(StatusPedido.EM_PREPARACAO.podeTransitarPara(StatusPedido.CANCELADO));
	}

	@Test
	void entregue_naoDevePermitirNenhumaTransicao() {
		for (StatusPedido status : StatusPedido.values()) {
			assertFalse(StatusPedido.ENTREGUE.podeTransitarPara(status));
		}
	}

	@Test
	void cancelado_naoDevePermitirNenhumaTransicao() {
		for (StatusPedido status : StatusPedido.values()) {
			assertFalse(StatusPedido.CANCELADO.podeTransitarPara(status));
		}
	}

	@Test
	void pronto_naoDevePermitirCancelamento() {
		assertFalse(StatusPedido.PRONTO.podeTransitarPara(StatusPedido.CANCELADO));
	}
}
