package com.v1.articrawler.dto;

import jakarta.validation.constraints.NotBlank;

public record PressFeedRequest(@NotBlank String name, @NotBlank String feedUrl, Long categoryId, boolean enabled) {}
