package com.money.kark_profit.service.feature;

import com.money.kark_profit.constants.ApplicationCode;
import com.money.kark_profit.exception.DatabaseException;
import com.money.kark_profit.model.TransactionModel;
import com.money.kark_profit.model.UserProfileModel;
import com.money.kark_profit.repository.TransactionRepository;
import com.money.kark_profit.repository.UserProfileRepository;
import com.money.kark_profit.transform.request.ReportRequest;
import com.money.kark_profit.transform.response.ReportResponse;
import com.money.kark_profit.utils.ResponseBuilderUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final UserProfileRepository userProfileRepository;

    @Cacheable(
            value = "reportCache", // a new cache name
            key = "#userId + '-' + #reportRequest.lastNDay"
    )
    public ResponseBuilderUtils<List<ReportResponse>> generateReport(
            ReportRequest reportRequest,
            int userId,
            HttpServletRequest httpServletRequest) {

        // Verify user
        UserProfileModel userProfile = userProfileRepository.findById(userId).orElse(null);
        if (userProfile == null) {
            throw new DatabaseException(ApplicationCode.DBE_001, ApplicationCode.DBE_001_MSG);
        }

        // Fetch transactions for last N days
        List<TransactionModel> transactions =
                getTransactionsLastNDays(userId, reportRequest.getLastNDay());

        /*
         * Calculate total deposit & withdrawal globally
         * (not per symbol)
         */
        BigDecimal totalDeposit = transactions.stream()
                .filter(t -> "DEPOSIT".equalsIgnoreCase(t.getType()))
                .map(t -> Optional.ofNullable(t.getPnl())
                        .map(BigDecimal::valueOf)
                        .orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalWithdrawal = transactions.stream()
                .filter(t -> "WITHDRAWAL".equalsIgnoreCase(t.getType()))
                .map(t -> Optional.ofNullable(t.getPnl())
                        .map(BigDecimal::valueOf)
                        .orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Group transactions by symbol
        Map<String, List<TransactionModel>> transactionsBySymbol = transactions.stream()
                .filter(t -> t.getSymbol() != null) // ignore deposit/withdrawal rows
                .collect(Collectors.groupingBy(TransactionModel::getSymbol));

        List<ReportResponse> reports = new ArrayList<>();

        for (Map.Entry<String, List<TransactionModel>> entry : transactionsBySymbol.entrySet()) {

            String symbol = entry.getKey();
            List<TransactionModel> symbolTxns = entry.getValue();

            BigDecimal totalProfit = BigDecimal.ZERO;
            BigDecimal totalLoss = BigDecimal.ZERO;

            Map<LocalDate, BigDecimal> dailyPnL = new HashMap<>();

            for (TransactionModel t : symbolTxns) {

                BigDecimal pnl = Optional.ofNullable(t.getPnl())
                        .map(BigDecimal::valueOf)
                        .orElse(BigDecimal.ZERO);

                if ("LOSS".equalsIgnoreCase(t.getType())) {
                    totalLoss = totalLoss.add(pnl);
                    pnl = pnl.negate();
                }
                else if ("PROFIT".equalsIgnoreCase(t.getType())) {
                    totalProfit = totalProfit.add(pnl);
                }
                else {
                    continue;
                }

                LocalDate date = t.getDate()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                dailyPnL.put(date,
                        dailyPnL.getOrDefault(date, BigDecimal.ZERO).add(pnl));
            }

            BigDecimal netProfit = totalProfit.subtract(totalLoss);

            String mostGainedDate = dailyPnL.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(e -> e.getKey().toString())
                    .orElse(null);

            String mostLossDate = dailyPnL.entrySet().stream()
                    .min(Map.Entry.comparingByValue())
                    .map(e -> e.getKey().toString())
                    .orElse(null);

            BigDecimal totalProfitDiv = totalProfit.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal totalLossDiv = totalLoss.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal profitDiv = netProfit.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

            BigDecimal totalDepositDiv = totalDeposit.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal totalWithdrawalDiv = totalWithdrawal.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            ReportResponse report = ReportResponse.builder()
                    .symbol(symbol)
                    .currency("USD")
                    .totalProfit(totalProfitDiv)
                    .totalLoss(totalLossDiv)
                    .profit(profitDiv)
                    .totalDeposit(totalDepositDiv)
                    .totalWithdrawal(totalWithdrawalDiv)
                    .mostGainedDate(mostGainedDate)
                    .mostLossDate(mostLossDate)
                    .result(profitDiv.compareTo(BigDecimal.ZERO) > 0
                            ? "WIN"
                            : profitDiv.compareTo(BigDecimal.ZERO) < 0
                            ? "LOSE"
                            : "DRAW")
                    .build();

            reports.add(report);
        }

        return new ResponseBuilderUtils<>(
                ApplicationCode.HTTP_200,
                ApplicationCode.FETCH,
                reports
        );
    }

    public List<TransactionModel> getTransactionsLastNDays(Integer userId, int n) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(n);

        LocalDateTime fromDate = startDate.atStartOfDay(); // 00:00:00
        LocalDateTime toDate = LocalDate.now().atTime(23, 59, 59); // today 23:59:59

        // fetch transactions since fromDate
        return transactionRepository.findByUserIdSince(userId, fromDate, toDate);
    }
}
