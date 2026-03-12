package com.money.kark_profit.controller;

import com.money.kark_profit.service.TransactionService;
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

    //report: for history purpose only

    //fetch report + in the last n days

    //total: profit
    //total: loss
    //profit: eg: -500 or +500
    //total: deposit
    //total: withdrawal
    //most gained day 02/03/2026
    //most loss:  day 03/03/2026 *can also be the same day as gained day

    //result -> WIN or LOSE or DRAW (type profit and loss only)
    // if profit more than loss -> WIN
    // if loss more than gain -> LOSE
    // else DRAW

    private final ReportService reportService;
    private final UserService userService;


    @PostMapping("/generate")
    private ResponseEntity<ResponseBuilderUtils<List<ReportResponse>>> generateReport(@RequestBody ReportRequest reportRequest, HttpServletRequest httpServletRequest) {
        return new ResponseEntity<>(reportService.generateReport(
                reportRequest, userService.extractUserId(httpServletRequest), httpServletRequest), HttpStatus.OK);
    }



}
