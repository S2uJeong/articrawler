package com.v1.articrawler.news;

import com.v1.articrawler.domain.Article;
import com.v1.articrawler.domain.Category;
import com.v1.articrawler.domain.CollectionRun;
import com.v1.articrawler.domain.CollectionStatus;
import com.v1.articrawler.domain.Keyword;
import com.v1.articrawler.domain.PressFeed;
import com.v1.articrawler.domain.SourceType;
import com.v1.articrawler.repository.ArticleRepository;
import com.v1.articrawler.repository.CategoryRepository;
import com.v1.articrawler.repository.CollectionRunRepository;
import com.v1.articrawler.repository.KeywordRepository;
import com.v1.articrawler.repository.PressFeedRepository;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Collects one category or keyword feed and persists the result. Each method is
 * {@code @Transactional} and is invoked from {@link NewsCollectionService} through this bean's
 * Spring proxy (never self-invoked), which is required for the annotation to actually apply.
 */
@Component
public class NewsSourceCollector {

  private static final Logger log = LoggerFactory.getLogger(NewsSourceCollector.class);
  private static final int SECONDARY_DEDUP_WINDOW_HOURS = 72;

  private final CategoryRepository categoryRepository;
  private final KeywordRepository keywordRepository;
  private final PressFeedRepository pressFeedRepository;
  private final ArticleRepository articleRepository;
  private final CollectionRunRepository collectionRunRepository;
  private final GoogleNewsRssClient rssClient;
  private final DirectRssFeedClient directRssFeedClient;
  private final ArticleEnrichmentService enrichmentService;
  private final long overlapMinutes;
  /** Article enrichment is a per-item network call (og:image scrape); run several concurrently so one collection pass doesn't take items-count * request-latency. */
  private final ExecutorService enrichmentExecutor = Executors.newFixedThreadPool(8);

  public NewsSourceCollector(
      CategoryRepository categoryRepository,
      KeywordRepository keywordRepository,
      PressFeedRepository pressFeedRepository,
      ArticleRepository articleRepository,
      CollectionRunRepository collectionRunRepository,
      GoogleNewsRssClient rssClient,
      DirectRssFeedClient directRssFeedClient,
      ArticleEnrichmentService enrichmentService,
      @Value("${articrawler.collection.overlap-minutes:60}") long overlapMinutes) {
    this.categoryRepository = categoryRepository;
    this.keywordRepository = keywordRepository;
    this.pressFeedRepository = pressFeedRepository;
    this.articleRepository = articleRepository;
    this.collectionRunRepository = collectionRunRepository;
    this.rssClient = rssClient;
    this.directRssFeedClient = directRssFeedClient;
    this.enrichmentService = enrichmentService;
    this.overlapMinutes = overlapMinutes;
  }

  @Transactional
  public CollectionRun collectCategory(Long categoryId) {
    Category category = categoryRepository.findById(categoryId).orElseThrow();
    CollectionRun run = new CollectionRun(SourceType.CATEGORY, category.getName());
    LocalDateTime runStart = run.getStartedAt();
    try {
      List<NewsFeedItem> items = rssClient.fetchByTopic(category.getProviderTopic());
      LocalDateTime cutoff = category.getLastCollectedAt() == null
          ? null
          : category.getLastCollectedAt().minusMinutes(overlapMinutes);
      processItems(items, cutoff, category.getName(), SourceType.CATEGORY, category.getName(), run);
      category.setLastCollectedAt(runStart);
      run.setStatus(run.getErrorMessage() == null ? CollectionStatus.SUCCESS : CollectionStatus.PARTIAL);
    } catch (Exception e) {
      log.warn("Category collection failed for {}: {}", category.getName(), e.toString());
      run.setStatus(CollectionStatus.FAILED);
      run.setErrorMessage(e.toString());
    }
    run.setFinishedAt(LocalDateTime.now());
    return collectionRunRepository.save(run);
  }

  @Transactional
  public CollectionRun collectKeyword(Long keywordId) {
    Keyword keyword = keywordRepository.findById(keywordId).orElseThrow();
    CollectionRun run = new CollectionRun(SourceType.KEYWORD, keyword.getKeyword());
    LocalDateTime runStart = run.getStartedAt();
    try {
      List<NewsFeedItem> items = rssClient.fetchByKeyword(keyword.getKeyword());
      LocalDateTime cutoff = keyword.getLastCollectedAt() == null
          ? null
          : keyword.getLastCollectedAt().minusMinutes(overlapMinutes);
      String category = keyword.getCategory() != null ? keyword.getCategory().getName() : null;
      processItems(items, cutoff, category, SourceType.KEYWORD, keyword.getKeyword(), run);
      keyword.setLastCollectedAt(runStart);
      run.setStatus(run.getErrorMessage() == null ? CollectionStatus.SUCCESS : CollectionStatus.PARTIAL);
    } catch (Exception e) {
      log.warn("Keyword collection failed for {}: {}", keyword.getKeyword(), e.toString());
      run.setStatus(CollectionStatus.FAILED);
      run.setErrorMessage(e.toString());
    }
    run.setFinishedAt(LocalDateTime.now());
    return collectionRunRepository.save(run);
  }

  @Transactional
  public CollectionRun collectPressFeed(Long pressFeedId) {
    PressFeed pressFeed = pressFeedRepository.findById(pressFeedId).orElseThrow();
    CollectionRun run = new CollectionRun(SourceType.PRESS, pressFeed.getName());
    LocalDateTime runStart = run.getStartedAt();
    try {
      List<NewsFeedItem> items = directRssFeedClient.fetch(pressFeed.getFeedUrl(), pressFeed.getName());
      LocalDateTime cutoff = pressFeed.getLastCollectedAt() == null
          ? null
          : pressFeed.getLastCollectedAt().minusMinutes(overlapMinutes);
      String category = pressFeed.getCategory() != null ? pressFeed.getCategory().getName() : null;
      processItems(items, cutoff, category, SourceType.PRESS, pressFeed.getName(), run);
      pressFeed.setLastCollectedAt(runStart);
      run.setStatus(run.getErrorMessage() == null ? CollectionStatus.SUCCESS : CollectionStatus.PARTIAL);
    } catch (Exception e) {
      log.warn("Press feed collection failed for {}: {}", pressFeed.getName(), e.toString());
      run.setStatus(CollectionStatus.FAILED);
      run.setErrorMessage(e.toString());
    }
    run.setFinishedAt(LocalDateTime.now());
    return collectionRunRepository.save(run);
  }

  private void processItems(
      List<NewsFeedItem> items,
      LocalDateTime cutoff,
      String category,
      SourceType sourceType,
      String sourceName,
      CollectionRun run) {
    run.setFetchedCount(items.size());

    List<NewsFeedItem> candidates = new ArrayList<>();
    for (NewsFeedItem item : items) {
      if (cutoff != null && item.publishedAt() != null && item.publishedAt().isBefore(cutoff)) {
        continue;
      }
      if (isDuplicate(item)) {
        run.setDuplicateCount(run.getDuplicateCount() + 1);
        continue;
      }
      candidates.add(item);
    }

    // Only fetch og:image/description for items the feed itself didn't already give us an image
    // for (some press feeds embed media:content or an <img> in the description directly).
    List<NewsFeedItem> needsEnrichment =
        candidates.stream().filter(i -> !StringUtils.hasText(i.imageUrl())).toList();
    Map<String, ArticleEnrichment> enrichments = enrichConcurrently(needsEnrichment);

    for (NewsFeedItem item : candidates) {
      try {
        ArticleEnrichment enrichment = enrichments.getOrDefault(item.link(), ArticleEnrichment.EMPTY);
        Article article = buildArticle(item, enrichment, category, sourceType, sourceName);
        articleRepository.save(article);
        run.setNewCount(run.getNewCount() + 1);
      } catch (Exception e) {
        log.warn("Failed to process feed item '{}': {}", item.title(), e.toString());
        String message = "[" + item.title() + "] " + e;
        run.setErrorMessage(message.length() > 500 ? message.substring(0, 500) : message);
      }
    }
  }

  private Map<String, ArticleEnrichment> enrichConcurrently(List<NewsFeedItem> items) {
    Map<String, ArticleEnrichment> results = new ConcurrentHashMap<>();
    List<CompletableFuture<Void>> futures = items.stream()
        .map(item -> CompletableFuture.runAsync(
            () -> results.put(item.link(), enrichmentService.enrich(item.link())), enrichmentExecutor))
        .toList();
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    return results;
  }

  @PreDestroy
  public void shutdown() {
    enrichmentExecutor.shutdown();
  }

  private boolean isDuplicate(NewsFeedItem item) {
    String normalizedLink = ArticleKeyGenerator.normalizeLink(item.link());
    String linkHash = ArticleKeyGenerator.hash(normalizedLink);
    if (articleRepository.existsByLinkHash(linkHash)) {
      return true;
    }
    if (item.publishedAt() == null || item.sourceName() == null) {
      return false;
    }
    String normalizedTitle = ArticleKeyGenerator.normalizeTitle(item.title());
    LocalDateTime from = item.publishedAt().minusHours(SECONDARY_DEDUP_WINDOW_HOURS);
    LocalDateTime to = item.publishedAt().plusHours(SECONDARY_DEDUP_WINDOW_HOURS);
    return !articleRepository
        .findByNormalizedTitleAndSourceNameAndPublishedAtBetween(normalizedTitle, item.sourceName(), from, to)
        .isEmpty();
  }

  private Article buildArticle(
      NewsFeedItem item,
      ArticleEnrichment enrichment,
      String category,
      SourceType sourceType,
      String sourceName) {
    Article article = new Article();
    article.setTitle(item.title());
    article.setNormalizedTitle(ArticleKeyGenerator.normalizeTitle(item.title()));
    // The feed's own description (when present) is real article text; enrichment's og:description
    // is only a fallback for feeds that don't carry one (e.g. Google News, whose feed description
    // is discarded upstream as non-meaningful).
    article.setSummary(StringUtils.hasText(item.description()) ? item.description() : enrichment.description());
    article.setImageUrl(StringUtils.hasText(item.imageUrl()) ? item.imageUrl() : enrichment.imageUrl());
    article.setSourceName(StringUtils.hasText(item.sourceName()) ? item.sourceName() : enrichment.siteName());
    article.setLink(item.link());
    String normalizedLink = ArticleKeyGenerator.normalizeLink(item.link());
    article.setLinkHash(ArticleKeyGenerator.hash(normalizedLink));
    article.setCategory(category);
    article.setCollectionSourceType(sourceType);
    article.setCollectionSourceName(sourceName);
    article.setPublishedAt(item.publishedAt());
    article.setCollectedAt(LocalDateTime.now());
    return article;
  }
}
