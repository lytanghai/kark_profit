package com.money.kark_profit.transform.response;

import org.springframework.http.MediaType;

public class FileType {
    String extension;
    MediaType mediaType;

    public static FileType image(String format) {
        FileType t = new FileType();
        t.extension = format;
        t.mediaType = MediaType.parseMediaType("image/" + format);
        return t;
    }

    static FileType pdf() {
        FileType t = new FileType();
        t.extension = "pdf";
        t.mediaType = MediaType.APPLICATION_PDF;
        return t;
    }
}