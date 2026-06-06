package com.raizesdonordeste.api.infrastructure.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.raizesdonordeste.api.domain.entity.Pedido;
import com.raizesdonordeste.api.domain.entity.Unidade;
import com.raizesdonordeste.api.domain.entity.Usuario;
import com.raizesdonordeste.api.domain.enums.CanalPedido;
import com.raizesdonordeste.api.domain.enums.FormaPagamento;
import com.raizesdonordeste.api.domain.enums.Role;
import com.raizesdonordeste.api.infrastructure.gateway.PagamentoGateway.PagamentoGatewayResponse;

@ExtendWith(MockitoExtension.class)
class PagarMeGatewayTest {

	@Mock
	private RestTemplate restTemplate;

	private PagarMeGateway gateway;

	private Pedido pedidoMock;

	@BeforeEach
	void setUp() {
		// Inicializamos injetando o mock do RestTemplate criado manualmente para evitar erros de context
		gateway = new PagarMeGateway("https://api.pagar.me/core/v5/orders", "sk_test_mock");

		try {
			var field = PagarMeGateway.class.getDeclaredField("restTemplate");
			field.setAccessible(true);
			field.set(gateway, restTemplate);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		Usuario cliente = new Usuario("Maria", "maria@email.com", "hash", Role.CLIENTE);
		Unidade unidade = new Unidade("Loja", "Rua A", "Recife", "PE", true);
		pedidoMock = new Pedido(cliente, unidade, CanalPedido.APP);

		try {
			var idField = Pedido.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(pedidoMock, 1L);

			var valorField = Pedido.class.getDeclaredField("valorTotal");
			valorField.setAccessible(true);
			valorField.set(pedidoMock, new BigDecimal("100.00"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	void processar_deveAprovarPagamento() {
		Map<String, Object> body = new HashMap<>();
		body.put("id", "or_123456");
		body.put("status", "paid");
		ResponseEntity<Map> responseEntity = new ResponseEntity<>(body, HttpStatus.OK);

		when(restTemplate.exchange(eq("https://api.pagar.me/core/v5/orders"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
			.thenReturn(responseEntity);

		PagamentoGatewayResponse response = gateway.processar(pedidoMock, FormaPagamento.PIX);

		assertTrue(response.aprovado());
		assertEquals("or_123456", response.transacaoId());
		assertNull(response.motivoRecusa());
	}

	@Test
	@SuppressWarnings("unchecked")
	void processar_deveRecusarQuandoStatusForFailed() {
		Map<String, Object> body = new HashMap<>();
		body.put("id", "or_123456");
		body.put("status", "failed");
		ResponseEntity<Map> responseEntity = new ResponseEntity<>(body, HttpStatus.OK);

		when(restTemplate.exchange(eq("https://api.pagar.me/core/v5/orders"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
			.thenReturn(responseEntity);

		PagamentoGatewayResponse response = gateway.processar(pedidoMock, FormaPagamento.CARTAO_CREDITO);

		assertFalse(response.aprovado());
		assertEquals("Transação não aprovada pelo gateway.", response.motivoRecusa());
		assertNull(response.transacaoId());
	}

	@Test
	@SuppressWarnings("unchecked")
	void processar_deveLidarComErro400() {
		when(restTemplate.exchange(eq("https://api.pagar.me/core/v5/orders"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
			.thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request", "{\"message\":\"Invalid parameter\"}".getBytes(), null));

		PagamentoGatewayResponse response = gateway.processar(pedidoMock, FormaPagamento.CARTAO_DEBITO);

		assertFalse(response.aprovado());
		assertTrue(response.motivoRecusa().contains("Recusado"));
	}
}
