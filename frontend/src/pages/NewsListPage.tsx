import { FormEvent, useEffect, useState } from 'react';
import { api, buildQuery } from '../api/client';
import { Article, Category, Keyword, PageResult, SourceCount } from '../types';
import ArticleCard from '../components/ArticleCard';
import Pagination from '../components/Pagination';

function formatDate(value: string | null): string {
  if (!value) return '-';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  return d.toLocaleString('ko-KR');
}

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

  const [newKeyword, setNewKeyword] = useState('');
  const [newKeywordCategoryId, setNewKeywordCategoryId] = useState('');

  function loadKeywords() {
    api.get<Keyword[]>('/keywords').then(setKeywords).catch(() => {});
  }

  useEffect(() => {
    api.get<Category[]>('/categories').then(setCategories).catch(() => {});
    loadKeywords();
    // Every press byline actually seen in the data (not just outlets we registered as a direct
    // feed) - e.g. MBC뉴스/머니투데이 only ever arrive via Google News, but should still be filterable.
    api.get<SourceCount[]>('/stats/sources').then(setSources).catch(() => {});
  }, []);

  async function addKeyword(e: FormEvent) {
    e.preventDefault();
    if (!newKeyword.trim()) return;
    await api.post('/keywords', {
      keyword: newKeyword.trim(),
      categoryId: newKeywordCategoryId ? Number(newKeywordCategoryId) : null,
      enabled: true,
    });
    setNewKeyword('');
    setNewKeywordCategoryId('');
    loadKeywords();
  }

  async function toggleKeyword(k: Keyword) {
    await api.put(`/keywords/${k.id}`, { keyword: k.keyword, categoryId: k.categoryId, enabled: !k.enabled });
    loadKeywords();
  }

  async function deleteKeyword(k: Keyword) {
    if (!confirm(`"${k.keyword}" 키워드를 삭제하시겠습니까?`)) return;
    await api.delete(`/keywords/${k.id}`);
    loadKeywords();
  }

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
    <div className="news-layout">
      <aside className="news-sidebar admin-card">
        <h2>관심 키워드 관리</h2>
        <p className="hint">검색 기반으로 수집할 키워드를 등록합니다. 카테고리를 지정하면 그 카테고리로 분류됩니다.</p>
        <form className="keyword-form-vertical" onSubmit={addKeyword}>
          <input placeholder="키워드 (예: ETF)" value={newKeyword} onChange={(e) => setNewKeyword(e.target.value)} />
          <select value={newKeywordCategoryId} onChange={(e) => setNewKeywordCategoryId(e.target.value)}>
            <option value="">카테고리 없음</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
          <button type="submit">추가</button>
        </form>
        <div className="keyword-list">
          {keywords.map((k) => (
            <div className="keyword-item" key={k.id}>
              <div className="keyword-item-main">
                <div className="keyword-item-name">{k.keyword}</div>
                <div className="keyword-item-meta">
                  {k.categoryName || '카테고리 없음'} · {formatDate(k.lastCollectedAt)}
                </div>
              </div>
              <input type="checkbox" checked={k.enabled} onChange={() => toggleKeyword(k)} />
              <button className="link-btn" onClick={() => deleteKeyword(k)}>
                삭제
              </button>
            </div>
          ))}
          {keywords.length === 0 && <div className="empty">등록된 키워드가 없습니다.</div>}
        </div>
      </aside>

      <div className="news-main">
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
            <div className="article-grid-3col">
              {result.content.map((a) => (
                <ArticleCard key={a.id} article={a} />
              ))}
            </div>
            {result.content.length === 0 && <div className="empty">수집된 기사가 없습니다.</div>}
            <Pagination page={page} totalPages={result.totalPages} onChange={setPage} />
          </>
        )}
      </div>
    </div>
  );
}
