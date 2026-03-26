package com.money.kark_profit.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "transaction")
@Data
public class TransactionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer sn;

    @Column(name = "symbol", length = 10)
    private String symbol;

    @Column(name = "pnl")
    private Double pnl;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "date")
    private Date date;

    @Column(name = "type")
    private String type;

    @Column(name = "user_id")
    private Integer userId;

}
