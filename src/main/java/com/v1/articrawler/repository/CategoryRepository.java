package com.v1.articrawler.repository;

import com.v1.articrawler.domain.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  List<Category> findByEnabledTrue();

  boolean existsByName(String name);
}
