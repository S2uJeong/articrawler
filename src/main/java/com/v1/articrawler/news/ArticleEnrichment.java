package com.v1.articrawler.news;

/** Best-effort metadata scraped from an article's original page (Open Graph tags). */
public record ArticleEnrichment(String imageUrl, String description, String siteName) {

  public static final ArticleEnrichment EMPTY = new ArticleEnrichment(null, null, null);
}
