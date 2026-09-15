import { useEffect, useState } from 'react';
import { api, buildQuery } from '../api/client';
import { Article, Category, Keyword, PageResult, SourceCount } from '../types';
import ArticleCard from '../components/ArticleCard';
import Pagination from '../components/Pagination';

export default function NewsListPage() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [keywords, setKeywords] = useState<Keyword[]>([]);
  const [sources, setSources] = useState<SourceCount[]>([]);
  const [query, setQuery] = useState('');
  const [category, setCategory] = useState('');
  const [keyword, setKeyword] = useState('');
  const [source, setSource] = useState('');
  const [page, setPage] = useState(0);
  const [result, setResult] = useState<PageResult<Article> | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.get<Category[]>('/categories').then(setCategories).catch(() => {});
    api.get<Keyword[]>('/keywords').then(setKeywords).catch(() => {});
    // Every press byline actually seen in the data (not just outlets we registered as a direct
    // feed) - e.g. MBC뉴스/머니투데이 only ever arrive via Google News, but should still be filterable.
    api.get<SourceCount[]>('/stats/sources').then(setSources).catch(() => {});
  }, []);

  useEffect(() => {
    setLoading(true);
    setError(null);
    const qs = buildQuery({ q: query, category, keyword, source, page, size: 12 });
    api
      .get<PageResult<Article>>(`/articles${qs}`)
      .then(setResult)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [query, category, keyword, source, page]);

  function handleSearchSubmit(e: React.FormEvent) {
    e.preventDefault();
    setPage(0);
  }

  return (
    <div>
      <form className="filter-bar" onSubmit={handleSearchSubmit}>
        <input
          type="search"
          placeholder="제목/요약 검색"
          defaultValue={query}
          onBlur={(e) => {
            setQuery(e.target.value);
            setPage(0);
          }}
        />
        <select
          value={category}
          onChange={(e) => {
            setCategory(e.target.value);
            setPage(0);
          }}
        >
          <option value="">전체 카테고리</option>
          {categories.map((c) => (
            <option key={c.id} value={c.name}>
              {c.name}
            </option>
          ))}
        </select>
        <select
          value={keyword}
          onChange={(e) => {
            setKeyword(e.target.value);
            setPage(0);
          }}
        >
          <option value="">전체 키워드</option>
          {keywords.map((k) => (
            <option key={k.id} value={k.keyword}>
              {k.keyword}
            </option>
          ))}
        </select>
        <select
          value={source}
          onChange={(e) => {
            setSource(e.target.value);
            setPage(0);
          }}
        >
          <option value="">전체 언론사</option>
          {sources.map((s) => (
            <option key={s.sourceName} value={s.sourceName}>
              {s.sourceName} ({s.count})
            </option>
          ))}
        </select>
        <button type="submit">검색</button>
        <a
          className="export-link"
          href={`/api/export/articles.csv${buildQuery({ q: query, category, keyword, source })}`}
        >
          CSV 다운로드
        </a>
        <a
          className="export-link"
          href={`/api/export/articles.json${buildQuery({ q: query, category, keyword, source })}`}
        >
          JSON 다운로드
        </a>
      </form>

      {error && <div className="error-box">{error}</div>}
      {loading && <div className="loading">불러오는 중...</div>}

      {result && (
        <>
          <div className="result-count">총 {result.totalElements.toLocaleString()}건</div>
          <div className="article-grid">
            {result.content.map((a) => (
              <ArticleCard key={a.id} article={a} />
            ))}
          </div>
          {result.content.length === 0 && <div className="empty">수집된 기사가 없습니다.</div>}
          <Pagination page={page} totalPages={result.totalPages} onChange={setPage} />
        </>
      )}
    </div>
  );
}
