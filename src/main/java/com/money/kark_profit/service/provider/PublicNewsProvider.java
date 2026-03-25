package com.money.kark_profit.service.provider;

import com.money.kark_profit.cache.EconomicEventsCache;
import com.money.kark_profit.cache.StringCache;
import com.money.kark_profit.constants.ApplicationCode;
import com.money.kark_profit.http.RestTemplateHttpClient;
import com.money.kark_profit.transform.request.InsightRequest;
import com.money.kark_profit.transform.response.EventCalendarResponse;
import com.money.kark_profit.transform.response.InsightResponse;
import com.money.kark_profit.transform.response.xml.GoogleNewsXmlResponse;
import com.money.kark_profit.utils.DateUtils;
import com.money.kark_profit.utils.ResponseBuilderUtils;
import com.money.kark_profit.utils.XmlConverterUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicNewsProvider {
    private static String GOOGLE_NEWS = "https://news.google.com/rss/search";
    private static String FOREX_FACTORY = "https://nfs.faireconomy.media/ff_calendar_thisweek.json";
    private static String AGENT_HEADER = "Mozilla/5.0";

    private final RestTemplateHttpClient restHttp;
    private final ObjectMapper objectMapper;

    public ResponseBuilderUtils<InsightResponse> fetchGoogleNews(InsightRequest request) {

        // Generate a unique cache key per query
        String cacheKey = "GOOGLE_NEWS_"
                + Optional.ofNullable(request.getCategory()).orElse("all")
                + "_"
                + Optional.ofNullable(request.getLastUpdated()).orElse("all");

        // Try to get cached XML
        String cachedXml = StringCache.get(cacheKey);
        GoogleNewsXmlResponse rssFeed;

        if (cachedXml != null) {
            log.info("Returning news from cache for key {}", cacheKey);
            rssFeed = XmlConverterUtils.parseRssXml(cachedXml);
        } else {
            // Build query params
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            Optional.ofNullable(request.getCategory()).ifPresent(c -> params.add("q", c));
            Optional.ofNullable(request.getLastUpdated()).ifPresent(w -> params.add("when", w));
            params.add("hl", "en-US");
            params.add("gl", "US");
            params.add("ceid", "US:en");

            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.add("User-Agent", AGENT_HEADER);

            // Fetch from API
            String xml = restHttp.get(GOOGLE_NEWS, params, headers, String.class);

            // Cache the result
            StringCache.put(cacheKey, xml);

            // Parse RSS
            rssFeed = XmlConverterUtils.parseRssXml(xml);
        }

        // Map RSS to response
        InsightResponse response = new InsightResponse();
        response.setCategory(
                Optional.ofNullable(rssFeed.getChannel())
                        .map(GoogleNewsXmlResponse.Channel::getTitle)
                        .map(title -> title.replaceAll("^\"|\"$", "")) // remove quotes
                        .orElse(null)
        );

        List<InsightResponse.InsightNews> news =
                Optional.ofNullable(rssFeed.getChannel())
                        .map(GoogleNewsXmlResponse.Channel::getItems)
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(item -> {
                            InsightResponse.InsightNews obj = new InsightResponse.InsightNews();

                            String title = item.getTitle();
                            if (title != null) {
                                title = title.replaceAll("^\"|\"$", ""); // remove quotes
                                obj.setTitle(title);
                            }

                            obj.setLink(item.getLink());
                            obj.setSource(
                                    Optional.ofNullable(item.getSource())
                                            .map(GoogleNewsXmlResponse.Source::getValue)
                                            .orElse(null)
                            );
                            return obj;
                        })
                        .toList();

        response.setTotal(news.size());
        response.setData(news);

        return new ResponseBuilderUtils<>(
                ApplicationCode.HTTP_200,
                ApplicationCode.FETCH,
                response
        );
    }
    @PostConstruct
    public void init() {
        updateForexFactoryCache();
    }

    @Cacheable(value = "forexFactory", key = "'fx'", unless = "#result.data.isEmpty()")
    public ResponseBuilderUtils<List<EventCalendarResponse>> getForexFactoryCache() {
        return new ResponseBuilderUtils<>(
                ApplicationCode.HTTP_200,
                "Cache not ready",
                Collections.emptyList()
        );
    }

    @Scheduled(fixedRate = 90 * 60 * 1000) // every 30 mins
    @CachePut(value = "forexFactory", key = "'fx'")
    public ResponseBuilderUtils<List<EventCalendarResponse>> updateForexFactoryCache() {

        String economicEventsJson = EconomicEventsCache.getEconomicEvents(restHttp, FOREX_FACTORY);

        List<EventCalendarResponse> events;
        try {
            events = objectMapper.readValue(
                            economicEventsJson,
                            new TypeReference<List<EventCalendarResponse>>() {}
                    ).stream()
                    .filter(e -> "USD".equals(e.getCountry()))
                    .peek(e -> e.setDate(DateUtils.localTimeConverter(e.getDate())))
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new ResponseBuilderUtils<>(
                ApplicationCode.HTTP_200,
                ApplicationCode.FETCH,
                events
        );
    }

}
