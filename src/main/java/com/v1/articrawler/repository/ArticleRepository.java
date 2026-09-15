package com.v1.articrawler.repository;

import com.v1.articrawler.domain.Article;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository
    extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {

  boolean existsByLinkHash(String linkHash);

  List<Article> findByNormalizedTitleAndSourceNameAndPublishedAtBetween(
      String normalizedTitle, String sourceName, LocalDateTime from, LocalDateTime to);

  /**
   * Candidates for image backfill: missing an image, and NOT a Google News link (those never
   * resolve to the real article via plain HTTP, so re-enriching them can never succeed).
   */
  List<Article> findByImageUrlIsNullAndLinkNotContainingOrderByIdAsc(String linkFragment, Pageable pageable);

  @Query(
      "select a.category as category, count(a) as count "
          + "from Article a where a.category is not null group by a.category order by count(a) desc")
  List<CategoryCount> countByCategory();

  @Query(
      "select a.collectionSourceName as keyword, count(a) as count "
          + "from Article a where a.collectionSourceType = com.v1.articrawler.domain.SourceType.KEYWORD "
          + "group by a.collectionSourceName order by count(a) desc")
  List<KeywordCount> countByKeyword();

  @Query(
      "select a, count(c) as clicks from Article a join ArticleClick c on c.article = a "
          + "where c.clickedAt between :from and :to group by a order by clicks desc")
  List<Object[]> findPopular(
      @Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

  interface CategoryCount {
    String getCategory();

    long getCount();
  }

  interface KeywordCount {
    String getKeyword();

    long getCount();
  }
}
