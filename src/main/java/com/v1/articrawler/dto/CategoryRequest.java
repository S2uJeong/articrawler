package com.v1.articrawler.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(@NotBlank String name, String providerTopic, boolean enabled) {}
