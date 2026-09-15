package com.v1.articrawler.news;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Fetches Google News RSS feeds (a free, public, no-key-required news source) - either a
 * provider "topic" section feed (category-based, matching the provider's own classification) or
 * a full-text search feed (keyword-based).
 */
@Component
public class GoogleNewsRssClient {

  private static final String USER_AGENT =
      "Mozilla/5.0 (compatible; ArticrawlerBot/1.0; +internal news dashboard)";
  private static final String LOCALE_PARAMS = "hl=ko&gl=KR&ceid=KR:ko";

  private final int timeoutMs;

  public GoogleNewsRssClient(
      @Value("${articrawler.collection.request-timeout-ms:6000}") int timeoutMs) {
    this.timeoutMs = timeoutMs;
  }

  public List<NewsFeedItem> fetchByTopic(String topic) throws IOException {
    String url = "https://news.google.com/rss/headlines/section/topic/" + topic + "?" + LOCALE_PARAMS;
    return fetch(url);
  }

  public List<NewsFeedItem> fetchByKeyword(String keyword) throws IOException {
    String encoded = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
    String url = "https://news.google.com/rss/search?q=" + encoded + "&" + LOCALE_PARAMS;
    return fetch(url);
  }

  private List<NewsFeedItem> fetch(String url) throws IOException {
    Document doc =
        Jsoup.connect(url)
            .userAgent(USER_AGENT)
            .timeout(timeoutMs)
            .parser(Parser.xmlParser())
            .get();

    Elements itemElements = doc.select("item");
    List<NewsFeedItem> items = new ArrayList<>(itemElements.size());
    for (Element item : itemElements) {
      String title = text(item, "title");
      String link = text(item, "link");
      if (!StringUtils.hasText(title) || !StringUtils.hasText(link)) {
        continue;
      }
      Element sourceEl = item.selectFirst("source");
      String sourceName = sourceEl != null ? sourceEl.text() : extractSourceFromTitle(title);
      String cleanedTitle = cleanTitle(title, sourceName);
      String description = meaningfulDescription(stripHtml(text(item, "description")), cleanedTitle);
      LocalDateTime publishedAt = parsePubDate(text(item, "pubDate"));
      items.add(new NewsFeedItem(cleanedTitle, link.trim(), sourceName, description, publishedAt, null));
    }
    return items;
  }

  private String text(Element item, String tag) {
    Element el = item.selectFirst(tag);
    return el == null ? null : el.text();
  }

  /**
   * Google News RSS descriptions are almost always just "{title} {source}" re-rendered as HTML,
   * not an actual summary - discard when it carries no information beyond the title.
   */
  private String meaningfulDescription(String description, String title) {
    if (!StringUtils.hasText(description) || !StringUtils.hasText(title)) {
      return description;
    }
    String normalizedDescription = description.replaceAll("\\s+", " ").trim().toLowerCase();
    String normalizedTitle = title.replaceAll("\\s+", " ").trim().toLowerCase();
    return normalizedDescription.startsWith(normalizedTitle) ? null : description;
  }

  private String stripHtml(String html) {
    if (!StringUtils.hasText(html)) {
      return null;
    }
    return Jsoup.parse(html).text();
  }

  /** Google News titles are typically "Headline - Source Name"; trim the trailing source. */
  private String cleanTitle(String title, String sourceName) {
    if (StringUtils.hasText(sourceName) && title.endsWith(" - " + sourceName)) {
      return title.substring(0, title.length() - (" - " + sourceName).length()).trim();
    }
    return title.trim();
  }

  private String extractSourceFromTitle(String title) {
    int idx = title.lastIndexOf(" - ");
    return idx >= 0 ? title.substring(idx + 3).trim() : null;
  }

  private LocalDateTime parsePubDate(String pubDate) {
    if (!StringUtils.hasText(pubDate)) {
      return null;
    }
    try {
      ZonedDateTime zoned = ZonedDateTime.parse(pubDate, DateTimeFormatter.RFC_1123_DATE_TIME);
      return zoned.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    } catch (DateTimeException e) {
      return null;
    }
  }
}
