package com.raizesdonordeste.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raizesdonordeste.api.domain.entity.Unidade;
import com.raizesdonordeste.api.dto.request.CriarUnidadeRequest;
import com.raizesdonordeste.api.infrastructure.repository.UnidadeRepository;

@Service
public class UnidadeService {

	private final UnidadeRepository unidadeRepository;

	public UnidadeService(UnidadeRepository unidadeRepository) {
		this.unidadeRepository = unidadeRepository;
	}

	@Transactional
	public Unidade criarUnidade(CriarUnidadeRequest request) {
		Unidade unidade = new Unidade(
			request.nome(),
			request.endereco(),
			request.cidade(),
			request.estado(),
			request.cozinhaCompleta()
		);
		return unidadeRepository.save(unidade);
	}
}
