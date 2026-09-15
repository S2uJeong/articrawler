package com.v1.articrawler.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/** Explicit, stable page shape for API responses (Spring's PageImpl is not a supported wire format). */
public record PageResponse<T>(
    List<T> content, int number, int size, long totalElements, int totalPages, boolean first, boolean last) {

  public static <T> PageResponse<T> from(Page<T> page) {
    return new PageResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.isFirst(),
        page.isLast());
  }
}
