package com.v1.articrawler.repository;

import com.v1.articrawler.domain.Keyword;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {

  List<Keyword> findByEnabledTrue();

  boolean existsByKeyword(String keyword);
}
