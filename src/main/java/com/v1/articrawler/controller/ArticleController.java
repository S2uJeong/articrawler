package com.v1.articrawler.controller;

import com.v1.articrawler.dto.ArticleResponse;
import com.v1.articrawler.dto.PageResponse;
import com.v1.articrawler.service.ArticleClickService;
import com.v1.articrawler.service.ArticleSearchCriteria;
import com.v1.articrawler.service.ArticleService;
import java.net.URI;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

  private final ArticleService articleService;
  private final ArticleClickService articleClickService;

  public ArticleController(ArticleService articleService, ArticleClickService articleClickService) {
    this.articleService = articleService;
    this.articleClickService = articleClickService;
  }

  @GetMapping
  public PageResponse<ArticleResponse> search(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String source,
      @RequestParam(required = false) LocalDate from,
      @RequestParam(required = false) LocalDate to,
      @PageableDefault(size = 20) Pageable pageable) {
    ArticleSearchCriteria criteria =
        new ArticleSearchCriteria(
            q,
            category,
            keyword,
            source,
            from == null ? null : from.atStartOfDay(),
            to == null ? null : to.plusDays(1).atStartOfDay());
    return PageResponse.from(articleService.search(criteria, pageable));
  }

  @GetMapping("/{id}")
  public ArticleResponse get(@PathVariable Long id) {
    return articleService.getById(id);
  }

  /** Logs the click, then redirects the browser to the article's original URL. */
  @GetMapping("/{id}/open")
  public ResponseEntity<Void> open(@PathVariable Long id) {
    String link = articleClickService.registerClickAndGetLink(id);
    return ResponseEntity.status(302).location(URI.create(link)).build();
  }
}
