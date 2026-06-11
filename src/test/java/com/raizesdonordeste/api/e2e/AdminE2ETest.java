package com.raizesdonordeste.api.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.JdkClientHttpRequestFactory;

import com.raizesdonordeste.api.domain.entity.Usuario;
import com.raizesdonordeste.api.domain.enums.Role;
import com.raizesdonordeste.api.infrastructure.repository.UsuarioRepository;
import com.raizesdonordeste.api.infrastructure.security.JwtTokenProvider;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AdminE2ETest {

	@LocalServerPort
	private int port;

	private RestTemplate restTemplate = new RestTemplate(new JdkClientHttpRequestFactory());

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

	private String tokenAdmin;

	@BeforeEach
	void setUp() {
		jdbcTemplate.execute("TRUNCATE TABLE auditoria_log, ponto_fidelidade, fidelidade_consentimento, pagamento, item_pedido, pedido, estoque_unidade, usuario, produto, unidade RESTART IDENTITY CASCADE");

		Usuario admin = new Usuario("Admin E2E", "admin@e2e.com", passwordEncoder.encode("senha123"), Role.ADMIN);
		admin = usuarioRepository.save(admin);

		String jwt = jwtTokenProvider.gerarToken(admin);
		tokenAdmin = "Bearer " + jwt;
	}

	@Test
	void deveCriarUnidadeComSucesso_Retorna201() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", tokenAdmin);

		Map<String, Object> body = Map.of(
			"nome", "Nova Unidade E2E",
			"endereco", "Rua do Admin, 123",
			"cidade", "Fortaleza",
			"estado", "CE",
			"cozinhaCompleta", true
		);

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
		String url = "http://localhost:" + port + "/unidades";

		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
			url,
			HttpMethod.POST,
			request,
			new ParameterizedTypeReference<Map<String, Object>>() {}
		);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody().get("id"));
		assertEquals("Nova Unidade E2E", response.getBody().get("nome"));
	}

	@Test
	void deveCriarProdutoComSucesso_Retorna201() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", tokenAdmin);

		Map<String, Object> body = Map.of(
			"nome", "Bolo de Rolo E2E",
			"descricao", "Bolo de rolo tradicional",
			"preco", 25.50,
			"categoria", "Sobremesas",
			"sazonal", false
		);

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
		String url = "http://localhost:" + port + "/produtos";

		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
			url,
			HttpMethod.POST,
			request,
			new ParameterizedTypeReference<Map<String, Object>>() {}
		);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody().get("id"));
		assertEquals("Bolo de Rolo E2E", response.getBody().get("nome"));
	}
}
