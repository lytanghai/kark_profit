package com.money.kark_profit.repository;

import com.money.kark_profit.model.TransactionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionModel, Integer>, JpaSpecificationExecutor<TransactionModel> {
    Page<TransactionModel> findAll(Pageable pageable);

    TransactionModel findBySn(Integer sn);
}
