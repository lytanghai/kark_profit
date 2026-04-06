package com.money.kark_profit.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Service
public class FileConverterService {
    private static final String STORAGE_DIRECTORY = "stored-files";

    // Store file info along with the encoded data
    public static class FileData {
        private String encodedContent;
        private String originalFilename;
        private String contentType;
        private long fileSize;

        // Getters and setters
        public String getEncodedContent() { return encodedContent; }
        public void setEncodedContent(String encodedContent) { this.encodedContent = encodedContent; }
        public String getOriginalFilename() { return originalFilename; }
        public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    }

    public FileConverterService() throws IOException {
        Path path = Paths.get(STORAGE_DIRECTORY);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    public String convertAndStoreFile(MultipartFile file, String referenceName) throws IOException {
        // Convert any file to byte array
        byte[] fileBytes = file.getBytes();

        // Encode to Base64 string
        String encodedFile = Base64.getEncoder().encodeToString(fileBytes);

        // Store metadata along with encoded content
        FileData fileData = new FileData();
        fileData.setEncodedContent(encodedFile);
        fileData.setOriginalFilename(file.getOriginalFilename());
        fileData.setContentType(file.getContentType());
        fileData.setFileSize(file.getSize());

        // Save as JSON (using simple format for simplicity)
        String jsonContent = String.format(
                "{\"filename\":\"%s\",\"contentType\":\"%s\",\"size\":%d,\"data\":\"%s\"}",
                escapeJson(fileData.getOriginalFilename()),
                fileData.getContentType(),
                fileData.getFileSize(),
                fileData.getEncodedContent()
        );

        Path filePath = Paths.get(STORAGE_DIRECTORY, referenceName + ".txt");
        Files.write(filePath, jsonContent.getBytes());

        return "File stored successfully. Reference: " + referenceName;
    }

    public byte[] decodeAndRetrieveFile(String referenceName) throws IOException {
        Path filePath = Paths.get(STORAGE_DIRECTORY, referenceName + ".txt");

        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + referenceName);
        }

        String jsonContent = new String(Files.readAllBytes(filePath));

        // Extract the Base64 data from JSON (simple parsing)
        String dataPrefix = "\"data\":\"";
        int startIndex = jsonContent.indexOf(dataPrefix) + dataPrefix.length();
        int endIndex = jsonContent.lastIndexOf("\"}");

        if (startIndex < dataPrefix.length() || endIndex < 0) {
            throw new IOException("Invalid file format");
        }

        String encodedString = jsonContent.substring(startIndex, endIndex);

        // Decode from Base64 to byte array
        return Base64.getDecoder().decode(encodedString);
    }

    public String getContentType(String referenceName) throws IOException {
        Path filePath = Paths.get(STORAGE_DIRECTORY, referenceName + ".txt");
        String jsonContent = new String(Files.readAllBytes(filePath));

        String typePrefix = "\"contentType\":\"";
        int startIndex = jsonContent.indexOf(typePrefix) + typePrefix.length();
        int endIndex = jsonContent.indexOf("\",", startIndex);

        if (startIndex < typePrefix.length() || endIndex < 0) {
            return "application/octet-stream";
        }

        return jsonContent.substring(startIndex, endIndex);
    }

    private String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}