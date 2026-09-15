package com.v1.articrawler.service;

import com.v1.articrawler.domain.Category;
import com.v1.articrawler.dto.CategoryRequest;
import com.v1.articrawler.dto.CategoryResponse;
import com.v1.articrawler.exception.DuplicateResourceException;
import com.v1.articrawler.exception.NotFoundException;
import com.v1.articrawler.repository.CategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CategoryService {

  private final CategoryRepository categoryRepository;

  public CategoryService(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  public List<CategoryResponse> findAll() {
    return categoryRepository.findAll().stream().map(CategoryResponse::from).toList();
  }

  @Transactional
  public CategoryResponse create(CategoryRequest request) {
    if (categoryRepository.existsByName(request.name())) {
      throw new DuplicateResourceException("Category already exists: " + request.name());
    }
    Category category = new Category(request.name(), request.providerTopic(), request.enabled());
    return CategoryResponse.from(categoryRepository.save(category));
  }

  @Transactional
  public CategoryResponse update(Long id, CategoryRequest request) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Category not found: " + id));
    category.setName(request.name());
    category.setProviderTopic(request.providerTopic());
    category.setEnabled(request.enabled());
    return CategoryResponse.from(category);
  }

  @Transactional
  public void delete(Long id) {
    if (!categoryRepository.existsById(id)) {
      throw new NotFoundException("Category not found: " + id);
    }
    categoryRepository.deleteById(id);
  }
}
