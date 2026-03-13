package com.money.kark_profit.repository;

import com.money.kark_profit.model.ConfigurationModel;
import com.money.kark_profit.model.UserProfileModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfileModel, Integer>, JpaSpecificationExecutor<UserProfileModel> {
    Optional<UserProfileModel> findByUsername(String username);
}