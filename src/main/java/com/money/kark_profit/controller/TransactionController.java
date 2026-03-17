package com.money.kark_profit.controller;

import com.money.kark_profit.service.TransactionService;
import com.money.kark_profit.service.feature.PerformanceService;
import com.money.kark_profit.transform.request.CommonRequest;
import com.money.kark_profit.transform.request.PerformanceRequest;
import com.money.kark_profit.transform.request.TransactionRequest;
import com.money.kark_profit.transform.response.MonthlyPnLResponse;
import com.money.kark_profit.transform.response.TransactionListingResponse;
import com.money.kark_profit.utils.ResponseBuilderUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;
    private final PerformanceService performanceService;

    /**PERFORMANCE****/
    @PostMapping("/monthly/performance")
    public ResponseEntity<ResponseBuilderUtils<MonthlyPnLResponse>> get(HttpServletRequest request, @RequestBody PerformanceRequest performanceRequest) {
        return new ResponseEntity<>(performanceService.groupPnLByDay(request, performanceRequest), HttpStatus.OK);
    }

    @PostMapping("/fetch")
    public ResponseEntity<ResponseBuilderUtils<Page<TransactionListingResponse>>> fetchPnL(@RequestBody TransactionRequest transactionRequest, HttpServletRequest request) {
        return new ResponseEntity<>(transactionService.listing(transactionRequest, request), HttpStatus.OK);
    }
    /**PERFORMANCE****/

    @PostMapping("/create")
    public ResponseEntity<ResponseBuilderUtils> insertNewPnL(@RequestBody TransactionRequest transactionRequest, HttpServletRequest request) {
        return new ResponseEntity<>(transactionService.insertNewPnL(transactionRequest, request), HttpStatus.OK);
    }

    @PostMapping("/update")
    public ResponseEntity<ResponseBuilderUtils> updatePnL(@RequestBody TransactionRequest transactionRequest, HttpServletRequest request) {
        return new ResponseEntity<>(transactionService.updatePnL(transactionRequest, request), HttpStatus.OK);
    }

    @PostMapping("/delete")
    public ResponseEntity<ResponseBuilderUtils> deletePnL(@RequestBody TransactionRequest transactionRequest, HttpServletRequest request) {
        return new ResponseEntity<>(transactionService.deletePnL(transactionRequest, request), HttpStatus.OK);
    }

    @PostMapping("/merge-transaction")
    public ResponseEntity<ResponseBuilderUtils> mergeTransaction(HttpServletRequest request,
                                 @RequestBody CommonRequest commonRequest) throws ParseException {
        return new ResponseEntity<>(transactionService.mergeTransaction(request, commonRequest), HttpStatus.OK);
    }
}
