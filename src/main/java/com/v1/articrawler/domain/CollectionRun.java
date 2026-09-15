package com.v1.articrawler.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** One execution of the collector for a single category or keyword - used for the job/error dashboard. */
@Entity
@Table(name = "collection_run")
@Getter
@Setter
@NoArgsConstructor
public class CollectionRun {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_type", nullable = false, length = 20)
  private SourceType sourceType;

  @Column(name = "source_name", nullable = false, length = 100)
  private String sourceName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private CollectionStatus status;

  @Column(name = "started_at", nullable = false)
  private LocalDateTime startedAt;

  @Column(name = "finished_at")
  private LocalDateTime finishedAt;

  @Column(name = "fetched_count", nullable = false)
  private int fetchedCount = 0;

  @Column(name = "new_count", nullable = false)
  private int newCount = 0;

  @Column(name = "duplicate_count", nullable = false)
  private int duplicateCount = 0;

  @Lob
  @Column(name = "error_message")
  private String errorMessage;

  public CollectionRun(SourceType sourceType, String sourceName) {
    this.sourceType = sourceType;
    this.sourceName = sourceName;
    this.startedAt = LocalDateTime.now();
    this.status = CollectionStatus.SUCCESS;
  }
}
