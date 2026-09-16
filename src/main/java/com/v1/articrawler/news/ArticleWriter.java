package com.v1.articrawler.news;

import com.v1.articrawler.domain.Article;
import com.v1.articrawler.repository.ArticleRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Saves a single article in its own transaction (REQUIRES_NEW), isolated from the enclosing
 * collection-run transaction. Without this, a save failure on one item (e.g. a column-length
 * constraint violation) marks the whole surrounding transaction rollback-only, which later
 * surfaces as an {@code UnexpectedRollbackException} and silently aborts every source still
 * left in that {@link NewsCollectionService#collectAll()} pass - even though the per-item
 * try/catch in {@link NewsSourceCollector#processItems} looks like it already handled it.
 */
@Component
public class ArticleWriter {

  private final ArticleRepository articleRepository;

  public ArticleWriter(ArticleRepository articleRepository) {
    this.articleRepository = articleRepository;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void save(Article article) {
    articleRepository.save(article);
  }
}
