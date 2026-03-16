package com.money.kark_profit.repository;

import com.money.kark_profit.model.UserProfileModel;
import com.money.kark_profit.transform.interfaze.UserSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfileModel, Integer>, JpaSpecificationExecutor<UserProfileModel> {
    Optional<UserProfileModel> findByUsername(String username);
    Optional<UserProfileModel> findByEmail(String email);

    @Query(value = "SELECT DISTINCT u.id, u.username, u.email FROM user_profile u", nativeQuery = true)
    List<UserSummary> fetchUsers();
}