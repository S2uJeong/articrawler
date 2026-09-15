package com.v1.articrawler.repository;

import com.v1.articrawler.domain.PressFeed;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PressFeedRepository extends JpaRepository<PressFeed, Long> {

  List<PressFeed> findByEnabledTrue();

  boolean existsByFeedUrl(String feedUrl);
}
