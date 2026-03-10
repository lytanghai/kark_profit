package com.money.kark_profit.transform.response;

import com.money.kark_profit.model.ConfigurationModel;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ConfigurationResponse {
    private Long totalElement;
    private Integer numberOfElement;
    private Integer size;
    private Integer totalPage;
    private List<ConfigurationModel> content;
}
