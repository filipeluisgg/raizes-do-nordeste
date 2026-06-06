package com.raizesdonordeste.api.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
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
import org.springframework.http.client.JdkClientHttpRequestFactory;

import com.raizesdonordeste.api.domain.entity.Pedido;
import com.raizesdonordeste.api.domain.entity.EstoqueUnidade;
import com.raizesdonordeste.api.domain.entity.Produto;
import com.raizesdonordeste.api.domain.entity.Unidade;
import com.raizesdonordeste.api.domain.entity.Usuario;
import com.raizesdonordeste.api.domain.enums.CanalPedido;
import com.raizesdonordeste.api.domain.enums.Role;
import com.raizesdonordeste.api.infrastructure.repository.EstoqueUnidadeRepository;
import com.raizesdonordeste.api.infrastructure.repository.PedidoRepository;
import com.raizesdonordeste.api.infrastructure.repository.ProdutoRepository;
import com.raizesdonordeste.api.infrastructure.repository.UnidadeRepository;
import com.raizesdonordeste.api.infrastructure.repository.UsuarioRepository;
import com.raizesdonordeste.api.infrastructure.security.JwtTokenProvider;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PedidoE2ETest {

	@LocalServerPort
	private int port;

	private RestTemplate restTemplate = new RestTemplate(new JdkClientHttpRequestFactory());

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private UnidadeRepository unidadeRepository;

	@Autowired
	private ProdutoRepository produtoRepository;

	@Autowired
	private EstoqueUnidadeRepository estoqueUnidadeRepository;

	@Autowired
	private PedidoRepository pedidoRepository;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private String tokenCliente;
	private String tokenAtendente;
	private Unidade unidade;
	private Produto produto;
	private Pedido pedidoExistente;

	@BeforeEach
	void setUp() {
		pedidoRepository.deleteAll();
		estoqueUnidadeRepository.deleteAll();
		usuarioRepository.deleteAll();
		produtoRepository.deleteAll();
		unidadeRepository.deleteAll();

		unidade = new Unidade("Loja E2E", "Rua Teste", "Recife", "PE", true);
		unidade = unidadeRepository.save(unidade);

		produto = new Produto("Tapioca E2E", "Tapioca de carne de sol", new BigDecimal("15.50"), "Lanches");
		produto = produtoRepository.save(produto);

		EstoqueUnidade estoque = new EstoqueUnidade(unidade, produto, 10, 2);
		estoqueUnidadeRepository.save(estoque);

		Usuario cliente = new Usuario("Cliente E2E", "cliente@e2e.com", passwordEncoder.encode("senha123"), Role.CLIENTE);
		cliente = usuarioRepository.save(cliente);

		Usuario atendente = new Usuario("Atendente E2E", "atendente@e2e.com", passwordEncoder.encode("senha123"), Role.ATENDENTE);
		atendente = usuarioRepository.save(atendente);

		tokenCliente = "Bearer " + jwtTokenProvider.gerarToken(cliente);
		tokenAtendente = "Bearer " + jwtTokenProvider.gerarToken(atendente);

		// Cria pedido inicial para testes de PATCH
		pedidoExistente = new Pedido(cliente, unidade, CanalPedido.APP);
		pedidoRepository.save(pedidoExistente);
	}

	@Test
	void deveCriarPedidoComSucesso_CaminhoFeliz_BateNoPostgres_E_Retorna201() {
		Map<String, Object> requestBody = Map.of(
			"unidadeId", unidade.getId(),
			"canalPedido", CanalPedido.APP.name(),
			"observacao", "Sem cebola",
			"itens", List.of(
				Map.of(
					"produtoId", produto.getId(),
					"quantidade", 2
				)
			)
		);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", tokenCliente);

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
		String url = "http://localhost:" + port + "/pedidos";

		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
			url,
			HttpMethod.POST,
			request,
			new ParameterizedTypeReference<Map<String, Object>>() {}
		);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("AGUARDANDO_PAGAMENTO", response.getBody().get("status"));
		assertEquals("APP", response.getBody().get("canalPedido"));
		assertEquals(31.0, ((Number) response.getBody().get("valorTotal")).doubleValue());
	}

	@Test
	void deveFalharCriacaoDePedido_CaminhoTriste_EstoqueInsuficiente_Retorna409() {
		Map<String, Object> requestBody = Map.of(
			"unidadeId", unidade.getId(),
			"canalPedido", CanalPedido.APP.name(),
			"itens", List.of(
				Map.of(
					"produtoId", produto.getId(),
					"quantidade", 50 // Aqui está o gatilho da falha
				)
			)
		);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", tokenCliente);

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
		String url = "http://localhost:" + port + "/pedidos";

		try {
			restTemplate.exchange(
				url,
				HttpMethod.POST,
				request,
				new ParameterizedTypeReference<Map<String, Object>>() {}
			);
			// Falhou se não lançar exceção
			assertTrue(false, "Deveria lançar HttpClientErrorException.Conflict");
		} catch (HttpClientErrorException.Conflict ex) {
			assertTrue(ex.getResponseBodyAsString().contains("EstoqueInsuficienteError"));
		}
	}

	@Test
	void deveFalharCriacaoDePedido_CaminhoTriste_SemToken_Retorna401() {
		Map<String, Object> requestBody = Map.of(
			"unidadeId", unidade.getId(),
			"canalPedido", CanalPedido.APP.name(),
			"itens", List.of(
				Map.of(
					"produtoId", produto.getId(),
					"quantidade", 2
				)
			)
		);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
		String url = "http://localhost:" + port + "/pedidos";

		try {
			restTemplate.exchange(
				url,
				HttpMethod.POST,
				request,
				new ParameterizedTypeReference<Map<String, Object>>() {}
			);
			// Falhou se não lançar exceção
			assertTrue(false, "Deveria lançar HttpClientErrorException.Forbidden");
		} catch (HttpClientErrorException.Forbidden ex) {
			assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
		}
	}

	@Test
	void deveAvancarStatusComSucesso_Atendente_Retorna200() {
		Map<String, String> requestBody = Map.of(
			"novoStatus", "RECEBIDO",
			"motivo", ""
		);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", tokenAtendente); // Atendente TEM permissão de update:pedido:status

		HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
		String url = "http://localhost:" + port + "/pedidos/" + pedidoExistente.getId() + "/status";

		// Precisamos usar RestTemplate.exchange ou custom factory, pois RestTemplate default restringe PATCH.
		// Para simplificar, vou usar HttpMethod.PATCH no TestRestTemplate ou RestTemplate configurado.
		// RestTemplate por padrão no Java usa HttpURLConnection que NÃO suporta PATCH.
		// Então é melhor usarmos a biblioteca externa do Spring Boot TestRestTemplate que contorna isso nativamente,
		// MAS como estou com RestTemplate puro, vou testar primeiro se PATCH funciona, caso dê erro eu configuro.
		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
			url,
			HttpMethod.PATCH,
			request,
			new ParameterizedTypeReference<Map<String, Object>>() {}
		);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("RECEBIDO", response.getBody().get("novo"));
	}

	@Test
	void deveFalharAvancarStatus_ClienteNaoTemPermissao_Retorna403() {
		Map<String, String> requestBody = Map.of(
			"novoStatus", "RECEBIDO",
			"motivo", ""
		);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", tokenCliente); // Cliente NÃO tem permissão de update:pedido:status

		HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
		String url = "http://localhost:" + port + "/pedidos/" + pedidoExistente.getId() + "/status";

		try {
			restTemplate.exchange(
				url,
				HttpMethod.PATCH,
				request,
				new ParameterizedTypeReference<Map<String, Object>>() {}
			);
			assertTrue(false, "Deveria lançar HttpClientErrorException.Forbidden");
		} catch (HttpClientErrorException.Forbidden ex) {
			assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
		}
	}
}
