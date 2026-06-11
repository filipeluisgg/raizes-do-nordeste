package com.raizesdonordeste.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raizesdonordeste.api.domain.entity.Produto;
import com.raizesdonordeste.api.dto.request.CriarProdutoRequest;
import com.raizesdonordeste.api.infrastructure.repository.ProdutoRepository;

@Service
public class ProdutoService {

	private final ProdutoRepository produtoRepository;

	public ProdutoService(ProdutoRepository produtoRepository) {
		this.produtoRepository = produtoRepository;
	}

	@Transactional
	public Produto criarProduto(CriarProdutoRequest request) {
		Produto produto = new Produto(
			request.nome(),
			request.descricao(),
			request.preco(),
			request.categoria()
		);
		
		produto.setSazonal(request.sazonal());
		if (Boolean.TRUE.equals(request.sazonal())) {
			produto.setDisponivelDe(request.disponivelDe());
			produto.setDisponivelAte(request.disponivelAte());
		}

		return produtoRepository.save(produto);
	}
}
