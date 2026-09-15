package com.v1.articrawler.dto;

import java.time.LocalDate;

public record DailyCollectionStatResponse(
    LocalDate date, int runCount, int fetchedCount, int newCount, int failedCount) {}
