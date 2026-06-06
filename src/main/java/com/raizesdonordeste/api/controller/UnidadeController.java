package com.raizesdonordeste.api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.raizesdonordeste.api.domain.entity.EstoqueUnidade;
import com.raizesdonordeste.api.dto.request.AjustarEstoqueRequest;
import com.raizesdonordeste.api.service.AuditoriaService;
import com.raizesdonordeste.api.service.EstoqueService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/unidades")
public class UnidadeController {

	private final EstoqueService estoqueService;
	private final AuditoriaService auditoriaService;

	public UnidadeController(EstoqueService estoqueService, AuditoriaService auditoriaService) {
		this.estoqueService = estoqueService;
		this.auditoriaService = auditoriaService;
	}

	@GetMapping("/{unidadeId}/cardapio")
	@PreAuthorize("hasAuthority('read:cardapio')")
	public ResponseEntity<List<Map<String, Object>>> listarCardapio(@PathVariable Long unidadeId) {
		List<EstoqueUnidade> estoques = estoqueService.listarCardapio(unidadeId);

		List<Map<String, Object>> response = estoques.stream()
			.map(e -> Map.<String, Object>of(
				"produtoId", e.getProduto().getId(),
				"nome", e.getProduto().getNome(),
				"descricao", e.getProduto().getDescricao() != null ? e.getProduto().getDescricao() : "",
				"preco", e.getProduto().getPreco(),
				"categoria", e.getProduto().getCategoria(),
				"quantidade", e.getQuantidade()
			))
			.toList();

		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{unidadeId}/estoque/{produtoId}")
	@PreAuthorize("hasAuthority('manage:estoque')")
	public ResponseEntity<Map<String, Object>> ajustarEstoque(
			@PathVariable Long unidadeId,
			@PathVariable Long produtoId,
			@Valid @RequestBody AjustarEstoqueRequest request,
			Authentication auth,
			HttpServletRequest httpRequest) {

		EstoqueUnidade estoque = estoqueService.listarCardapio(unidadeId).stream()
			.filter(e -> e.getProduto().getId().equals(produtoId))
			.findFirst()
			.orElse(null);

		int anterior = estoque != null ? estoque.getQuantidade() : 0;

		EstoqueUnidade atualizado = estoqueService.ajustarEstoque(unidadeId, produtoId, request.quantidade());

		auditoriaService.registrar(
			(Long) auth.getPrincipal(), "AJUSTE_ESTOQUE", "EstoqueUnidade", atualizado.getId(),
			"{\"quantidade\": " + anterior + "}",
			"{\"quantidade\": " + request.quantidade() + "}",
			request.motivo(),
			httpRequest.getRemoteAddr()
		);

		Map<String, Object> response = Map.of(
			"produtoId", produtoId,
			"anterior", anterior,
			"nova", atualizado.getQuantidade()
		);
		return ResponseEntity.ok(response);
	}
}
