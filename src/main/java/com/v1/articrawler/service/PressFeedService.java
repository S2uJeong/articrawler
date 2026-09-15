package com.v1.articrawler.service;

import com.v1.articrawler.domain.Category;
import com.v1.articrawler.domain.PressFeed;
import com.v1.articrawler.dto.PressFeedRequest;
import com.v1.articrawler.dto.PressFeedResponse;
import com.v1.articrawler.exception.DuplicateResourceException;
import com.v1.articrawler.exception.NotFoundException;
import com.v1.articrawler.repository.CategoryRepository;
import com.v1.articrawler.repository.PressFeedRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PressFeedService {

  private final PressFeedRepository pressFeedRepository;
  private final CategoryRepository categoryRepository;

  public PressFeedService(PressFeedRepository pressFeedRepository, CategoryRepository categoryRepository) {
    this.pressFeedRepository = pressFeedRepository;
    this.categoryRepository = categoryRepository;
  }

  public List<PressFeedResponse> findAll() {
    return pressFeedRepository.findAll().stream().map(PressFeedResponse::from).toList();
  }

  @Transactional
  public PressFeedResponse create(PressFeedRequest request) {
    if (pressFeedRepository.existsByFeedUrl(request.feedUrl())) {
      throw new DuplicateResourceException("Press feed already exists: " + request.feedUrl());
    }
    PressFeed pressFeed = new PressFeed(request.name(), request.feedUrl(), resolveCategory(request.categoryId()), request.enabled());
    return PressFeedResponse.from(pressFeedRepository.save(pressFeed));
  }

  @Transactional
  public PressFeedResponse update(Long id, PressFeedRequest request) {
    PressFeed pressFeed = pressFeedRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Press feed not found: " + id));
    pressFeed.setName(request.name());
    pressFeed.setFeedUrl(request.feedUrl());
    pressFeed.setCategory(resolveCategory(request.categoryId()));
    pressFeed.setEnabled(request.enabled());
    return PressFeedResponse.from(pressFeed);
  }

  @Transactional
  public void delete(Long id) {
    if (!pressFeedRepository.existsById(id)) {
      throw new NotFoundException("Press feed not found: " + id);
    }
    pressFeedRepository.deleteById(id);
  }

  private Category resolveCategory(Long categoryId) {
    if (categoryId == null) {
      return null;
    }
    return categoryRepository.findById(categoryId)
        .orElseThrow(() -> new NotFoundException("Category not found: " + categoryId));
  }
}
