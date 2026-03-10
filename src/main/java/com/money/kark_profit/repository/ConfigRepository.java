package com.money.kark_profit.repository;

import com.money.kark_profit.model.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfigRepository extends JpaRepository<Configuration, Integer>, JpaSpecificationExecutor<Configuration> {
    Optional<Configuration> findByName(String name);
}