package com.raizesdonordeste.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.raizesdonordeste.api.domain.entity.AuditoriaLog;
import com.raizesdonordeste.api.domain.entity.Usuario;
import com.raizesdonordeste.api.domain.enums.Role;
import com.raizesdonordeste.api.domain.exception.RecursoNaoEncontradoException;
import com.raizesdonordeste.api.infrastructure.repository.AuditoriaLogRepository;
import com.raizesdonordeste.api.infrastructure.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

	@Mock
	private AuditoriaLogRepository auditoriaRepository;
	@Mock
	private UsuarioRepository usuarioRepository;

	@InjectMocks
	private AuditoriaService auditoriaService;

	private Usuario usuarioMock;

	@BeforeEach
	void setUp() {
		usuarioMock = new Usuario("Admin", "admin@email.com", "hash", Role.ADMIN);
	}

	@Nested
	class Registrar {

		@Test
		void deveLancarExcecaoQuandoUsuarioNaoExiste() {
			when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

			assertThrows(
				RecursoNaoEncontradoException.class,
				() -> auditoriaService.registrar(99L, "CANCELAMENTO", "Pedido", 1L, null, null, null, null)
			);
		}

		@Test
		void deveRegistrarLogComSucesso() {
			when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));

			auditoriaService.registrar(
				1L, "AJUSTE_ESTOQUE", "EstoqueUnidade", 5L,
				"{\"quantidade\": 10}", "{\"quantidade\": 20}",
				"Reposição semanal", "192.168.1.1"
			);

			verify(auditoriaRepository).save(any(AuditoriaLog.class));
		}
	}

	@Nested
	class Listar {

		@Test
		void deveRetornarListaOrdenadaPorData() {
			when(auditoriaRepository.findAllByOrderByCriadoEmDesc()).thenReturn(List.of());

			List<AuditoriaLog> resultado = auditoriaService.listar();

			assertEquals(0, resultado.size());
			verify(auditoriaRepository).findAllByOrderByCriadoEmDesc();
		}
	}

	@Nested
	class ListarPorEntidade {

		@Test
		void deveFiltrarPorEntidadeEId() {
			when(auditoriaRepository.findByEntidadeAndEntidadeId("Pedido", 1L)).thenReturn(List.of());

			List<AuditoriaLog> resultado = auditoriaService.listarPorEntidade("Pedido", 1L);

			assertEquals(0, resultado.size());
			verify(auditoriaRepository).findByEntidadeAndEntidadeId("Pedido", 1L);
		}
	}
}
