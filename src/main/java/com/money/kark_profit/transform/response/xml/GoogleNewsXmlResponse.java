package com.money.kark_profit.transform.response.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "rss")
public class GoogleNewsXmlResponse {

    @JacksonXmlProperty(localName = "channel")
    private Channel channel;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Channel {
        private String title;
        private String link;
        private String lastBuildDate;

        @JacksonXmlProperty(localName = "image")
        private Image image;

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "item")
        private List<Item> items;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Image {
        private String title;
        private String url;
        private String link;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private String title;
        private String link;
        private String pubDate;
        private Source source;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Source {

        @JacksonXmlProperty(isAttribute = true, localName = "url")
        private String url;

        @JacksonXmlText
        private String value;
    }
}
