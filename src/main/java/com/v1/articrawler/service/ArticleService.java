package com.v1.articrawler.service;

import com.v1.articrawler.domain.Article;
import com.v1.articrawler.dto.ArticleResponse;
import com.v1.articrawler.exception.NotFoundException;
import com.v1.articrawler.repository.ArticleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ArticleService {

  private final ArticleRepository articleRepository;

  public ArticleService(ArticleRepository articleRepository) {
    this.articleRepository = articleRepository;
  }

  public Page<ArticleResponse> search(ArticleSearchCriteria criteria, Pageable pageable) {
    return articleRepository
        .findAll(ArticleSpecifications.fromCriteria(criteria), pageable)
        .map(ArticleResponse::from);
  }

  public ArticleResponse getById(Long id) {
    return ArticleResponse.from(findEntity(id));
  }

  public Article findEntity(Long id) {
    return articleRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Article not found: " + id));
  }
}
