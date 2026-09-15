package com.v1.articrawler.news;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.util.StringUtils;

/**
 * Builds the two keys used for de-duplicating articles: a normalized-link hash (primary) and a
 * normalized title (secondary, catches the same story re-published under a different URL).
 */
public final class ArticleKeyGenerator {

  private static final Set<String> TRACKING_PARAMS =
      Set.of("utm_source", "utm_medium", "utm_campaign", "utm_term", "utm_content", "fbclid",
          "gclid", "ref", "from", "rss");

  private static final Pattern NON_ALNUM = Pattern.compile("[^0-9\\p{IsHangul}a-zA-Z]+");

  private ArticleKeyGenerator() {}

  public static String normalizeLink(String rawLink) {
    if (!StringUtils.hasText(rawLink)) {
      return "";
    }
    try {
      URI uri = URI.create(rawLink.trim());
      String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
      String path = uri.getPath() == null ? "" : uri.getPath();
      if (path.endsWith("/") && path.length() > 1) {
        path = path.substring(0, path.length() - 1);
      }
      String query = filterQuery(uri.getRawQuery());
      String normalized = host + path + (query.isEmpty() ? "" : "?" + query);
      return normalized;
    } catch (IllegalArgumentException e) {
      return rawLink.trim().toLowerCase(Locale.ROOT);
    }
  }

  private static String filterQuery(String rawQuery) {
    if (!StringUtils.hasText(rawQuery)) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (String pair : rawQuery.split("&")) {
      String key = pair.contains("=") ? pair.substring(0, pair.indexOf('=')) : pair;
      if (TRACKING_PARAMS.contains(key.toLowerCase(Locale.ROOT))) {
        continue;
      }
      if (!sb.isEmpty()) {
        sb.append('&');
      }
      sb.append(pair);
    }
    return sb.toString();
  }

  public static String hash(String normalizedLink) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] bytes = digest.digest(normalizedLink.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder(bytes.length * 2);
      for (byte b : bytes) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  public static String normalizeTitle(String title) {
    if (!StringUtils.hasText(title)) {
      return "";
    }
    return NON_ALNUM.matcher(title.toLowerCase(Locale.ROOT)).replaceAll("");
  }
}
