package com.raizesdonordeste.api.controller;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.raizesdonordeste.api.dto.RelatorioVendasDTO;
import com.raizesdonordeste.api.service.RelatorioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/relatorios")
@Tag(name = "Relatórios", description = "Endpoints para relatórios gerenciais")
@SecurityRequirement(name = "bearer-jwt")
public class RelatorioController {

	private final RelatorioService relatorioService;

	public RelatorioController(RelatorioService relatorioService) {
		this.relatorioService = relatorioService;
	}

	@GetMapping("/vendas")
	@PreAuthorize("hasAuthority('read:auditoria')")
	@Operation(summary = "Gerar relatório de vendas por unidade")
	public ResponseEntity<RelatorioVendasDTO> gerarRelatorioVendas(
			@RequestParam Long unidadeId,
			@RequestParam(defaultValue = "30") Integer periodoDias) {
		
		LocalDateTime fim = LocalDateTime.now();
		LocalDateTime inicio = fim.minusDays(periodoDias);
		
		RelatorioVendasDTO relatorio = relatorioService.gerarRelatorioPorUnidade(unidadeId, inicio, fim);
		return ResponseEntity.ok(relatorio);
	}
}
