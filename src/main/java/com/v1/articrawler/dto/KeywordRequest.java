package com.v1.articrawler.dto;

import jakarta.validation.constraints.NotBlank;

public record KeywordRequest(@NotBlank String keyword, Long categoryId, boolean enabled) {}
