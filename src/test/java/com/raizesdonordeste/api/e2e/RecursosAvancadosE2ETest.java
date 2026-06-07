package com.raizesdonordeste.api.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.raizesdonordeste.api.domain.entity.CampanhaFidelidade;
import com.raizesdonordeste.api.domain.entity.EstoqueUnidade;
import com.raizesdonordeste.api.domain.entity.FidelidadeConsentimento;
import com.raizesdonordeste.api.domain.entity.Produto;
import com.raizesdonordeste.api.domain.entity.Unidade;
import com.raizesdonordeste.api.domain.entity.Usuario;
import com.raizesdonordeste.api.domain.enums.CanalPedido;
import com.raizesdonordeste.api.domain.enums.Role;
import com.raizesdonordeste.api.dto.RelatorioVendasDTO;
import com.raizesdonordeste.api.dto.request.CriarPedidoRequest;
import com.raizesdonordeste.api.infrastructure.repository.CampanhaFidelidadeRepository;
import com.raizesdonordeste.api.infrastructure.repository.EstoqueUnidadeRepository;
import com.raizesdonordeste.api.infrastructure.repository.FidelidadeConsentimentoRepository;
import com.raizesdonordeste.api.infrastructure.repository.ProdutoRepository;
import com.raizesdonordeste.api.infrastructure.repository.UnidadeRepository;
import com.raizesdonordeste.api.infrastructure.repository.UsuarioRepository;
import com.raizesdonordeste.api.infrastructure.security.JwtTokenProvider;
import com.raizesdonordeste.api.service.PedidoService;
import com.raizesdonordeste.api.service.PedidoService.ItemRequest;
import com.raizesdonordeste.api.service.FidelidadeService;
import com.raizesdonordeste.api.dto.ResgatePontosRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RecursosAvancadosE2ETest {

	@LocalServerPort
	private int port;

	private RestTemplate restTemplate = new RestTemplate();

	@Autowired private JdbcTemplate jdbcTemplate;
	@Autowired private UsuarioRepository usuarioRepository;
	@Autowired private UnidadeRepository unidadeRepository;
	@Autowired private ProdutoRepository produtoRepository;
	@Autowired private EstoqueUnidadeRepository estoqueUnidadeRepository;
	@Autowired private CampanhaFidelidadeRepository campanhaRepository;
	@Autowired private FidelidadeConsentimentoRepository consentimentoRepository;
	@Autowired private JwtTokenProvider jwtTokenProvider;
	@Autowired private PasswordEncoder passwordEncoder;
	@Autowired private PedidoService pedidoService;
	@Autowired private FidelidadeService fidelidadeService;

	private String tokenCliente;
	private String tokenGerente;
	private Unidade unidade;
	private Produto produtoAtivo;
	private Produto produtoSazonal;
	private Usuario cliente;
	private Usuario gerente;

	@BeforeEach
	void setUp() {
		jdbcTemplate.execute("TRUNCATE TABLE auditoria_log, ponto_fidelidade, fidelidade_consentimento, pagamento, item_pedido, pedido, estoque_unidade, usuario, produto, unidade, campanha_fidelidade RESTART IDENTITY CASCADE");

		unidade = new Unidade("Loja E2E", "Rua Teste", "Recife", "PE", true);
		unidade = unidadeRepository.save(unidade);

		produtoAtivo = new Produto("Bolo E2E", "Bolo Regional", new BigDecimal("10.00"), "Sobremesas");
		produtoAtivo = produtoRepository.save(produtoAtivo);

		produtoSazonal = new Produto("Pamonha Junina", "Pamonha de milho", new BigDecimal("15.00"), "Sazonal");
		produtoSazonal.setSazonal(true);
		// Disponível só no futuro, então não deve permitir hoje
		produtoSazonal.setDisponivelDe(LocalDate.now().plusDays(10));
		produtoSazonal.setDisponivelAte(LocalDate.now().plusDays(30));
		produtoSazonal = produtoRepository.save(produtoSazonal);

		estoqueUnidadeRepository.save(new EstoqueUnidade(unidade, produtoAtivo, 100, 10));
		estoqueUnidadeRepository.save(new EstoqueUnidade(unidade, produtoSazonal, 50, 5));

		cliente = new Usuario("Cliente Teste", "cliente@teste.com", passwordEncoder.encode("123"), Role.CLIENTE);
		cliente = usuarioRepository.save(cliente);

		gerente = new Usuario("Gerente Teste", "gerente@teste.com", passwordEncoder.encode("123"), Role.GERENTE);
		gerente = usuarioRepository.save(gerente);

		// Consentimento para pontos
		FidelidadeConsentimento consentimento = new FidelidadeConsentimento(cliente, true, "127.0.0.1", "1.0");
		consentimentoRepository.save(consentimento);

		tokenCliente = jwtTokenProvider.gerarToken(cliente);
		tokenGerente = jwtTokenProvider.gerarToken(gerente);
	}

	private String url(String path) {
		return "http://localhost:" + port + path;
	}

	private HttpHeaders headers(String token) {
		HttpHeaders h = new HttpHeaders();
		h.setBearerAuth(token);
		return h;
	}

	@Test
	void deveBloquearProdutoSazonalForaDaJanela() {
		CriarPedidoRequest request = new CriarPedidoRequest(
				unidade.getId(),
				CanalPedido.APP,
				"Sem cebola",
				List.of(new CriarPedidoRequest.ItemRequest(produtoSazonal.getId(), 1))
		);

		HttpEntity<CriarPedidoRequest> entity = new HttpEntity<>(request, headers(tokenCliente));

		try {
			restTemplate.exchange(url("/pedidos"), HttpMethod.POST, entity, Map.class);
		} catch (HttpClientErrorException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertThat(e.getResponseBodyAsString()).contains("ProdutoForaDeTemporada");
		}
	}

	@Test
	void deveMultiplicarPontosPorCampanhaDeFidelidade() {
		// Criar campanha de 2.5x para a unidade atual
		CampanhaFidelidade campanha = new CampanhaFidelidade("Campanha de Feriado", new BigDecimal("2.50"), 
			LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(2), unidade);
		campanhaRepository.save(campanha);

		CriarPedidoRequest request = new CriarPedidoRequest(
				unidade.getId(),
				CanalPedido.APP,
				"",
				List.of(new CriarPedidoRequest.ItemRequest(produtoAtivo.getId(), 2)) // 20.00 reais
		);

		HttpEntity<CriarPedidoRequest> entity = new HttpEntity<>(request, headers(tokenCliente));
		ResponseEntity<Map> response = restTemplate.exchange(url("/pedidos"), HttpMethod.POST, entity, Map.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		Long pedidoId = ((Number) response.getBody().get("pedidoId")).longValue();

		// Creditar pontos manualmente (simulando pagamento aprovado)
		com.raizesdonordeste.api.domain.entity.Pedido p = pedidoService.buscarPorId(pedidoId);
		fidelidadeService.creditarPontos(p);

		int saldo = fidelidadeService.calcularSaldo(cliente.getId());
		// Total: 20 reais. Com multiplicador 2.5 => 50 pontos
		assertThat(saldo).isEqualTo(50);
	}

	@Test
	void deveResgatarPontosComSucesso() {
		// Criar pedido 1 e dar pontos
		var p1 = pedidoService.criarPedido(cliente.getId(), unidade.getId(), CanalPedido.APP, "", 
			List.of(new ItemRequest(produtoAtivo.getId(), 10))); // 100 reais
		fidelidadeService.creditarPontos(p1); // Ganha 100 pontos

		// Agora criar um segundo pedido para usar desconto
		var pedidoPraDesconto = pedidoService.criarPedido(cliente.getId(), unidade.getId(), CanalPedido.APP, "", 
			List.of(new ItemRequest(produtoAtivo.getId(), 2))); // 20 reais

		ResgatePontosRequest resgateReq = new ResgatePontosRequest();
		resgateReq.setPedidoId(pedidoPraDesconto.getId());
		resgateReq.setPontos(50); // Desconto de 5 reais (cada 10 pts = 1 real)

		HttpEntity<ResgatePontosRequest> entity = new HttpEntity<>(resgateReq, headers(tokenCliente));
		ResponseEntity<Map> response = restTemplate.exchange(url("/fidelidade/resgate"), HttpMethod.POST, entity, Map.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().get("mensagem").toString()).contains("Resgate efetuado com sucesso");

		// Verifica saldo (tinha 100 - 50 = 50)
		int saldo = fidelidadeService.calcularSaldo(cliente.getId());
		assertThat(saldo).isEqualTo(50);
	}

	@Test
	void deveGerarRelatorioVendas() {
		// Criar dois pedidos
		var p1 = pedidoService.criarPedido(cliente.getId(), unidade.getId(), CanalPedido.APP, "", 
			List.of(new ItemRequest(produtoAtivo.getId(), 1))); // 10 reais
		
		// Avançar corretamente
		pedidoService.atualizarStatus(p1.getId(), com.raizesdonordeste.api.domain.enums.StatusPedido.RECEBIDO);
		pedidoService.atualizarStatus(p1.getId(), com.raizesdonordeste.api.domain.enums.StatusPedido.EM_PREPARACAO);
		pedidoService.atualizarStatus(p1.getId(), com.raizesdonordeste.api.domain.enums.StatusPedido.PRONTO);
		pedidoService.atualizarStatus(p1.getId(), com.raizesdonordeste.api.domain.enums.StatusPedido.ENTREGUE);

		var p2 = pedidoService.criarPedido(cliente.getId(), unidade.getId(), CanalPedido.BALCAO, "", 
			List.of(new ItemRequest(produtoAtivo.getId(), 2))); // 20 reais
		pedidoService.atualizarStatus(p2.getId(), com.raizesdonordeste.api.domain.enums.StatusPedido.RECEBIDO);
		pedidoService.atualizarStatus(p2.getId(), com.raizesdonordeste.api.domain.enums.StatusPedido.EM_PREPARACAO);
		pedidoService.atualizarStatus(p2.getId(), com.raizesdonordeste.api.domain.enums.StatusPedido.PRONTO);
		pedidoService.atualizarStatus(p2.getId(), com.raizesdonordeste.api.domain.enums.StatusPedido.ENTREGUE);

		HttpEntity<Void> entity = new HttpEntity<>(headers(tokenGerente)); // Gerente lê relatório (read:auditoria)
		ResponseEntity<RelatorioVendasDTO> response = restTemplate.exchange(
			url("/relatorios/vendas?unidadeId=" + unidade.getId() + "&periodoDias=7"), 
			HttpMethod.GET, entity, RelatorioVendasDTO.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		RelatorioVendasDTO relatorio = response.getBody();
		assertThat(relatorio).isNotNull();
		assertThat(relatorio.getTotalPedidos()).isEqualTo(2);
		assertThat(relatorio.getValorTotal()).isEqualByComparingTo(new BigDecimal("30.00"));
	}

}
