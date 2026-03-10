package com.money.kark_profit.transform.response.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

@Data
@JacksonXmlRootElement(localName = "rss")
public class GoogleNewsXmlResponse {

    @JacksonXmlProperty(localName = "channel")
    private Channel channel;

    @Data
    public static class Channel {
        private String generator;
        private String title;
        private String link;
        private String language;
        private String webMaster;
        private String copyright;
        private String lastBuildDate;
        private Image image;
        private String description;

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "item")
        private List<Item> items;
    }

    @Data
    public static class Image {
        private String title;
        private String url;
        private String link;
        private Integer height;
        private Integer width;
    }

    @Data
    public static class Item {
        private String title;
        private String link;
        private Guid guid;
        private String pubDate;
        private String description;
        private Source source;
    }

    @Data
    public static class Guid {
        @JacksonXmlProperty(isAttribute = true)
        private boolean isPermaLink;
        private String value;
    }

    @Data
    public static class Source {
        @JacksonXmlProperty(isAttribute = true)
        private String url;
        private String value;
    }
}

//String xml = "<rss version=\"2.0\" xmlns:media=\"http://search.yahoo.com/mrss/\"> ... </rss>";
//
//RssFeed rssFeed = XmlConverter.parseRssXml(xml);
//
//System.out.println("Channel title: " + rssFeed.getChannel().getTitle());
//
//        rssFeed.getChannel().getItems().forEach(item -> {
//        System.out.println("Item title: " + item.getTitle());
//        System.out.println("Link: " + item.getLink());
//        System.out.println("Source: " + item.getSource().getValue());
//        });