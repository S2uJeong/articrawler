package com.v1.articrawler.news;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NewsCollectionScheduler {

  private static final Logger log = LoggerFactory.getLogger(NewsCollectionScheduler.class);

  private final NewsCollectionService newsCollectionService;

  public NewsCollectionScheduler(NewsCollectionService newsCollectionService) {
    this.newsCollectionService = newsCollectionService;
  }

  @Scheduled(
      fixedDelayString = "${articrawler.collection.interval-ms:900000}",
      initialDelayString = "${articrawler.collection.initial-delay-ms:10000}")
  public void runScheduledCollection() {
    log.info("Scheduled news collection starting");
    newsCollectionService.collectAll();
    log.info("Scheduled news collection finished");
  }
}
