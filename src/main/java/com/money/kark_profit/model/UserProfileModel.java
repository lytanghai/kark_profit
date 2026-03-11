package com.money.kark_profit.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "user_profile")
@Data
public class UserProfileModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    private String username;

    private String password;

    private Boolean status;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "last_updated_at")
    private Date lastUpdatedAt;

}