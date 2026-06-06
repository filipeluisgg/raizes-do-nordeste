package com.raizesdonordeste.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

import com.raizesdonordeste.api.domain.entity.EstoqueUnidade;
import com.raizesdonordeste.api.domain.entity.Produto;
import com.raizesdonordeste.api.domain.entity.Unidade;
import com.raizesdonordeste.api.domain.exception.EstoqueInsuficienteException;
import com.raizesdonordeste.api.domain.exception.RecursoNaoEncontradoException;
import com.raizesdonordeste.api.infrastructure.repository.EstoqueUnidadeRepository;

@ExtendWith(MockitoExtension.class)
class EstoqueServiceTest {

	@Mock
	private EstoqueUnidadeRepository estoqueRepository;

	@InjectMocks
	private EstoqueService estoqueService;

	private EstoqueUnidade estoqueMock;

	@BeforeEach
	void setUp() {
		Unidade unidade = new Unidade("Loja 1", "Rua A", "Recife", "PE", true);
		Produto produto = new Produto("Acarajé", "Acarajé baiano", new BigDecimal("15.90"), "Salgados");
		estoqueMock = new EstoqueUnidade(unidade, produto, 10, 3);
	}

	@Nested
	class ValidarEBaixarEstoque {

		@Test
		void deveLancarExcecaoQuandoEstoqueNaoEncontrado() {
			when(estoqueRepository.findByUnidadeIdAndProdutoId(1L, 1L)).thenReturn(Optional.empty());

			RecursoNaoEncontradoException ex = assertThrows(
				RecursoNaoEncontradoException.class,
				() -> estoqueService.validarEBaixarEstoque(1L, 1L, 5)
			);
			assertEquals(404, ex.getStatusCode());
		}

		@Test
		void deveLancarExcecaoQuandoQuantidadeInsuficiente() {
			when(estoqueRepository.findByUnidadeIdAndProdutoId(1L, 1L)).thenReturn(Optional.of(estoqueMock));

			assertThrows(
				EstoqueInsuficienteException.class,
				() -> estoqueService.validarEBaixarEstoque(1L, 1L, 15)
			);
		}

		@Test
		void deveBaixarEstoqueComSucesso() {
			when(estoqueRepository.findByUnidadeIdAndProdutoId(1L, 1L)).thenReturn(Optional.of(estoqueMock));
			when(estoqueRepository.save(any(EstoqueUnidade.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

			estoqueService.validarEBaixarEstoque(1L, 1L, 4);

			assertEquals(6, estoqueMock.getQuantidade());
			verify(estoqueRepository).save(estoqueMock);
		}
	}

	@Nested
	class EstornarEstoque {

		@Test
		void deveLancarExcecaoQuandoEstoqueNaoEncontrado() {
			when(estoqueRepository.findByUnidadeIdAndProdutoId(1L, 1L)).thenReturn(Optional.empty());

			assertThrows(
				RecursoNaoEncontradoException.class,
				() -> estoqueService.estornarEstoque(1L, 1L, 5)
			);
		}

		@Test
		void deveReporEstoqueComSucesso() {
			when(estoqueRepository.findByUnidadeIdAndProdutoId(1L, 1L)).thenReturn(Optional.of(estoqueMock));
			when(estoqueRepository.save(any(EstoqueUnidade.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

			estoqueService.estornarEstoque(1L, 1L, 5);

			assertEquals(15, estoqueMock.getQuantidade());
			verify(estoqueRepository).save(estoqueMock);
		}
	}

	@Nested
	class AjustarEstoque {

		@Test
		void deveLancarExcecaoQuandoEstoqueNaoEncontrado() {
			when(estoqueRepository.findByUnidadeIdAndProdutoId(1L, 1L)).thenReturn(Optional.empty());

			assertThrows(
				RecursoNaoEncontradoException.class,
				() -> estoqueService.ajustarEstoque(1L, 1L, 20)
			);
		}

		@Test
		void deveAjustarQuantidadeComSucesso() {
			when(estoqueRepository.findByUnidadeIdAndProdutoId(1L, 1L)).thenReturn(Optional.of(estoqueMock));
			when(estoqueRepository.save(any(EstoqueUnidade.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

			EstoqueUnidade resultado = estoqueService.ajustarEstoque(1L, 1L, 20);

			assertEquals(20, resultado.getQuantidade());
		}
	}
}
