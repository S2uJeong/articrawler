package com.v1.articrawler.service;

import java.time.LocalDateTime;

public record ArticleSearchCriteria(
    String query, String category, String keyword, String source, LocalDateTime from, LocalDateTime to) {}
