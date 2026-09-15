package com.v1.articrawler.dto;

import com.v1.articrawler.domain.CollectionRun;
import com.v1.articrawler.domain.CollectionStatus;
import com.v1.articrawler.domain.SourceType;
import java.time.LocalDateTime;

public record CollectionRunResponse(
    Long id,
    SourceType sourceType,
    String sourceName,
    CollectionStatus status,
    LocalDateTime startedAt,
    LocalDateTime finishedAt,
    int fetchedCount,
    int newCount,
    int duplicateCount,
    String errorMessage) {

  public static CollectionRunResponse from(CollectionRun r) {
    return new CollectionRunResponse(
        r.getId(),
        r.getSourceType(),
        r.getSourceName(),
        r.getStatus(),
        r.getStartedAt(),
        r.getFinishedAt(),
        r.getFetchedCount(),
        r.getNewCount(),
        r.getDuplicateCount(),
        r.getErrorMessage());
  }
}
