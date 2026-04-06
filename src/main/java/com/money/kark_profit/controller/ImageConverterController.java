package com.money.kark_profit.controller;

import com.money.kark_profit.service.FileConverterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
public class ImageConverterController {
    @Autowired
    private FileConverterService fileConverterService;

    @PostMapping("/encode")
    public ResponseEntity<String> encodeFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String referenceName) {
        try {
            String result = fileConverterService.convertAndStoreFile(file, referenceName);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/decode/{referenceName}")
    public ResponseEntity<byte[]> decodeFile(@PathVariable String referenceName) {
        try {
            byte[] fileBytes = fileConverterService.decodeAndRetrieveFile(referenceName);
            String contentType = fileConverterService.getContentType(referenceName);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=" + referenceName + "_restored")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(fileBytes);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}