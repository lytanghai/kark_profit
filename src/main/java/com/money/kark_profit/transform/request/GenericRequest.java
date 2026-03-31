package com.money.kark_profit.transform.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GenericRequest {

    @JsonProperty("base_pdf")
    private String basePdf;

    private String data;

}
