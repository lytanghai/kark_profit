package com.money.kark_profit.repository;

import com.money.kark_profit.model.TransactionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionModel, Integer>, JpaSpecificationExecutor<TransactionModel> {
    Page<TransactionModel> findAll(Pageable pageable);

    TransactionModel findBySnAndUserId(Integer sn, Integer userId);

    void deleteBySn(Integer sn);

    @Modifying
    @Query(value = "DELETE FROM transaction t WHERE t.sn IN :sns", nativeQuery = true)
    void deleteBySnIn(@Param("sns") List<Integer> sns);

    @Query(value = "SELECT * FROM transaction t " +
            "WHERE EXTRACT(YEAR FROM t.date) = :year  " +
            "AND EXTRACT(MONTH FROM t.date) = :month",
            nativeQuery = true)
    List<TransactionModel> queryMonthlyRecord(@Param("year") Integer year,
                                           @Param("month") Integer month);

    @Query(value = "SELECT * FROM transaction t " +
            "WHERE t.user_id = :userId " +
            "AND EXTRACT(YEAR FROM t.date) = :year " +
            "AND EXTRACT(MONTH FROM t.date) = :month",
            nativeQuery = true)
    List<TransactionModel> queryMonthlyTxn(@Param("userId") Integer userId,
                                           @Param("year") Integer year,
                                           @Param("month") Integer month);

    @Query(value = "SELECT * FROM transaction t WHERE t.user_id = :userId AND t.date >= :fromDate AND t.date <= :toDate", nativeQuery = true)
    List<TransactionModel> findByUserIdSince(
            @Param("userId") Integer userId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    // Find transactions by user ID and date range
    @Query(value = "SELECT * FROM transaction t " +
            "WHERE t.user_id = :userId " +
            "AND t.date >= :startDate AND t.date <= :endDate " +
            "ORDER BY t.date DESC", nativeQuery = true)
    List<TransactionModel> findByUserIdAndDateBetween(
            @Param("userId") Integer userId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

}
