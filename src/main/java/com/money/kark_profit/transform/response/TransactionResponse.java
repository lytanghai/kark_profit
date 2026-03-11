package com.money.kark_profit.transform.response;

import com.money.kark_profit.model.TransactionModel;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TransactionResponse {
    private Long totalElement;
    private Integer numberOfElement;
    private Integer size;
    private Integer totalPage;
    private List<TransactionModel> content;
}
