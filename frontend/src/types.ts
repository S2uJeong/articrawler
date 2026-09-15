export interface Article {
  id: number;
  title: string;
  summary: string | null;
  imageUrl: string | null;
  sourceName: string | null;
  category: string | null;
  publishedAt: string | null;
  collectedAt: string;
  viewCount: number;
  link: string;
}

export interface PageResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface Category {
  id: number;
  name: string;
  providerTopic: string | null;
  enabled: boolean;
  lastCollectedAt: string | null;
}

export interface Keyword {
  id: number;
  keyword: string;
  categoryId: number | null;
  categoryName: string | null;
  enabled: boolean;
  lastCollectedAt: string | null;
}

export interface PressFeed {
  id: number;
  name: string;
  feedUrl: string;
  categoryId: number | null;
  categoryName: string | null;
  enabled: boolean;
  lastCollectedAt: string | null;
}

export interface PopularArticle {
  article: Article;
  clickCount: number;
}

export interface TrendingArticle {
  article: Article;
  recentClicks: number;
  previousClicks: number;
  growthRatio: number;
}

export interface CategoryCount {
  category: string;
  count: number;
}

export interface KeywordCount {
  keyword: string;
  count: number;
}

export interface SourceCount {
  sourceName: string;
  count: number;
}

export type CollectionSourceType = 'CATEGORY' | 'KEYWORD' | 'PRESS';
export type CollectionStatus = 'SUCCESS' | 'PARTIAL' | 'FAILED';

export interface CollectionRun {
  id: number;
  sourceType: CollectionSourceType;
  sourceName: string;
  status: CollectionStatus;
  startedAt: string;
  finishedAt: string | null;
  fetchedCount: number;
  newCount: number;
  duplicateCount: number;
  errorMessage: string | null;
}

export interface DailyCollectionStat {
  date: string;
  runCount: number;
  fetchedCount: number;
  newCount: number;
  failedCount: number;
}
