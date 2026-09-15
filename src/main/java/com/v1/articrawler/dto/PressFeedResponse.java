package com.v1.articrawler.dto;

import com.v1.articrawler.domain.PressFeed;
import java.time.LocalDateTime;

public record PressFeedResponse(
    Long id,
    String name,
    String feedUrl,
    Long categoryId,
    String categoryName,
    boolean enabled,
    LocalDateTime lastCollectedAt) {

  public static PressFeedResponse from(PressFeed p) {
    return new PressFeedResponse(
        p.getId(),
        p.getName(),
        p.getFeedUrl(),
        p.getCategory() != null ? p.getCategory().getId() : null,
        p.getCategory() != null ? p.getCategory().getName() : null,
        p.isEnabled(),
        p.getLastCollectedAt());
  }
}
