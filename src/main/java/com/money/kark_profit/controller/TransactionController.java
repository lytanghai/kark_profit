package com.money.kark_profit.controller;

import com.money.kark_profit.model.TransactionModel;
import com.money.kark_profit.service.TransactionService;
import com.money.kark_profit.transform.request.TransactionRequest;
import com.money.kark_profit.utils.ResponseBuilderUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/create")
    public ResponseEntity<ResponseBuilderUtils> insertNewPnL(@RequestBody TransactionRequest transactionRequest, HttpServletRequest request) {
        return new ResponseEntity<>(transactionService.insertNewPnL(transactionRequest, request), HttpStatus.OK);
    }

    @PostMapping("/update")
    public ResponseEntity<ResponseBuilderUtils> updatePnL(@RequestBody TransactionRequest transactionRequest, HttpServletRequest request) {
        return new ResponseEntity<>(transactionService.updatePnL(transactionRequest, request), HttpStatus.OK);
    }

    @PostMapping("/fetch")
    public ResponseEntity<ResponseBuilderUtils<Page<TransactionModel>>> fetchPnL(@RequestBody TransactionRequest transactionRequest, HttpServletRequest request) {
        return new ResponseEntity<>(transactionService.listing(transactionRequest, request), HttpStatus.OK);
    }

    @PostMapping("/delete")
    public ResponseEntity<ResponseBuilderUtils> deletePnL(@RequestBody TransactionRequest transactionRequest, HttpServletRequest request) {
        return new ResponseEntity<>(transactionService.deletePnL(transactionRequest, request), HttpStatus.OK);
    }
}
