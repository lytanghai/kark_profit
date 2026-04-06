package com.money.kark_profit.repository;

import com.money.kark_profit.model.ConfigurationModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfigurationRepository extends JpaRepository<ConfigurationModel, Integer>, JpaSpecificationExecutor<ConfigurationModel> {
    Optional<ConfigurationModel> findByName(String name);
    @Query(value = "SELECT * FROM configuration c " +
            "WHERE EXTRACT(YEAR FROM c.created_at) = :year  " +
            "AND EXTRACT(MONTH FROM c.created_at) = :month",
            nativeQuery = true)
    List<ConfigurationModel> queryMonthlyRecord(@Param("year") Integer year,
                                              @Param("month") Integer month);
}