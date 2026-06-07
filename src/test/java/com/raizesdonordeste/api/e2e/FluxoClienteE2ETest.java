package com.raizesdonordeste.api.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import com.raizesdonordeste.api.domain.entity.EstoqueUnidade;
import com.raizesdonordeste.api.domain.entity.Produto;
import com.raizesdonordeste.api.domain.entity.Unidade;
import com.raizesdonordeste.api.domain.entity.Usuario;
import com.raizesdonordeste.api.domain.enums.CanalPedido;
import com.raizesdonordeste.api.domain.enums.FormaPagamento;
import com.raizesdonordeste.api.domain.enums.Role;
import com.raizesdonordeste.api.infrastructure.gateway.PagamentoGateway;
import com.raizesdonordeste.api.infrastructure.gateway.PagamentoGateway.PagamentoGatewayResponse;
import com.raizesdonordeste.api.infrastructure.repository.AuditoriaLogRepository;
import com.raizesdonordeste.api.infrastructure.repository.EstoqueUnidadeRepository;
import com.raizesdonordeste.api.infrastructure.repository.FidelidadeConsentimentoRepository;
import com.raizesdonordeste.api.infrastructure.repository.PagamentoRepository;
import com.raizesdonordeste.api.infrastructure.repository.PedidoRepository;
import com.raizesdonordeste.api.infrastructure.repository.PontoFidelidadeRepository;
import com.raizesdonordeste.api.infrastructure.repository.ProdutoRepository;
import com.raizesdonordeste.api.infrastructure.repository.UnidadeRepository;
import com.raizesdonordeste.api.infrastructure.repository.UsuarioRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FluxoClienteE2ETest {

	@LocalServerPort
	private int port;

	private RestTemplate restTemplate = new RestTemplate(new JdkClientHttpRequestFactory());

	@Autowired private UsuarioRepository usuarioRepository;
	@Autowired private UnidadeRepository unidadeRepository;
	@Autowired private ProdutoRepository produtoRepository;
	@Autowired private EstoqueUnidadeRepository estoqueUnidadeRepository;
	@Autowired private PedidoRepository pedidoRepository;
	@Autowired private PagamentoRepository pagamentoRepository;
	@Autowired private PontoFidelidadeRepository pontoFidelidadeRepository;
	@Autowired private FidelidadeConsentimentoRepository fidelidadeConsentimentoRepository;
	@Autowired private AuditoriaLogRepository auditoriaLogRepository;
	@Autowired private PasswordEncoder passwordEncoder;
	@Autowired private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

	@MockitoBean
	private PagamentoGateway pagamentoGateway;

	private Unidade unidade;
	private Produto produto;

	@BeforeEach
	void setUp() {
		jdbcTemplate.execute("TRUNCATE TABLE auditoria_log, ponto_fidelidade, fidelidade_consentimento, pagamento, item_pedido, pedido, estoque_unidade, usuario, produto, unidade RESTART IDENTITY CASCADE");

		unidade = new Unidade("Loja E2E Cliente", "Rua Y", "Recife", "PE", true);
		unidade = unidadeRepository.save(unidade);

		produto = new Produto("Baião de Dois", "Prato feito", new BigDecimal("35.00"), "Prato Principal");
		produto = produtoRepository.save(produto);

		EstoqueUnidade estoque = new EstoqueUnidade(unidade, produto, 50, 5);
		estoqueUnidadeRepository.save(estoque);

		Usuario cliente = new Usuario("Cliente Jornada", "jornada@e2e.com", passwordEncoder.encode("123"), Role.CLIENTE);
		usuarioRepository.save(cliente);
	}

	@Test
	void deveExecutarJornadaCompletaDoCliente_Login_Ate_GanharPontosFidelidade() {
		// 1. LOGIN
		Map<String, String> loginBody = Map.of("email", "jornada@e2e.com", "senha", "123");
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		ResponseEntity<Map<String, Object>> loginResp = restTemplate.exchange(
			"http://localhost:" + port + "/auth/login",
			HttpMethod.POST,
			new HttpEntity<>(loginBody, headers),
			new ParameterizedTypeReference<Map<String, Object>>() {}
		);
		assertEquals(HttpStatus.OK, loginResp.getStatusCode());
		String token = "Bearer " + loginResp.getBody().get("token");

		headers.set("Authorization", token);

		// 2. DAR CONSENTIMENTO DE FIDELIDADE (LGPD)
		Map<String, Object> consentBody = Map.of("consentimento", true, "versaoTermo", "v1.0");
		ResponseEntity<Map<String, Object>> consentResp = restTemplate.exchange(
			"http://localhost:" + port + "/fidelidade/consentimento",
			HttpMethod.PUT,
			new HttpEntity<>(consentBody, headers),
			new ParameterizedTypeReference<Map<String, Object>>() {}
		);
		assertEquals(HttpStatus.OK, consentResp.getStatusCode());

		// 3. CRIAR PEDIDO
		Map<String, Object> pedidoBody = Map.of(
			"unidadeId", unidade.getId(),
			"canalPedido", CanalPedido.APP.name(),
			"itens", List.of(Map.of("produtoId", produto.getId(), "quantidade", 2)) // 2 * 35.00 = 70.00
		);
		ResponseEntity<Map<String, Object>> pedidoResp = restTemplate.exchange(
			"http://localhost:" + port + "/pedidos",
			HttpMethod.POST,
			new HttpEntity<>(pedidoBody, headers),
			new ParameterizedTypeReference<Map<String, Object>>() {}
		);
		assertEquals(HttpStatus.CREATED, pedidoResp.getStatusCode());
		Number pedidoIdNum = (Number) pedidoResp.getBody().get("pedidoId");
		Long pedidoId = pedidoIdNum.longValue();
		assertEquals("AGUARDANDO_PAGAMENTO", pedidoResp.getBody().get("status"));

		// 4. MOCK DO PAGAMENTO E EFETUAR PAGAMENTO
		when(pagamentoGateway.processar(any(), any()))
			.thenReturn(new PagamentoGatewayResponse(true, "MOCK-TRANS-123", null));

		Map<String, Object> pagBody = Map.of("formaPagamento", FormaPagamento.CARTAO_CREDITO.name());
		ResponseEntity<Map<String, Object>> pagResp = restTemplate.exchange(
			"http://localhost:" + port + "/pagamentos/" + pedidoId,
			HttpMethod.POST,
			new HttpEntity<>(pagBody, headers),
			new ParameterizedTypeReference<Map<String, Object>>() {}
		);
		assertEquals(HttpStatus.OK, pagResp.getStatusCode());
		assertEquals("APROVADO", pagResp.getBody().get("status"));

		// 5. CONSULTAR EXTRATO DE FIDELIDADE E GARANTIR QUE RECEBEU OS PONTOS
		ResponseEntity<Map<String, Object>> extratoResp = restTemplate.exchange(
			"http://localhost:" + port + "/fidelidade/extrato",
			HttpMethod.GET,
			new HttpEntity<>(headers),
			new ParameterizedTypeReference<Map<String, Object>>() {}
		);
		
		assertEquals(HttpStatus.OK, extratoResp.getStatusCode());
		Number saldo = (Number) extratoResp.getBody().get("saldoAtual");
		// 70.00 de pedido resulta em 70 pontos (na regra do FidelidadeService, é 1 ponto por real)
		assertEquals(70, saldo.intValue());
	}
}
