package com.v1.articrawler.service;

import com.v1.articrawler.domain.Article;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ArticleSpecifications {

  private ArticleSpecifications() {}

  public static Specification<Article> fromCriteria(ArticleSearchCriteria criteria) {
    return (root, query, cb) -> {
      List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

      if (StringUtils.hasText(criteria.query())) {
        String like = "%" + criteria.query().toLowerCase() + "%";
        predicates.add(
            cb.or(
                cb.like(cb.lower(root.get("title")), like),
                cb.like(cb.lower(root.get("summary")), like)));
      }
      if (StringUtils.hasText(criteria.category())) {
        predicates.add(cb.equal(root.get("category"), criteria.category()));
      }
      if (StringUtils.hasText(criteria.keyword())) {
        predicates.add(cb.equal(root.get("collectionSourceName"), criteria.keyword()));
      }
      if (StringUtils.hasText(criteria.source())) {
        predicates.add(cb.equal(root.get("sourceName"), criteria.source()));
      }
      if (criteria.from() != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("publishedAt"), criteria.from()));
      }
      if (criteria.to() != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("publishedAt"), criteria.to()));
      }
      return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
    };
  }
}
