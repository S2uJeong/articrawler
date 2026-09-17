import { NavLink, Route, Routes } from 'react-router-dom';
import NewsListPage from './pages/NewsListPage';
import PopularPage from './pages/PopularPage';
import StatsPage from './pages/StatsPage';
// AdminPage는 화면 탭에서 숨김 처리. 백엔드 및 라우트는 유지.
import AdminPage from './pages/AdminPage';

export default function App() {
  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="app-header-inner">
          <span className="brand">뉴스 대시보드</span>
          <nav className="app-nav">
            <NavLink to="/" end className={({ isActive }) => (isActive ? 'active' : '')}>
              뉴스 목록
            </NavLink>
            <NavLink to="/popular" className={({ isActive }) => (isActive ? 'active' : '')}>
              인기 뉴스
            </NavLink>
            {/* 통계 탭은 화면에서 숨김 처리. 백엔드 및 라우트는 유지. */}
          </nav>
        </div>
      </header>
      <main className="app-main">
        <Routes>
          <Route path="/" element={<NewsListPage />} />
          <Route path="/popular" element={<PopularPage />} />
          <Route path="/stats" element={<StatsPage />} />
          <Route path="/admin" element={<AdminPage />} />
        </Routes>
      </main>
    </div>
  );
}
