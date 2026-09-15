package com.v1.articrawler.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A press outlet's own RSS feed, collected directly (not through Google News). Direct links mean
 * og:image enrichment actually works, and several outlets embed an image right in the feed.
 */
@Entity
@Table(name = "press_feed")
@Getter
@Setter
@NoArgsConstructor
public class PressFeed {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Display name for the outlet, e.g. "연합뉴스". Also used as Article.sourceName. */
  @Column(nullable = false, length = 100)
  private String name;

  @Column(name = "feed_url", nullable = false, unique = true, length = 500)
  private String feedUrl;

  /** Category articles from this feed should be classified/displayed under. */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

  @Column(nullable = false)
  private boolean enabled = true;

  @Column(name = "last_collected_at")
  private LocalDateTime lastCollectedAt;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  public PressFeed(String name, String feedUrl, Category category, boolean enabled) {
    this.name = name;
    this.feedUrl = feedUrl;
    this.category = category;
    this.enabled = enabled;
  }
}
