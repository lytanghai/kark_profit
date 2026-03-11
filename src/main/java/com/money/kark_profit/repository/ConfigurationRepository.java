package com.money.kark_profit.repository;

import com.money.kark_profit.model.ConfigurationModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfigurationRepository extends JpaRepository<ConfigurationModel, Integer>, JpaSpecificationExecutor<ConfigurationModel> {
    Optional<ConfigurationModel> findByName(String name);
}