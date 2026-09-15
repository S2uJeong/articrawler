package com.v1.articrawler.news;

/** Outcome of one image-backfill pass. */
public record BackfillResult(int scanned, int updated) {}
