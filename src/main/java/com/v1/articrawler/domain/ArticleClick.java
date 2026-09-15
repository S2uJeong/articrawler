package com.v1.articrawler.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** One record of a user opening an article's original page - the raw signal behind popularity stats. */
@Entity
@Table(
    name = "article_click",
    indexes = {
      @Index(name = "idx_click_article_time", columnList = "article_id, clicked_at"),
      @Index(name = "idx_click_time", columnList = "clicked_at")
    })
@Getter
@Setter
@NoArgsConstructor
public class ArticleClick {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "article_id", nullable = false)
  private Article article;

  @Column(name = "clicked_at", nullable = false)
  private LocalDateTime clickedAt = LocalDateTime.now();

  public ArticleClick(Article article) {
    this.article = article;
  }
}
