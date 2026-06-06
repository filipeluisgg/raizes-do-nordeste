package com.raizesdonordeste.api.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raizesdonordeste.api.domain.entity.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
}
