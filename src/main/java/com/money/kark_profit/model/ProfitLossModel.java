package com.money.kark_profit.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "profit_loss_model")
@Data
public class ProfitLossModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer sn;

    @Column(name = "symbol", length = 10)
    private String symbol;

    @Column(name = "lot_size")
    private Double lotSize;

    @Column(name = "pnl")
    private Double pnl;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "date")
    private Date date;

}
