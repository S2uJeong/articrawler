package com.v1.articrawler.service;

import com.v1.articrawler.domain.Article;
import com.v1.articrawler.domain.ArticleClick;
import com.v1.articrawler.repository.ArticleClickRepository;
import com.v1.articrawler.repository.ArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArticleClickService {

  private final ArticleRepository articleRepository;
  private final ArticleClickRepository articleClickRepository;

  public ArticleClickService(
      ArticleRepository articleRepository, ArticleClickRepository articleClickRepository) {
    this.articleRepository = articleRepository;
    this.articleClickRepository = articleClickRepository;
  }

  /** Logs a click and returns the article's original URL to redirect the user to. */
  @Transactional
  public String registerClickAndGetLink(Long articleId) {
    Article article = articleRepository.findById(articleId)
        .orElseThrow(() -> new com.v1.articrawler.exception.NotFoundException("Article not found: " + articleId));
    article.setViewCount(article.getViewCount() + 1);
    articleClickRepository.save(new ArticleClick(article));
    return article.getLink();
  }
}
