package com.raizesdonordeste.api.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.raizesdonordeste.api.domain.entity.Usuario;
import com.raizesdonordeste.api.domain.enums.Role;

import io.jsonwebtoken.Claims;

class JwtTokenProviderTest {

	private JwtTokenProvider jwtTokenProvider;

	@BeforeEach
	void setUp() {
		jwtTokenProvider = new JwtTokenProvider(
			"test-secret-key-that-is-at-least-32-characters-long",
			3600000
		);
	}

	private Usuario criarUsuario(Long id, String email, Role role) {
		Usuario usuario = new Usuario("Teste", email, "hash", role);
		try {
			var field = Usuario.class.getDeclaredField("id");
			field.setAccessible(true);
			field.set(usuario, id);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return usuario;
	}

	@Test
	void gerarToken_deveGerarTokenValido() {
		Usuario usuario = criarUsuario(1L, "teste@email.com", Role.CLIENTE);
		String token = jwtTokenProvider.gerarToken(usuario);

		assertNotNull(token);
		assertTrue(jwtTokenProvider.validarToken(token));
	}

	@Test
	void extrairClaims_deveConterDadosCorretos() {
		Usuario usuario = criarUsuario(1L, "teste@email.com", Role.CLIENTE);
		String token = jwtTokenProvider.gerarToken(usuario);

		Claims claims = jwtTokenProvider.extrairClaims(token);

		assertEquals("1", claims.getSubject());
		assertEquals("teste@email.com", claims.get("email"));
		assertEquals("CLIENTE", claims.get("role"));
		assertNotNull(claims.get("features"));
	}

	@Test
	void extrairClaims_deveConterFeaturesDaRole() {
		Usuario usuario = criarUsuario(1L, "gerente@email.com", Role.GERENTE);
		String token = jwtTokenProvider.gerarToken(usuario);

		Claims claims = jwtTokenProvider.extrairClaims(token);
		String features = claims.get("features", String.class);

		assertTrue(features.contains("cancel:pedido"));
		assertTrue(features.contains("manage:estoque"));
		assertTrue(features.contains("read:auditoria"));
	}

	@Test
	void validarToken_deveRetornarFalseParaTokenInvalido() {
		assertFalse(jwtTokenProvider.validarToken("token.invalido.aqui"));
	}

	@Test
	void validarToken_deveRetornarFalseParaNull() {
		assertFalse(jwtTokenProvider.validarToken(null));
	}

	@Test
	void extrairUsuarioId_deveRetornarIdCorreto() {
		Usuario usuario = criarUsuario(42L, "teste@email.com", Role.ADMIN);
		String token = jwtTokenProvider.gerarToken(usuario);

		assertEquals(42L, jwtTokenProvider.extrairUsuarioId(token));
	}
}
