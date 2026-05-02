package com.money.kark_profit.controller;

import com.money.kark_profit.service.FileConverterService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

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

    @GetMapping("/stream/{referenceName}")
    public void streamImage(@PathVariable String referenceName,
                            @RequestParam(required = false, defaultValue = "original") String format,
                            HttpServletResponse response) throws IOException {
        try {
            byte[] fileBytes = fileConverterService.decodeAndRetrieveFile(referenceName);
            String contentType = fileConverterService.getContentType(referenceName);

            // If PNG format is requested and it's an image, convert it
            if ("png".equalsIgnoreCase(format) && contentType.startsWith("image/")) {
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(fileBytes));
                if (image != null) {
                    ByteArrayOutputStream pngStream = new ByteArrayOutputStream();
                    ImageIO.write(image, "png", pngStream);
                    fileBytes = pngStream.toByteArray();
                    contentType = "image/png";
                }
            }

            String extension = contentType.equals("image/png") ? ".png" :
                    contentType.equals("image/jpeg") ? ".jpg" :
                            contentType.equals("image/gif") ? ".gif" : "";

            response.setContentType(contentType);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename=\"" + referenceName + extension + "\"");
            response.setContentLength(fileBytes.length);

            response.getOutputStream().write(fileBytes);
            response.getOutputStream().flush();

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error: " + e.getMessage());
        }
    }
}