package com.money.kark_profit.controller;

import com.money.kark_profit.service.UserService;
import com.money.kark_profit.service.feature.ReportService;
import com.money.kark_profit.transform.request.GenericRequest;
import com.money.kark_profit.transform.request.ReportRequest;
import com.money.kark_profit.transform.response.ReportResponse;
import com.money.kark_profit.utils.ResponseBuilderUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    private final UserService userService;


    @PostMapping("/generate")
    private ResponseEntity<ResponseBuilderUtils<List<ReportResponse>>> generateReport(@RequestBody ReportRequest reportRequest, HttpServletRequest httpServletRequest) {
        return new ResponseEntity<>(reportService.generateReport(
                reportRequest, userService.extractUserId(httpServletRequest), httpServletRequest), HttpStatus.OK);
    }

    // --- 1. RECEIVE PDF (e.g., from a file upload) and CONVERT to Base64 String ---
    @PostMapping("/encode")
    public ResponseEntity<String> encodePdf(@RequestParam("file") MultipartFile file) {
        try {
            byte[] fileBytes = file.getBytes();
            String base64String = Base64.getEncoder().encodeToString(fileBytes);
            return ResponseEntity.ok(base64String);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to encode PDF.");
        }
    }

    // --- 2. DECODE Base64 String and RETURN as PDF File ---
    @PostMapping("/decode")
    public void decodePdf(@RequestBody GenericRequest request) throws IOException {
        try {
            // Decode the Base64 string back to a byte array
            byte[] pdfBytes = Base64.getDecoder().decode(request.getData());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);

            // #1 Use 'inline' to open in browser, or 'attachment' to force download
            headers.setContentDispositionFormData("inline", "document.pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            //#2 or generate into file
            Files.write(Paths.get("document.pdf"), pdfBytes);

//            return null;
        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

}
