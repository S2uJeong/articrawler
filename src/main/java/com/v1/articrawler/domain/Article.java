package com.v1.articrawler.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A collected news article. Only metadata is stored (no full body) - title, summary, image,
 * press, category and the original URL users are redirected to.
 */
@Entity
@Table(
    name = "article",
    indexes = {
      @Index(name = "idx_article_link_hash", columnList = "link_hash", unique = true),
      @Index(name = "idx_article_normalized_title", columnList = "normalized_title"),
      @Index(name = "idx_article_published_at", columnList = "published_at"),
      @Index(name = "idx_article_category", columnList = "category")
    })
@Getter
@Setter
@NoArgsConstructor
public class Article {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 500)
  private String title;

  @Column(name = "normalized_title", nullable = false, length = 500)
  private String normalizedTitle;

  @Column(name = "summary", length = 2000)
  private String summary;

  @Column(name = "image_url", length = 1000)
  private String imageUrl;

  @Column(name = "source_name", length = 200)
  private String sourceName;

  @Column(name = "link", nullable = false, length = 1000)
  private String link;

  @Column(name = "link_hash", nullable = false, length = 64)
  private String linkHash;

  @Column(name = "category", length = 100)
  private String category;

  @Enumerated(EnumType.STRING)
  @Column(name = "collection_source_type", length = 20)
  private SourceType collectionSourceType;

  @Column(name = "collection_source_name", length = 100)
  private String collectionSourceName;

  @Column(name = "published_at")
  private LocalDateTime publishedAt;

  @Column(name = "collected_at", nullable = false)
  private LocalDateTime collectedAt = LocalDateTime.now();

  @Column(name = "view_count", nullable = false)
  private long viewCount = 0;
}
