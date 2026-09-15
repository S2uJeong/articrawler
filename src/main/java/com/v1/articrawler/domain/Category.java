package com.v1.articrawler.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A provider-side news category/section the admin wants to follow (e.g. a Google News topic
 * section). Collection reuses the provider's own classification instead of AI analysis.
 */
@Entity
@Table(name = "category")
@Getter
@Setter
@NoArgsConstructor
public class Category {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 100)
  private String name;

  /** Provider topic/section code used to fetch the category feed (e.g. "BUSINESS"). Null = display-only, not auto-collected. */
  @Column(name = "provider_topic", length = 100)
  private String providerTopic;

  @Column(nullable = false)
  private boolean enabled = true;

  @Column(name = "last_collected_at")
  private LocalDateTime lastCollectedAt;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  public Category(String name, String providerTopic, boolean enabled) {
    this.name = name;
    this.providerTopic = providerTopic;
    this.enabled = enabled;
  }
}
