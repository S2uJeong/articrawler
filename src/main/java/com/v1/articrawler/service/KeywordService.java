package com.v1.articrawler.service;

import com.v1.articrawler.domain.Category;
import com.v1.articrawler.domain.Keyword;
import com.v1.articrawler.dto.KeywordRequest;
import com.v1.articrawler.dto.KeywordResponse;
import com.v1.articrawler.exception.DuplicateResourceException;
import com.v1.articrawler.exception.NotFoundException;
import com.v1.articrawler.repository.CategoryRepository;
import com.v1.articrawler.repository.KeywordRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class KeywordService {

  private final KeywordRepository keywordRepository;
  private final CategoryRepository categoryRepository;

  public KeywordService(KeywordRepository keywordRepository, CategoryRepository categoryRepository) {
    this.keywordRepository = keywordRepository;
    this.categoryRepository = categoryRepository;
  }

  public List<KeywordResponse> findAll() {
    return keywordRepository.findAll().stream().map(KeywordResponse::from).toList();
  }

  @Transactional
  public KeywordResponse create(KeywordRequest request) {
    if (keywordRepository.existsByKeyword(request.keyword())) {
      throw new DuplicateResourceException("Keyword already exists: " + request.keyword());
    }
    Keyword keyword = new Keyword(request.keyword(), resolveCategory(request.categoryId()), request.enabled());
    return KeywordResponse.from(keywordRepository.save(keyword));
  }

  @Transactional
  public KeywordResponse update(Long id, KeywordRequest request) {
    Keyword keyword = keywordRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Keyword not found: " + id));
    keyword.setKeyword(request.keyword());
    keyword.setCategory(resolveCategory(request.categoryId()));
    keyword.setEnabled(request.enabled());
    return KeywordResponse.from(keyword);
  }

  @Transactional
  public void delete(Long id) {
    if (!keywordRepository.existsById(id)) {
      throw new NotFoundException("Keyword not found: " + id);
    }
    keywordRepository.deleteById(id);
  }

  private Category resolveCategory(Long categoryId) {
    if (categoryId == null) {
      return null;
    }
    return categoryRepository.findById(categoryId)
        .orElseThrow(() -> new NotFoundException("Category not found: " + categoryId));
  }
}
