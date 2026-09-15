package com.v1.articrawler.news;

import com.v1.articrawler.domain.Article;
import com.v1.articrawler.repository.ArticleRepository;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * One-off maintenance job: re-visit already-collected articles that are missing an image and try
 * og:image enrichment again. Only makes sense for direct-link articles (press feeds) - Google
 * News links never resolve to the real page via plain HTTP, so they're excluded rather than
 * wasting requests on something that structurally cannot succeed.
 */
@Service
public class ArticleBackfillService {

  private static final Logger log = LoggerFactory.getLogger(ArticleBackfillService.class);
  private static final int BATCH_LIMIT = 300;
  private static final String GOOGLE_NEWS_LINK_FRAGMENT = "news.google.com";

  private final ArticleRepository articleRepository;
  private final ArticleEnrichmentService enrichmentService;
  private final ExecutorService enrichmentExecutor = Executors.newFixedThreadPool(8);

  public ArticleBackfillService(ArticleRepository articleRepository, ArticleEnrichmentService enrichmentService) {
    this.articleRepository = articleRepository;
    this.enrichmentService = enrichmentService;
  }

  @Transactional
  public BackfillResult backfillImages() {
    List<Article> candidates = articleRepository.findByImageUrlIsNullAndLinkNotContainingOrderByIdAsc(
        GOOGLE_NEWS_LINK_FRAGMENT, PageRequest.of(0, BATCH_LIMIT));

    Map<Long, ArticleEnrichment> enrichments = new ConcurrentHashMap<>();
    List<CompletableFuture<Void>> futures = candidates.stream()
        .map(a -> CompletableFuture.runAsync(
            () -> enrichments.put(a.getId(), enrichmentService.enrich(a.getLink())), enrichmentExecutor))
        .toList();
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    int updated = 0;
    for (Article article : candidates) {
      ArticleEnrichment enrichment = enrichments.get(article.getId());
      if (enrichment == null || !StringUtils.hasText(enrichment.imageUrl())) {
        continue;
      }
      article.setImageUrl(enrichment.imageUrl());
      if (!StringUtils.hasText(article.getSummary()) && StringUtils.hasText(enrichment.description())) {
        article.setSummary(enrichment.description());
      }
      updated++;
    }

    log.info("Image backfill: scanned {}, updated {}", candidates.size(), updated);
    return new BackfillResult(candidates.size(), updated);
  }

  @PreDestroy
  public void shutdown() {
    enrichmentExecutor.shutdown();
  }
}
