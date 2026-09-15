package com.v1.articrawler.repository;

import com.v1.articrawler.domain.CollectionRun;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionRunRepository extends JpaRepository<CollectionRun, Long> {

  List<CollectionRun> findTop50ByOrderByStartedAtDesc();

  List<CollectionRun> findByStartedAtAfterOrderByStartedAtDesc(LocalDateTime after);
}
