package com.raizesdonordeste.api.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.raizesdonordeste.api.domain.entity.Pagamento;
import com.raizesdonordeste.api.dto.request.ProcessarPagamentoRequest;
import com.raizesdonordeste.api.service.PagamentoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoController {

	private final PagamentoService pagamentoService;

	public PagamentoController(PagamentoService pagamentoService) {
		this.pagamentoService = pagamentoService;
	}

	@PostMapping("/{pedidoId}")
	@PreAuthorize("hasAuthority('create:pedido')")
	public ResponseEntity<Map<String, Object>> processarPagamento(
			@PathVariable Long pedidoId,
			@Valid @RequestBody ProcessarPagamentoRequest request) {

		Pagamento pagamento = pagamentoService.processarPagamento(pedidoId, request.formaPagamento());

		Map<String, Object> response = new java.util.HashMap<>();
		response.put("transacaoId", pagamento.getTransacaoExternaId());
		response.put("status", pagamento.getStatus().name());
		response.put("formaPagamento", pagamento.getFormaPagamento().name());
		response.put("valorPago", pagamento.getValorPago());
		if (pagamento.getMotivoRecusa() != null) {
			response.put("motivoRecusa", pagamento.getMotivoRecusa());
		}

		return ResponseEntity.ok(response);
	}
}
