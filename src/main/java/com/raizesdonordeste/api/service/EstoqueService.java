package com.raizesdonordeste.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raizesdonordeste.api.domain.entity.EstoqueUnidade;
import com.raizesdonordeste.api.domain.exception.RecursoNaoEncontradoException;
import com.raizesdonordeste.api.infrastructure.repository.EstoqueUnidadeRepository;

@Service
public class EstoqueService {

	private final EstoqueUnidadeRepository estoqueRepository;

	public EstoqueService(EstoqueUnidadeRepository estoqueRepository) {
		this.estoqueRepository = estoqueRepository;
	}

	@Transactional
	public void validarEBaixarEstoque(Long unidadeId, Long produtoId, int quantidade) {
		EstoqueUnidade estoque = estoqueRepository.findByUnidadeIdAndProdutoId(unidadeId, produtoId)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Estoque", produtoId));

		estoque.baixar(quantidade);
		estoqueRepository.save(estoque);
	}

	@Transactional
	public void estornarEstoque(Long unidadeId, Long produtoId, int quantidade) {
		EstoqueUnidade estoque = estoqueRepository.findByUnidadeIdAndProdutoId(unidadeId, produtoId)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Estoque", produtoId));

		estoque.repor(quantidade);
		estoqueRepository.save(estoque);
	}

	@Transactional
	public EstoqueUnidade ajustarEstoque(Long unidadeId, Long produtoId, int novaQuantidade) {
		EstoqueUnidade estoque = estoqueRepository.findByUnidadeIdAndProdutoId(unidadeId, produtoId)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Estoque", produtoId));

		estoque.setQuantidade(novaQuantidade);
		return estoqueRepository.save(estoque);
	}

	public List<EstoqueUnidade> listarCardapio(Long unidadeId) {
		return estoqueRepository.findByUnidadeIdAndQuantidadeGreaterThan(unidadeId, 0);
	}
}
