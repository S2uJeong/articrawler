package com.v1.articrawler.news;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Supplements the RSS feed with a representative image and a richer summary by reading the
 * article's own page (Open Graph meta tags). Failures are swallowed - enrichment is optional and
 * must never fail a collection run (guide section 5: HTML collection is used alongside
 * RSS/API only where useful, not as a hard dependency).
 *
 * <p>Google News RSS article links (news.google.com/rss/articles/...) resolve to a Google-hosted
 * interstitial page - the real publisher redirect only happens via client-side JS in a browser,
 * so a plain HTTP fetch lands on Google's own generic page instead of the article. Enrichment is
 * skipped for those links (verified by hand: identical og:image/description across articles).
 */
@Component
public class ArticleEnrichmentService {

  private static final Logger log = LoggerFactory.getLogger(ArticleEnrichmentService.class);
  private static final String USER_AGENT =
      "Mozilla/5.0 (compatible; ArticrawlerBot/1.0; +internal news dashboard)";

  private final int timeoutMs;
  private final boolean enabled;

  public ArticleEnrichmentService(
      @Value("${articrawler.collection.request-timeout-ms:6000}") int timeoutMs,
      @Value("${articrawler.collection.enrich-images:true}") boolean enabled) {
    this.timeoutMs = timeoutMs;
    this.enabled = enabled;
  }

  public ArticleEnrichment enrich(String link) {
    if (!enabled || !StringUtils.hasText(link) || link.contains("news.google.com")) {
      return ArticleEnrichment.EMPTY;
    }
    try {
      Document doc = Jsoup.connect(link).userAgent(USER_AGENT).timeout(timeoutMs).get();
      String image = metaContent(doc, "og:image");
      String description = metaContent(doc, "og:description");
      String siteName = metaContent(doc, "og:site_name");
      return new ArticleEnrichment(image, description, siteName);
    } catch (Exception e) {
      log.debug("Article enrichment skipped for {}: {}", link, e.toString());
      return ArticleEnrichment.EMPTY;
    }
  }

  private String metaContent(Document doc, String property) {
    var el = doc.selectFirst("meta[property=" + property + "]");
    if (el == null) {
      el = doc.selectFirst("meta[name=" + property + "]");
    }
    return el == null ? null : el.attr("content");
  }
}
