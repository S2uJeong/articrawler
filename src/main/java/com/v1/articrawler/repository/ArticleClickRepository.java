package com.v1.articrawler.repository;

import com.v1.articrawler.domain.ArticleClick;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleClickRepository extends JpaRepository<ArticleClick, Long> {

  long countByArticleIdAndClickedAtBetween(Long articleId, LocalDateTime from, LocalDateTime to);
}
