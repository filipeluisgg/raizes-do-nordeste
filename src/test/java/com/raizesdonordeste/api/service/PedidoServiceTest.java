package com.raizesdonordeste.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.raizesdonordeste.api.domain.entity.Pedido;
import com.raizesdonordeste.api.domain.entity.Produto;
import com.raizesdonordeste.api.domain.entity.Unidade;
import com.raizesdonordeste.api.domain.entity.Usuario;
import com.raizesdonordeste.api.domain.enums.CanalPedido;
import com.raizesdonordeste.api.domain.enums.Role;
import com.raizesdonordeste.api.domain.enums.StatusPedido;
import com.raizesdonordeste.api.domain.exception.EstoqueInsuficienteException;
import com.raizesdonordeste.api.domain.exception.NegocioException;
import com.raizesdonordeste.api.domain.exception.RecursoNaoEncontradoException;
import com.raizesdonordeste.api.domain.exception.TransicaoStatusInvalidaException;
import com.raizesdonordeste.api.infrastructure.repository.PedidoRepository;
import com.raizesdonordeste.api.infrastructure.repository.ProdutoRepository;
import com.raizesdonordeste.api.infrastructure.repository.UnidadeRepository;
import com.raizesdonordeste.api.infrastructure.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

	@Mock
	private PedidoRepository pedidoRepository;
	@Mock
	private UsuarioRepository usuarioRepository;
	@Mock
	private UnidadeRepository unidadeRepository;
	@Mock
	private ProdutoRepository produtoRepository;
	@Mock
	private EstoqueService estoqueService;

	@InjectMocks
	private PedidoService pedidoService;

	private Usuario cliente;
	private Unidade unidade;
	private Produto produto;

	@BeforeEach
	void setUp() {
		cliente = new Usuario("João", "joao@email.com", "hash", Role.CLIENTE);
		unidade = new Unidade("Loja Recife", "Rua A, 100", "Recife", "PE", true);
		produto = new Produto("Cuscuz", "Cuscuz com ovo", new BigDecimal("10.00"), "Comida");
		setId(cliente, 1L);
		setId(unidade, 1L);
		setId(produto, 1L);
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
	class CriarPedido {

		@Test
		void deveCriarPedidoComSucesso_CaminhoFeliz() {
			when(usuarioRepository.findById(1L)).thenReturn(Optional.of(cliente));
			when(unidadeRepository.findById(1L)).thenReturn(Optional.of(unidade));
			when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
			doNothing().when(estoqueService).validarEBaixarEstoque(anyLong(), anyLong(), anyInt());
			when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

			List<PedidoService.ItemRequest> itens = List.of(new PedidoService.ItemRequest(1L, 2));
			Pedido resultado = pedidoService.criarPedido(1L, 1L, CanalPedido.APP, "Sem pimenta", itens);

			assertEquals(StatusPedido.AGUARDANDO_PAGAMENTO, resultado.getStatus());
			assertEquals(CanalPedido.APP, resultado.getCanalPedido());
			assertEquals(1, resultado.getItens().size());
			verify(estoqueService).validarEBaixarEstoque(1L, 1L, 2);
		}

		@Test
		void deveLancarExcecaoQuandoClienteNaoExiste() {
			when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

			assertThrows(
				RecursoNaoEncontradoException.class,
				() -> pedidoService.criarPedido(99L, 1L, CanalPedido.APP, null, List.of())
			);
		}

		@Test
		void deveLancarExcecaoQuandoUnidadeInativa() {
			unidade.setAtiva(false);
			when(usuarioRepository.findById(1L)).thenReturn(Optional.of(cliente));
			when(unidadeRepository.findById(1L)).thenReturn(Optional.of(unidade));

			NegocioException ex = assertThrows(
				NegocioException.class,
				() -> pedidoService.criarPedido(1L, 1L, CanalPedido.TOTEM, null, List.of())
			);
			assertEquals(422, ex.getStatusCode());
		}

		@Test
		void deveLancarExcecaoQuandoListaDeItensVazia() {
			when(usuarioRepository.findById(1L)).thenReturn(Optional.of(cliente));
			when(unidadeRepository.findById(1L)).thenReturn(Optional.of(unidade));

			NegocioException ex = assertThrows(
				NegocioException.class,
				() -> pedidoService.criarPedido(1L, 1L, CanalPedido.WEB, null, Collections.emptyList())
			);
			assertEquals(400, ex.getStatusCode());
		}

		@Test
		void deveLancarExcecaoQuandoListaDeItensNula() {
			when(usuarioRepository.findById(1L)).thenReturn(Optional.of(cliente));
			when(unidadeRepository.findById(1L)).thenReturn(Optional.of(unidade));

			NegocioException ex = assertThrows(
				NegocioException.class,
				() -> pedidoService.criarPedido(1L, 1L, CanalPedido.BALCAO, null, null)
			);
			assertEquals(400, ex.getStatusCode());
		}

		@Test
		void deveLancarExcecaoQuandoProdutoInativo() {
			produto.setAtivo(false);
			when(usuarioRepository.findById(1L)).thenReturn(Optional.of(cliente));
			when(unidadeRepository.findById(1L)).thenReturn(Optional.of(unidade));
			when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

			List<PedidoService.ItemRequest> itens = List.of(new PedidoService.ItemRequest(1L, 1));

			assertThrows(
				RecursoNaoEncontradoException.class,
				() -> pedidoService.criarPedido(1L, 1L, CanalPedido.APP, null, itens)
			);
		}

		@Test
		void deveLancarExcecaoQuandoEstoqueInsuficiente() {
			when(usuarioRepository.findById(1L)).thenReturn(Optional.of(cliente));
			when(unidadeRepository.findById(1L)).thenReturn(Optional.of(unidade));
			when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
			doThrow(new EstoqueInsuficienteException("Cuscuz", 2, 10))
				.when(estoqueService).validarEBaixarEstoque(1L, 1L, 10);

			List<PedidoService.ItemRequest> itens = List.of(new PedidoService.ItemRequest(1L, 10));

			EstoqueInsuficienteException ex = assertThrows(
				EstoqueInsuficienteException.class,
				() -> pedidoService.criarPedido(1L, 1L, CanalPedido.APP, null, itens)
			);
			assertEquals(409, ex.getStatusCode());
		}
	}

	@Nested
	class AtualizarStatus {

		@Test
		void deveAvancarStatusComSucesso_CaminhoFeliz() {
			Pedido pedido = new Pedido(cliente, unidade, CanalPedido.APP);
			pedido.avancarStatus(StatusPedido.RECEBIDO);
			when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
			when(pedidoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

			Pedido resultado = pedidoService.atualizarStatus(1L, StatusPedido.EM_PREPARACAO);

			assertEquals(StatusPedido.EM_PREPARACAO, resultado.getStatus());
		}

		@Test
		void deveLancarExcecaoQuandoPedidoNaoExiste() {
			when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

			assertThrows(
				RecursoNaoEncontradoException.class,
				() -> pedidoService.atualizarStatus(99L, StatusPedido.RECEBIDO)
			);
		}

		@Test
		void deveLancarExcecaoQuandoTransicaoInvalida() {
			Pedido pedido = new Pedido(cliente, unidade, CanalPedido.APP);
			when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

			assertThrows(
				TransicaoStatusInvalidaException.class,
				() -> pedidoService.atualizarStatus(1L, StatusPedido.ENTREGUE)
			);
		}

		@Test
		void deveChamarEstornoDeEstoqueQuandoCancelado() {
			Pedido pedido = new Pedido(cliente, unidade, CanalPedido.APP);
			when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
			when(pedidoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

			Pedido resultado = pedidoService.atualizarStatus(1L, StatusPedido.CANCELADO);

			assertEquals(StatusPedido.CANCELADO, resultado.getStatus());
		}
	}

	@Nested
	class BuscarPorId {

		@Test
		void deveLancarExcecaoQuandoPedidoNaoExiste() {
			when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

			RecursoNaoEncontradoException ex = assertThrows(
				RecursoNaoEncontradoException.class,
				() -> pedidoService.buscarPorId(99L)
			);
			assertEquals(404, ex.getStatusCode());
		}
	}
}
