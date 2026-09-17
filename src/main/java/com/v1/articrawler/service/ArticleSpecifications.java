package com.v1.articrawler.service;

import com.v1.articrawler.domain.Article;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ArticleSpecifications {

  // 관리자 없이 운영하는 동안은 금융 카테고리만 서비스한다. 범위를 넓히려면 이 값만 바꾸면 된다.
  private static final String FIXED_CATEGORY = "금융";

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
      predicates.add(cb.equal(root.get("category"), FIXED_CATEGORY));
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
      // 대표 이미지가 없는 기사는 목록/내보내기에서 제외한다.
      predicates.add(cb.isNotNull(root.get("imageUrl")));
      predicates.add(cb.notEqual(root.get("imageUrl"), ""));
      return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
    };
  }
}
