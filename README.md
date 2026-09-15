# articrawler

관심 분야(기본값: 금융) 뉴스를 자동으로 수집해서 사내에서 한눈에 볼 수 있게 모아주는 뉴스 대시보드입니다. 백엔드는 Spring Boot(Java 21) + H2 파일 DB, 프론트엔드는 React + TypeScript로 되어 있습니다. 설계 배경은 `develop-guide.md`를 참고하세요.

## 요구사항

- **Java 21** (JDK) — Gradle Wrapper가 포함되어 있어 Gradle을 따로 설치할 필요는 없습니다.
- **Node.js 18 이상** / npm — 프론트엔드 개발 서버용입니다.

## 빠른 시작 (개발 모드)

```bash
git clone <이 저장소 URL>
cd articrawler
```

### 1) 백엔드 실행

```bash
./gradlew bootRun
```

- 기본적으로 **http://localhost:8080** 에서 뜹니다.
- 최초 실행 시 `data/` 폴더에 H2 DB 파일이 자동으로 생성되고, 기본 카테고리("금융")·키워드(금융/증권/금리/은행/채권/ETF)·언론사 RSS 피드(연합뉴스·뉴시스·동아일보·경향신문·MBN·한겨레·조선일보)가 자동으로 시딩됩니다. 별도 DB 설치나 초기 데이터 입력이 필요 없습니다.
- 서버가 뜬 뒤 약 10초 후 첫 뉴스 수집이 자동 실행되고, 이후 15분 간격으로 반복됩니다. 수집 상태는 관리(Admin) 페이지의 "수집 작업 및 오류 현황"에서 확인할 수 있습니다.

### 2) 프론트엔드 실행 (새 터미널)

```bash
cd frontend
npm install
npm run dev
```

- **http://localhost:5173** 에서 뜨고, `/api` 요청은 자동으로 백엔드(기본 8080)로 프록시됩니다.

### 3) 브라우저에서 확인

http://localhost:5173 접속하면 뉴스 목록이 바로 보입니다. 컬렉션이 아직 안 끝났으면 첫 화면은 비어있을 수 있으니, 관리 페이지에서 "지금 수집 실행"을 눌러 바로 채울 수 있습니다.

## 포트가 이미 사용 중이라면

백엔드 포트를 바꿔서 띄우고 싶으면:

```bash
SERVER_PORT=8099 ./gradlew bootRun
```

이 경우 `frontend/vite.config.ts`의 `server.proxy['/api'].target`도 같은 포트로 맞춰줘야 프론트엔드가 정상적으로 API를 호출합니다.

## 프로덕션 빌드 (단일 서버로 배포)

설계 가이드대로 별도 프론트엔드 호스팅 없이 Spring Boot 서버 하나로 운영하려면:

```bash
./gradlew copyFrontend bootJar
java -jar build/libs/articrawler-0.0.1-SNAPSHOT.jar
```

`copyFrontend`가 프론트엔드를 빌드해서 `src/main/resources/static`에 복사하고, `bootJar`가 이를 포함한 단일 실행 가능 jar를 만듭니다. Node가 없는 환경에서 백엔드만 빌드/실행하려면 `copyFrontend` 없이 그냥 `./gradlew bootJar`를 쓰면 됩니다(이 경우 정적 프론트엔드는 포함되지 않습니다).

## 주요 기능

- 관심 카테고리/키워드/언론사 RSS 피드 관리 (관리 페이지에서 추가·삭제·on-off)
- Google News RSS(카테고리/키워드 검색) + 언론사 직접 RSS(연합뉴스 등) 병행 수집, 15분 주기 스케줄러
- 링크·제목 기반 중복 제거, 증분 수집(마지막 수집 시각 기준 + 안전 오버랩)
- 기사 목록 검색/필터(키워드, 카테고리, 언론사) + 대표 이미지 + 원문 이동
- 클릭 집계 기반 일간/주간/월간 인기 기사, 급상승 기사
- 카테고리별/언론사별 통계, 기간별 수집 현황
- CSV/JSON 다운로드
- 수집 작업 및 오류 현황, 수동 수집 실행, 기존 기사 이미지 백필

## 프로젝트 구조

```
articrawler/
├── src/main/java/com/v1/articrawler/
│   ├── domain/        # JPA 엔티티 (Article, Category, Keyword, PressFeed, ...)
│   ├── repository/    # Spring Data JPA 리포지토리
│   ├── news/           # RSS 수집/파싱/보강 로직 (Google News, 언론사 직접 피드)
│   ├── service/        # 검색, 통계, 내보내기 등 서비스 계층
│   ├── controller/     # REST API
│   └── config/         # CORS, 초기 데이터 시딩 등
├── src/main/resources/application.properties
├── frontend/            # React + TypeScript (Vite)
├── data/                # H2 DB 파일 (최초 실행 시 자동 생성, git에는 포함 안 됨)
├── develop-guide.md      # 최초 요구사항/설계 문서
├── report.md             # 언론사 RSS 조사 보고서 (이미지 확보 방식별 정리)
└── develop-history.md    # 개발 이력
```

## 참고

- H2 콘솔: 서버 실행 중 http://localhost:8080/h2-console 에서 JDBC URL `jdbc:h2:file:./data/articrawler`, 사용자 `sa`, 비밀번호 빈 칸으로 접속하면 DB를 직접 조회할 수 있습니다.
- 수집 주기·타임아웃 등은 `application.properties`의 `articrawler.collection.*` 값으로 조정할 수 있습니다.
