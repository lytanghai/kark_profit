package com.money.kark_profit.controller;

import com.money.kark_profit.service.UserService;
import com.money.kark_profit.service.feature.ReportService;
import com.money.kark_profit.transform.request.ReportRequest;
import com.money.kark_profit.transform.response.ReportResponse;
import com.money.kark_profit.utils.ResponseBuilderUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
