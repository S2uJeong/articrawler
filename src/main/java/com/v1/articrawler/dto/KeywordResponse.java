package com.v1.articrawler.dto;

import com.v1.articrawler.domain.Keyword;
import java.time.LocalDateTime;

public record KeywordResponse(
    Long id,
    String keyword,
    Long categoryId,
    String categoryName,
    boolean enabled,
    LocalDateTime lastCollectedAt) {

  public static KeywordResponse from(Keyword k) {
    return new KeywordResponse(
        k.getId(),
        k.getKeyword(),
        k.getCategory() != null ? k.getCategory().getId() : null,
        k.getCategory() != null ? k.getCategory().getName() : null,
        k.isEnabled(),
        k.getLastCollectedAt());
  }
}
