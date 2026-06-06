package com.raizesdonordeste.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

import com.raizesdonordeste.api.domain.entity.FidelidadeConsentimento;
import com.raizesdonordeste.api.domain.entity.Pedido;
import com.raizesdonordeste.api.domain.entity.PontoFidelidade;
import com.raizesdonordeste.api.domain.entity.Unidade;
import com.raizesdonordeste.api.domain.entity.Usuario;
import com.raizesdonordeste.api.domain.enums.CanalPedido;
import com.raizesdonordeste.api.domain.enums.Role;
import com.raizesdonordeste.api.domain.exception.RecursoNaoEncontradoException;
import com.raizesdonordeste.api.infrastructure.repository.FidelidadeConsentimentoRepository;
import com.raizesdonordeste.api.infrastructure.repository.PontoFidelidadeRepository;
import com.raizesdonordeste.api.infrastructure.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class FidelidadeServiceTest {

	@Mock
	private PontoFidelidadeRepository pontoRepository;
	@Mock
	private FidelidadeConsentimentoRepository consentimentoRepository;
	@Mock
	private UsuarioRepository usuarioRepository;

	@InjectMocks
	private FidelidadeService fidelidadeService;

	private Usuario clienteMock;
	private Pedido pedidoMock;

	@BeforeEach
	void setUp() {
		clienteMock = new Usuario("Maria", "maria@email.com", "hash", Role.CLIENTE);
		setId(clienteMock, 1L);
		Unidade unidade = new Unidade("Loja 1", "Rua A", "Recife", "PE", true);
		pedidoMock = new Pedido(clienteMock, unidade, CanalPedido.APP);
		setId(pedidoMock, 10L);
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
	class CreditarPontos {

		@Test
		void naoDeveCreditarQuandoClienteNaoTemConsentimento() {
			when(consentimentoRepository.findConsentimentoAtual(1L)).thenReturn(null);

			fidelidadeService.creditarPontos(pedidoMock);

			verify(pontoRepository, never()).save(any());
		}

		@Test
		void naoDeveCreditarQuandoConsentimentoFalso() {
			FidelidadeConsentimento consentimento = new FidelidadeConsentimento(
				clienteMock, false, "127.0.0.1", "1.0"
			);
			when(consentimentoRepository.findConsentimentoAtual(1L)).thenReturn(consentimento);

			fidelidadeService.creditarPontos(pedidoMock);

			verify(pontoRepository, never()).save(any());
		}

		@Test
		void naoDeveCreditarQuandoPontosJaForamCreditadosParaPedido() {
			FidelidadeConsentimento consentimento = new FidelidadeConsentimento(
				clienteMock, true, "127.0.0.1", "1.0"
			);
			when(consentimentoRepository.findConsentimentoAtual(1L)).thenReturn(consentimento);
			when(pontoRepository.existsByPedidoId(10L)).thenReturn(true);

			fidelidadeService.creditarPontos(pedidoMock);

			verify(pontoRepository, never()).save(any());
		}

		@Test
		void naoDeveCreditarQuandoValorTotalZero() {
			FidelidadeConsentimento consentimento = new FidelidadeConsentimento(
				clienteMock, true, "127.0.0.1", "1.0"
			);
			when(consentimentoRepository.findConsentimentoAtual(1L)).thenReturn(consentimento);
			when(pontoRepository.existsByPedidoId(10L)).thenReturn(false);

			fidelidadeService.creditarPontos(pedidoMock);

			verify(pontoRepository, never()).save(any());
		}

		@Test
		void deveCreditarPontosQuandoTodosOsPreRequisitosAtendidos() {
			FidelidadeConsentimento consentimento = new FidelidadeConsentimento(
				clienteMock, true, "127.0.0.1", "1.0"
			);
			when(consentimentoRepository.findConsentimentoAtual(1L)).thenReturn(consentimento);
			when(pontoRepository.existsByPedidoId(10L)).thenReturn(false);

			try {
				var valorField = Pedido.class.getDeclaredField("valorTotal");
				valorField.setAccessible(true);
				valorField.set(pedidoMock, new BigDecimal("150.00"));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			fidelidadeService.creditarPontos(pedidoMock);

			verify(pontoRepository).save(any(PontoFidelidade.class));
		}
	}

	@Nested
	class PossuiConsentimentoAtivo {

		@Test
		void deveRetornarFalseQuandoNenhumConsentimentoExiste() {
			when(consentimentoRepository.findConsentimentoAtual(1L)).thenReturn(null);
			assertFalse(fidelidadeService.possuiConsentimentoAtivo(1L));
		}

		@Test
		void deveRetornarFalseQuandoUltimoConsentimentoFalso() {
			FidelidadeConsentimento consentimento = new FidelidadeConsentimento(
				clienteMock, false, "127.0.0.1", "1.0"
			);
			when(consentimentoRepository.findConsentimentoAtual(1L)).thenReturn(consentimento);
			assertFalse(fidelidadeService.possuiConsentimentoAtivo(1L));
		}

		@Test
		void deveRetornarTrueQuandoUltimoConsentimentoVerdadeiro() {
			FidelidadeConsentimento consentimento = new FidelidadeConsentimento(
				clienteMock, true, "127.0.0.1", "1.0"
			);
			when(consentimentoRepository.findConsentimentoAtual(1L)).thenReturn(consentimento);
			assertTrue(fidelidadeService.possuiConsentimentoAtivo(1L));
		}
	}

	@Nested
	class AtualizarConsentimento {

		@Test
		void deveLancarExcecaoQuandoClienteNaoExiste() {
			when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

			RecursoNaoEncontradoException ex = assertThrows(
				RecursoNaoEncontradoException.class,
				() -> fidelidadeService.atualizarConsentimento(99L, true, "2.0", "127.0.0.1")
			);
			assertEquals(404, ex.getStatusCode());
		}

		@Test
		void deveSalvarNovoRegistroDeConsentimento() {
			when(usuarioRepository.findById(1L)).thenReturn(Optional.of(clienteMock));
			when(consentimentoRepository.save(any(FidelidadeConsentimento.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

			FidelidadeConsentimento resultado = fidelidadeService.atualizarConsentimento(
				1L, false, "2.0", "192.168.1.1"
			);

			assertNotNull(resultado);
			assertFalse(resultado.getConsentimento());
			verify(consentimentoRepository).save(any(FidelidadeConsentimento.class));
		}
	}
}
