package com.v1.articrawler.dto;

import com.v1.articrawler.news.BackfillResult;

public record BackfillResponse(int scanned, int updated) {

  public static BackfillResponse from(BackfillResult result) {
    return new BackfillResponse(result.scanned(), result.updated());
  }
}
