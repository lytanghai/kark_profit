package com.money.kark_profit.repository;

import com.money.kark_profit.model.ProfitLossModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfitLossRepository extends JpaRepository<ProfitLossModel, Integer>, JpaSpecificationExecutor<ProfitLossModel> {
    Page<ProfitLossModel> findAll(Pageable pageable);

    ProfitLossModel findBySn(Integer sn);
}
