package com.v1.articrawler.controller;

import com.v1.articrawler.dto.PressFeedRequest;
import com.v1.articrawler.dto.PressFeedResponse;
import com.v1.articrawler.service.PressFeedService;
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
@RequestMapping("/api/press-feeds")
public class PressFeedController {

  private final PressFeedService pressFeedService;

  public PressFeedController(PressFeedService pressFeedService) {
    this.pressFeedService = pressFeedService;
  }

  @GetMapping
  public List<PressFeedResponse> findAll() {
    return pressFeedService.findAll();
  }

  @PostMapping
  public PressFeedResponse create(@Valid @RequestBody PressFeedRequest request) {
    return pressFeedService.create(request);
  }

  @PutMapping("/{id}")
  public PressFeedResponse update(@PathVariable Long id, @Valid @RequestBody PressFeedRequest request) {
    return pressFeedService.update(id, request);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    pressFeedService.delete(id);
  }
}
