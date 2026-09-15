# 언론사 RSS 피드 조사 보고서

## 배경

뉴스 목록 페이지에서 기사 대표 이미지가 전부 "NEWS" 플레이스홀더로만 보이는 문제가 있었다. 원인은 기존에 사용하던 Google News RSS의 기사 링크(`news.google.com/rss/articles/...`)가 실제 언론사 페이지가 아니라 Google이 만든 중간 페이지로 연결되기 때문이었다. 이 중간 페이지는 브라우저의 자바스크립트로만 실제 기사로 리디렉션되므로, 서버가 단순 HTTP 요청으로 읽으면 Google의 generic한 페이지만 읽히고, 거기서 이미지를 긁으면 모든 기사에 똑같은 의미 없는 이미지가 붙는다. 실제로 Google News RSS 문서 전체를 확인해도 `media:content`, `enclosure`, `img` 태그가 전혀 없어 RSS 자체에서 이미지를 얻는 것도 불가능함을 확인했다.

해결책은 Google News를 거치지 않고 언론사가 직접 제공하는 RSS를 수집하는 것이다. 직접 링크는 실제 기사 URL이므로 (1) RSS 자체에 이미지가 내장되어 있거나 (2) 없더라도 기사 페이지에서 og:image를 정상적으로 스크래핑할 수 있다.

## 조사 방법

경제/금융 섹션 RSS를 제공할 것으로 추정되는 언론사 URL을 후보로 잡아 `curl`로 직접 요청해 응답 코드, 아이템 개수, 링크가 직접 링크인지, RSS 안에 이미지 관련 태그(`media:content`, `media:thumbnail`, `enclosure`, 본문 내 `<img>`)가 있는지를 확인했다. 이미지가 없는 경우엔 실제 기사 페이지에 접속해 `og:image` 메타 태그가 존재하는지 추가로 확인했다.

## 결과

### 바로 사용 가능 — 코드 수정 없이 현재 파서로 작동

| 언론사 | RSS URL | 이미지 확보 방식 | 비고 |
|---|---|---|---|
| 연합뉴스 (경제) | `https://www.yna.co.kr/rss/economy.xml` | `<media:content url="...">` 태그로 이미지가 RSS 안에 직접 포함 | 이미 등록됨. 검증한 것 중 가장 안정적 |
| 뉴시스 (경제) | `https://newsis.com/RSS/economy.xml` | `<description>` 본문 안에 `<img src="...">` 태그 포함 | 이미 등록됨 |
| 동아일보 (경제) | `https://rss.donga.com/economy.xml` | `<media:content>` 태그로 이미지 내장 | 연합뉴스와 동일한 방식으로 가장 안정적 |
| 경향신문 (경제) | `https://www.khan.co.kr/rss/rssdata/economy_news.xml` | RSS엔 이미지 없음 → 직접 링크라 기사 페이지의 `og:image` 스크래핑으로 확보 (실제 확인함) | |
| MBN (경제) | `https://www.mbn.co.kr/rss/economy` | 위와 동일 (`og:image` 스크래핑) | |
| 한겨레 (경제) | `https://www.hani.co.kr/rss/economy/` | `<description>` 안에 `<img>` 태그 포함 | 이미지는 있으나 본문 텍스트가 거의 없어(표 마크업만 있는 경우 다수) 요약문은 비어있는 경우가 많음 |
| 한국경제 | `https://www.hankyung.com/feed/economy` | RSS엔 이미지 없음 → `og:image` 스크래핑으로 확보 (사전에 검증함) | |

### 코드 수정이 필요했던 것

| 언론사 | RSS URL | 문제 | 조치 |
|---|---|---|---|
| 조선일보 (경제) | `https://www.chosun.com/arc/outboundfeeds/rss/category/economy/?outputType=xml` | 이미지가 `<description>`이 아니라 `<content:encoded>` 태그 안에 들어있어서 기존 파서가 찾지 못함 (`<description>` 자체는 비어있음) | `DirectRssFeedClient`가 `content:encoded`도 함께 확인하도록 수정 |

### 막혀있거나 사용 불가능

| 언론사 | 사유 |
|---|---|
| 중앙일보 | 시도한 RSS 엔드포인트가 "서비스 종료 안내" HTML 페이지를 반환 — 해당 RSS 서비스 자체가 폐지된 것으로 보임 |
| 매일경제 | HTTP 403 — 봇 차단으로 추정 |
| 헤럴드경제, 이데일리 | 시도한 URL이 RSS가 아닌 일반 HTML 페이지를 반환 (경로가 틀렸을 가능성, 정확한 엔드포인트 추가 조사 필요) |
| 아시아경제, 서울경제, YTN 경제, 파이낸셜뉴스 | HTTP 404 — 경로가 틀림, 정확한 엔드포인트 추가 조사 필요 |
| 머니투데이 | 찾아낸 URL(`rss.mt.co.kr/mt_news.xml`)이 경제 전용이 아니라 생활·세계·정책 등이 뒤섞인 전체 뉴스 피드라 "금융" 카테고리로 등록하기엔 부적합 |

## 결론 및 조치

바로 사용 가능한 4곳(동아일보, 경향신문, MBN, 한겨레)과 코드 수정이 필요했던 조선일보까지 총 5곳을 이번에 추가했다. "막혀있거나 사용 불가능" 항목은 추가 URL 조사가 필요한 상태로 남겨둔다.
