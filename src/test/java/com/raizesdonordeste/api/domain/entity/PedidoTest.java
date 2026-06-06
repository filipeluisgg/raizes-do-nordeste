package com.raizesdonordeste.api.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.raizesdonordeste.api.domain.enums.CanalPedido;
import com.raizesdonordeste.api.domain.enums.StatusPedido;
import com.raizesdonordeste.api.domain.exception.TransicaoStatusInvalidaException;

class PedidoTest {

	private Pedido pedido;

	@BeforeEach
	void setUp() {
		pedido = new Pedido(null, null, CanalPedido.APP);
	}

	@Test
	void novoPedido_deveIniciarComStatusAguardandoPagamento() {
		assertEquals(StatusPedido.AGUARDANDO_PAGAMENTO, pedido.getStatus());
	}

	@Test
	void avancarStatus_devePermitirTransicaoValida() {
		pedido.avancarStatus(StatusPedido.RECEBIDO);
		assertEquals(StatusPedido.RECEBIDO, pedido.getStatus());
	}

	@Test
	void avancarStatus_devePermitirFluxoCompleto() {
		pedido.avancarStatus(StatusPedido.RECEBIDO);
		pedido.avancarStatus(StatusPedido.EM_PREPARACAO);
		pedido.avancarStatus(StatusPedido.PRONTO);
		pedido.avancarStatus(StatusPedido.ENTREGUE);
		assertEquals(StatusPedido.ENTREGUE, pedido.getStatus());
	}

	@Test
	void avancarStatus_devePermitirCancelamentoDeAguardandoPagamento() {
		pedido.avancarStatus(StatusPedido.CANCELADO);
		assertEquals(StatusPedido.CANCELADO, pedido.getStatus());
	}

	@Test
	void avancarStatus_devePermitirCancelamentoDeRecebido() {
		pedido.avancarStatus(StatusPedido.RECEBIDO);
		pedido.avancarStatus(StatusPedido.CANCELADO);
		assertEquals(StatusPedido.CANCELADO, pedido.getStatus());
	}

	@Test
	void avancarStatus_deveLancarExcecaoParaTransicaoInvalida() {
		TransicaoStatusInvalidaException ex = assertThrows(
			TransicaoStatusInvalidaException.class,
			() -> pedido.avancarStatus(StatusPedido.EM_PREPARACAO)
		);
		assertEquals(409, ex.getStatusCode());
	}

	@Test
	void avancarStatus_naoDevePermitirCancelamentoDeEmPreparacao() {
		pedido.avancarStatus(StatusPedido.RECEBIDO);
		pedido.avancarStatus(StatusPedido.EM_PREPARACAO);
		assertThrows(
			TransicaoStatusInvalidaException.class,
			() -> pedido.avancarStatus(StatusPedido.CANCELADO)
		);
	}

	@Test
	void avancarStatus_naoDevePermitirTransicaoAposEntregue() {
		pedido.avancarStatus(StatusPedido.RECEBIDO);
		pedido.avancarStatus(StatusPedido.EM_PREPARACAO);
		pedido.avancarStatus(StatusPedido.PRONTO);
		pedido.avancarStatus(StatusPedido.ENTREGUE);
		assertThrows(
			TransicaoStatusInvalidaException.class,
			() -> pedido.avancarStatus(StatusPedido.RECEBIDO)
		);
	}

	@Test
	void avancarStatus_naoDevePermitirTransicaoAposCancelado() {
		pedido.avancarStatus(StatusPedido.CANCELADO);
		assertThrows(
			TransicaoStatusInvalidaException.class,
			() -> pedido.avancarStatus(StatusPedido.RECEBIDO)
		);
	}
}
