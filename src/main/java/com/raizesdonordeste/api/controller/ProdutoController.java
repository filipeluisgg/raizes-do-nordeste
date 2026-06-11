package com.raizesdonordeste.api.controller;

import java.net.URI;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.raizesdonordeste.api.domain.entity.Produto;
import com.raizesdonordeste.api.dto.request.CriarProdutoRequest;
import com.raizesdonordeste.api.service.ProdutoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

	private final ProdutoService produtoService;

	public ProdutoController(ProdutoService produtoService) {
		this.produtoService = produtoService;
	}

	@PostMapping
	@PreAuthorize("hasAuthority('manage:produto')")
	public ResponseEntity<Map<String, Object>> criarProduto(@Valid @RequestBody CriarProdutoRequest request) {
		Produto produto = produtoService.criarProduto(request);

		URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
			.path("/{id}")
			.buildAndExpand(produto.getId())
			.toUri();

		Map<String, Object> response = Map.of(
			"id", produto.getId(),
			"nome", produto.getNome(),
			"preco", produto.getPreco(),
			"categoria", produto.getCategoria()
		);
		
		return ResponseEntity.created(uri).body(response);
	}
}
