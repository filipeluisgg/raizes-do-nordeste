package com.raizesdonordeste.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.raizesdonordeste.api.domain.entity.Usuario;
import com.raizesdonordeste.api.domain.enums.Role;
import com.raizesdonordeste.api.domain.exception.NegocioException;
import com.raizesdonordeste.api.infrastructure.repository.FidelidadeConsentimentoRepository;
import com.raizesdonordeste.api.infrastructure.repository.UsuarioRepository;
import com.raizesdonordeste.api.infrastructure.security.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private FidelidadeConsentimentoRepository consentimentoRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@InjectMocks
	private AuthService authService;

	private Usuario usuarioMock;

	@BeforeEach
	void setUp() {
		usuarioMock = new Usuario("Maria", "maria@email.com", "$2a$hash", Role.CLIENTE);
		setId(usuarioMock, 1L);
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
	class Registrar {

		@Test
		void deveRegistrarUsuarioComSucesso_CaminhoFeliz() {
			when(usuarioRepository.existsByEmail("maria@email.com")).thenReturn(false);
			when(passwordEncoder.encode("senha123")).thenReturn("$2a$hash");
			when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
				Usuario u = invocation.getArgument(0);
				setId(u, 1L);
				return u;
			});

			Usuario resultado = authService.registrar(
				"Maria", "maria@email.com", "senha123", "81999999999", true, "127.0.0.1"
			);

			assertNotNull(resultado);
			assertEquals("Maria", resultado.getNome());
			assertEquals(Role.CLIENTE, resultado.getRole());
			verify(consentimentoRepository).save(any());
		}

		@Test
		void deveLancarExcecaoQuandoEmailJaExiste_CaminhoTriste() {
			when(usuarioRepository.existsByEmail("maria@email.com")).thenReturn(true);

			NegocioException ex = assertThrows(
				NegocioException.class,
				() -> authService.registrar("Maria", "maria@email.com", "senha123", "81999999999", true, "127.0.0.1")
			);

			assertEquals(409, ex.getStatusCode());
			assertEquals("ConflictError", ex.getName());
			verify(usuarioRepository, never()).save(any());
		}

		@Test
		void deveSempreRegistrarConsentimentoLGPD_CaminhoFeliz() {
			when(usuarioRepository.existsByEmail("joao@email.com")).thenReturn(false);
			when(passwordEncoder.encode(anyString())).thenReturn("hash");
			when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
				Usuario u = invocation.getArgument(0);
				setId(u, 2L);
				return u;
			});

			authService.registrar("João", "joao@email.com", "senha", null, false, "192.168.1.1");

			verify(consentimentoRepository).save(any());
		}
	}

	@Nested
	class Login {

		@Test
		void deveRetornarTokenQuandoCredenciaisValidas_CaminhoFeliz() {
			when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(usuarioMock));
			when(passwordEncoder.matches("senha123", "$2a$hash")).thenReturn(true);
			when(jwtTokenProvider.gerarToken(usuarioMock)).thenReturn("jwt-token-aqui");

			String token = authService.login("maria@email.com", "senha123");

			assertEquals("jwt-token-aqui", token);
		}

		@Test
		void deveLancarExcecaoQuandoUsuarioNaoExiste_CaminhoTriste() {
			when(usuarioRepository.findByEmail("inexistente@email.com")).thenReturn(Optional.empty());

			NegocioException ex = assertThrows(
				NegocioException.class,
				() -> authService.login("inexistente@email.com", "senha123")
			);

			assertEquals(401, ex.getStatusCode());
			assertEquals("UnauthorizedError", ex.getName());
		}

		@Test
		void deveLancarExcecaoQuandoUsuarioInativo_CaminhoTriste() {
			usuarioMock.setAtivo(false);
			when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(usuarioMock));

			NegocioException ex = assertThrows(
				NegocioException.class,
				() -> authService.login("maria@email.com", "senha123")
			);

			assertEquals(401, ex.getStatusCode());
		}

		@Test
		void deveLancarExcecaoQuandoSenhaInvalida_CaminhoTriste() {
			when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(usuarioMock));
			when(passwordEncoder.matches("senhaErrada", "$2a$hash")).thenReturn(false);

			NegocioException ex = assertThrows(
				NegocioException.class,
				() -> authService.login("maria@email.com", "senhaErrada")
			);

			assertEquals(401, ex.getStatusCode());
			assertEquals("UnauthorizedError", ex.getName());
		}
	}
}
