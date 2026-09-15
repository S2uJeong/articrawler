package com.v1.articrawler.news;

import com.v1.articrawler.domain.Category;
import com.v1.articrawler.domain.Keyword;
import com.v1.articrawler.domain.PressFeed;
import com.v1.articrawler.repository.CategoryRepository;
import com.v1.articrawler.repository.KeywordRepository;
import com.v1.articrawler.repository.PressFeedRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Orchestrates one collection pass: for every enabled category/keyword/press feed, delegate to
 * {@link NewsSourceCollector} (a separate bean, so its {@code @Transactional} methods are
 * actually applied - calling them from here goes through Spring's proxy, unlike a self-invoked
 * call on the same instance).
 *
 * Incremental collection: each source remembers its own last-collected time; the next run only
 * needs items published after that (minus a safety overlap window), while the link/title dedup
 * check in {@link NewsSourceCollector} is the real guard against double-counting re-fetched items.
 */
@Service
public class NewsCollectionService {

  private static final Logger log = LoggerFactory.getLogger(NewsCollectionService.class);

  private final CategoryRepository categoryRepository;
  private final KeywordRepository keywordRepository;
  private final PressFeedRepository pressFeedRepository;
  private final NewsSourceCollector sourceCollector;

  public NewsCollectionService(
      CategoryRepository categoryRepository,
      KeywordRepository keywordRepository,
      PressFeedRepository pressFeedRepository,
      NewsSourceCollector sourceCollector) {
    this.categoryRepository = categoryRepository;
    this.keywordRepository = keywordRepository;
    this.pressFeedRepository = pressFeedRepository;
    this.sourceCollector = sourceCollector;
  }

  /** Runs collection for every enabled category, keyword, and press feed. Safe to call concurrently - synchronized to avoid overlapping runs racing on the same dedup keys. */
  public synchronized void collectAll() {
    List<Category> categories = categoryRepository.findByEnabledTrue().stream()
        .filter(c -> StringUtils.hasText(c.getProviderTopic()))
        .toList();
    List<Keyword> keywords = keywordRepository.findByEnabledTrue();
    List<PressFeed> pressFeeds = pressFeedRepository.findByEnabledTrue();

    log.info(
        "Starting news collection: {} categories, {} keywords, {} press feeds",
        categories.size(),
        keywords.size(),
        pressFeeds.size());
    for (Category category : categories) {
      sourceCollector.collectCategory(category.getId());
    }
    for (Keyword keyword : keywords) {
      sourceCollector.collectKeyword(keyword.getId());
    }
    for (PressFeed pressFeed : pressFeeds) {
      sourceCollector.collectPressFeed(pressFeed.getId());
    }
  }
}
