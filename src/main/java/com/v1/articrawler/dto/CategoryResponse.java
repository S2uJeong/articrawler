package com.v1.articrawler.dto;

import com.v1.articrawler.domain.Category;
import java.time.LocalDateTime;

public record CategoryResponse(
    Long id, String name, String providerTopic, boolean enabled, LocalDateTime lastCollectedAt) {

  public static CategoryResponse from(Category c) {
    return new CategoryResponse(c.getId(), c.getName(), c.getProviderTopic(), c.isEnabled(), c.getLastCollectedAt());
  }
}
