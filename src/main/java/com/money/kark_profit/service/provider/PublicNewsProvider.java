package com.money.kark_profit.service.provider;

import com.money.kark_profit.constants.ApplicationCode;
import com.money.kark_profit.http.RestTemplateHttpClient;
import com.money.kark_profit.transform.request.MarketNewsRequest;
import com.money.kark_profit.transform.response.MarketNewsResponse;
import com.money.kark_profit.transform.response.xml.GoogleNewsXmlResponse;
import com.money.kark_profit.utils.ResponseBuilderUtils;
import com.money.kark_profit.utils.XmlConverterUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PublicNewsProvider {
    private static String GOOGLE_NEWS = "https://news.google.com/rss/search";
    private static String AGENT_HEADER = "Mozilla/5.0";

    private final RestTemplateHttpClient restHttp;

    public ResponseBuilderUtils<MarketNewsResponse> fetchGoogleNews(MarketNewsRequest request) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        Optional.ofNullable(request.getCategory())
                .ifPresent(c -> params.add("q", c));

        Optional.ofNullable(request.getLastUpdated())
                .ifPresent(w -> params.add("when", w));

        params.add("hl", "en-US");
        params.add("gl", "US");
        params.add("ceid", "US:en");

        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Agent", AGENT_HEADER);

        String xml = restHttp.get(GOOGLE_NEWS, params, headers, String.class);

        GoogleNewsXmlResponse rssFeed = XmlConverterUtils.parseRssXml(xml);

        MarketNewsResponse response = new MarketNewsResponse();
        response.setCategory(
                Optional.ofNullable(rssFeed.getChannel())
                        .map(GoogleNewsXmlResponse.Channel::getTitle)
                        .orElse(null)
        );


        List<MarketNewsResponse.MarketNews> news =
                Optional.ofNullable(rssFeed.getChannel())
                        .map(GoogleNewsXmlResponse.Channel::getItems)
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(item -> {
                            MarketNewsResponse.MarketNews obj = new MarketNewsResponse.MarketNews();
                            String title = item.getTitle();
                            if (title != null)
                                title = title.replaceAll("^\"|\"$", ""); // remove only leading/trailing quotes
                            obj.setTitle(title);
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

}
