package com.v1.articrawler.dto;

import com.v1.articrawler.domain.Article;
import java.time.LocalDateTime;

public record ArticleResponse(
    Long id,
    String title,
    String summary,
    String imageUrl,
    String sourceName,
    String category,
    LocalDateTime publishedAt,
    LocalDateTime collectedAt,
    long viewCount,
    String link) {

  public static ArticleResponse from(Article a) {
    return new ArticleResponse(
        a.getId(),
        a.getTitle(),
        a.getSummary(),
        a.getImageUrl(),
        a.getSourceName(),
        a.getCategory(),
        a.getPublishedAt(),
        a.getCollectedAt(),
        a.getViewCount(),
        a.getLink());
  }
}
