package com.money.kark_profit.repository;

import com.money.kark_profit.model.UserProfileModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfileModel, Integer> {
    Optional<UserProfileModel> findByUsername(String username);
}