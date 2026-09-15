import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { PopularArticle, TrendingArticle } from '../types';
import ArticleCard from '../components/ArticleCard';

type Tab = 'daily' | 'weekly' | 'monthly' | 'trending';

const TABS: { key: Tab; label: string }[] = [
  { key: 'daily', label: '일간 인기' },
  { key: 'weekly', label: '주간 인기' },
  { key: 'monthly', label: '월간 인기' },
  { key: 'trending', label: '급상승' },
];

export default function PopularPage() {
  const [tab, setTab] = useState<Tab>('daily');
  const [popular, setPopular] = useState<PopularArticle[]>([]);
  const [trending, setTrending] = useState<TrendingArticle[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    if (tab === 'trending') {
      api
        .get<TrendingArticle[]>('/stats/trending?limit=12')
        .then(setTrending)
        .finally(() => setLoading(false));
    } else {
      api
        .get<PopularArticle[]>(`/stats/popular?period=${tab}&limit=12`)
        .then(setPopular)
        .finally(() => setLoading(false));
    }
  }, [tab]);

  return (
    <div>
      <div className="tab-bar">
        {TABS.map((t) => (
          <button
            key={t.key}
            className={tab === t.key ? 'tab active' : 'tab'}
            onClick={() => setTab(t.key)}
          >
            {t.label}
          </button>
        ))}
      </div>

      {loading && <div className="loading">불러오는 중...</div>}

      {tab !== 'trending' && !loading && (
        <div className="article-grid">
          {popular.map((p, idx) => (
            <ArticleCard key={p.article.id} article={p.article} badge={`#${idx + 1} 클릭 ${p.clickCount}`} />
          ))}
          {popular.length === 0 && <div className="empty">아직 클릭 데이터가 없습니다.</div>}
        </div>
      )}

      {tab === 'trending' && !loading && (
        <div className="article-grid">
          {trending.map((t) => (
            <ArticleCard
              key={t.article.id}
              article={t.article}
              badge={`최근 ${t.recentClicks} / 이전 ${t.previousClicks}`}
            />
          ))}
          {trending.length === 0 && <div className="empty">급상승 기사가 없습니다.</div>}
        </div>
      )}
    </div>
  );
}
