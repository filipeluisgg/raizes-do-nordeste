package com.raizesdonordeste.api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.raizesdonordeste.api.domain.entity.AuditoriaLog;
import com.raizesdonordeste.api.service.AuditoriaService;

@RestController
@RequestMapping("/auditoria")
public class AuditoriaController {

	private final AuditoriaService auditoriaService;

	public AuditoriaController(AuditoriaService auditoriaService) {
		this.auditoriaService = auditoriaService;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('read:auditoria')")
	public ResponseEntity<List<Map<String, Object>>> listarLogs() {
		List<AuditoriaLog> logs = auditoriaService.listar();

		List<Map<String, Object>> response = logs.stream()
			.map(this::mapLogResponse)
			.toList();

		return ResponseEntity.ok(response);
	}

	private Map<String, Object> mapLogResponse(AuditoriaLog log) {
		java.util.Map<String, Object> map = new java.util.HashMap<>();
		map.put("id", log.getId());
		map.put("usuarioId", log.getUsuario().getId());
		map.put("acao", log.getAcao());
		map.put("entidade", log.getEntidade());
		map.put("entidadeId", log.getEntidadeId());
		map.put("dadosAntes", log.getDadosAntes());
		map.put("dadosDepois", log.getDadosDepois());
		map.put("motivo", log.getMotivo());
		map.put("criadoEm", log.getCriadoEm().toString());
		return map;
	}
}
