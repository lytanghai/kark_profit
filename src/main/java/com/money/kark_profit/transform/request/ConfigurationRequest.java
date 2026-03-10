package com.money.kark_profit.transform.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class ConfigurationRequest {

    @NotNull @NotBlank
    private String name;

    @NotNull @NotBlank
    private String value;

    //for delete and update
    private Integer id;

    //for listing
    private Integer page;
    private Integer size = 10;
    private Boolean status;
    private Date createdAt;
}