package com.raizesdonordeste.api.infrastructure.gateway;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.raizesdonordeste.api.domain.entity.Pedido;
import com.raizesdonordeste.api.domain.enums.FormaPagamento;

@Primary
@Service
public class PagarMeGateway implements PagamentoGateway {

	private final RestTemplate restTemplate;
	private final String apiUrl;
	private final String secretKey;

	public PagarMeGateway(
			@Value("${pagarme.api.url}") String apiUrl,
			@Value("${pagarme.api.secret-key}") String secretKey) {
		this.apiUrl = apiUrl;
		this.secretKey = secretKey;
		this.restTemplate = new RestTemplate();
	}

	@Override
	public PagamentoGatewayResponse processar(Pedido pedido, FormaPagamento formaPagamento) {
		Map<String, Object> request = buildPayload(pedido, formaPagamento);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.setBasicAuth(secretKey, "");
		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

		try {
			ResponseEntity<Map> response = restTemplate.exchange(
				apiUrl, HttpMethod.POST, entity, Map.class
			);

			if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
				String status = (String) response.getBody().get("status");
				String id = (String) response.getBody().get("id");

				if ("paid".equalsIgnoreCase(status) || "pending".equalsIgnoreCase(status)) {
					return new PagamentoGatewayResponse(true, id, null);
				} else {
					return new PagamentoGatewayResponse(false, null, "Transação não aprovada pelo gateway.");
				}
			}

			return new PagamentoGatewayResponse(false, null, "Erro na comunicação com o Pagar.me.");
		} catch (HttpClientErrorException e) {
			return new PagamentoGatewayResponse(false, null, "Recusado: " + e.getResponseBodyAsString());
		} catch (Exception e) {
			throw new RuntimeException("Falha ao comunicar com Pagar.me: " + e.getMessage(), e);
		}
	}

	private Map<String, Object> buildPayload(Pedido pedido, FormaPagamento formaPagamento) {
		Map<String, Object> payload = new HashMap<>();

		Map<String, Object> customer = new HashMap<>();
		customer.put("name", pedido.getCliente().getNome() != null ? pedido.getCliente().getNome() : "Cliente Raizes");
		customer.put("email", pedido.getCliente().getEmail());
		customer.put("type", "individual");
		customer.put("document", "00000000000"); // Mock doc for sandbox
		payload.put("customer", customer);

		int amountInCents = pedido.getValorTotal().multiply(new BigDecimal("100")).intValue();
		Map<String, Object> item = new HashMap<>();
		item.put("amount", amountInCents);
		item.put("description", "Pedido " + pedido.getId() + " - Raízes do Nordeste");
		item.put("quantity", 1);
		payload.put("items", List.of(item));

		Map<String, Object> payment = new HashMap<>();
		if (formaPagamento == FormaPagamento.PIX) {
			payment.put("payment_method", "pix");
			Map<String, Object> pix = new HashMap<>();
			pix.put("expires_in", 3600);
			payment.put("pix", pix);
		} else {
			// fallback para CC em ambiente de teste usando cartão de teste genérico
			payment.put("payment_method", "credit_card");
			Map<String, Object> cc = new HashMap<>();
			cc.put("installments", 1);
			cc.put("statement_descriptor", "RAIZES");
			Map<String, Object> card = new HashMap<>();
			card.put("number", "4111111111111111");
			card.put("holder_name", pedido.getCliente().getNome());
			card.put("exp_month", 12);
			card.put("exp_year", 2030);
			card.put("cvv", "123");
			cc.put("card", card);
			payment.put("credit_card", cc);
		}
		payload.put("payments", List.of(payment));

		return payload;
	}
}
