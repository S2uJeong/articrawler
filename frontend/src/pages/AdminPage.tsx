import { FormEvent, useEffect, useState } from 'react';
import { api } from '../api/client';
import { Category, CollectionRun, Keyword, PressFeed } from '../types';

const SOURCE_TYPE_LABEL: Record<string, string> = { CATEGORY: '카테고리', KEYWORD: '키워드', PRESS: '언론사' };

function formatDate(value: string | null): string {
  if (!value) return '-';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  return d.toLocaleString('ko-KR');
}

export default function AdminPage() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [keywords, setKeywords] = useState<Keyword[]>([]);
  const [pressFeeds, setPressFeeds] = useState<PressFeed[]>([]);
  const [runs, setRuns] = useState<CollectionRun[]>([]);
  const [newCategoryName, setNewCategoryName] = useState('');
  const [newCategoryTopic, setNewCategoryTopic] = useState('');
  const [newKeyword, setNewKeyword] = useState('');
  const [newKeywordCategoryId, setNewKeywordCategoryId] = useState('');
  const [newFeedName, setNewFeedName] = useState('');
  const [newFeedUrl, setNewFeedUrl] = useState('');
  const [newFeedCategoryId, setNewFeedCategoryId] = useState('');
  const [message, setMessage] = useState<string | null>(null);

  function reload() {
    api.get<Category[]>('/categories').then(setCategories).catch(() => {});
    api.get<Keyword[]>('/keywords').then(setKeywords).catch(() => {});
    api.get<PressFeed[]>('/press-feeds').then(setPressFeeds).catch(() => {});
    api.get<CollectionRun[]>('/collection/runs').then(setRuns).catch(() => {});
  }

  useEffect(reload, []);

  async function addCategory(e: FormEvent) {
    e.preventDefault();
    if (!newCategoryName.trim()) return;
    await api.post('/categories', {
      name: newCategoryName.trim(),
      providerTopic: newCategoryTopic.trim() || null,
      enabled: true,
    });
    setNewCategoryName('');
    setNewCategoryTopic('');
    reload();
  }

  async function toggleCategory(c: Category) {
    await api.put(`/categories/${c.id}`, { name: c.name, providerTopic: c.providerTopic, enabled: !c.enabled });
    reload();
  }

  async function deleteCategory(c: Category) {
    if (!confirm(`"${c.name}" 카테고리를 삭제하시겠습니까?`)) return;
    await api.delete(`/categories/${c.id}`);
    reload();
  }

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
    reload();
  }

  async function toggleKeyword(k: Keyword) {
    await api.put(`/keywords/${k.id}`, { keyword: k.keyword, categoryId: k.categoryId, enabled: !k.enabled });
    reload();
  }

  async function deleteKeyword(k: Keyword) {
    if (!confirm(`"${k.keyword}" 키워드를 삭제하시겠습니까?`)) return;
    await api.delete(`/keywords/${k.id}`);
    reload();
  }

  async function addPressFeed(e: FormEvent) {
    e.preventDefault();
    if (!newFeedName.trim() || !newFeedUrl.trim()) return;
    await api.post('/press-feeds', {
      name: newFeedName.trim(),
      feedUrl: newFeedUrl.trim(),
      categoryId: newFeedCategoryId ? Number(newFeedCategoryId) : null,
      enabled: true,
    });
    setNewFeedName('');
    setNewFeedUrl('');
    setNewFeedCategoryId('');
    reload();
  }

  async function togglePressFeed(p: PressFeed) {
    await api.put(`/press-feeds/${p.id}`, {
      name: p.name,
      feedUrl: p.feedUrl,
      categoryId: p.categoryId,
      enabled: !p.enabled,
    });
    reload();
  }

  async function deletePressFeed(p: PressFeed) {
    if (!confirm(`"${p.name}" 피드를 삭제하시겠습니까?`)) return;
    await api.delete(`/press-feeds/${p.id}`);
    reload();
  }

  async function triggerCollection() {
    setMessage('수집 작업을 시작했습니다. 잠시 후 새로고침해 주세요.');
    await api.post('/collection/run');
    setTimeout(reload, 3000);
  }

  async function triggerBackfill() {
    setMessage('기존 기사 이미지 백필 작업을 실행 중입니다...');
    try {
      const result = await api.post<{ scanned: number; updated: number }>('/collection/backfill-images');
      setMessage(`백필 완료: ${result.scanned}건 검사, ${result.updated}건 이미지 업데이트됨.`);
    } catch (e) {
      setMessage(`백필 실패: ${(e as Error).message}`);
    }
  }

  return (
    <div className="admin-grid">
      <section className="admin-card">
        <h2>관심 카테고리 관리</h2>
        <p className="hint">뉴스 제공처의 카테고리(Google News 토픽 섹션 코드)를 등록합니다. 예: BUSINESS</p>
        <form className="inline-form" onSubmit={addCategory}>
          <input
            placeholder="카테고리명 (예: 금융)"
            value={newCategoryName}
            onChange={(e) => setNewCategoryName(e.target.value)}
          />
          <input
            placeholder="Provider Topic (예: BUSINESS)"
            value={newCategoryTopic}
            onChange={(e) => setNewCategoryTopic(e.target.value)}
          />
          <button type="submit">추가</button>
        </form>
        <table className="admin-table">
          <thead>
            <tr>
              <th>이름</th>
              <th>Topic</th>
              <th>마지막 수집</th>
              <th>사용</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {categories.map((c) => (
              <tr key={c.id}>
                <td>{c.name}</td>
                <td>{c.providerTopic || '-'}</td>
                <td>{formatDate(c.lastCollectedAt)}</td>
                <td>
                  <input type="checkbox" checked={c.enabled} onChange={() => toggleCategory(c)} />
                </td>
                <td>
                  <button className="link-btn" onClick={() => deleteCategory(c)}>
                    삭제
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="admin-card">
        <h2>관심 키워드 관리</h2>
        <p className="hint">검색 기반으로 수집할 키워드를 등록합니다. 카테고리를 지정하면 그 카테고리로 분류됩니다.</p>
        <form className="inline-form" onSubmit={addKeyword}>
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
        <table className="admin-table">
          <thead>
            <tr>
              <th>키워드</th>
              <th>카테고리</th>
              <th>마지막 수집</th>
              <th>사용</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {keywords.map((k) => (
              <tr key={k.id}>
                <td>{k.keyword}</td>
                <td>{k.categoryName || '-'}</td>
                <td>{formatDate(k.lastCollectedAt)}</td>
                <td>
                  <input type="checkbox" checked={k.enabled} onChange={() => toggleKeyword(k)} />
                </td>
                <td>
                  <button className="link-btn" onClick={() => deleteKeyword(k)}>
                    삭제
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="admin-card">
        <h2>언론사 RSS 피드 관리</h2>
        <p className="hint">
          언론사가 직접 제공하는 RSS를 등록합니다. Google News와 달리 실제 기사 링크라서 대표 이미지가 정상적으로 표시됩니다.
        </p>
        <form className="inline-form" onSubmit={addPressFeed}>
          <input placeholder="언론사명 (예: 연합뉴스)" value={newFeedName} onChange={(e) => setNewFeedName(e.target.value)} />
          <input
            placeholder="RSS Feed URL"
            value={newFeedUrl}
            onChange={(e) => setNewFeedUrl(e.target.value)}
          />
          <select value={newFeedCategoryId} onChange={(e) => setNewFeedCategoryId(e.target.value)}>
            <option value="">카테고리 없음</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
          <button type="submit">추가</button>
        </form>
        <table className="admin-table">
          <thead>
            <tr>
              <th>언론사</th>
              <th>카테고리</th>
              <th>마지막 수집</th>
              <th>사용</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {pressFeeds.map((p) => (
              <tr key={p.id}>
                <td>{p.name}</td>
                <td>{p.categoryName || '-'}</td>
                <td>{formatDate(p.lastCollectedAt)}</td>
                <td>
                  <input type="checkbox" checked={p.enabled} onChange={() => togglePressFeed(p)} />
                </td>
                <td>
                  <button className="link-btn" onClick={() => deletePressFeed(p)}>
                    삭제
                  </button>
                </td>
              </tr>
            ))}
            {pressFeeds.length === 0 && (
              <tr>
                <td colSpan={5} className="empty">
                  등록된 언론사 피드가 없습니다.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </section>

      <section className="admin-card admin-card-wide">
        <div className="admin-card-header">
          <h2>수집 작업 및 오류 현황</h2>
          <div className="button-group">
            <button onClick={triggerBackfill}>기존 기사 이미지 백필</button>
            <button onClick={triggerCollection}>지금 수집 실행</button>
          </div>
        </div>
        {message && <div className="info-box">{message}</div>}
        <table className="admin-table">
          <thead>
            <tr>
              <th>구분</th>
              <th>대상</th>
              <th>상태</th>
              <th>시작</th>
              <th>조회</th>
              <th>신규</th>
              <th>중복</th>
              <th>오류</th>
            </tr>
          </thead>
          <tbody>
            {runs.map((r) => (
              <tr key={r.id}>
                <td>{SOURCE_TYPE_LABEL[r.sourceType] || r.sourceType}</td>
                <td>{r.sourceName}</td>
                <td className={`status-${r.status.toLowerCase()}`}>{r.status}</td>
                <td>{formatDate(r.startedAt)}</td>
                <td>{r.fetchedCount}</td>
                <td>{r.newCount}</td>
                <td>{r.duplicateCount}</td>
                <td className="error-cell">{r.errorMessage || '-'}</td>
              </tr>
            ))}
            {runs.length === 0 && (
              <tr>
                <td colSpan={8} className="empty">
                  아직 실행된 수집 작업이 없습니다.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </section>
    </div>
  );
}
