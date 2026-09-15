package com.v1.articrawler.controller;

import com.v1.articrawler.dto.BackfillResponse;
import com.v1.articrawler.dto.CollectionRunResponse;
import com.v1.articrawler.service.CollectionAdminService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/collection")
public class CollectionController {

  private final CollectionAdminService collectionAdminService;

  public CollectionController(CollectionAdminService collectionAdminService) {
    this.collectionAdminService = collectionAdminService;
  }

  @GetMapping("/runs")
  public List<CollectionRunResponse> recentRuns() {
    return collectionAdminService.recentRuns();
  }

  @PostMapping("/run")
  public Map<String, String> triggerManual() {
    collectionAdminService.triggerManualCollection();
    return Map.of("status", "started");
  }

  /** Re-tries og:image enrichment for already-collected direct-link articles that are still missing an image. Runs synchronously (bounded batch), so the result is returned immediately. */
  @PostMapping("/backfill-images")
  public BackfillResponse backfillImages() {
    return BackfillResponse.from(collectionAdminService.backfillImages());
  }
}
