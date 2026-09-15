package com.v1.articrawler.controller;

import com.v1.articrawler.dto.CategoryCountResponse;
import com.v1.articrawler.dto.DailyCollectionStatResponse;
import com.v1.articrawler.dto.KeywordCountResponse;
import com.v1.articrawler.dto.PopularArticleResponse;
import com.v1.articrawler.dto.SourceCountResponse;
import com.v1.articrawler.dto.TrendingArticleResponse;
import com.v1.articrawler.service.StatsService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

  private final StatsService statsService;

  public StatsController(StatsService statsService) {
    this.statsService = statsService;
  }

  @GetMapping("/popular")
  public List<PopularArticleResponse> popular(
      @RequestParam(defaultValue = "daily") String period, @RequestParam(defaultValue = "10") int limit) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime from =
        switch (period) {
          case "weekly" -> now.minusDays(7);
          case "monthly" -> now.minusDays(30);
          default -> now.minusDays(1);
        };
    return statsService.popular(from, now, limit);
  }

  @GetMapping("/trending")
  public List<TrendingArticleResponse> trending(@RequestParam(defaultValue = "10") int limit) {
    return statsService.trending(limit);
  }

  @GetMapping("/categories")
  public List<CategoryCountResponse> categoryCounts() {
    return statsService.categoryCounts();
  }

  @GetMapping("/keywords")
  public List<KeywordCountResponse> keywordCounts() {
    return statsService.keywordCounts();
  }

  @GetMapping("/sources")
  public List<SourceCountResponse> sourceCounts() {
    return statsService.sourceCounts();
  }

  @GetMapping("/collection")
  public List<DailyCollectionStatResponse> collectionStats(@RequestParam(defaultValue = "14") int days) {
    return statsService.collectionStats(days);
  }
}
