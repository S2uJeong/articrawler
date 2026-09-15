package com.v1.articrawler.dto;

public record TrendingArticleResponse(
    ArticleResponse article, long recentClicks, long previousClicks, double growthRatio) {}
