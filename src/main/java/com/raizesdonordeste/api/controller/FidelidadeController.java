package com.raizesdonordeste.api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.raizesdonordeste.api.domain.entity.FidelidadeConsentimento;
import com.raizesdonordeste.api.domain.entity.PontoFidelidade;
import com.raizesdonordeste.api.dto.request.AtualizarConsentimentoRequest;
import com.raizesdonordeste.api.service.FidelidadeService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/fidelidade")
public class FidelidadeController {

	private final FidelidadeService fidelidadeService;

	public FidelidadeController(FidelidadeService fidelidadeService) {
		this.fidelidadeService = fidelidadeService;
	}

	@GetMapping("/extrato")
	@PreAuthorize("hasAuthority('read:fidelidade')")
	public ResponseEntity<Map<String, Object>> consultarExtrato(Authentication auth) {
		Long clienteId = (Long) auth.getPrincipal();

		int saldo = fidelidadeService.calcularSaldo(clienteId);
		List<PontoFidelidade> historico = fidelidadeService.listarHistorico(clienteId);

		List<Map<String, Object>> historicoResponse = historico.stream()
			.map(p -> Map.<String, Object>of(
				"pontos", p.getPontos(),
				"pedidoId", p.getPedido().getId(),
				"criadoEm", p.getCriadoEm().toString()
			))
			.toList();

		Map<String, Object> response = Map.of(
			"saldoAtual", saldo,
			"historico", historicoResponse
		);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/consentimento")
	@PreAuthorize("hasAuthority('manage:consentimento')")
	public ResponseEntity<Map<String, Object>> atualizarConsentimento(
			@Valid @RequestBody AtualizarConsentimentoRequest request,
			Authentication auth,
			HttpServletRequest httpRequest) {

		Long clienteId = (Long) auth.getPrincipal();
		FidelidadeConsentimento consentimento = fidelidadeService.atualizarConsentimento(
			clienteId, request.consentimento(), request.versaoTermo(), httpRequest.getRemoteAddr()
		);

		Map<String, Object> response = Map.of(
			"consentimento", consentimento.getConsentimento(),
			"registradoEm", consentimento.getCriadoEm().toString()
		);
		return ResponseEntity.ok(response);
	}
}
