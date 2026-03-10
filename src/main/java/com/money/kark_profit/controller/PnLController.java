package com.money.kark_profit.controller;

import com.money.kark_profit.model.ProfitLossModel;
import com.money.kark_profit.service.ProfitLossService;
import com.money.kark_profit.transform.request.ProfitLossRequest;
import com.money.kark_profit.utils.ResponseBuilderUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/pnl")
@RequiredArgsConstructor
public class PnLController {

    private final ProfitLossService profitLossService;

    @PostMapping("/create")
    public ResponseEntity<ResponseBuilderUtils> insertNewPnL(@RequestBody ProfitLossRequest profitLossRequest) {
        return new ResponseEntity<>(profitLossService.insertNewPnL(profitLossRequest), HttpStatus.OK);
    }

    @PostMapping("/update")
    public ResponseEntity<ResponseBuilderUtils> updatePnL(@RequestBody ProfitLossRequest profitLossRequest) {
        return new ResponseEntity<>(profitLossService.updatePnL(profitLossRequest), HttpStatus.OK);
    }

    @PostMapping("/fetch")
    public ResponseEntity<ResponseBuilderUtils<Page<ProfitLossModel>>> fetchPnL(@RequestBody ProfitLossRequest profitLossRequest) {
        return new ResponseEntity<>(profitLossService.listing(profitLossRequest), HttpStatus.OK);
    }

    @PostMapping("/delete")
    public ResponseEntity<ResponseBuilderUtils> deletePnL(@RequestBody ProfitLossRequest profitLossRequest) {
        return new ResponseEntity<>(profitLossService.deletePnL(profitLossRequest), HttpStatus.OK);
    }
}
