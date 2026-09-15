package com.v1.articrawler.news;

import java.io.IOException;
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
 * Fetches a press outlet's own RSS feed directly (e.g. Yonhap, Newsis). Unlike Google News RSS,
 * these links point straight at the publisher, so og:image enrichment on them actually works -
 * and several outlets embed an image right in the feed (media:content, or an &lt;img&gt; inside
 * the description), which is even better since it skips the extra fetch entirely.
 */
@Component
public class DirectRssFeedClient {

  private static final String USER_AGENT =
      "Mozilla/5.0 (compatible; ArticrawlerBot/1.0; +internal news dashboard)";

  private final int timeoutMs;

  public DirectRssFeedClient(
      @Value("${articrawler.collection.request-timeout-ms:6000}") int timeoutMs) {
    this.timeoutMs = timeoutMs;
  }

  public List<NewsFeedItem> fetch(String feedUrl, String sourceName) throws IOException {
    Document doc =
        Jsoup.connect(feedUrl)
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
      String rawDescription = rawInnerContent(item, "description");
      // Some outlets (e.g. Chosun) leave <description> empty and put the real body - image
      // included - in <content:encoded> instead.
      String rawContentEncoded = namespacedRawInnerContent(item, "content:encoded");
      String imageUrl = feedImage(item, rawDescription, rawContentEncoded);
      String description = firstMeaningful(summarize(rawDescription), summarize(rawContentEncoded));
      LocalDateTime publishedAt = parsePubDate(text(item, "pubDate"));
      items.add(new NewsFeedItem(title.trim(), link.trim(), sourceName, description, publishedAt, imageUrl));
    }
    return items;
  }

  private String text(Element item, String tag) {
    Element el = item.selectFirst(tag);
    return el == null ? null : el.text();
  }

  /**
   * The tag's raw inner markup, as a literal string ready to be re-parsed as HTML. Deliberately
   * uses {@code text()}, not {@code html()}: under the XML parser, most feeds wrap this content
   * in a CDATA section, and {@code html()} re-serializes it wrapped in literal {@code <![CDATA[
   * ]]>} markers (which a subsequent {@code Jsoup.parse()} can't make sense of), whereas
   * {@code text()} correctly unwraps CDATA/entity-escaped content to the raw markup string either way.
   */
  private String rawInnerContent(Element item, String tag) {
    Element el = item.selectFirst(tag);
    return el == null ? null : el.text();
  }

  /** Like {@link #rawInnerContent}, but for a namespaced tag name (e.g. "content:encoded") that CSS-selector syntax can't address directly. */
  private String namespacedRawInnerContent(Element item, String tag) {
    Element el = item.getElementsByTag(tag).first();
    return el == null ? null : el.text();
  }

  /**
   * media:content/media:thumbnail (Yonhap/Donga-style) first, then an &lt;img&gt; inside the
   * description (Newsis/Hani-style), then one inside content:encoded (Chosun-style).
   */
  private String feedImage(Element item, String rawDescriptionHtml, String rawContentEncodedHtml) {
    Element media = item.getElementsByTag("media:content").first();
    if (media == null) {
      media = item.getElementsByTag("media:thumbnail").first();
    }
    if (media != null && StringUtils.hasText(media.attr("url"))) {
      return media.attr("url");
    }
    String fromDescription = firstImageSrc(rawDescriptionHtml);
    if (fromDescription != null) {
      return fromDescription;
    }
    return firstImageSrc(rawContentEncodedHtml);
  }

  private String firstImageSrc(String html) {
    if (!StringUtils.hasText(html)) {
      return null;
    }
    Element img = Jsoup.parse(html).selectFirst("img");
    return img != null && StringUtils.hasText(img.attr("src")) ? img.attr("src") : null;
  }

  private String firstMeaningful(String... candidates) {
    for (String candidate : candidates) {
      if (StringUtils.hasText(candidate)) {
        return candidate;
      }
    }
    return null;
  }

  private String summarize(String rawDescriptionHtml) {
    if (!StringUtils.hasText(rawDescriptionHtml)) {
      return null;
    }
    String text = Jsoup.parse(rawDescriptionHtml).text().trim();
    if (!StringUtils.hasText(text)) {
      return null;
    }
    return text.length() > 500 ? text.substring(0, 500) : text;
  }

  private LocalDateTime parsePubDate(String pubDate) {
    if (!StringUtils.hasText(pubDate)) {
      return null;
    }
    try {
      ZonedDateTime zoned = ZonedDateTime.parse(pubDate.trim(), DateTimeFormatter.RFC_1123_DATE_TIME);
      return zoned.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    } catch (DateTimeException e) {
      return null;
    }
  }
}
