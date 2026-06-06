package com.raizesdonordeste.api.domain.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "unidade")
public class Unidade {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String nome;

	@Column(nullable = false, length = 200)
	private String endereco;

	@Column(nullable = false, length = 100)
	private String cidade;

	@Column(nullable = false, length = 2)
	private String estado;

	@Column(name = "cozinha_completa", nullable = false)
	private Boolean cozinhaCompleta;

	@Column(nullable = false)
	private Boolean ativa = true;

	@CreationTimestamp
	@Column(name = "criado_em", nullable = false, updatable = false)
	private LocalDateTime criadoEm;

	protected Unidade() {
	}

	public Unidade(String nome, String endereco, String cidade, String estado, Boolean cozinhaCompleta) {
		this.nome = nome;
		this.endereco = endereco;
		this.cidade = cidade;
		this.estado = estado;
		this.cozinhaCompleta = cozinhaCompleta;
	}

	public Long getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEndereco() {
		return endereco;
	}

	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}

	public String getCidade() {
		return cidade;
	}

	public void setCidade(String cidade) {
		this.cidade = cidade;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public Boolean getCozinhaCompleta() {
		return cozinhaCompleta;
	}

	public void setCozinhaCompleta(Boolean cozinhaCompleta) {
		this.cozinhaCompleta = cozinhaCompleta;
	}

	public Boolean getAtiva() {
		return ativa;
	}

	public void setAtiva(Boolean ativa) {
		this.ativa = ativa;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Unidade unidade = (Unidade) o;
		return id != null && Objects.equals(id, unidade.id);
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : System.identityHashCode(this);
	}
}
