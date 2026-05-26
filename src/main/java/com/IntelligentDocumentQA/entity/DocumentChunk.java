package com.IntelligentDocumentQA.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "document_chunks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentChunk {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "document_id", nullable = false)
	private Document document;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String chunkText;

	@JdbcTypeCode(SqlTypes.VECTOR)
	@Column(nullable = false, columnDefinition = "vector(768)")
	private float[] embedding;

	@Column(nullable = false)
	private Integer chunkIndex;

	@Column
	private Integer pageNumber;

	@Column
	private Integer pageStart;

	@Column
	private Integer pageEnd;
}
