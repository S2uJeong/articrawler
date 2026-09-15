package com.v1.articrawler.service;

import com.v1.articrawler.dto.CollectionRunResponse;
import com.v1.articrawler.news.ArticleBackfillService;
import com.v1.articrawler.news.BackfillResult;
import com.v1.articrawler.news.NewsCollectionService;
import com.v1.articrawler.repository.CollectionRunRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class CollectionAdminService {

  private static final Logger log = LoggerFactory.getLogger(CollectionAdminService.class);

  private final NewsCollectionService newsCollectionService;
  private final ArticleBackfillService articleBackfillService;
  private final CollectionRunRepository collectionRunRepository;

  public CollectionAdminService(
      NewsCollectionService newsCollectionService,
      ArticleBackfillService articleBackfillService,
      CollectionRunRepository collectionRunRepository) {
    this.newsCollectionService = newsCollectionService;
    this.articleBackfillService = articleBackfillService;
    this.collectionRunRepository = collectionRunRepository;
  }

  public List<CollectionRunResponse> recentRuns() {
    return collectionRunRepository.findTop50ByOrderByStartedAtDesc().stream()
        .map(CollectionRunResponse::from)
        .toList();
  }

  @Async
  public void triggerManualCollection() {
    log.info("Manual news collection triggered");
    newsCollectionService.collectAll();
  }

  public BackfillResult backfillImages() {
    log.info("Manual image backfill triggered");
    return articleBackfillService.backfillImages();
  }
}
