package com.money.kark_profit.service.provider;

import com.money.kark_profit.constants.ApplicationCode;
import com.money.kark_profit.exception.SystemException;
import com.money.kark_profit.transform.request.NewsClassifierRequest;
import com.money.kark_profit.transform.response.NewsArticleResponse;
import com.money.kark_profit.utils.ResponseBuilderUtils;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.net.URLConnection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsAggregatorProvider {

    // Cache the full news aggregation for 15 min
    @Cacheable(value = "newsCache")
    public List<NewsArticleResponse> getAllNews() {
        List<NewsArticleResponse> news = new java.util.ArrayList<>();
        news.addAll(fetch("https://feeds.bbci.co.uk/news/rss.xml", "BBC"));
        news.addAll(fetch("https://www.reuters.com/world/rss", "Reuters"));
        news.addAll(fetch("https://www.aljazeera.com/xml/rss/all.xml", "AlJazeera"));

        return news.stream()
                .sorted((a,b) -> b.getPublishedAt().compareTo(a.getPublishedAt()))
                .limit(50)
                .toList();
    }

    public ResponseBuilderUtils<List<NewsArticleResponse>> getFilteredNews(String category, String market, String impact, String keyword) {
        List<NewsArticleResponse> newsResult =  getAllNews().stream()
                .filter(n -> category == null || n.getCategory().equalsIgnoreCase(category))
                .filter(n -> market == null || java.util.Arrays.asList(n.getMarketTags()).contains(market.toUpperCase()))
                .filter(n -> impact == null || n.getImpact().equalsIgnoreCase(impact))
                .filter(n -> keyword == null || n.getTitle().toLowerCase().contains(keyword.toLowerCase())
                        || n.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
        return new ResponseBuilderUtils<>(
                ApplicationCode.HTTP_200,
                ApplicationCode.FETCH,
                newsResult
        );
    }

    public List<NewsArticleResponse> fetch(String url, String source) {
        try {
            URL feedUrl = new URL(url);
            URLConnection conn = feedUrl.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

            SyndFeed feed = new SyndFeedInput().build(new XmlReader(conn));

            return feed.getEntries().stream().map(entry -> {
                NewsArticleResponse article = new NewsArticleResponse();
                article.setSource(source);
                article.setTitle(entry.getTitle());
                article.setLink(entry.getLink());
                article.setDescription(entry.getDescription() != null ? entry.getDescription().getValue() : "");
                article.setPublishedAt(entry.getPublishedDate() != null ? entry.getPublishedDate().toInstant().toString() : "");

                String text = article.getTitle() + " " + article.getDescription();
                String category = NewsClassifierRequest.classify(text);
                article.setCategory(category);
                article.setImpact(NewsClassifierRequest.detectImpact(category));
                article.setMarketTags(NewsClassifierRequest.detectMarkets(category));
                return article;
            }).toList();

        } catch (Exception e) {
            throw new SystemException(e.getMessage());
        }
    }
}