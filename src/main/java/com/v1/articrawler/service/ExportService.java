package com.v1.articrawler.service;

import com.v1.articrawler.domain.Article;
import com.v1.articrawler.dto.ArticleResponse;
import com.v1.articrawler.repository.ArticleRepository;
import java.io.IOException;
import java.io.Writer;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ExportService {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final int MAX_EXPORT_ROWS = 20000;
  private static final String[] CSV_HEADERS = {
    "id", "title", "summary", "sourceName", "category", "publishedAt", "viewCount", "link"
  };

  private final ArticleRepository articleRepository;

  public ExportService(ArticleRepository articleRepository) {
    this.articleRepository = articleRepository;
  }

  public List<ArticleResponse> exportList(ArticleSearchCriteria criteria) {
    return articleRepository
        .findAll(
            ArticleSpecifications.fromCriteria(criteria),
            Sort.by(Sort.Direction.DESC, "publishedAt"))
        .stream()
        .limit(MAX_EXPORT_ROWS)
        .map(ArticleResponse::from)
        .toList();
  }

  public void writeCsv(ArticleSearchCriteria criteria, Writer writer) throws IOException {
    writer.write(String.join(",", CSV_HEADERS));
    writer.write("\n");
    List<Article> articles = articleRepository
        .findAll(
            ArticleSpecifications.fromCriteria(criteria),
            Sort.by(Sort.Direction.DESC, "publishedAt"))
        .stream()
        .limit(MAX_EXPORT_ROWS)
        .toList();
    for (Article a : articles) {
      writer.write(
          String.join(
              ",",
              csv(String.valueOf(a.getId())),
              csv(a.getTitle()),
              csv(a.getSummary()),
              csv(a.getSourceName()),
              csv(a.getCategory()),
              csv(a.getPublishedAt() == null ? "" : a.getPublishedAt().format(DATE_FORMAT)),
              csv(String.valueOf(a.getViewCount())),
              csv(a.getLink())));
      writer.write("\n");
    }
    writer.flush();
  }

  private String csv(String value) {
    if (value == null) {
      return "";
    }
    String escaped = value.replace("\"", "\"\"").replace("\n", " ").replace("\r", " ");
    return "\"" + escaped + "\"";
  }
}
