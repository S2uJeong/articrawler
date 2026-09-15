import { Article } from '../types';

function formatDate(value: string | null): string {
  if (!value) return '-';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  return d.toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export default function ArticleCard({ article, badge }: { article: Article; badge?: string }) {
  return (
    <a
      className="article-card"
      href={`/api/articles/${article.id}/open`}
      target="_blank"
      rel="noreferrer"
    >
      <div className="article-thumb">
        {article.imageUrl ? (
          <img src={article.imageUrl} alt="" loading="lazy" />
        ) : (
          <div className="article-thumb-placeholder">NEWS</div>
        )}
      </div>
      <div className="article-body">
        <div className="article-meta">
          {article.category && <span className="tag">{article.category}</span>}
          {article.sourceName && <span className="source">{article.sourceName}</span>}
          <span className="date">{formatDate(article.publishedAt)}</span>
          {badge && <span className="badge">{badge}</span>}
        </div>
        <h3 className="article-title">{article.title}</h3>
        {article.summary && <p className="article-summary">{article.summary}</p>}
        <div className="article-footer">
          <span>조회 {article.viewCount.toLocaleString()}</span>
        </div>
      </div>
    </a>
  );
}
