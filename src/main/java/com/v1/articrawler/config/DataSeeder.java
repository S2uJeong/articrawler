package com.v1.articrawler.config;

import com.v1.articrawler.domain.Category;
import com.v1.articrawler.domain.Keyword;
import com.v1.articrawler.domain.PressFeed;
import com.v1.articrawler.repository.CategoryRepository;
import com.v1.articrawler.repository.KeywordRepository;
import com.v1.articrawler.repository.PressFeedRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Seeds the default interest categories/keywords from the guide's example (금융, 증권, 금리, 은행, 채권, ETF). */
@Component
public class DataSeeder implements CommandLineRunner {

  private final CategoryRepository categoryRepository;
  private final KeywordRepository keywordRepository;
  private final PressFeedRepository pressFeedRepository;

  public DataSeeder(
      CategoryRepository categoryRepository,
      KeywordRepository keywordRepository,
      PressFeedRepository pressFeedRepository) {
    this.categoryRepository = categoryRepository;
    this.keywordRepository = keywordRepository;
    this.pressFeedRepository = pressFeedRepository;
  }

  @Override
  public void run(String... args) {
    if (categoryRepository.count() > 0 || keywordRepository.count() > 0) {
      return;
    }

    Category finance = categoryRepository.save(new Category("금융", "BUSINESS", true));

    for (String kw : new String[] {"금융", "증권", "금리", "은행", "채권", "ETF"}) {
      keywordRepository.save(new Keyword(kw, finance, true));
    }

    // Direct press RSS feeds - real article links, so og:image enrichment actually works. Image
    // source per outlet is documented in report.md; all verified by hand before adding.
    pressFeedRepository.save(new PressFeed("연합뉴스", "https://www.yna.co.kr/rss/economy.xml", finance, true));
    pressFeedRepository.save(new PressFeed("뉴시스", "https://newsis.com/RSS/economy.xml", finance, true));
    pressFeedRepository.save(new PressFeed("동아일보", "https://rss.donga.com/economy.xml", finance, true));
    pressFeedRepository.save(
        new PressFeed("경향신문", "https://www.khan.co.kr/rss/rssdata/economy_news.xml", finance, true));
    pressFeedRepository.save(new PressFeed("MBN", "https://www.mbn.co.kr/rss/economy", finance, true));
    pressFeedRepository.save(new PressFeed("한겨레", "https://www.hani.co.kr/rss/economy/", finance, true));
    pressFeedRepository.save(
        new PressFeed(
            "조선일보",
            "https://www.chosun.com/arc/outboundfeeds/rss/category/economy/?outputType=xml",
            finance,
            true));
  }
}
