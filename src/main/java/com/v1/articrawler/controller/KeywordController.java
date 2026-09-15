package com.v1.articrawler.controller;

import com.v1.articrawler.dto.KeywordRequest;
import com.v1.articrawler.dto.KeywordResponse;
import com.v1.articrawler.service.KeywordService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/keywords")
public class KeywordController {

  private final KeywordService keywordService;

  public KeywordController(KeywordService keywordService) {
    this.keywordService = keywordService;
  }

  @GetMapping
  public List<KeywordResponse> findAll() {
    return keywordService.findAll();
  }

  @PostMapping
  public KeywordResponse create(@Valid @RequestBody KeywordRequest request) {
    return keywordService.create(request);
  }

  @PutMapping("/{id}")
  public KeywordResponse update(@PathVariable Long id, @Valid @RequestBody KeywordRequest request) {
    return keywordService.update(id, request);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    keywordService.delete(id);
  }
}
