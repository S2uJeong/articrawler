package com.v1.articrawler.controller;

import com.v1.articrawler.dto.ArticleResponse;
import com.v1.articrawler.service.ArticleSearchCriteria;
import com.v1.articrawler.service.ExportService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/export")
public class ExportController {

  private final ExportService exportService;

  public ExportController(ExportService exportService) {
    this.exportService = exportService;
  }

  @GetMapping("/articles.csv")
  public void exportCsv(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) LocalDate from,
      @RequestParam(required = false) LocalDate to,
      HttpServletResponse response)
      throws IOException {
    response.setContentType("text/csv; charset=UTF-8");
    response.setHeader("Content-Disposition", "attachment; filename=\"articles.csv\"");
    // Leading BOM so Excel opens the UTF-8 file (with Korean text) correctly.
    response.getWriter().write('﻿');
    exportService.writeCsv(criteria(q, category, keyword, from, to), response.getWriter());
  }

  @GetMapping("/articles.json")
  public List<ArticleResponse> exportJson(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) LocalDate from,
      @RequestParam(required = false) LocalDate to,
      HttpServletResponse response) {
    response.setHeader("Content-Disposition", "attachment; filename=\"articles.json\"");
    return exportService.exportList(criteria(q, category, keyword, from, to));
  }

  private ArticleSearchCriteria criteria(
      String q, String category, String keyword, LocalDate from, LocalDate to) {
    return new ArticleSearchCriteria(
        q,
        category,
        keyword,
        from == null ? null : from.atStartOfDay(),
        to == null ? null : to.plusDays(1).atStartOfDay());
  }
}
