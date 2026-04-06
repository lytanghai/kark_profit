package com.money.kark_profit.repository;

import com.money.kark_profit.model.UserProfileModel;
import com.money.kark_profit.transform.interfaze.UserSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfileModel, Integer>, JpaSpecificationExecutor<UserProfileModel> {
    Optional<UserProfileModel> findByUsername(String username);
    Optional<UserProfileModel> findByEmail(String email);

    @Query(value = "SELECT DISTINCT u.id, u.username, u.email FROM user_profile u", nativeQuery = true)
    List<UserSummary> fetchUsers();

    @Query(value = "SELECT * FROM user_profile u " +
            "WHERE EXTRACT(YEAR FROM u.created_at) = :year  " +
            "AND EXTRACT(MONTH FROM u.created_at) = :month",
            nativeQuery = true)
    List<UserProfileModel> queryMonthlyRecord(@Param("year") Integer year,
                                                @Param("month") Integer month);
}