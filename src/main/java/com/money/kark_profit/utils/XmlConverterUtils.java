package com.money.kark_profit.utils;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.money.kark_profit.transform.response.xml.GoogleNewsXmlResponse;

public class XmlConverterUtils {

    public static GoogleNewsXmlResponse parseRssXml(String xml) {
        try {
            XmlMapper xmlMapper = new XmlMapper();
            return xmlMapper.readValue(xml, GoogleNewsXmlResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse XML", e);
        }
    }
}