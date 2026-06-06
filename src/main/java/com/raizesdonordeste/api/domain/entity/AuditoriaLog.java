package com.raizesdonordeste.api.domain.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "auditoria_log")
public class AuditoriaLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_id")
	private Usuario usuario;

	@Column(nullable = false, length = 100)
	private String acao;

	@Column(nullable = false, length = 100)
	private String entidade;

	@Column(name = "entidade_id")
	private Long entidadeId;

	@Lob
	@Column(name = "dados_antes", columnDefinition = "TEXT")
	private String dadosAntes;

	@Lob
	@Column(name = "dados_depois", columnDefinition = "TEXT")
	private String dadosDepois;

	@Column(length = 500)
	private String motivo;

	@Column(name = "ip_origem", length = 45)
	private String ipOrigem;

	@CreationTimestamp
	@Column(name = "criado_em", nullable = false, updatable = false)
	private LocalDateTime criadoEm;

	protected AuditoriaLog() {
	}

	public AuditoriaLog(Usuario usuario, String acao, String entidade, Long entidadeId) {
		this.usuario = usuario;
		this.acao = acao;
		this.entidade = entidade;
		this.entidadeId = entidadeId;
	}

	public Long getId() {
		return id;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public String getAcao() {
		return acao;
	}

	public void setAcao(String acao) {
		this.acao = acao;
	}

	public String getEntidade() {
		return entidade;
	}

	public void setEntidade(String entidade) {
		this.entidade = entidade;
	}

	public Long getEntidadeId() {
		return entidadeId;
	}

	public void setEntidadeId(Long entidadeId) {
		this.entidadeId = entidadeId;
	}

	public String getDadosAntes() {
		return dadosAntes;
	}

	public void setDadosAntes(String dadosAntes) {
		this.dadosAntes = dadosAntes;
	}

	public String getDadosDepois() {
		return dadosDepois;
	}

	public void setDadosDepois(String dadosDepois) {
		this.dadosDepois = dadosDepois;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	public String getIpOrigem() {
		return ipOrigem;
	}

	public void setIpOrigem(String ipOrigem) {
		this.ipOrigem = ipOrigem;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AuditoriaLog that = (AuditoriaLog) o;
		return id != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : System.identityHashCode(this);
	}
}
