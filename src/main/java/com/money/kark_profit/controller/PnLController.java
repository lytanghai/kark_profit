package com.money.kark_profit.controller;

import com.money.kark_profit.model.ProfitLossModel;
import com.money.kark_profit.service.ProfitLossService;
import com.money.kark_profit.transform.request.ProfitLossReq;
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
    public ResponseEntity<ResponseBuilderUtils> insertNewPnL(@RequestBody ProfitLossReq profitLossReq) {
        return new ResponseEntity<>(profitLossService.insertNewPnL(profitLossReq), HttpStatus.OK);
    }

    @PostMapping("/update")
    public ResponseEntity<ResponseBuilderUtils> updatePnL(@RequestBody ProfitLossReq profitLossReq) {
        return new ResponseEntity<>(profitLossService.updatePnL(profitLossReq), HttpStatus.OK);
    }

    @PostMapping("/fetch")
    public ResponseEntity<ResponseBuilderUtils<Page<ProfitLossModel>>> fetchPnL(@RequestBody ProfitLossReq profitLossReq) {
        return new ResponseEntity<>(profitLossService.listing(profitLossReq), HttpStatus.OK);
    }

    @PostMapping("/delete")
    public ResponseEntity<ResponseBuilderUtils> deletePnL(@RequestBody ProfitLossReq profitLossReq) {
        return new ResponseEntity<>(profitLossService.deletePnL(profitLossReq), HttpStatus.OK);
    }
}
