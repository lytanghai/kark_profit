package com.money.kark_profit.transform.request;

import lombok.Data;

import java.util.Date;

@Data
public class RegisterRequest {
    private String username;
    private String email;
    private String password;

    private Integer id;
    //for listing
    private Integer page;
    private Integer size = 10;
    private Boolean status;
    private Date createdAt;
}