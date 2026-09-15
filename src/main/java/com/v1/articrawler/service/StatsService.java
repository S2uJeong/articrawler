package com.v1.articrawler.service;

import com.v1.articrawler.domain.Article;
import com.v1.articrawler.domain.CollectionRun;
import com.v1.articrawler.domain.CollectionStatus;
import com.v1.articrawler.dto.ArticleResponse;
import com.v1.articrawler.dto.CategoryCountResponse;
import com.v1.articrawler.dto.DailyCollectionStatResponse;
import com.v1.articrawler.dto.KeywordCountResponse;
import com.v1.articrawler.dto.PopularArticleResponse;
import com.v1.articrawler.dto.TrendingArticleResponse;
import com.v1.articrawler.repository.ArticleClickRepository;
import com.v1.articrawler.repository.ArticleRepository;
import com.v1.articrawler.repository.CollectionRunRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StatsService {

  private static final long TRENDING_MIN_RECENT_CLICKS = 3;

  private final ArticleRepository articleRepository;
  private final ArticleClickRepository articleClickRepository;
  private final CollectionRunRepository collectionRunRepository;

  public StatsService(
      ArticleRepository articleRepository,
      ArticleClickRepository articleClickRepository,
      CollectionRunRepository collectionRunRepository) {
    this.articleRepository = articleRepository;
    this.articleClickRepository = articleClickRepository;
    this.collectionRunRepository = collectionRunRepository;
  }

  public List<PopularArticleResponse> popular(LocalDateTime from, LocalDateTime to, int limit) {
    List<Object[]> rows = articleRepository.findPopular(from, to, PageRequest.of(0, limit));
    return rows.stream()
        .map(row -> new PopularArticleResponse(ArticleResponse.from((Article) row[0]), (Long) row[1]))
        .toList();
  }

  public List<TrendingArticleResponse> trending(int limit) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime dayAgo = now.minusHours(24);
    LocalDateTime twoDaysAgo = now.minusHours(48);

    List<Object[]> recentRows = articleRepository.findPopular(dayAgo, now, PageRequest.of(0, Math.max(limit * 3, 30)));
    return recentRows.stream()
        .map(row -> {
          Article article = (Article) row[0];
          long recent = (Long) row[1];
          long previous = articleClickRepository.countByArticleIdAndClickedAtBetween(article.getId(), twoDaysAgo, dayAgo);
          double ratio = (double) recent / (previous + 1);
          return new TrendingArticleResponse(ArticleResponse.from(article), recent, previous, ratio);
        })
        .filter(t -> t.recentClicks() >= TRENDING_MIN_RECENT_CLICKS)
        .sorted(Comparator.comparingDouble(TrendingArticleResponse::growthRatio).reversed())
        .limit(limit)
        .toList();
  }

  public List<CategoryCountResponse> categoryCounts() {
    return articleRepository.countByCategory().stream()
        .map(r -> new CategoryCountResponse(r.getCategory(), r.getCount()))
        .toList();
  }

  public List<KeywordCountResponse> keywordCounts() {
    return articleRepository.countByKeyword().stream()
        .map(r -> new KeywordCountResponse(r.getKeyword(), r.getCount()))
        .toList();
  }

  public List<DailyCollectionStatResponse> collectionStats(int days) {
    LocalDateTime cutoff = LocalDateTime.now().minusDays(days).toLocalDate().atStartOfDay();
    List<CollectionRun> runs = collectionRunRepository.findByStartedAtAfterOrderByStartedAtDesc(cutoff);

    Map<LocalDate, List<CollectionRun>> byDate = runs.stream()
        .collect(Collectors.groupingBy(r -> r.getStartedAt().toLocalDate(), TreeMap::new, Collectors.toList()));

    return byDate.entrySet().stream()
        .map(e -> {
          List<CollectionRun> dayRuns = e.getValue();
          int fetched = dayRuns.stream().mapToInt(CollectionRun::getFetchedCount).sum();
          int added = dayRuns.stream().mapToInt(CollectionRun::getNewCount).sum();
          int failed = (int) dayRuns.stream().filter(r -> r.getStatus() == CollectionStatus.FAILED).count();
          return new DailyCollectionStatResponse(e.getKey(), dayRuns.size(), fetched, added, failed);
        })
        .sorted(Comparator.comparing(DailyCollectionStatResponse::date).reversed())
        .toList();
  }
}
