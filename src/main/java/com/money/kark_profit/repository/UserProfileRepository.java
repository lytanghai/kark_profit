package com.money.kark_profit.repository;

import com.money.kark_profit.model.UserProfileModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfileModel, Integer>, JpaSpecificationExecutor<UserProfileModel> {
    Optional<UserProfileModel> findByUsername(String username);

    @Query(value = "SELECT DISTINCT u.id FROM user_profile u", nativeQuery = true)
    List<Integer> fetchUserIds();
}