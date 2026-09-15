package com.v1.articrawler.news;

import java.time.LocalDateTime;

/**
 * A single item parsed out of an RSS feed, before dedup/enrichment/persistence.
 *
 * @param imageUrl image already embedded in the feed itself (media:content, or an &lt;img&gt;
 *     found in the description), if any - null means no image was found and og:image enrichment
 *     should be tried instead.
 */
public record NewsFeedItem(
    String title,
    String link,
    String sourceName,
    String description,
    LocalDateTime publishedAt,
    String imageUrl) {}
