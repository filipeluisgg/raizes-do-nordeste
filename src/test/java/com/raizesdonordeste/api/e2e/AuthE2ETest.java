package com.raizesdonordeste.api.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.raizesdonordeste.api.domain.entity.Usuario;
import com.raizesdonordeste.api.domain.enums.Role;
import com.raizesdonordeste.api.infrastructure.repository.UsuarioRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthE2ETest {

	@LocalServerPort
	private int port;

	private RestTemplate restTemplate = new RestTemplate();

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

	@BeforeEach
	void setUp() {
		jdbcTemplate.execute("TRUNCATE TABLE auditoria_log, ponto_fidelidade, fidelidade_consentimento, pagamento, item_pedido, pedido, estoque_unidade, usuario, produto, unidade RESTART IDENTITY CASCADE");

		// Criando usuário de teste para o login
		Usuario cliente = new Usuario("Login E2E", "login@e2e.com", passwordEncoder.encode("senhaForte123"), Role.CLIENTE);
		usuarioRepository.save(cliente);
	}

	@Test
	void deveRealizarLoginComSucesso_CaminhoFeliz_RetornaTokenJWT_E_200() {
		Map<String, String> requestBody = Map.of(
			"email", "login@e2e.com",
			"senha", "senhaForte123"
		);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
		String url = "http://localhost:" + port + "/auth/login";

		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
			url,
			HttpMethod.POST,
			request,
			new ParameterizedTypeReference<Map<String, Object>>() {}
		);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().containsKey("token"));
		assertNotNull(response.getBody().get("token"));
	}

	@Test
	void deveFalharLogin_CaminhoTriste_SenhaIncorreta_Retorna401() {
		Map<String, String> requestBody = Map.of(
			"email", "login@e2e.com",
			"senha", "senhaErrada123"
		);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
		String url = "http://localhost:" + port + "/auth/login";

		try {
			restTemplate.exchange(
				url,
				HttpMethod.POST,
				request,
				new ParameterizedTypeReference<Map<String, Object>>() {}
			);
			assertTrue(false, "Deveria lançar exceção de Unauthorized");
		} catch (HttpClientErrorException.Unauthorized ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			// Spring Security default auth fail
		}
	}

	@Test
	void deveFalharLogin_CaminhoTriste_UsuarioInexistente_Retorna401() {
		Map<String, String> requestBody = Map.of(
			"email", "inexistente@e2e.com",
			"senha", "senha123"
		);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
		String url = "http://localhost:" + port + "/auth/login";

		try {
			restTemplate.exchange(
				url,
				HttpMethod.POST,
				request,
				new ParameterizedTypeReference<Map<String, Object>>() {}
			);
			assertTrue(false, "Deveria lançar exceção de Unauthorized");
		} catch (HttpClientErrorException.Unauthorized ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
		}
	}
}
