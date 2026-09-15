import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { CategoryCount, DailyCollectionStat, KeywordCount } from '../types';

function BarList({ items, labelKey, valueKey }: { items: any[]; labelKey: string; valueKey: string }) {
  const max = Math.max(1, ...items.map((i) => i[valueKey]));
  return (
    <div className="bar-list">
      {items.map((item) => (
        <div className="bar-row" key={item[labelKey]}>
          <span className="bar-label">{item[labelKey]}</span>
          <div className="bar-track">
            <div className="bar-fill" style={{ width: `${(item[valueKey] / max) * 100}%` }} />
          </div>
          <span className="bar-value">{item[valueKey].toLocaleString()}</span>
        </div>
      ))}
      {items.length === 0 && <div className="empty">데이터가 없습니다.</div>}
    </div>
  );
}

export default function StatsPage() {
  const [categoryCounts, setCategoryCounts] = useState<CategoryCount[]>([]);
  const [keywordCounts, setKeywordCounts] = useState<KeywordCount[]>([]);
  const [dailyStats, setDailyStats] = useState<DailyCollectionStat[]>([]);

  useEffect(() => {
    api.get<CategoryCount[]>('/stats/categories').then(setCategoryCounts).catch(() => {});
    api.get<KeywordCount[]>('/stats/keywords').then(setKeywordCounts).catch(() => {});
    api.get<DailyCollectionStat[]>('/stats/collection?days=14').then(setDailyStats).catch(() => {});
  }, []);

  return (
    <div className="stats-grid">
      <section className="stats-card">
        <h2>카테고리별 기사 수</h2>
        <BarList items={categoryCounts} labelKey="category" valueKey="count" />
      </section>

      <section className="stats-card">
        <h2>키워드별 기사 수</h2>
        <BarList items={keywordCounts} labelKey="keyword" valueKey="count" />
      </section>

      <section className="stats-card stats-card-wide">
        <h2>기간별 수집 현황 (최근 14일)</h2>
        <table className="stats-table">
          <thead>
            <tr>
              <th>날짜</th>
              <th>수집 작업 수</th>
              <th>조회된 기사</th>
              <th>신규 기사</th>
              <th>실패</th>
            </tr>
          </thead>
          <tbody>
            {dailyStats.map((d) => (
              <tr key={d.date}>
                <td>{d.date}</td>
                <td>{d.runCount}</td>
                <td>{d.fetchedCount}</td>
                <td>{d.newCount}</td>
                <td className={d.failedCount > 0 ? 'stat-fail' : ''}>{d.failedCount}</td>
              </tr>
            ))}
            {dailyStats.length === 0 && (
              <tr>
                <td colSpan={5} className="empty">
                  데이터가 없습니다.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </section>
    </div>
  );
}
