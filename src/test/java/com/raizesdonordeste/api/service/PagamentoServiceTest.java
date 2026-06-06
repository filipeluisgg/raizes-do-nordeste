package com.raizesdonordeste.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.raizesdonordeste.api.domain.entity.Pagamento;
import com.raizesdonordeste.api.domain.entity.Pedido;
import com.raizesdonordeste.api.domain.entity.Unidade;
import com.raizesdonordeste.api.domain.entity.Usuario;
import com.raizesdonordeste.api.domain.enums.CanalPedido;
import com.raizesdonordeste.api.domain.enums.FormaPagamento;
import com.raizesdonordeste.api.domain.enums.StatusPagamento;
import com.raizesdonordeste.api.domain.enums.StatusPedido;
import com.raizesdonordeste.api.domain.exception.NegocioException;
import com.raizesdonordeste.api.domain.exception.RecursoNaoEncontradoException;
import com.raizesdonordeste.api.infrastructure.gateway.PagamentoGateway;
import com.raizesdonordeste.api.infrastructure.gateway.PagamentoGateway.PagamentoGatewayResponse;
import com.raizesdonordeste.api.infrastructure.repository.PagamentoRepository;
import com.raizesdonordeste.api.infrastructure.repository.PedidoRepository;
import com.raizesdonordeste.api.domain.enums.Role;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

	@Mock
	private PagamentoRepository pagamentoRepository;
	@Mock
	private PedidoRepository pedidoRepository;
	@Mock
	private PagamentoGateway pagamentoGateway;
	@Mock
	private FidelidadeService fidelidadeService;

	@InjectMocks
	private PagamentoService pagamentoService;

	private Pedido pedidoMock;

	@BeforeEach
	void setUp() {
		Usuario cliente = new Usuario("João", "joao@email.com", "hash", Role.CLIENTE);
		Unidade unidade = new Unidade("Loja 1", "Rua A", "Recife", "PE", true);
		pedidoMock = new Pedido(cliente, unidade, CanalPedido.APP);
		setId(pedidoMock, 1L);
	}

	private void setId(Object obj, Long id) {
		try {
			var field = obj.getClass().getDeclaredField("id");
			field.setAccessible(true);
			field.set(obj, id);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Nested
	class ProcessarPagamento {

		@Test
		void deveLancarExcecaoQuandoPedidoNaoExiste() {
			when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

			RecursoNaoEncontradoException ex = assertThrows(
				RecursoNaoEncontradoException.class,
				() -> pagamentoService.processarPagamento(99L, FormaPagamento.PIX)
			);
			assertEquals(404, ex.getStatusCode());
		}

		@Test
		void deveLancarExcecaoQuandoPedidoNaoEstaAguardandoPagamento() {
			pedidoMock.avancarStatus(StatusPedido.RECEBIDO);
			when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoMock));

			NegocioException ex = assertThrows(
				NegocioException.class,
				() -> pagamentoService.processarPagamento(1L, FormaPagamento.PIX)
			);
			assertEquals(409, ex.getStatusCode());
			assertEquals("PagamentoInvalidoError", ex.getName());
		}

		@Test
		void deveLancarExcecaoQuandoJaExistePagamentoParaPedido() {
			when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoMock));
			when(pagamentoRepository.existsByPedidoId(1L)).thenReturn(true);

			NegocioException ex = assertThrows(
				NegocioException.class,
				() -> pagamentoService.processarPagamento(1L, FormaPagamento.CARTAO_CREDITO)
			);
			assertEquals(409, ex.getStatusCode());
			assertEquals("PagamentoDuplicadoError", ex.getName());
		}

		@Test
		void deveRegistrarPagamentoRecusadoQuandoGatewayFalha() {
			when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoMock));
			when(pagamentoRepository.existsByPedidoId(1L)).thenReturn(false);
			when(pagamentoGateway.processar(any(Pedido.class), any(FormaPagamento.class)))
				.thenThrow(new RuntimeException("Timeout de conexão"));
			when(pagamentoRepository.save(any(Pagamento.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

			Pagamento resultado = pagamentoService.processarPagamento(1L, FormaPagamento.PIX);

			assertNotNull(resultado);
			assertEquals(StatusPagamento.RECUSADO, resultado.getStatus());
			assertNotNull(resultado.getMotivoRecusa());
			verify(fidelidadeService, never()).creditarPontos(any());
		}

		@Test
		void deveAprovarPagamentoEAvancarStatusQuandoGatewayAprova() {
			when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoMock));
			when(pagamentoRepository.existsByPedidoId(1L)).thenReturn(false);
			when(pagamentoGateway.processar(any(Pedido.class), any(FormaPagamento.class)))
				.thenReturn(new PagamentoGatewayResponse(true, "txn-123", null));
			when(pagamentoRepository.save(any(Pagamento.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
			when(pedidoRepository.save(any(Pedido.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

			Pagamento resultado = pagamentoService.processarPagamento(1L, FormaPagamento.CARTAO_CREDITO);

			assertEquals(StatusPagamento.APROVADO, resultado.getStatus());
			assertEquals("txn-123", resultado.getTransacaoExternaId());
			assertEquals(StatusPedido.RECEBIDO, pedidoMock.getStatus());
			verify(fidelidadeService).creditarPontos(pedidoMock);
		}

		@Test
		void deveRegistrarRecusaQuandoGatewayRecusa() {
			when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoMock));
			when(pagamentoRepository.existsByPedidoId(1L)).thenReturn(false);
			when(pagamentoGateway.processar(any(Pedido.class), any(FormaPagamento.class)))
				.thenReturn(new PagamentoGatewayResponse(false, null, "Saldo insuficiente"));
			when(pagamentoRepository.save(any(Pagamento.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

			Pagamento resultado = pagamentoService.processarPagamento(1L, FormaPagamento.CARTAO_DEBITO);

			assertEquals(StatusPagamento.RECUSADO, resultado.getStatus());
			assertEquals("Saldo insuficiente", resultado.getMotivoRecusa());
			assertEquals(StatusPedido.AGUARDANDO_PAGAMENTO, pedidoMock.getStatus());
			verify(fidelidadeService, never()).creditarPontos(any());
		}
	}
}
